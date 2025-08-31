package com.example.calorietracker.data.models

import kotlinx.serialization.Serializable

@Serializable
data class FoodAnalysisModels(
    val category: FoodCategory,
    val nutrition: Nutrition,
    val recipes: List<Recipe>
)

@Serializable
data class FoodCategory(
    val name: String,
    val probability: Double
)

@Serializable
data class Nutrition(
    val recipesUsed: Int,
    val calories: Nutrient,
    val fat: Nutrient,
    val protein: Nutrient,
    val carbs: Nutrient
)

@Serializable
data class Nutrient(
    val value: Double,
    val unit: String
)

@Serializable
data class Recipe(
    val id: Int,
    val title: String,
    val imageType: String,
    val url: String
)
