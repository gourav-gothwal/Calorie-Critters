package com.example.nutrisnapapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class UserStatsViewModel : ViewModel() {

    private val _caloriesConsumed = MutableLiveData(0)
    val caloriesConsumed: LiveData<Int> get() = _caloriesConsumed

    private val _waterIntake = MutableLiveData(0)
    val waterIntake: LiveData<Int> get() = _waterIntake

    fun addCalories(amount: Int) {
        _caloriesConsumed.value = (_caloriesConsumed.value ?: 0) + amount
    }

    fun addWater(amount: Int) {
        _waterIntake.value = (_waterIntake.value ?: 0) + amount
    }
}
