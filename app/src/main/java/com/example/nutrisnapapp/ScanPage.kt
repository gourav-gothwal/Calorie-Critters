package com.example.nutrisnapapp

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.example.nutrisnapapp.data.remote.RecipeRetrofitClient
import com.example.nutrisnapapp.databinding.FragmentScanPageBinding
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class ScanPage : Fragment() {

    private var _binding: FragmentScanPageBinding? = null
    private val binding get() = _binding!!

    private val PICK_IMAGE = 100
    private var imageUri: Uri? = null
    private val apiKey = "8e39ae5c14d94cddb27382e5f012f959"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScanPageBinding.inflate(inflater, container, false)

        binding.buttonSelectImage.setOnClickListener {
            selectImageFromGallery()
        }

        return binding.root
    }

    private fun selectImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            imageUri = data.data
            binding.imageViewPreview.setImageURI(imageUri)

            if (imageUri != null) {
                analyzeFoodImage(imageUri!!)
            }
        }
    }

    private fun analyzeFoodImage(uri: Uri) {
        binding.textViewCalorieInfo.text = "Analyzing image..."

        lifecycleScope.launch {
            try {
                val file = uriToFile(requireContext(), uri)
                val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

                val response = RecipeRetrofitClient.api.analyzeImage(apiKey, body)
                if (response.isSuccessful && response.body() != null) {
                    val analysis = response.body()!!

                    // Food name and calories from your model
                    val foodName = analysis.category.name
                    val calories = analysis.nutrition.calories

                    binding.textViewCalorieInfo.text =
                        "Food: $foodName\nCalories: ${calories.value} ${calories.unit} (per 100g)"

                } else {
                    binding.textViewCalorieInfo.text = "Analysis failed: ${response.message()}"
                }

            } catch (e: Exception) {
                binding.textViewCalorieInfo.text = "Error: ${e.message}"
            }
        }
    }


    // Helper function to convert URI to File
    private fun uriToFile(context: Context, uri: Uri): File {
        val returnCursor = context.contentResolver.query(uri, null, null, null, null)
        val nameIndex = returnCursor?.getColumnIndex(OpenableColumns.DISPLAY_NAME) ?: 0
        returnCursor?.moveToFirst()
        val fileName = returnCursor?.getString(nameIndex) ?: "temp_image"
        returnCursor?.close()

        val file = File(context.cacheDir, fileName)
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        val outputStream = FileOutputStream(file)
        inputStream?.copyTo(outputStream)
        inputStream?.close()
        outputStream.close()
        return file
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
