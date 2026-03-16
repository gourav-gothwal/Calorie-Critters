package com.example.nutrisnapapp

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.inputmethod.EditorInfo
import android.content.Context
import android.view.inputmethod.InputMethodManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.nutrisnapapp.adapters.MealAdapter
import com.example.nutrisnapapp.data.models.RecipeItem
import com.example.nutrisnapapp.data.models.RecipeSearchResponse
import com.example.nutrisnapapp.data.remote.RecipeRetrofitClient
import com.example.nutrisnapapp.databinding.FragmentHomePageBinding
import com.example.nutrisnapapp.viewmodel.RecipeViewModel
import com.example.nutrisnapapp.viewmodel.UserStatsViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class HomePage : Fragment() {

    private var _binding: FragmentHomePageBinding? = null
    private val binding get() = _binding!!

    private lateinit var mealAdapter: MealAdapter
    private val statsViewModel: UserStatsViewModel by activityViewModels()
    private val recipeViewModel: RecipeViewModel by activityViewModels()

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
        setupSwipeRefresh()
        setupClickListeners()
        setupRecyclerView()
        setupObservers()
        
        // Initial fetch - ViewModel will decide if it needs to hit the network
        recipeViewModel.fetchRandomRecipes()
        
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
        // Initial values will be handled by observers
        binding.caloriesCircularProgress.progress = 0f
        binding.waterCircularProgress.progress = 0f
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setColorSchemeResources(R.color.accent_vivid)
        binding.swipeRefresh.setOnRefreshListener {
            recipeViewModel.fetchRandomRecipes(forceRefresh = true)
        }
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

        mealAdapter = MealAdapter(emptyList()) { meal ->
            // Navigate to recipe detail via fragment transaction
            val detailFragment = RecipeDetailFragment().apply {
                arguments = Bundle().apply {
                    putInt("mealId", meal.id)
                    putString("mealTitle", meal.title)
                    putString("mealImage", meal.image)
                }
            }
            parentFragmentManager.beginTransaction()
                .replace(R.id.flFragment, detailFragment)
                .addToBackStack(null)
                .commit()
        }
        
        binding.recyclerViewMeals.adapter = mealAdapter
        
        setupSearch()
    }

    private fun setupObservers() {
        // Recipe Observers
        lifecycleScope.launch {
            recipeViewModel.recipes.collect { recipes ->
                mealAdapter.updateData(recipes)
                binding.textNoMeals.visibility = if (recipes.isEmpty()) View.VISIBLE else View.GONE
            }
        }

        lifecycleScope.launch {
            recipeViewModel.isLoading.collect { isLoading ->
                binding.swipeRefresh.isRefreshing = isLoading
            }
        }

        lifecycleScope.launch {
            recipeViewModel.errorMessage.collect { message ->
                if (message != null) {
                    binding.textNoMeals.visibility = View.VISIBLE
                    binding.textNoMeals.text = message
                }
            }
        }

        // Stats Observers
        statsViewModel.caloriesConsumed.observe(viewLifecycleOwner) { calories ->
            binding.caloriesCount.text = calories.toString()
            binding.caloriesCircularProgress.setProgressWithAnimation(calories.toFloat(), 500)
        }

        statsViewModel.waterIntake.observe(viewLifecycleOwner) { water ->
            binding.waterIntake.text = water.toString()
            binding.waterCircularProgress.setProgressWithAnimation(water.toFloat(), 500)
        }

        statsViewModel.calorieGoal.observe(viewLifecycleOwner) { goal ->
            binding.caloriesCircularProgress.progressMax = goal.toFloat()
            binding.caloriesGoal.text = "of $goal"
        }

        statsViewModel.waterGoal.observe(viewLifecycleOwner) { goal ->
            binding.waterCircularProgress.progressMax = goal.toFloat()
            binding.waterGoal.text = "of $goal"
        }
    }

    private fun setupSearch() {
        binding.editSearch.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = v.text.toString()
                if (query.isNotEmpty()) {
                    searchMeals(query)
                    hideKeyboard()
                }
                true
            } else {
                false
            }
        }

        binding.editSearch.addTextChangedListener {
            val query = it.toString()
            binding.btnClearSearch.visibility = if (query.isEmpty()) View.GONE else View.VISIBLE
            
            if (query.isEmpty()) {
                // If search cleared, go back to random list (locally)
                recipeViewModel.fetchRandomRecipes(forceRefresh = false)
            }
        }

        binding.btnClearSearch.setOnClickListener {
            binding.editSearch.text?.clear()
        }
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
    }

    private fun searchMeals(query: String) {
        recipeViewModel.searchRecipes(query)
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
        statsViewModel.addCalories(caloriesToAdd)
    }

    private fun updateTotalWater(waterToAdd: Int) {
        statsViewModel.addWater(waterToAdd)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
