package com.hritikbhat.bellweather.data.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.hritikbhat.bellweather.data.db.AppDao

class MainViewModelFactory(private val appDao: AppDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(appDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
