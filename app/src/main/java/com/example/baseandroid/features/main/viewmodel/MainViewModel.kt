package com.example.baseandroid.features.main.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.baseandroid.features.main.models.UserResponse
import com.example.baseandroid.features.main.repository.MainRepository
import com.example.baseandroid.utils.NetworkResult
import kotlinx.coroutines.launch

class MainViewModel constructor(private val repository: MainRepository) : ViewModel() {

    private val _allUserData: MutableLiveData<NetworkResult<List<UserResponse>>> = MutableLiveData()
    val allUserData: LiveData<NetworkResult<List<UserResponse>>> = _allUserData
    fun fetchAllUsers() = viewModelScope.launch {
        repository.getAllUsers().collect { values ->
            _allUserData.value = values
        }
    }
}
