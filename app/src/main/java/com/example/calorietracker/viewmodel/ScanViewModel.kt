package com.example.calorietracker.ui.scan

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.calorietracker.BuildConfig
import com.example.calorietracker.data.model.CalorieResponse
import com.example.calorietracker.data.remote.NutritionRequest
import com.example.calorietracker.data.remote.RetrofitClient
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class UiState {
    object Idle : UiState()
    object Loading : UiState()
    data class Success(val response: CalorieResponse) : UiState()
    data class Error(val message: String) : UiState()
}

class ScanViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState

    fun processImage(bitmap: Bitmap) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                // 1. Use ML Kit Image Labeling to identify the food
                val foodLabel = getFoodLabelFromImage(bitmap)

                if (foodLabel != null) {
                    // 2. Create the request body and call the Nutritionix API
                    val request = NutritionRequest(query = foodLabel) // <-- FIX IS HERE
                    val response = RetrofitClient.instance.getCalorieInfo(
                        appId = BuildConfig.NUTRITION_APP_ID,
                        appKey = BuildConfig.NUTRITION_APP_KEY,
                        request = request // <-- AND HERE
                    )

                    if (response.isSuccessful && response.body() != null) {
                        _uiState.value = UiState.Success(response.body()!!)
                    } else {
                        _uiState.value = UiState.Error("API Error: ${response.message()}")
                    }
                } else {
                    _uiState.value = UiState.Error("Could not identify food in the image.")
                }
            } catch (e: Exception) {
                // Handle exceptions from ML Kit or Networking
                _uiState.value = UiState.Error("An error occurred: ${e.message}")
            }
        }
    }

    private suspend fun getFoodLabelFromImage(bitmap: Bitmap): String? {
        val image = InputImage.fromBitmap(bitmap, 0)
        val options = ImageLabelerOptions.Builder()
            .setConfidenceThreshold(0.7f) // Only accept labels with high confidence
            .build()
        val labeler = ImageLabeling.getClient(options)

        // .await() is from the kotlinx-coroutines-play-services library, making it a suspend function
        val labels = labeler.process(image).await()

        // Return the first label with the highest confidence
        return labels.firstOrNull()?.text
    }
}