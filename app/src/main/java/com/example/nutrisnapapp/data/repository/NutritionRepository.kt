package com.example.nutrisnapapp.data.repository

import android.util.Log
import com.example.nutrisnapapp.data.local.dao.MealDao
import com.example.nutrisnapapp.data.local.dao.UserDao
import com.example.nutrisnapapp.data.local.entities.MealLogEntity
import com.example.nutrisnapapp.data.local.entities.UserProfileEntity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await

class NutritionRepository(
    private val mealDao: MealDao,
    private val userDao: UserDao,
    private val firestore: FirebaseFirestore
) {
    private val TAG = "NutritionRepository"
    private val MEALS_COLLECTION = "meals"
    private val USERS_COLLECTION = "users"

    // --- MEALS LOGIC ---

    fun getMealsForUser(userId: String): Flow<List<MealLogEntity>> = mealDao.getAllMeals(userId)

    fun getDailyMeals(userId: String, startOfDay: Long): Flow<List<MealLogEntity>> = 
        mealDao.getMealsForDay(userId, startOfDay)

    suspend fun logMeal(meal: MealLogEntity) {
        // 1. Save to Local Room immediately
        mealDao.insertMeal(meal.copy(isSynced = false))

        // 2. Effort to sync with Firestore
        try {
            firestore.collection(MEALS_COLLECTION)
                .document(meal.id)
                .set(meal)
                .await()
            
            // 3. If successful, mark as synced
            mealDao.markAsSynced(meal.id)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync meal to Firestore: ${e.message}")
            // It remains in Room with isSynced = false
        }
    }

    suspend fun syncRemoteToLocal(userId: String) {
        try {
            val snapshot = firestore.collection(MEALS_COLLECTION)
                .whereEqualTo("userId", userId)
                .get()
                .await()
            
            val remoteMeals = snapshot.toObjects(MealLogEntity::class.java)
            if (remoteMeals.isNotEmpty()) {
                mealDao.insertMeals(remoteMeals.map { it.copy(isSynced = true) })
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching from Firestore: ${e.message}")
        }
    }

    // --- PROFILE LOGIC ---

    fun getProfile(userId: String): Flow<UserProfileEntity?> = userDao.getUserProfile(userId)

    suspend fun updateProfile(profile: UserProfileEntity) {
        // Save Local
        userDao.insertUserProfile(profile)

        // Save Remote
        try {
            firestore.collection(USERS_COLLECTION)
                .document(profile.uid)
                .set(profile)
                .await()
        } catch (e: Exception) {
            Log.e(TAG, "Error updating profile Firestore: ${e.message}")
        }
    }

    suspend fun fetchProfileFromServer(userId: String) {
        try {
            val document = firestore.collection(USERS_COLLECTION)
                .document(userId)
                .get()
                .await()
            
            val profile = document.toObject(UserProfileEntity::class.java)
            if (profile != null) {
                userDao.insertUserProfile(profile)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching profile: ${e.message}")
        }
    }
}
