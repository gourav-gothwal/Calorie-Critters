package com.example.nutrisnapapp.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "meal_logs")
data class MealLogEntity(
    @PrimaryKey val id: String = "", 
    val userId: String = "",
    val timestamp: Long = 0L,
    val mealType: String = "",
    val foodName: String = "",
    val calories: Double = 0.0,
    val protein: Double = 0.0,
    val carbs: Double = 0.0,
    val fat: Double = 0.0,
    val imageUrl: String? = null,
    val isSynced: Boolean = false
)
