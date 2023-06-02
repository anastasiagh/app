package com.example.app

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.app.databinding.ActivityMainBinding
import com.example.app.databinding.ActivityTestBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TestActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTestBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)

        binding = ActivityTestBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        getData()

        binding.pressHereBtn.setOnClickListener {
            getData()
        }

    }

    private fun getData() {
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Please wait while the data is fetched")
        progressDialog.show()

        RetrofitInstance.apiInterface.getData().enqueue(object : Callback<responseDataClass?> {
            override fun onResponse(
                call: Call<responseDataClass?>,
                response: Response<responseDataClass?>
            ) {
                binding.countText.text = response.body()?.count.toString()
                binding.pageText.text = response.body()?.page.toString()
                binding.lastItemIndexText.text = response.body()?.lastItemIndex.toString()
                progressDialog.dismiss()
            }

            override fun onFailure(call: Call<responseDataClass?>, t: Throwable) {
                Toast.makeText(this@TestActivity, "${t.localizedMessage}", Toast.LENGTH_SHORT).show()
                progressDialog.dismiss()
            }
        })
    }
}