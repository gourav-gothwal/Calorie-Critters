package com.example.nutrisnapapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Get the username from the intent extras
        val userName = intent.getStringExtra("name") ?: ""

        // Create the HomePage fragment with the arguments
        val homeFragment = HomePage().apply {
            arguments = Bundle().apply {
                putString("name", userName)
            }
        }

        // Initialize the bottom navigation view
        bottomNavigationView = findViewById(R.id.bottomNavigationView)

        // Load the HomePage fragment by default
        supportFragmentManager.beginTransaction()
            .replace(R.id.flFragment, homeFragment)
            .commit()

        // Set the bottom navigation item selected listener
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.bottom_home -> {
                    loadFragment(HomePage().apply {
                        arguments = Bundle().apply {
                            putString("name", userName)
                        }
                    })
                }

                R.id.bottom_scan -> {
                    loadFragment(ScanPage())
                }

                R.id.bottom_profile -> {
                    loadFragment(ProfilePage())
                }
            }
            true
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.flFragment, fragment)
            .addToBackStack(null)
            .commit()
    }
}
