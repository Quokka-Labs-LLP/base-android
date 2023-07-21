package com.example.baseandroid.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.baseandroid.repository.MainRepository
import com.example.baseandroid.viewmodel.MainViewModel
import java.lang.IllegalArgumentException

class MainViewModelFactory constructor(private val repository: MainRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(MainViewModel::class.java)){
            MainViewModel(this.repository) as T
        }else{
            throw IllegalArgumentException("ViewModel not found.")
        }
    }

}