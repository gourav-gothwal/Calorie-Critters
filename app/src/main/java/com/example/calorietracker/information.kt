package com.example.calorietracker

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class information : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_information)

        val startButton = findViewById<Button>(R.id.button_journey)
        val editTextName = findViewById<EditText>(R.id.textInputEditText) // Check the correct IDs
        val editTextTargetCalories = findViewById<EditText>(R.id.textInputEditText3) // Check the correct IDs

        startButton.setOnClickListener {
            val name = editTextName.text.toString()
            val targetCalories = editTextTargetCalories.text.toString()

            if (name.isBlank() || targetCalories.isBlank()) {
                Toast.makeText(
                    this,
                    "Please enter both name and target calories",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val calories = targetCalories.toIntOrNull()
            if (calories == null) {
                Toast.makeText(
                    this,
                    "Please enter a valid number for target calories",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val intent = Intent(this, MainActivity2::class.java).apply {
                putExtra("name", name)
                putExtra("targetCalories", calories)
            }
            startActivity(intent)
        }
    }
}
