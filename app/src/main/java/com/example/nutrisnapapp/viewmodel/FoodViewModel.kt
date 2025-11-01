package com.example.nutrisnapapp.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutrisnapapp.R
import com.example.nutrisnapapp.data.models.FoodAnalysisModels
import com.example.nutrisnapapp.data.remote.RecipeRetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class FoodViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState

    fun analyzeFoodImage(imageUri: Uri) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val context = getApplication<Application>().applicationContext
                val fileBytes = context.contentResolver.openInputStream(imageUri)?.use {
                    it.readBytes()
                }

                if (fileBytes == null) {
                    _uiState.value = UiState.Error("Could not read image file.")
                    return@launch
                }

                val requestBody = fileBytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
                val imagePart = MultipartBody.Part.createFormData("file", "food_image.jpg", requestBody)

                val apiKey = getApplication<Application>().getString(R.string.api_key)
                val response = RecipeRetrofitClient.api.analyzeImage(apiKey, imagePart)

                if (response.isSuccessful && response.body() != null) {
                    _uiState.value = UiState.Success(response.body()!!)
                } else {
                    _uiState.value = UiState.Error("API Error: ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Network Failure: ${e.message}")
            }
        }
    }
}
