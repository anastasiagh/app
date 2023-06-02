package com.example.app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.app.databinding.ActivityMainBinding
import com.example.app.databinding.ActivityNewBinding
import com.google.android.material.bottomnavigation.BottomNavigationItemView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView

class newActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNewBinding

    lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new)

        binding = ActivityNewBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
    }


}


