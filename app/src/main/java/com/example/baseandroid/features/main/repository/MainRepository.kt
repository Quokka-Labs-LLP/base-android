package com.example.baseandroid.features.main.repository

import com.example.baseandroid.features.main.models.UserResponse
import com.example.baseandroid.network.ApiInterface
import com.example.baseandroid.utils.BaseApiResponse
import com.example.baseandroid.utils.NetworkResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class MainRepository(private val apiInterface: ApiInterface) : BaseApiResponse() {
    suspend fun getAllUsers(): Flow<NetworkResult<List<UserResponse>>> {
        return flow<NetworkResult<List<UserResponse>>> {
            emit(safeApiCall { apiInterface.getAllUsers() })
        }.flowOn(Dispatchers.IO)
    }
}
