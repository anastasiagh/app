package com.example.app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.example.app.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
//import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.parcelize.Parcelize

class MainActivity : AppCompatActivity() {
    private lateinit var deletedTransaction: Transaction
    private lateinit var transactions : List<Transaction>
    private lateinit var oldTransactions : List<Transaction>
    private lateinit var transactionAdapter: TransactionAdapter
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var binding: ActivityMainBinding
    private lateinit var db : AppDatabase
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        transactions = arrayListOf()

        transactionAdapter = TransactionAdapter(transactions)
        linearLayoutManager = LinearLayoutManager(this)

        db = Room.databaseBuilder(this, AppDatabase::class.java, "transactions").build()

        binding.recyclerview.apply {
            adapter = transactionAdapter
            layoutManager = linearLayoutManager
        }

//        updateDashboard()
//        fetchAll()

        //sign out
        firebaseAuth = FirebaseAuth.getInstance()

        binding.signOutText.setOnClickListener {
            firebaseAuth.signOut()
            val intent = Intent(this, SignIn::class.java)
            startActivity(intent)
        }

        //swipe to delete
        val itemTouchHelper = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT){
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                deleteTransaction(transactions[viewHolder.adapterPosition])
            }
        }

        val swipeHelper = ItemTouchHelper(itemTouchHelper)
        swipeHelper.attachToRecyclerView(binding.recyclerview)


        binding.addButton.setOnClickListener {
            val intent = Intent(this, AddTransactionActivity::class.java)
            startActivity(intent)
        }

        binding.scanButton.setOnClickListener {
            val intent = Intent(this, ScanActivity::class.java)
            startActivity(intent)
        }
    }

    private fun fetchAll(){
        GlobalScope.launch {
//            db.transactionDao().insertAll(Transaction(0, "me", -200.0, "it's me"))
            transactions = db.transactionDao().getAll()

            runOnUiThread {
                transactionAdapter.setData(transactions)
                updateDashboard()
            }
        }
    }

    private fun updateDashboard() {
        val totalAmount = transactions.map { it.amount }.sum()
        val budgetAmount = transactions.filter { it.amount>0 }.map { it.amount }.sum()
        val expenseAmount = totalAmount-budgetAmount

        binding.balance.text = "$%.2f".format(totalAmount)
        binding.budget.text = "$%.2f".format(budgetAmount)
        binding.expense.text = "$%.2f".format(expenseAmount)
    }

    private fun undoDelete(){
        GlobalScope.launch {
            db.transactionDao().insertAll(deletedTransaction)

            transactions = oldTransactions

            runOnUiThread {
                updateDashboard()
                transactionAdapter.setData(transactions)
                showSnackbar()
            }
        }
    }

    private fun showSnackbar(){
        val view = findViewById<View>(R.id.coordinator)
        val snackbar = Snackbar.make(view, "Transaction deleted", Snackbar.LENGTH_LONG)
        snackbar.setAction("Undo"){
            undoDelete()
        }
            .setActionTextColor(ContextCompat.getColor(this, R.color.red))
            .setTextColor(ContextCompat.getColor(this, R.color.white))
            .show()
    }

    private fun deleteTransaction(transaction: Transaction){
        deletedTransaction = transaction
        oldTransactions = transactions

        GlobalScope.launch {
            db.transactionDao().delete(transaction)

            transactions = transactions.filter { it.id !=transaction.id }
            runOnUiThread {
                updateDashboard()
                showSnackbar()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        fetchAll()
    }

    override fun onStop() {
        super.onStop()
    }
}