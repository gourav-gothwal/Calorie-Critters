package com.example.nutrisnapapp

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nutrisnapapp.adapters.MealAdapter
import com.example.nutrisnapapp.data.models.RecipeItem
import com.example.nutrisnapapp.data.remote.RecipeRetrofitClient
import com.example.nutrisnapapp.databinding.FragmentHomePageBinding
import com.example.nutrisnapapp.viewmodel.UserStatsViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class HomePage : Fragment() {

    private var _binding: FragmentHomePageBinding? = null
    private val binding get() = _binding!!

    private var totalCalories = 0
    private var totalWater = 0
    private val calorieGoal = 2000
    private val waterGoal = 3000
    
    private lateinit var mealAdapter: MealAdapter
    private val mealList = mutableListOf<RecipeItem>()
    private val statsViewModel: UserStatsViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomePageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUserInfo()
        setupProgressBars()
        setupClickListeners()
        setupRecyclerView()
        fetchRandomMeals()
        animateEntrance()
    }

    private fun setupUserInfo() {
        // Get user name from Firebase or arguments
        val currentUser = FirebaseAuth.getInstance().currentUser
        
        // Priority: 1. Intent argument, 2. Firebase displayName, 3. Email prefix, 4. "User"
        val userName = when {
            // Check if passed via arguments
            !arguments?.getString("name").isNullOrEmpty() -> {
                arguments?.getString("name")
            }
            // Check Firebase display name (from Google Sign-In)
            !currentUser?.displayName.isNullOrEmpty() -> {
                // Get first name only if there's a space
                currentUser?.displayName?.split(" ")?.firstOrNull() 
                    ?: currentUser?.displayName
            }
            // Use email prefix as fallback
            !currentUser?.email.isNullOrEmpty() -> {
                currentUser?.email?.substringBefore("@")?.replaceFirstChar { it.uppercase() }
            }
            else -> "User"
        }
        
        binding.userName.text = userName ?: "User"
    }

    private fun setupProgressBars() {
        binding.caloriesCircularProgress.progressMax = calorieGoal.toFloat()
        binding.waterCircularProgress.progressMax = waterGoal.toFloat()
        binding.caloriesCircularProgress.progress = totalCalories.toFloat()
        binding.waterCircularProgress.progress = totalWater.toFloat()
        
        updateCaloriesUI()
        updateWaterUI()
    }

    private fun setupClickListeners() {
        // Quick action buttons
        binding.btnAddCalories.setOnClickListener {
            showCalorieDialog()
        }

        binding.btnAddWater.setOnClickListener {
            showWaterDialog()
        }

        // FAB - navigate to scan page via bottom nav
        binding.fabAdd.setOnClickListener {
            // Select the scan tab in bottom navigation
            activity?.findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottomNavigationView)
                ?.selectedItemId = R.id.bottom_scan
        }

        // Calorie card tap
        binding.cardCalories.setOnClickListener {
            showCalorieDialog()
        }

        // Water card tap
        binding.cardWater.setOnClickListener {
            showWaterDialog()
        }
    }

    private fun setupRecyclerView() {
        binding.recyclerViewMeals.layoutManager = LinearLayoutManager(
            requireContext(), 
            LinearLayoutManager.VERTICAL, 
            false
        )

        mealAdapter = MealAdapter(mealList) { meal ->
            // Navigate to recipe detail via fragment transaction
            val detailFragment = RecipeDetailFragment().apply {
                arguments = Bundle().apply {
                    putInt("mealId", meal.id)
                }
            }
            parentFragmentManager.beginTransaction()
                .replace(R.id.flFragment, detailFragment)
                .addToBackStack(null)
                .commit()
        }
        
        binding.recyclerViewMeals.adapter = mealAdapter
    }

    private fun animateEntrance() {
        // Simple fade-in for cards
        binding.cardCalories.alpha = 0f
        binding.cardWater.alpha = 0f
        
        binding.cardCalories.animate()
            .alpha(1f)
            .setDuration(400)
            .setStartDelay(100)
            .start()
            
        binding.cardWater.animate()
            .alpha(1f)
            .setDuration(400)
            .setStartDelay(200)
            .start()
    }

    private fun showCalorieDialog() {
        val input = EditText(requireContext()).apply {
            hint = "Enter calories"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            setPadding(48, 32, 48, 32)
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Add Calories")
            .setView(input)
            .setPositiveButton("Add") { _, _ ->
                val value = input.text.toString().toIntOrNull() ?: 0
                if (value > 0) {
                    updateTotalCalories(value)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showWaterDialog() {
        val input = EditText(requireContext()).apply {
            hint = "Enter ml"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            setPadding(48, 32, 48, 32)
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Add Water")
            .setView(input)
            .setPositiveButton("Add") { _, _ ->
                val value = input.text.toString().toIntOrNull() ?: 0
                if (value > 0) {
                    updateTotalWater(value)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateTotalCalories(caloriesToAdd: Int) {
        totalCalories += caloriesToAdd
        updateCaloriesUI()
        
        // Animate progress
        binding.caloriesCircularProgress.setProgressWithAnimation(
            totalCalories.toFloat(), 
            500
        )
    }

    private fun updateTotalWater(waterToAdd: Int) {
        totalWater += waterToAdd
        updateWaterUI()
        
        // Animate progress
        binding.waterCircularProgress.setProgressWithAnimation(
            totalWater.toFloat(), 
            500
        )
    }

    private fun updateCaloriesUI() {
        binding.caloriesCount.text = totalCalories.toString()
        binding.caloriesGoal.text = "of $calorieGoal"
    }

    private fun updateWaterUI() {
        binding.waterIntake.text = totalWater.toString()
        binding.waterGoal.text = "of $waterGoal"
    }

    private fun fetchRandomMeals() {
        lifecycleScope.launch {
            try {
                val response = RecipeRetrofitClient.api.getRandomMeals(
                    apiKey = getString(R.string.api_key),
                    number = 5
                )

                Log.d("HomePage", "Recipes returned: ${response.recipes.size}")

                mealList.clear()
                mealList.addAll(response.recipes)
                mealAdapter.notifyDataSetChanged()

            } catch (e: Exception) {
                Log.e("HomePage", "API call failed", e)
                // Don't show toast for API failures - just log it
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
