package com.example.nutrisnapapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutrisnapapp.data.local.entities.UserProfileEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class UserStatsViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _caloriesConsumed = MutableLiveData(0)
    val caloriesConsumed: LiveData<Int> get() = _caloriesConsumed

    private val _waterIntake = MutableLiveData(0)
    val waterIntake: LiveData<Int> get() = _waterIntake

    private val _calorieGoal = MutableLiveData(2000)
    val calorieGoal: LiveData<Int> get() = _calorieGoal

    private val _waterGoal = MutableLiveData(3000)
    val waterGoal: LiveData<Int> get() = _waterGoal

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    init {
        loadDailyStats()
    }

    private fun loadDailyStats() {
        val uid = auth.currentUser?.uid ?: return
        val today = dateFormat.format(Date())

        firestore.collection("users").document(uid)
            .collection("daily_stats").document(today)
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener

                if (snapshot != null && snapshot.exists()) {
                    _caloriesConsumed.value = snapshot.getLong("calories")?.toInt() ?: 0
                    _waterIntake.value = snapshot.getLong("water")?.toInt() ?: 0
                } else {
                    _caloriesConsumed.value = 0
                    _waterIntake.value = 0
                }
            }

        // Also load user profile goals
        firestore.collection("users").document(uid)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val goal = snapshot.getLong("dailyCalorieGoal")?.toInt() ?: 2000
                    _calorieGoal.value = goal
                }
            }
    }

    fun addCalories(amount: Int) {
        val uid = auth.currentUser?.uid ?: return
        val today = dateFormat.format(Date())
        val newTotal = (caloriesConsumed.value ?: 0) + amount

        firestore.collection("users").document(uid)
            .collection("daily_stats").document(today)
            .set(mapOf("calories" to newTotal, "water" to (waterIntake.value ?: 0)))
    }

    fun addWater(amount: Int) {
        val uid = auth.currentUser?.uid ?: return
        val today = dateFormat.format(Date())
        val newTotal = (waterIntake.value ?: 0) + amount

        firestore.collection("users").document(uid)
            .collection("daily_stats").document(today)
            .set(mapOf("calories" to (caloriesConsumed.value ?: 0), "water" to newTotal))
    }
    
    fun setCalorieGoal(goal: Int) {
        val uid = auth.currentUser?.uid ?: return
        _calorieGoal.value = goal
        firestore.collection("users").document(uid)
            .update("dailyCalorieGoal", goal)
    }

    fun refreshStats() {
        loadDailyStats()
    }
}
