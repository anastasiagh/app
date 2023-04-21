package com.example.app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.widget.addTextChangedListener
import androidx.room.Room
import com.example.app.databinding.ActivityAddTransactionBinding
import com.example.app.databinding.ActivityMainBinding
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class AddTransactionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddTransactionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_transaction)

        binding = ActivityAddTransactionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.labelInput.addTextChangedListener {
            if(it!!.count() > 0)
                binding.labelLayout.error = null
        }

        binding.amountInput.addTextChangedListener {
            if(it!!.count() > 0)
                binding.amountLayout.error = null
        }

        binding.addTransactionButton.setOnClickListener {
            val label = binding.labelInput.text.toString()
            val amount = binding.amountInput.text.toString().toDoubleOrNull()
            val description = binding.descriptionInput.text.toString()

            if(label.isEmpty())
                binding.labelLayout.error = "Please enter a valid label"
            else if(amount == null)
                binding.amountLayout.error = "Please enter a valid amount"
            else {
                val transaction = Transaction(0, label, amount, description)
                insert(transaction)
            }
        }

        binding.closeButton.setOnClickListener {
            finish()
        }
    }

    private fun insert(transaction: Transaction) {
        val db : AppDatabase = Room.databaseBuilder(this, AppDatabase::class.java, "transactions").build()
        GlobalScope.launch{
            db.transactionDao().insertAll(transaction)
            finish()
        }
    }

}