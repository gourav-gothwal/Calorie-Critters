package com.example.nutrisnapapp.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutrisnapapp.data.models.FoodAnalysisModels
import com.example.nutrisnapapp.data.remote.RecipeRetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream

// --- Sealed class representing UI states ---
sealed class UiState {
    object Idle : UiState()
    object Loading : UiState()
    data class Success(val response: FoodAnalysisModels) : UiState()
    data class Error(val message: String) : UiState()
}

// --- ViewModel ---
class ScanViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState

    // Main function to analyze a food image
    fun analyzeFoodImage(bitmap: Bitmap, apiKey: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val imagePart = bitmapToMultipart(bitmap)

                // Call Spoonacular API
                val response = RecipeRetrofitClient.api.analyzeImage(
                    apiKey = apiKey,
                    part = imagePart
                )

                if (response.isSuccessful && response.body() != null) {
                    _uiState.value = UiState.Success(response.body()!!)
                } else {
                    _uiState.value =
                        UiState.Error("Spoonacular API Error: ${response.code()} - ${response.message()}")
                }

            } catch (e: Exception) {
                _uiState.value = UiState.Error("Error: ${e.message}")
            }
        }
    }

    // Helper function to convert Bitmap to MultipartBody.Part
    private fun bitmapToMultipart(bitmap: Bitmap, name: String = "image"): MultipartBody.Part {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
        val byteArray = stream.toByteArray()
        val requestBody = byteArray.toRequestBody("image/jpeg".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData("file", "$name.jpg", requestBody)
    }
}
