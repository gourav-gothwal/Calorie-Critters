package com.example.calorietracker.data.remote

import com.example.calorietracker.data.models.FoodAnalysisModels
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface SpoonacularApiService {

    @Multipart
    @POST("food/images/analyze")
    suspend fun analyzeImage(
        @Query("apiKey") apiKey: String,
        @Part file: MultipartBody.Part
    ): Response<FoodAnalysisModels>
}