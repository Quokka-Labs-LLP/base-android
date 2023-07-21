package com.example.baseandroid.app

import android.app.Application


class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this@MyApp
    }

    companion object {
        private var instance: MyApp? = null
        fun getInstance(): MyApp? {
            if (instance == null) {
                synchronized(MyApp::class.java) {
                    if (instance == null) instance =
                        MyApp()
                }
            }
            return instance
        }

    }


}
