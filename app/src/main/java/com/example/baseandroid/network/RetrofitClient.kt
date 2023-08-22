package com.example.baseandroid.network

import android.content.Context
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.example.baseandroid.BuildConfig
import com.example.baseandroid.utils.BASE_URL
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    fun getInstance(context : Context): Retrofit {
        val mHttpLoggingInterceptor = HttpLoggingInterceptor()
        if (BuildConfig.DEBUG) {
            mHttpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        } else
            mHttpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BASIC)

        val mOkHttpClient = OkHttpClient
            .Builder()
            .addInterceptor(ChuckerInterceptor(context))
            .addInterceptor(mHttpLoggingInterceptor)
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(mOkHttpClient)
            .build()
    }
}
