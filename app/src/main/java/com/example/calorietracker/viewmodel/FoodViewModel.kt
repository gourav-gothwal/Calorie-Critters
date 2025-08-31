package com.example.calorietracker.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.calorietracker.R
import com.example.calorietracker.data.models.FoodAnalysisModels
import com.example.calorietracker.data.remote.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

// A sealed class to represent all possible UI states
sealed class UiState {
    object Idle : UiState()
    object Loading : UiState()
    data class Success(val data: FoodAnalysisModels) : UiState()
    data class Error(val message: String) : UiState()
}

class FoodViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState

    fun analyzeFoodImage(imageUri: Uri) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading // Set loading state
            try {
                // Get the file bytes safely
                val context = getApplication<Application>().applicationContext
                val fileBytes = context.contentResolver.openInputStream(imageUri)?.use {
                    it.readBytes() // .use will automatically close the stream
                }

                if (fileBytes == null) {
                    _uiState.value = UiState.Error("Could not read image file.")
                    return@launch
                }

                val requestBody = fileBytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
                val imagePart = MultipartBody.Part.createFormData("file", "food_image.jpg", requestBody)

                // FIX: Get the API key from the application context
                val apiKey = getApplication<Application>().getString(R.string.api_key)

                val response = RetrofitClient.apiService.analyzeImage(apiKey, imagePart)

                if (response.isSuccessful && response.body() != null) {
                    _uiState.value = UiState.Success(response.body()!!) // Set success state
                } else {
                    _uiState.value = UiState.Error("API Error: ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Network Failure: ${e.message}")
            }
        }
    }
}