package com.example.nutrisnapapp.data.models

import com.google.gson.annotations.SerializedName

data class FoodAnalysisModels(
    @SerializedName("category")
    val category: FoodCategory,
    @SerializedName("nutrition")
    val nutrition: Nutrition,
    @SerializedName("recipes")
    val recipes: List<AnalyzedRecipe>? = null
)

data class FoodCategory(
    @SerializedName("name")
    val name: String,
    @SerializedName("probability")
    val probability: Double
)

data class Nutrition(
    @SerializedName("recipesUsed")
    val recipesUsed: Int,
    @SerializedName("calories")
    val calories: Nutrient,
    @SerializedName("fat")
    val fat: Nutrient,
    @SerializedName("protein")
    val protein: Nutrient,
    @SerializedName("carbs")
    val carbs: Nutrient
)

data class Nutrient(
    @SerializedName("value")
    val value: Double,
    @SerializedName("unit")
    val unit: String,
    @SerializedName("confidenceRange95Percent")
    val confidenceRange95Percent: ConfidenceRange? = null,
    @SerializedName("standardDeviation")
    val standardDeviation: Double? = null
)

data class ConfidenceRange(
    @SerializedName("min")
    val min: Double,
    @SerializedName("max")
    val max: Double
)

data class AnalyzedRecipe(
    @SerializedName("id")
    val id: Int,
    @SerializedName("title")
    val title: String,
    @SerializedName("imageType")
    val imageType: String? = null,
    @SerializedName("sourceUrl")
    val sourceUrl: String? = null
)
