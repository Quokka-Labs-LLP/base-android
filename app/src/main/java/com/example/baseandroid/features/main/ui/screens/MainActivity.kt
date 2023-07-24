package com.example.baseandroid.features.main.ui.screens

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.baseandroid.R
import com.example.baseandroid.databinding.ActivityMainBinding
import com.example.baseandroid.features.main.adapter.UserAdapter
import com.example.baseandroid.features.main.viewmodel.MainViewModel
import com.example.baseandroid.utils.NetworkResult
import com.example.baseandroid.utils.NetworkUtils.isNetworkAvailable
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val mainViewModel by viewModels<MainViewModel>()
    private val userAdapter = UserAdapter(this)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initComponents()
        callUserApi()
    }

    private fun initComponents() {
        binding.recyclerview.apply {
            adapter = userAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
            setHasFixedSize(true)
        }
    }

    private fun callUserApi() {
        if (isNetworkAvailable()) {
            mainViewModel.fetchAllUsers()
            mainViewModel.allUserData.observe(this) { response ->
                when (response) {
                    is NetworkResult.Success -> {
                        // bind data to the view
                        response.data?.let { userAdapter.setUserList(it) }
                    }
                    is NetworkResult.Error -> {
                        // show error message
                        Toast.makeText(this, response.message, Toast.LENGTH_LONG).show()
                    }
                    is NetworkResult.Loading -> {
                        //show a progress bar
                    }

                }

            }
        } else
            Toast.makeText(this, getString(R.string.check_internet), Toast.LENGTH_LONG).show()
    }
}