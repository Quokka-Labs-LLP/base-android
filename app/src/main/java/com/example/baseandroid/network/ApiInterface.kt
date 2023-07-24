package com.example.baseandroid.network

import com.example.baseandroid.features.main.models.UserResponse
import retrofit2.Response
import retrofit2.http.GET

interface ApiInterface {
    @GET("/posts")
    suspend fun getAllUsers():Response<List<UserResponse>>
}