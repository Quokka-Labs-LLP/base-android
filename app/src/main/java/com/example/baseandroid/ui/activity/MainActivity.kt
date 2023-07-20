package com.example.baseandroid.ui.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.baseandroid.R
import com.example.baseandroid.adapter.UserAdapter
import com.example.baseandroid.databinding.ActivityMainBinding
import com.example.baseandroid.model.UserResponse
import com.example.baseandroid.network.ApiInterface
import com.example.baseandroid.network.RetrofitClient
import com.example.baseandroid.utils.NetworkUtils.isNetworkAvailable
import com.example.baseandroid.utils.NetworkUtils.isWifiConnected
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        callUserApi()
    }

    private fun callUserApi() {
        if (isNetworkAvailable(this) || isWifiConnected(this)){
            val retrofit = RetrofitClient.getInstance()
            val apiInterface = retrofit.create(ApiInterface::class.java)
            lifecycleScope.launch {
                try {
                    val response = apiInterface.getAllUsers()
                    if (response.isSuccessful) {
                        response.body()?.let { setAdapter(it) }
                    } else {
                        Toast.makeText(this@MainActivity, response.errorBody().toString(), Toast.LENGTH_LONG).show()
                    }
                }catch (Ex:Exception){
                    Ex.localizedMessage?.let { Log.e("Error", it) }
                }
            }
        }
        else{
            Toast.makeText(this@MainActivity, R.string.check_internet, Toast.LENGTH_LONG).show()
        }
    }

    private fun setAdapter(userList:List<UserResponse>) {
        binding.recyclerview.apply {
            adapter = UserAdapter(userList,this@MainActivity)
            layoutManager = LinearLayoutManager(this@MainActivity)
            setHasFixedSize(true)
        }
    }
}