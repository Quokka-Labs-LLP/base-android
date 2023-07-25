package com.example.baseandroid.di

import com.example.baseandroid.features.main.repository.MainRepository
import com.example.baseandroid.features.main.viewmodel.MainViewModel
import com.example.baseandroid.network.ApiInterface
import com.example.baseandroid.utils.BASE_URL
import com.example.baseandroid.utils.CONNECTION_TIMEOUT
import okhttp3.OkHttpClient
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

val viewModelModule = module {
    viewModel {
        MainViewModel(get())
    }
}

val repositoryModule = module {
    single {
        MainRepository(get())
    }
}

val apiModule = module {
    fun provideUseApi(retrofit: Retrofit): ApiInterface {
        return retrofit.create(ApiInterface::class.java)
    }

    single { provideUseApi(get()) }
}

val retrofitModule = module {

    fun provideGsonConverterFactory(): GsonConverterFactory {
        return GsonConverterFactory.create()
    }

    fun provideHttpClient(): OkHttpClient {
        return OkHttpClient
            .Builder()
            .readTimeout(CONNECTION_TIMEOUT, TimeUnit.SECONDS)
            .connectTimeout(CONNECTION_TIMEOUT, TimeUnit.SECONDS)
            .build()
    }

    fun provideRetrofit(factory: GsonConverterFactory, client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(factory)
            .client(client)
            .build()
    }

    single { provideGsonConverterFactory() }
    single { provideHttpClient() }
    single { provideRetrofit(get(), get()) }
}
