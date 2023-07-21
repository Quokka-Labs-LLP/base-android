package com.example.baseandroid.repository

import com.example.baseandroid.network.ApiInterface
import com.example.baseandroid.network.RetrofitClient

class MainRepository constructor(private val apiInterface: ApiInterface) {
    suspend fun getAllUsers() = apiInterface.getAllUsers()
}