package com.example.nutrisnapapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.nutrisnapapp.data.local.dao.MealDao
import com.example.nutrisnapapp.data.local.dao.UserDao
import com.example.nutrisnapapp.data.local.entities.MealLogEntity
import com.example.nutrisnapapp.data.local.entities.UserProfileEntity

@Database(entities = [MealLogEntity::class, UserProfileEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun mealDao(): MealDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "nutrisnap_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
