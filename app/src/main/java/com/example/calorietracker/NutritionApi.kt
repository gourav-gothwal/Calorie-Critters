package com.example.calorietracker

import com.example.calorietracker.data.models.CalorieResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface NutritionApi {
    @GET("natural/nutrients") // This endpoint is better for natural language queries
    suspend fun getCalorieInfo(
        @Header("x-app-id") appId: String,
        @Header("x-app-key") appKey: String,
        @Query("query") foodItem: String
    ): Response<CalorieResponse>
}