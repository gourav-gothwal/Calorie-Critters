package com.example.nutrisnapapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashScreen : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        // Initialize Firebase
        com.google.firebase.FirebaseApp.initializeApp(this)

        // Get FirebaseAuth instance
        auth = FirebaseAuth.getInstance()

        // Delay for 2 seconds using coroutine
        lifecycleScope.launch {
            delay(2000)
            if (auth.currentUser != null) {
                startActivity(Intent(this@SplashScreen, MainActivity::class.java))
            } else {
                startActivity(Intent(this@SplashScreen, LoginScreen::class.java))
            }
            finish()
        }
    }
}
