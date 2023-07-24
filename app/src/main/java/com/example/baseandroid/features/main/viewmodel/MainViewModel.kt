package com.example.baseandroid.features.main.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.baseandroid.features.main.models.UserResponse
import com.example.baseandroid.features.main.repository.MainRepository
import com.example.baseandroid.utils.NetworkResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: MainRepository,
    application: Application
) : AndroidViewModel(application) {

    private val _allUserData: MutableLiveData<NetworkResult<List<UserResponse>>> = MutableLiveData()
    val allUserData: LiveData<NetworkResult<List<UserResponse>>> = _allUserData
    fun fetchAllUsers() = viewModelScope.launch {
        repository.getAllUsers().collect { values ->
            _allUserData.value = values
        }
    }
}