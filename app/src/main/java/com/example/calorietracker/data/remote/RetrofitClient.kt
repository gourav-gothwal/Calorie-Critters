package com.example.calorietracker.data.remote

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit

object RetrofitClient {
    private const val BASE_URL = "https://api.spoonacular.com/"

    private val json = Json { ignoreUnknownKeys = true } // Important to ignore fields you don't need

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .client(OkHttpClient.Builder().build())
        .build()

    val apiService: SpoonacularApiService = retrofit.create(SpoonacularApiService::class.java)
}