package com.example.calorietracker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.calorietracker.databinding.FragmentProfilePageBinding
import java.util.Locale
import kotlin.text.toDoubleOrNull

class ProfilePage : Fragment() {

    private var _binding: FragmentProfilePageBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfilePageBinding.inflate(inflater, container, false)
        setupUI()
        return binding.root
    }

    private fun setupUI() {
        // Set the username from arguments
        val userName = arguments?.getString("name") ?: "Guest"
        binding.profileName.text = userName

        // Add listener to display BMI when inputs change
        binding.weightInput.setOnFocusChangeListener { _, hasFocus -> if (!hasFocus) showBMI() }
        binding.heightInput.setOnFocusChangeListener { _, hasFocus -> if (!hasFocus) showBMI() }
    }

    // Method to calculate BMI
    private fun calculateBMI(weight: Double, height: Double): String {
        if (height <= 0) {
            return "Invalid height"
        }
        if (weight <= 0) {
            return "Invalid weight"
        }
        val bmi = weight / (height * height)
        return String.format(Locale.getDefault(), "Your BMI is %.2f", bmi)
    }

    private fun showBMI() {
        val weightValue = binding.weightInput.text.toString().toDoubleOrNull() ?: 0.0
        val heightValue = binding.heightInput.text.toString().toDoubleOrNull() ?: 0.0

        val bmiText = calculateBMI(weightValue, heightValue)
        binding.bmiResult.text = bmiText
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}