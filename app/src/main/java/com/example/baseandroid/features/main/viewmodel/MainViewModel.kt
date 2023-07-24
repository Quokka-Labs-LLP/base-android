package com.example.baseandroid.features.main.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.baseandroid.features.main.models.UserResponse
import com.example.baseandroid.features.main.repository.MainRepository
import com.example.baseandroid.utils.NetworkUtils.isNetworkAvailable
import com.example.baseandroid.utils.NetworkUtils.isWifiConnected
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException

class MainViewModel(private val repository: MainRepository) : ViewModel() {
    private val _allUserData = MutableLiveData<List<UserResponse>>()
    val allUserData: LiveData<List<UserResponse>> get() = _allUserData
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    /*API CALL FOR GET ALL USERS*/
    fun getAllUsers() = viewModelScope.launch(Dispatchers.IO) {
        try {
            if (isNetworkAvailable() || isWifiConnected()) {
                val response = repository.getAllUsers()
                if (response.isSuccessful) {
                    _allUserData.postValue(response.body())
                } else _errorMessage.postValue(response.message())
            } else _errorMessage.postValue("Check your Internet.")
        } catch (e: IOException) {
            Log.e("TAG", e.message.toString())
            _errorMessage.postValue(e.message.toString())
        }
    }
}
