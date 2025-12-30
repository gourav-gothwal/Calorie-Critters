package com.example.nutrisnapapp.data.models

import com.google.gson.annotations.SerializedName

data class RecipeSearchResponse(
    val results: List<RecipeItem>
)

data class RecipeItem(
    val id: Int,
    val title: String,
    val image: String?
)

data class RecipeDetailResponse(
    @SerializedName("id")
    val id: Int,
    @SerializedName("title")
    val title: String,
    @SerializedName("image")
    val image: String?,
    @SerializedName("summary")
    val summary: String?,
    @SerializedName("instructions")
    val instructions: String?,
    @SerializedName("readyInMinutes")
    val readyInMinutes: Int?,
    @SerializedName("servings")
    val servings: Int?,
    @SerializedName("sourceUrl")
    val sourceUrl: String?,
    @SerializedName("extendedIngredients")
    val extendedIngredients: List<Ingredient>?,
    @SerializedName("vegetarian")
    val vegetarian: Boolean?,
    @SerializedName("vegan")
    val vegan: Boolean?,
    @SerializedName("glutenFree")
    val glutenFree: Boolean?,
    @SerializedName("dairyFree")
    val dairyFree: Boolean?,
    @SerializedName("healthScore")
    val healthScore: Int?,
    @SerializedName("pricePerServing")
    val pricePerServing: Double?,
    @SerializedName("nutrition")
    val nutrition: RecipeNutrition?
)

data class Ingredient(
    @SerializedName("id")
    val id: Int?,
    @SerializedName("name")
    val name: String,
    @SerializedName("amount")
    val amount: Double,
    @SerializedName("unit")
    val unit: String?,
    @SerializedName("original")
    val original: String?
)

data class RecipeNutrition(
    @SerializedName("nutrients")
    val nutrients: List<RecipeNutrient>?
)

data class RecipeNutrient(
    @SerializedName("name")
    val name: String,
    @SerializedName("amount")
    val amount: Double,
    @SerializedName("unit")
    val unit: String
)

data class RandomRecipeResponse(
    val recipes: List<RecipeItem>
)
