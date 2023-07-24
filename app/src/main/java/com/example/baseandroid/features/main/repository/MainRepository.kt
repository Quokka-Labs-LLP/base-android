package com.example.baseandroid.features.main.repository

import com.example.baseandroid.network.ApiInterface

class MainRepository constructor(private val apiInterface: ApiInterface) {
    suspend fun getAllUsers() = apiInterface.getAllUsers()
}
