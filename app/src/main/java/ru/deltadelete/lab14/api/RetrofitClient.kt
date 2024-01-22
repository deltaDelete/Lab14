package ru.deltadelete.lab14.api

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException

import java.util.logging.Logger





object RetrofitClient {
    private lateinit var retrofit: Retrofit

    fun getClient(baseUrl: String): Retrofit {
        if (!this::retrofit.isInitialized) {
            retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(
                    GsonConverterFactory.create(
                        GsonBuilder()
                            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").create()
                    )
                )
                .build()
        }
        return retrofit
    }
}