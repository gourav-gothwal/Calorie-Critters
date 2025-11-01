package com.example.nutrisnapapp

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class Information : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_information) // ✅ Make sure XML name is correct

        // ✅ Reference views
        val caloriesInput = findViewById<EditText>(R.id.editTextText3)
        val ageInput = findViewById<EditText>(R.id.editTextText4)
        val genderInput = findViewById<EditText>(R.id.editTextText5)
        val buttonStart = findViewById<Button>(R.id.button3)

        buttonStart.setOnClickListener {
            val calories = caloriesInput.text.toString().trim()
            val age = ageInput.text.toString().trim()
            val gender = genderInput.text.toString().trim()

            // ✅ Simple validation
            if (calories.isEmpty() || age.isEmpty() || gender.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            } else {
                // ✅ Next Step: You can save to SharedPreferences, Firebase, or navigate
                Toast.makeText(
                    this,
                    "Calories: $calories\nAge: $age\nGender: $gender",
                    Toast.LENGTH_LONG
                ).show()

                // TODO: You can navigate to HomeActivity or Dashboard here
                // startActivity(Intent(this, HomeActivity::class.java))
                // finish()
            }
        }
    }
}
