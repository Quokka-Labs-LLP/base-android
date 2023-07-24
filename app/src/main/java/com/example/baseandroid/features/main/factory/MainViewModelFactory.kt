package com.example.baseandroid.features.main.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.baseandroid.features.main.repository.MainRepository
import com.example.baseandroid.features.main.viewmodel.MainViewModel

class MainViewModelFactory constructor(private val repository: MainRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(MainViewModel::class.java)) {
            throw IllegalArgumentException("ViewModel not found.")
        }
        return MainViewModel(this.repository) as T
    }
}
