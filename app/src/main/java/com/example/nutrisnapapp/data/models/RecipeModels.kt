package com.example.nutrisnapapp.data.models

data class RecipeSearchResponse(
    val results: List<RecipeItem>
)

data class RecipeItem(
    val id: Int,
    val title: String,
    val image: String
)

data class RecipeDetailResponse(
    val id: Int,
    val title: String,
    val image: String,
    val summary: String,
    val instructions: String?,
    val readyInMinutes: Int,
    val servings: Int,
    val sourceUrl: String,
    val extendedIngredients: List<Ingredient>
)

data class Ingredient(
    val name: String,
    val amount: Double,
    val unit: String
)

data class RandomRecipeResponse(
    val recipes: List<RecipeItem>
)
