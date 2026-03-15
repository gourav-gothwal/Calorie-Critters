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
        setupSwipeRefresh()
        setupClickListeners()
        setupRecyclerView()
        
        // Only fetch if we don't have data yet (prevents refreshing every time we return to home)
        if (mealList.isEmpty()) {
            fetchRandomMeals()
        }
        
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

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setColorSchemeResources(R.color.accent_vivid)
        binding.swipeRefresh.setOnRefreshListener {
            fetchRandomMeals()
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

        mealAdapter = MealAdapter(mealList) { meal ->
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
                fetchRandomMeals() // Show default meals when search is cleared
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
        lifecycleScope.launch {
            if (_binding == null) return@launch
            try {
                binding.swipeRefresh.isRefreshing = true
                val response = RecipeRetrofitClient.api.searchRecipes(
                    apiKey = BuildConfig.SPOONACULAR_API_KEY,
                    query = query,
                    number = 20
                )

                if (response.results.isEmpty()) {
                    binding.textNoMeals.visibility = View.VISIBLE
                    binding.textNoMeals.text = "No results found for '$query'"
                    mealList.clear()
                    mealAdapter.notifyDataSetChanged()
                } else {
                    binding.textNoMeals.visibility = View.GONE
                    mealList.clear()
                    mealList.addAll(response.results)
                    mealAdapter.notifyDataSetChanged()
                }

            } catch (e: Exception) {
                Log.e("HomePage", "Search failed: ${e.message}")
                if (e.message?.contains("402") == true) {
                    binding.textNoMeals.visibility = View.VISIBLE
                    binding.textNoMeals.text = "Daily limit reached. Search will work tomorrow."
                }
            } finally {
                binding.swipeRefresh.isRefreshing = false
            }
        }
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
            if (_binding == null) return@launch
            try {
                val response = RecipeRetrofitClient.api.getRandomMeals(
                    apiKey = BuildConfig.SPOONACULAR_API_KEY
                )
                
                if (response.recipes.isEmpty()) {
                    showFallbackMeals()
                } else {
                    if (_binding == null) return@launch
                    binding.textNoMeals.visibility = View.GONE
                    mealList.clear()
                    mealList.addAll(response.recipes)
                    mealAdapter.notifyDataSetChanged()
                }

            } catch (e: Exception) {
                Log.e("HomePage", "API call failed: ${e.message}", e)
                showFallbackMeals()
                if (e.message?.contains("402") == true) {
                    Log.e("HomePage", "Spoonacular Quota Exceeded (402)")
                }
            } finally {
                binding.swipeRefresh.isRefreshing = false
            }
        }
    }

    private fun showFallbackMeals() {
        if (_binding == null) return
        // Show offline/placeholder meals so the app doesn't look empty
        binding.textNoMeals.visibility = View.VISIBLE
        binding.textNoMeals.text = "Using local samples (API limit reached)"
        
        val fallbackMeals = listOf(
            RecipeItem(1, "Healthy Avocado Toast", "https://images.unsplash.com/photo-1525351484163-7529414344d8"),
            RecipeItem(2, "Fresh Berry Smoothie", "https://images.unsplash.com/photo-1553530666-ba11a7da3888"),
            RecipeItem(3, "Grilled Chicken Salad", "https://images.unsplash.com/photo-1546069901-ba9599a7e63c")
        )
        
        mealList.clear()
        mealList.addAll(fallbackMeals)
        mealAdapter.notifyDataSetChanged()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
