package com.example.nutrisnapapp.data.remote

import com.example.nutrisnapapp.data.models.FoodAnalysisModels
import com.example.nutrisnapapp.data.models.RandomRecipeResponse
import com.example.nutrisnapapp.data.models.RecipeDetailResponse
import com.example.nutrisnapapp.data.models.RecipeSearchResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface RecipeApiService {

    @GET("recipes/random")
    suspend fun getRandomMeals(
        @Query("apiKey") apiKey: String,
        @Query("number") number: Int = 10
    ): RandomRecipeResponse


    @GET("recipes/{id}/information")
    suspend fun getRecipeDetails(
        @Path("id") id: Int,
        @Query("apiKey") apiKey: String
    ): RecipeDetailResponse

    @Multipart
    @POST("food/images/analyze")
    suspend fun analyzeImage(
        @Query("apiKey") apiKey: String,
        @Part part: MultipartBody.Part
    ): Response<FoodAnalysisModels>
}
