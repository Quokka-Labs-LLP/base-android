package com.example.baseandroid.features.main.repository

import com.example.baseandroid.features.main.models.UserResponse
import com.example.baseandroid.network.ApiInterface
import com.example.baseandroid.utils.BaseApiResponse
import com.example.baseandroid.utils.NetworkResult
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

@ActivityRetainedScoped
class MainRepository @Inject constructor(private val apiInterface: ApiInterface) : BaseApiResponse() {
    suspend fun getAllUsers(): Flow<NetworkResult<List<UserResponse>>> {
        return flow<NetworkResult<List<UserResponse>>> {
            emit(safeApiCall { apiInterface.getAllUsers() })
        }.flowOn(Dispatchers.IO)
    }
}