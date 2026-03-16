package com.example.nutrisnapapp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutrisnapapp.BuildConfig
import com.example.nutrisnapapp.data.models.RecipeItem
import com.example.nutrisnapapp.data.remote.RecipeRetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RecipeViewModel : ViewModel() {

    // These are the "meal ideas" that only refresh on cold start or pull-to-refresh
    private val _randomRecipes = MutableStateFlow<List<RecipeItem>>(emptyList())
    
    // This is what the UI currently displays (could be meal ideas or search results)
    private val _recipes = MutableStateFlow<List<RecipeItem>>(emptyList())
    val recipes: StateFlow<List<RecipeItem>> = _recipes

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun fetchRandomRecipes(forceRefresh: Boolean = false) {
        // If we already have random recipes and NOT forcing a refresh, just show them
        if (!forceRefresh && _randomRecipes.value.isNotEmpty()) {
            _recipes.value = _randomRecipes.value
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val response = RecipeRetrofitClient.api.getRandomMeals(
                    apiKey = BuildConfig.SPOONACULAR_API_KEY
                )
                
                val resultList = if (response.recipes.isEmpty()) {
                    getFallbackRecipes()
                } else {
                    response.recipes
                }
                
                _randomRecipes.value = resultList
                _recipes.value = resultList
                
            } catch (e: Exception) {
                Log.e("RecipeViewModel", "API call failed: ${e.message}", e)
                if (_randomRecipes.value.isEmpty()) {
                    val fallback = getFallbackRecipes()
                    _randomRecipes.value = fallback
                    _recipes.value = fallback
                } else {
                    // Just revert to existing random recipes
                    _recipes.value = _randomRecipes.value
                }
                
                if (e.message?.contains("402") == true) {
                    _errorMessage.value = "Daily limit reached. Using local samples."
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun searchRecipes(query: String) {
        if (query.isEmpty()) {
            _recipes.value = _randomRecipes.value
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val response = RecipeRetrofitClient.api.searchRecipes(
                    apiKey = BuildConfig.SPOONACULAR_API_KEY,
                    query = query,
                    number = 20
                )

                if (response.results.isEmpty()) {
                    _errorMessage.value = "No results found for '$query'"
                    _recipes.value = emptyList()
                } else {
                    _recipes.value = response.results
                }
            } catch (e: Exception) {
                Log.e("RecipeViewModel", "Search failed: ${e.message}")
                if (e.message?.contains("402") == true) {
                    _errorMessage.value = "Daily limit reached. Search will work tomorrow."
                } else {
                    _errorMessage.value = "Search failed: ${e.message}"
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun getFallbackRecipes(): List<RecipeItem> {
        return listOf(
            RecipeItem(1, "Healthy Avocado Toast", "https://images.unsplash.com/photo-1525351484163-7529414344d8"),
            RecipeItem(2, "Fresh Berry Smoothie", "https://images.unsplash.com/photo-1553530666-ba11a7da3888"),
            RecipeItem(3, "Grilled Chicken Salad", "https://images.unsplash.com/photo-1546069901-ba9599a7e63c")
        )
    }
}
