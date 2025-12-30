package com.example.nutrisnapapp

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.nutrisnapapp.data.remote.RecipeRetrofitClient
import com.example.nutrisnapapp.databinding.FragmentScanPageBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ScanPage : Fragment() {

    private var _binding: FragmentScanPageBinding? = null
    private val binding get() = _binding!!

    private val PICK_IMAGE = 100
    private val TAKE_PHOTO = 101
    private val CAMERA_PERMISSION_CODE = 102
    
    private var imageUri: Uri? = null
    private var currentPhotoPath: String? = null
    private val apiKey = "8e39ae5c14d94cddb27382e5f012f959"

    companion object {
        private const val TAG = "ScanPage"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScanPageBinding.inflate(inflater, container, false)

        setupClickListeners()
        animateEntrance()

        return binding.root
    }

    private fun setupClickListeners() {
        binding.buttonSelectImage.setOnClickListener {
            selectImageFromGallery()
        }

        binding.buttonTakePhoto.setOnClickListener {
            checkCameraPermissionAndCapture()
        }

        // Allow clicking on image container to select image too
        binding.imageContainer.setOnClickListener {
            selectImageFromGallery()
        }
    }

    private fun animateEntrance() {
        // Fade in animation for cards
        binding.mainCard.alpha = 0f
        binding.mainCard.translationY = 50f
        binding.mainCard.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(500)
            .setStartDelay(200)
            .start()

        binding.tipsCard.alpha = 0f
        binding.tipsCard.translationY = 50f
        binding.tipsCard.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(500)
            .setStartDelay(400)
            .start()
    }

    private fun selectImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE)
    }

    private fun checkCameraPermissionAndCapture() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
        } else {
            dispatchTakePictureIntent()
        }
    }

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(requireActivity().packageManager) != null) {
            val photoFile: File? = createImageFile()
            photoFile?.let {
                val photoURI: Uri = FileProvider.getUriForFile(
                    requireContext(),
                    "${requireContext().packageName}.fileprovider",
                    it
                )
                imageUri = photoURI
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(takePictureIntent, TAKE_PHOTO)
            }
        }
    }

    private fun createImageFile(): File? {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply {
            currentPhotoPath = absolutePath
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                PICK_IMAGE -> {
                    if (data != null) {
                        imageUri = data.data
                        showImagePreview()
                        imageUri?.let { analyzeFoodImage(it) }
                    }
                }
                TAKE_PHOTO -> {
                    showImagePreview()
                    imageUri?.let { analyzeFoodImage(it) }
                }
            }
        }
    }

    private fun showImagePreview() {
        // Hide placeholder, show image
        binding.placeholderContent.visibility = View.GONE
        binding.imageViewPreview.visibility = View.VISIBLE
        binding.imageViewPreview.setImageURI(imageUri)

        // Animate the image appearing
        binding.imageViewPreview.alpha = 0f
        binding.imageViewPreview.scaleX = 0.9f
        binding.imageViewPreview.scaleY = 0.9f
        binding.imageViewPreview.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(300)
            .start()

        // Hide tips card when analyzing
        binding.tipsCard.animate()
            .alpha(0f)
            .translationY(-20f)
            .setDuration(200)
            .withEndAction {
                binding.tipsCard.visibility = View.GONE
            }
            .start()
    }

    private fun showLoading(show: Boolean) {
        if (show) {
            binding.loadingContainer.visibility = View.VISIBLE
            binding.loadingContainer.alpha = 0f
            binding.loadingContainer.animate()
                .alpha(1f)
                .setDuration(200)
                .start()
        } else {
            binding.loadingContainer.animate()
                .alpha(0f)
                .setDuration(200)
                .withEndAction {
                    binding.loadingContainer.visibility = View.GONE
                }
                .start()
        }
    }

    private fun showResultCard(show: Boolean) {
        if (show) {
            binding.resultCard.visibility = View.VISIBLE
            binding.resultCard.alpha = 0f
            binding.resultCard.translationY = 30f
            binding.resultCard.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(400)
                .start()
        } else {
            binding.resultCard.visibility = View.GONE
        }
    }

    private fun analyzeFoodImage(uri: Uri) {
        showLoading(true)
        showResultCard(false)

        lifecycleScope.launch {
            try {
                val file = withContext(Dispatchers.IO) {
                    uriToFile(requireContext(), uri)
                }
                
                Log.d(TAG, "File created: ${file.absolutePath}, size: ${file.length()}")
                
                val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

                Log.d(TAG, "Sending request to API...")
                
                val response = withContext(Dispatchers.IO) {
                    RecipeRetrofitClient.api.analyzeImage(apiKey, body)
                }
                
                showLoading(false)
                
                Log.d(TAG, "Response code: ${response.code()}")
                Log.d(TAG, "Response message: ${response.message()}")
                
                if (response.isSuccessful && response.body() != null) {
                    val analysis = response.body()!!
                    
                    Log.d(TAG, "Analysis successful: ${analysis.category.name}")

                    // Food name and nutrition from API response
                    val foodName = analysis.category.name
                    val nutrition = analysis.nutrition

                    // Update UI with results
                    binding.foodNameText.text = foodName.replaceFirstChar { 
                        it.titlecase(Locale.getDefault()) 
                    }
                    binding.caloriesValue.text = nutrition.calories.value.toInt().toString()
                    binding.proteinValue.text = "${nutrition.protein.value.toInt()}g"
                    binding.carbsValue.text = "${nutrition.carbs.value.toInt()}g"
                    binding.fatValue.text = "${nutrition.fat.value.toInt()}g"

                    // Make sure all nutrition elements are visible
                    binding.foodHeader.visibility = View.VISIBLE
                    binding.nutritionHeader.visibility = View.VISIBLE
                    binding.nutritionGrid.visibility = View.VISIBLE
                    binding.textViewCalorieInfo.visibility = View.GONE

                    showResultCard(true)

                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, "API Error: ${response.code()} - ${response.message()}")
                    Log.e(TAG, "Error body: $errorBody")
                    
                    val errorMessage = when (response.code()) {
                        401 -> "Invalid API key. Please check your API key."
                        402 -> "API quota exceeded. Please try again later."
                        404 -> "API endpoint not found."
                        500 -> "Server error. Please try again later."
                        else -> "Analysis failed (${response.code()}): ${response.message()}"
                    }
                    showError(errorMessage)
                }

            } catch (e: Exception) {
                Log.e(TAG, "Exception during analysis", e)
                showLoading(false)
                showError("Error: ${e.localizedMessage ?: "Unknown error occurred"}")
            }
        }
    }

    private fun showError(message: String) {
        binding.resultCard.visibility = View.VISIBLE
        binding.textViewCalorieInfo.visibility = View.VISIBLE
        binding.textViewCalorieInfo.text = message
        binding.nutritionGrid.visibility = View.GONE
        binding.nutritionHeader.visibility = View.GONE
        binding.foodHeader.visibility = View.GONE

        // Animate result card
        binding.resultCard.alpha = 0f
        binding.resultCard.translationY = 30f
        binding.resultCard.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(400)
            .start()

        // Show tips card again
        binding.tipsCard.visibility = View.VISIBLE
        binding.tipsCard.alpha = 0f
        binding.tipsCard.translationY = 0f
        binding.tipsCard.animate()
            .alpha(1f)
            .setDuration(300)
            .start()
    }

    private fun uriToFile(context: Context, uri: Uri): File {
        val returnCursor = context.contentResolver.query(uri, null, null, null, null)
        val nameIndex = returnCursor?.getColumnIndex(OpenableColumns.DISPLAY_NAME) ?: 0
        returnCursor?.moveToFirst()
        val fileName = returnCursor?.getString(nameIndex) ?: "temp_image.jpg"
        returnCursor?.close()

        val file = File(context.cacheDir, fileName)
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        val outputStream = FileOutputStream(file)
        inputStream?.copyTo(outputStream)
        inputStream?.close()
        outputStream.close()
        
        Log.d(TAG, "URI to File: $fileName, size: ${file.length()} bytes")
        
        return file
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
