package com.example.app

//import kotlinx.android.synthetic.main.activity_main.*

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.example.app.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {
    private lateinit var deletedTransaction: Transaction
    private lateinit var transactions: List<Transaction>
    private lateinit var oldTransactions: List<Transaction>
    private lateinit var transactionAdapter: TransactionAdapter
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var binding: ActivityMainBinding
    private lateinit var db: AppDatabase
    private lateinit var firebaseAuth: FirebaseAuth
    lateinit var bottomNavigationView: BottomNavigationView
    lateinit var openDialog: Button

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

        //bottomNavigation
        bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.home -> {
                    val intent = Intent(this, newActivity::class.java)
                    startActivity(intent)
                    true
                }

                R.id.categories -> {
                    val intent = Intent(this, Categories::class.java)
                    startActivity(intent)
                    true
                }

                R.id.scanButton -> {
                    val intent = Intent(this, ScanActivity::class.java)
                    startActivity(intent)
                    true
                }

                R.id.transactions -> {
                    val intent = Intent(this, Transaction::class.java)
                    startActivity(intent)
                    true
                }

                R.id.profile -> {
                    val intent = Intent(this, Profile::class.java)
                    startActivity(intent)
                    true
                }

                else -> {
                    false
                }
            }
        }

        //sign out
        firebaseAuth = FirebaseAuth.getInstance()

        binding.signOutText.setOnClickListener {
            firebaseAuth.signOut()
            val intent = Intent(this, SignIn::class.java)
            startActivity(intent)
        }


        //addtransaction dialog

//        infoTv = findViewById<View>(android.R.id.info_tv)

        binding.addButton.setOnClickListener {
//            val intent = Intent(this, AddTransactionActivity::class.java)
//            startActivity(intent)
            showCustomDialog()
        }


        //swipe to delete
        val itemTouchHelper = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
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


//        binding.addButton.setOnClickListener {
//            val intent = Intent(this, AddTransactionActivity::class.java)
//            startActivity(intent)
//        }

//        binding.scanButton.setOnClickListener {
//            val intent = Intent(this, ScanActivity::class.java)
//            startActivity(intent)
//        }
    }

    private fun fetchAll() {
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
        val budgetAmount = transactions.filter { it.amount > 0 }.map { it.amount }.sum()
        val expenseAmount = totalAmount - budgetAmount

        binding.balance.text = "$%.2f".format(totalAmount)
//        binding.budget.text = "$%.2f".format(budgetAmount)
        binding.expense.text = "$%.2f".format(expenseAmount)
    }

    private fun undoDelete() {
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

    private fun showSnackbar() {
        val view = findViewById<View>(R.id.coordinator)
        val snackbar = Snackbar.make(view, "Transaction deleted", Snackbar.LENGTH_LONG)
        snackbar.setAction("Undo") {
            undoDelete()
        }
            .setActionTextColor(ContextCompat.getColor(this, R.color.red))
            .setTextColor(ContextCompat.getColor(this, R.color.white))
            .show()
    }

    private fun deleteTransaction(transaction: Transaction) {
        deletedTransaction = transaction
        oldTransactions = transactions

        GlobalScope.launch {
            db.transactionDao().delete(transaction)

            transactions = transactions.filter { it.id != transaction.id }
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

    private fun showCustomDialog() {
        val dialog = Dialog(this@MainActivity)
        //We have added a title in the custom layout. So let's disable the default title.
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        //The user will be able to cancel the dialog bu clicking anywhere outside the dialog.
        dialog.setCancelable(true)
        //Mention the name of the layout of your custom dialog.
        dialog.setContentView(R.layout.activity_add_transaction)

        //Initializing the views of the dialog.
//        val label = dialog.findViewById<EditText>(R.id.expenseNameInput)
//        val amount = dialog.findViewById<EditText>(android.R.id.R.id.amountInput)
//        val termsCb = dialog.findViewById<CheckBox>(android.R.id.terms_cb)
        val submitButton = dialog.findViewById<Button>(R.id.addTransactionButton)

        submitButton.setOnClickListener {

            dialog.dismiss()
        }
        val closeButton = dialog.findViewById<Button>(R.id.closeButton)
        closeButton.setOnClickListener {
            dialog.dismiss()
        }

//        }
        dialog.show()
    }
}