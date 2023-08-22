package com.example.baseandroid.features.main.ui

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.baseandroid.R
import com.example.baseandroid.features.main.models.UserResponse
import com.example.baseandroid.features.main.ui.theme.BaseAndroidTheme
import com.example.baseandroid.features.main.viewmodel.MainViewModel
import com.example.baseandroid.navigation.SetNavGraph
import com.example.baseandroid.utils.NetworkResult
import com.example.baseandroid.utils.NetworkUtils
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {
    private val mainViewModel by viewModel<MainViewModel>()
    private val userList = mutableStateOf(listOf<UserResponse>())
    private lateinit var navController: NavHostController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BaseAndroidTheme {
                navController = rememberNavController()
                val modifier = Modifier.fillMaxSize()
                SetNavGraph(
                    navController = navController, modifier = modifier, userList = userList.value
                )
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
                        // show a progress bar
                    }
                }
            }
        } else Toast.makeText(this, getString(R.string.check_internet), Toast.LENGTH_LONG).show()
    }
}