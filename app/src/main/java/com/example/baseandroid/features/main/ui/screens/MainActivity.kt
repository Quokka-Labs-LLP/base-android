package com.example.baseandroid.features.main.ui.screens

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.baseandroid.R
import com.example.baseandroid.databinding.ActivityMainBinding
import com.example.baseandroid.features.main.adapter.UserAdapter
import com.example.baseandroid.features.main.factory.MainViewModelFactory
import com.example.baseandroid.features.main.repository.MainRepository
import com.example.baseandroid.features.main.viewmodel.MainViewModel
import com.example.baseandroid.features.setting.ui.screens.SettingActivity
import com.example.baseandroid.network.ApiInterface
import com.example.baseandroid.network.RetrofitClient
import com.example.baseandroid.utils.NetworkResult
import com.example.baseandroid.utils.NetworkUtils.isNetworkAvailable

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var apiInterface: ApiInterface
    private val userAdapter = UserAdapter(this)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        appBarNavigation()
        initComponents()
        callUserApi()
    }

    private fun appBarNavigation() {
        binding.topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.setting -> {
                    startActivity(Intent(this, SettingActivity::class.java))
                    true
                }

                else -> {
                    false
                }
            }
        }
    }

    private fun initComponents() {
        apiInterface = RetrofitClient.getInstance().create(ApiInterface::class.java)
        viewModel = ViewModelProvider(
            this,
            MainViewModelFactory(
                MainRepository(apiInterface),
            ),
        )[MainViewModel::class.java]
        binding.recyclerview.apply {
            adapter = userAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
            setHasFixedSize(true)
        }
    }

    private fun callUserApi() {
        if (isNetworkAvailable()) {
            viewModel.fetchAllUsers()
            viewModel.allUserData.observe(this) { response ->
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
                        // show a progress bar
                    }
                }
            }
        } else Toast.makeText(this, getString(R.string.check_internet), Toast.LENGTH_LONG).show()
    }
}
