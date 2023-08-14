package com.example.baseandroid.features.main

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import com.example.baseandroid.R
import com.example.baseandroid.features.main.models.UserResponse
import com.example.baseandroid.features.main.ui.components.AdapterItem
import com.example.baseandroid.features.main.ui.theme.BaseandroidTheme
import com.example.baseandroid.features.main.viewmodel.MainViewModel
import com.example.baseandroid.utils.NetworkResult
import com.example.baseandroid.utils.NetworkUtils
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val mainViewModel by viewModels<MainViewModel>()
    private val userList = mutableStateOf(listOf<UserResponse>())
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BaseandroidTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ) {
                    ListOfItems(modifier = Modifier, userList = userList.value)
                }
            }
        }
        callUserApi()
    }

    private fun callUserApi() {
        if (NetworkUtils.isNetworkAvailable()) {
            mainViewModel.fetchAllUsers()
            mainViewModel.allUserData.observe(this) { response ->
                when (response) {
                    is NetworkResult.Success -> {
                        // bind data to the view
                        response.data?.let {
                            userList.value = it
                        }
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
        } else Toast.makeText(this, getString(R.string.check_internet), Toast.LENGTH_LONG).show()
    }
}

@Composable
fun ListOfItems(modifier: Modifier, userList: List<UserResponse>) {
    LazyColumn {
        item {
            for (item in userList) AdapterItem(modifier = modifier, item)
        }
    }
}