package com.example.baseandroid.app

import android.app.Application
import com.example.baseandroid.di.apiModule
import com.example.baseandroid.di.repositoryModule
import com.example.baseandroid.di.retrofitModule
import com.example.baseandroid.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this@MyApp
        startKoin {
            androidLogger(Level.DEBUG)
            androidContext(this@MyApp)
            modules(listOf(repositoryModule, viewModelModule, retrofitModule, apiModule))
        }
    }

    companion object {
        private var instance: MyApp? = null
        fun getInstance(): MyApp? {
            if (instance == null) {
                synchronized(MyApp::class.java) {
                    if (instance == null) {
                        instance =
                            MyApp()
                    }
                }
            }
            return instance
        }
    }
}
