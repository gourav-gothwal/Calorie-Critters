package com.example.nutrisnapapp

import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import coil.load
import coil.transform.RoundedCornersTransformation
import com.example.nutrisnapapp.data.models.RecipeDetailResponse
import com.example.nutrisnapapp.data.remote.RecipeRetrofitClient
import com.example.nutrisnapapp.databinding.FragmentRecipeDetailBinding
import com.google.android.material.chip.Chip
import kotlinx.coroutines.launch

class RecipeDetailFragment : Fragment() {

    private var _binding: FragmentRecipeDetailBinding? = null
    private val binding get() = _binding!!
    
    private var recipeId: Int = -1

    companion object {
        private const val TAG = "RecipeDetail"
        
        fun newInstance(mealId: Int): RecipeDetailFragment {
            return RecipeDetailFragment().apply {
                arguments = Bundle().apply {
                    putInt("mealId", mealId)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecipeDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get initial data from arguments to show something immediately
        recipeId = arguments?.getInt("mealId", -1) ?: -1
        val initialTitle = arguments?.getString("mealTitle")
        val initialImage = arguments?.getString("mealImage")
        
        // Show initial data if available
        initialTitle?.let { binding.textTitle.text = it }
        initialImage?.let { imageUrl ->
            binding.imageRecipe.load(imageUrl) {
                crossfade(true)
                placeholder(R.drawable.image_preview_placeholder)
                error(R.drawable.image_preview_placeholder)
            }
        }
        
        Log.d(TAG, "Recipe ID: $recipeId")
        
        setupToolbar()
        
        if (recipeId != -1) {
            fetchRecipeDetails(recipeId)
        } else {
            showError("Invalid recipe ID")
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            // Go back
            parentFragmentManager.popBackStack()
        }
    }

    private fun fetchRecipeDetails(id: Int) {
        showLoading(true)
        
        lifecycleScope.launch {
            if (_binding == null) return@launch
            try {
                // Check if it's one of our mock IDs (1, 2, 3)
                if (id in 1..3) {
                    showMockDetails(id)
                    return@launch
                }
                
                val response = RecipeRetrofitClient.api.getRecipeDetails(
                    id = id,
                    apiKey = BuildConfig.SPOONACULAR_API_KEY
                )
                
                Log.d(TAG, "Recipe fetched: ${response.title}")
                displayRecipe(response)
                
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching recipe", e)
                val errorMessage = if (e.message?.contains("402") == true) {
                    "Daily limit reached (API 402). Showing partial data."
                } else {
                    "Failed to load details: ${e.localizedMessage}"
                }
                showError(errorMessage)
            } finally {
                showLoading(false)
            }
        }
    }

    private fun showMockDetails(id: Int) {
        val mock = when(id) {
            1 -> RecipeDetailResponse(1, "Healthy Avocado Toast", null, "A simple, heart-healthy breakfast with mashed avocado, chili flakes, and a squeeze of lime on sourdough.", "1. Toast bread. 2. Mash avocado. 3. Season and spread.", 10, 1, null, null, true, true, true, true, 95, null, null)
            2 -> RecipeDetailResponse(2, "Fresh Berry Smoothie", null, "A vibrant mix of strawberries, blueberries, and Greek yogurt for a nutrient-packed start.", "1. Add all items to blender. 2. Blend until smooth. 3. Serve cold.", 5, 2, null, null, true, false, true, false, 88, null, null)
            else -> RecipeDetailResponse(3, "Grilled Chicken Salad", null, "Lean chicken breast over a bed of fresh greens, cherry tomatoes, and balsamic glaze.", "1. Grill chicken until cooked. 2. Chop vegetables. 3. Toss with dressing.", 25, 1, null, null, false, false, true, true, 92, null, null)
        }
        displayRecipe(mock)
        showLoading(false)
    }

    private fun displayRecipe(recipe: RecipeDetailResponse) {
        if (_binding == null) return
        // Load image
        recipe.image?.let { imageUrl ->
            binding.imageRecipe.load(imageUrl) {
                crossfade(true)
                crossfade(400)
            }
        }

        // Title
        binding.textTitle.text = recipe.title

        // Quick info
        binding.textTime.text = "${recipe.readyInMinutes ?: "--"} min"
        binding.textServings.text = "${recipe.servings ?: "--"}"
        binding.textHealthScore.text = "${recipe.healthScore ?: "--"}"

        // Diet tags
        setupDietTags(recipe)

        // Summary (strip HTML tags)
        recipe.summary?.let { summary ->
            val cleanSummary = Html.fromHtml(summary, Html.FROM_HTML_MODE_LEGACY).toString()
            binding.textSummary.text = cleanSummary
        } ?: run {
            binding.textSummary.text = "No description available."
        }

        // Ingredients
        displayIngredients(recipe)

        // Instructions (strip HTML tags)
        recipe.instructions?.let { instructions ->
            val cleanInstructions = Html.fromHtml(instructions, Html.FROM_HTML_MODE_LEGACY).toString()
            binding.textInstructions.text = cleanInstructions.ifEmpty { "No instructions available." }
        } ?: run {
            binding.textInstructions.text = "No instructions available."
        }
    }

    private fun setupDietTags(recipe: RecipeDetailResponse) {
        binding.chipGroupDiet.removeAllViews()
        
        val tags = mutableListOf<String>()
        
        if (recipe.vegetarian == true) tags.add("Vegetarian")
        if (recipe.vegan == true) tags.add("Vegan")
        if (recipe.glutenFree == true) tags.add("Gluten-Free")
        if (recipe.dairyFree == true) tags.add("Dairy-Free")
        
        if (tags.isNotEmpty()) {
            binding.chipGroupDiet.visibility = View.VISIBLE
            
            tags.forEach { tag ->
                val chip = Chip(requireContext()).apply {
                    text = tag
                    isClickable = false
                    setChipBackgroundColorResource(R.color.background_light)
                    setTextColor(ContextCompat.getColor(context, R.color.text_primary))
                    textSize = 12f
                }
                binding.chipGroupDiet.addView(chip)
            }
        } else {
            binding.chipGroupDiet.visibility = View.GONE
        }
    }

    private fun displayIngredients(recipe: RecipeDetailResponse) {
        binding.ingredientsContainer.removeAllViews()
        
        val ingredients = recipe.extendedIngredients
        
        if (ingredients.isNullOrEmpty()) {
            val noIngredients = TextView(requireContext()).apply {
                text = "No ingredients available."
                setTextColor(ContextCompat.getColor(context, R.color.text_secondary))
                textSize = 14f
            }
            binding.ingredientsContainer.addView(noIngredients)
            return
        }
        
        ingredients.forEachIndexed { index, ingredient ->
            val ingredientView = createIngredientView(ingredient.original ?: "${ingredient.amount} ${ingredient.unit ?: ""} ${ingredient.name}")
            binding.ingredientsContainer.addView(ingredientView)
            
            // Add divider except for last item
            if (index < ingredients.size - 1) {
                val divider = View(requireContext()).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        1
                    ).apply {
                        topMargin = 12
                        bottomMargin = 12
                    }
                    setBackgroundColor(ContextCompat.getColor(context, R.color.divider))
                }
                binding.ingredientsContainer.addView(divider)
            }
        }
    }

    private fun createIngredientView(text: String): View {
        return LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            
            // Bullet point
            val bullet = TextView(context).apply {
                this.text = "●"
                setTextColor(ContextCompat.getColor(context, R.color.accent_vivid))
                textSize = 16f
                setPadding(0, 0, 16, 0)
            }
            addView(bullet)
            
            // Ingredient text
            val ingredientText = TextView(context).apply {
                this.text = text.replaceFirstChar { it.uppercase() }
                setTextColor(ContextCompat.getColor(context, R.color.text_primary))
                textSize = 14f
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
            }
            addView(ingredientText)
        }
    }

    private fun showLoading(show: Boolean) {
        _binding?.let { 
            it.loadingOverlay.visibility = if (show) View.VISIBLE else View.GONE
        }
    }

    private fun showError(message: String) {
        binding.textSummary.text = message
        binding.textInstructions.text = ""
        binding.ingredientsContainer.removeAllViews()
        
        val errorText = TextView(requireContext()).apply {
            text = "Could not load recipe details."
            setTextColor(ContextCompat.getColor(context, R.color.text_secondary))
            textSize = 14f
        }
        binding.ingredientsContainer.addView(errorText)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
