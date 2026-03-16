package com.example.nutrisnapapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.nutrisnapapp.data.local.entities.MealLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MealDao {
    @Query("SELECT * FROM meal_logs WHERE userId = :userId ORDER BY timestamp DESC")
    fun getAllMeals(userId: String): Flow<List<MealLogEntity>>

    @Query("SELECT * FROM meal_logs WHERE userId = :userId AND timestamp >= :startOfDay ORDER BY timestamp ASC")
    fun getMealsForDay(userId: String, startOfDay: Long): Flow<List<MealLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeal(meal: MealLogEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeals(meals: List<MealLogEntity>)

    @Query("DELETE FROM meal_logs WHERE id = :mealId")
    suspend fun deleteMeal(mealId: String)

    @Query("SELECT * FROM meal_logs WHERE isSynced = 0")
    suspend fun getUnsyncedMeals(): List<MealLogEntity>

    @Query("UPDATE meal_logs SET isSynced = 1 WHERE id = :mealId")
    suspend fun markAsSynced(mealId: String)
}
