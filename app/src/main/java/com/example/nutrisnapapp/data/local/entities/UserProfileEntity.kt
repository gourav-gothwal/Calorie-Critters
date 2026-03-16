package com.example.nutrisnapapp.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profiles")
data class UserProfileEntity(
    @PrimaryKey val uid: String = "",
    val displayName: String? = null,
    val email: String? = null,
    val age: Int? = null,
    val weight: Float? = null,
    val height: Int? = null,
    val gender: String? = null,
    val dailyCalorieGoal: Int? = null
)
