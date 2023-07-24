package com.example.baseandroid.features.main.ui.screens

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.baseandroid.databinding.ActivityMainBinding
import com.example.baseandroid.features.main.adapter.UserAdapter
import com.example.baseandroid.features.main.factory.MainViewModelFactory
import com.example.baseandroid.features.main.repository.MainRepository
import com.example.baseandroid.features.main.viewmodel.MainViewModel
import com.example.baseandroid.network.ApiInterface
import com.example.baseandroid.network.RetrofitClient

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var apiInterface: ApiInterface
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
        apiInterface = RetrofitClient.getInstance().create(ApiInterface::class.java)
        viewModel = ViewModelProvider(
            this,
            MainViewModelFactory(
                MainRepository
                (apiInterface),
            ),
        )[MainViewModel::class.java]
    }

    private fun callUserApi() {
        viewModel.getAllUsers()
        observeUserData()
    }

    private fun observeUserData() {
        viewModel.allUserData.observe(this) {
            if (it.isNotEmpty()) {
                userAdapter.setUserList(it)
            }
        }
        viewModel.errorMessage.observe(this) {
            if (!it.isNullOrEmpty()) {
                Toast.makeText(this, it.toString(), Toast.LENGTH_LONG).show()
            }
        }
    }
}
