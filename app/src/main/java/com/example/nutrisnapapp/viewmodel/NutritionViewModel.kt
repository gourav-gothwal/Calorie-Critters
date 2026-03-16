package com.example.nutrisnapapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.nutrisnapapp.data.local.entities.MealLogEntity
import com.example.nutrisnapapp.data.local.entities.UserProfileEntity
import com.example.nutrisnapapp.data.repository.NutritionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class NutritionViewModel(private val repository: NutritionRepository) : ViewModel() {

    fun getMeals(userId: String): Flow<List<MealLogEntity>> = repository.getMealsForUser(userId)

    fun getDailyMeals(userId: String, startOfDay: Long): Flow<List<MealLogEntity>> = 
        repository.getDailyMeals(userId, startOfDay)

    fun logMeal(meal: MealLogEntity) {
        viewModelScope.launch {
            repository.logMeal(meal)
        }
    }

    fun syncMeals(userId: String) {
        viewModelScope.launch {
            repository.syncRemoteToLocal(userId)
        }
    }

    fun getProfile(userId: String): Flow<UserProfileEntity?> = repository.getProfile(userId)

    fun updateProfile(profile: UserProfileEntity) {
        viewModelScope.launch {
            repository.updateProfile(profile)
        }
    }

    fun fetchProfile(userId: String) {
        viewModelScope.launch {
            repository.fetchProfileFromServer(userId)
        }
    }

    class Factory(private val repository: NutritionRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(NutritionViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return NutritionViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
