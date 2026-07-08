package com.example.northbridge.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NetworkClient {
    private const val BASE_URL = "https://api.your-domain.com/" // TODO: Replace with your production API URL

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val apiService: NorthbridgeApiService by lazy {
        retrofit.create(NorthbridgeApiService::class.java)
    }
}
