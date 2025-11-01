package com.example.nutrisnapapp

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nutrisnapapp.adapters.MealAdapter
import com.example.nutrisnapapp.data.remote.RecipeRetrofitClient
import kotlinx.coroutines.launch
import androidx.navigation.fragment.findNavController
import com.example.nutrisnapapp.data.models.RecipeItem
import com.example.nutrisnapapp.viewmodel.UserStatsViewModel


class HomePage : Fragment() {

    private lateinit var fabAdd: com.google.android.material.floatingactionbutton.FloatingActionButton

    private lateinit var totalCaloriesTextView: TextView
    private lateinit var totalWaterTextView: TextView
    private lateinit var calorieProgressBar: com.mikhaellopez.circularprogressbar.CircularProgressBar
    private lateinit var waterProgressBar: com.mikhaellopez.circularprogressbar.CircularProgressBar

    private lateinit var name: TextView

    private var totalCalories = 0
    private var totalWater = 0
    private lateinit var mealAdapter: MealAdapter
    private lateinit var recyclerViewMeals: RecyclerView
    private val mealList = mutableListOf<RecipeItem>()
    private val statsViewModel: UserStatsViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home_page, container, false)

        totalCaloriesTextView = view.findViewById(R.id.caloriesCount)
        totalWaterTextView = view.findViewById(R.id.waterIntake)
        calorieProgressBar = view.findViewById(R.id.caloriesCircularProgress)
        waterProgressBar = view.findViewById(R.id.waterCircularProgress)
        name = view.findViewById(R.id.userName)

        fabAdd = view.findViewById(R.id.fabAdd)
        fabAdd.setOnClickListener {
            showAddOptions()
        }

        val userName = arguments?.getString("name") ?: ""
        name.text = "$userName"

        calorieProgressBar.progress = totalCalories.toFloat()
        waterProgressBar.progress = totalWater.toFloat()
        calorieProgressBar.progressMax = 2000f
        waterProgressBar.progressMax = 3000f

        recyclerViewMeals = view.findViewById(R.id.recyclerViewMeals)
        recyclerViewMeals.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

// Initialize empty adapter
        // In HomePage.kt
        mealAdapter = MealAdapter(mealList) { meal ->
            val bundle = Bundle()
            bundle.putInt("mealId", meal.id)
            // Safe navigation from fragment
            findNavController().navigate(R.id.action_home_to_recipeDetail, bundle)
        }
        recyclerViewMeals.adapter = mealAdapter


// Fetch meals from API
        fetchRandomMeals()
        return view
    }

    private fun showCalorieAlert(context: Context) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Add Calories")
        builder.setMessage("Enter value")
        val input = EditText(context)
        builder.setView(input)
        builder.setPositiveButton("OK") { dialog, _ ->
            val value = input.text.toString()
            if (value.isNotEmpty()) {
                val calories = value.toIntOrNull() ?: 0
                updateTotalCalories(calories)
            } else {
                Toast.makeText(context, "Invalid input", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
        }
        builder.show()
    }

    private fun showWaterAlert(context: Context) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Add Water")
        builder.setMessage("Enter value (ml)")
        val input = EditText(context)
        builder.setView(input)
        builder.setPositiveButton("OK") { dialog, _ ->
            val value = input.text.toString()
            if (value.isNotEmpty()) {
                val water = value.toIntOrNull() ?: 0
                updateTotalWater(water)
            } else {
                Toast.makeText(context, "Invalid input", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
        }
        builder.show()
    }

    private fun updateTotalCalories(caloriesToAdd: Int) {
        totalCalories += caloriesToAdd
        totalCaloriesTextView.text = totalCalories.toString()
        calorieProgressBar.progress = totalCalories.toFloat()
    }

    private fun updateTotalWater(waterToAdd: Int) {
        totalWater += waterToAdd
        totalWaterTextView.text = totalWater.toString()
        waterProgressBar.progress = totalWater.toFloat()
    }

    private fun fetchRandomMeals() {
        lifecycleScope.launch {
            try {
                val response = RecipeRetrofitClient.api.getRandomMeals(
                    apiKey = getString(R.string.api_key),
                    number = 10
                )

                Log.d("HomePage", "Recipes returned: ${response.recipes.size}")
                response.recipes.forEach { Log.d("HomePage", it.title) }

                val meals = response.recipes
                mealList.clear()
                mealList.addAll(meals)
                mealAdapter.notifyDataSetChanged()

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "API call failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }


    private fun showAddOptions() {
        val options = arrayOf("Add Calories", "Add Water")
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
        builder.setTitle("Select Option")
        builder.setItems(options) { dialog, which ->
            when (which) {
                0 -> showCalorieAlert(requireContext())
                1 -> showWaterAlert(requireContext())
            }
        }
        builder.show()
    }
}
