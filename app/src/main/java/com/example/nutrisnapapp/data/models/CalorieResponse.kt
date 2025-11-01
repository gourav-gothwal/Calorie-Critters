package com.example.nutrisnapapp.data.models

import com.google.gson.annotations.SerializedName

data class CalorieResponse(
    @SerializedName("foods") val foods: List<Food>
)

data class Food(
    @SerializedName("food_name") val foodName: String,
    @SerializedName("nf_calories") val calories: Float
)