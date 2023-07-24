package com.example.baseandroid.network

import com.example.baseandroid.BuildConfig
import com.example.baseandroid.utils.BASE_URL
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    fun getInstance(): Retrofit {
        val mHttpLoggingInterceptor = HttpLoggingInterceptor()
        if (BuildConfig.DEBUG) {
            mHttpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        } else
            mHttpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BASIC)

        val mOkHttpClient = OkHttpClient
            .Builder()
            .addInterceptor(mHttpLoggingInterceptor)
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(mOkHttpClient)
            .build()
    }
}
