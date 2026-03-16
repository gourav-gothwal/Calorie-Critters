package com.example.nutrisnapapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.nutrisnapapp.data.local.entities.UserProfileEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Information : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_information)

        val caloriesInput = findViewById<EditText>(R.id.editTextText3)
        val ageInput = findViewById<EditText>(R.id.editTextText4)
        val genderInput = findViewById<EditText>(R.id.editTextText5)
        val buttonStart = findViewById<Button>(R.id.button3)

        buttonStart.setOnClickListener {
            val calories = caloriesInput.text.toString().trim()
            val age = ageInput.text.toString().trim()
            val gender = genderInput.text.toString().trim()

            if (calories.isEmpty() || age.isEmpty() || gender.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val uid = auth.currentUser?.uid
            if (uid == null) {
                Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Save to Firestore
            val userProfile = mutableMapOf<String, Any>(
                "uid" to uid,
                "dailyCalorieGoal" to (calories.toIntOrNull() ?: 2000),
                "age" to (age.toIntOrNull() ?: 0),
                "gender" to gender,
                "setupComplete" to true
            )

            buttonStart.isEnabled = false
            buttonStart.text = "Saving..."

            firestore.collection("users").document(uid)
                .set(userProfile)
                .addOnSuccessListener {
                    Toast.makeText(this, "Setup Complete!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
                .addOnFailureListener { e ->
                    buttonStart.isEnabled = true
                    buttonStart.text = "Complete Setup"
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
