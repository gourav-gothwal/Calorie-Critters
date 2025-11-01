package com.example.nutrisnapapp.data.models

import kotlinx.serialization.Serializable

@Serializable
data class FoodAnalysisModels(
    val category: FoodCategory,
    val nutrition: Nutrition,
    val recipes: List<RecipeItem>
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
