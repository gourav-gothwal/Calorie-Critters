package com.example.nutrisnapapp

import android.app.Application
import com.example.nutrisnapapp.data.local.AppDatabase
import com.example.nutrisnapapp.data.repository.NutritionRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.FirebaseApp

class MyApplication : Application() {

    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { 
        NutritionRepository(
            database.mealDao(), 
            database.userDao(), 
            FirebaseFirestore.getInstance()
        ) 
    }

    override fun onCreate() {
        super.onCreate()
    }
}
