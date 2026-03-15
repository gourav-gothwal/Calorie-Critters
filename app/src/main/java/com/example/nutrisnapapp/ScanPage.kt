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
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import java.util.Locale
import com.google.mlkit.vision.label.ImageLabel

class ScanPage : Fragment() {

    private var _binding: FragmentScanPageBinding? = null
    private val binding get() = _binding!!

    private val PICK_IMAGE = 100
    private val TAKE_PHOTO = 101
    private val CAMERA_PERMISSION_CODE = 102
    
    private var imageUri: Uri? = null
    private var currentPhotoPath: String? = null
    private val apiKey = BuildConfig.SPOONACULAR_API_KEY

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

                // --- ML-FIRST LOGIC ---
                // 1. Get local identification from ML Kit
                val labels = withContext(Dispatchers.Default) {
                    val inputImage = InputImage.fromFilePath(requireContext(), uri)
                    val labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)
                    
                    kotlin.coroutines.suspendCoroutine { continuation ->
                        labeler.process(inputImage)
                            .addOnSuccessListener { labels ->
                                continuation.resumeWith(Result.success(labels))
                            }
                            .addOnFailureListener { e ->
                                Log.e(TAG, "ML Kit Labeling failed", e)
                                continuation.resumeWith(Result.success(emptyList()))
                            }
                    }
                }

                // Log local labels for debugging
                labels.forEach { label ->
                    Log.d(TAG, "Local Label: ${label.text}, Confidence: ${label.confidence}")
                }

                // Filter out generic labels. We want SPECIFIC names.
                val genericLabels = listOf("food", "fruit", "vegetable", "produce", "dish", "meal", "snack", "cuisine", "ingredient", "tableware", "indoor", "outdoor", "plant", "yellow", "natural foods")
                val filteredLabels = labels.filter { it.text.lowercase() !in genericLabels }
                val topLocalLabel = filteredLabels.maxByOrNull { it.confidence }
                
                // Identify broad categories for validation
                val localSeesBanana = labels.any { it.text.lowercase().containsAny("banana", "plantain", "bananas") }
                val localSeesFruit = labels.any { it.text.lowercase().containsAny("fruit", "apple", "berry", "orange", "pear", "peach") }
                val localSeesDessert = labels.any { it.text.lowercase().containsAny("ice cream", "cake", "sweet", "dessert", "cookie") }

                Log.d(TAG, "Logic Scan - Banana: $localSeesBanana, Fruit: $localSeesFruit, Dessert: $localSeesDessert")

                // STRATEGY A: Direct specific hit from ML Kit (e.g., "Banana")
                // We ONLY do a direct hit if it's a SPECIFIC food (not "Fruit" or "Vegetable")
                if (topLocalLabel != null && topLocalLabel.confidence > 0.45) {
                    val candidateName = topLocalLabel.text.lowercase()
                    Log.d(TAG, "ML Kit confident in: $candidateName")
                    
                    val nutritionResponse = withContext(Dispatchers.IO) {
                        RecipeRetrofitClient.api.getNutritionByFoodName(apiKey, candidateName)
                    }
                    
                    if (nutritionResponse.isSuccessful && nutritionResponse.body() != null) {
                        showLoading(false)
                        updateUIWithResult(candidateName, nutritionResponse.body()!!)
                        showResultCard(true)
                        return@launch
                    }
                }

                // 2. FALLBACK: Use heavy Cloud Image Analysis
                Log.d(TAG, "Using fallback Cloud Image Analysis...")
                val response = withContext(Dispatchers.IO) {
                    RecipeRetrofitClient.api.analyzeImage(apiKey, body)
                }
                
                showLoading(false)
                
                if (response.isSuccessful && response.body() != null) {
                    val analysis = response.body()!!
                    val cloudName = analysis.category.name.lowercase()
                    Log.d(TAG, "Cloud API suggested: $cloudName")
                    
                    // CROSS-VERIFICATION: Handle Cloud "Hallucinations"
                    val cleanCloudName = cloudName.replace("_", " ").replace("-", " ") 
                    val cloudIsDessert = cleanCloudName.containsAny("ice cream", "dessert", "sundae", "parfait", "frozen", "cream")
                    
                    // Conflict Resolution: If Cloud says 'Dessert' but Local sees 'Banana' or 'Fruit'
                    if (cloudIsDessert && (localSeesBanana || localSeesFruit) && !localSeesDessert) {
                        val forceName = if (localSeesBanana) "banana" else "fruit"
                        Log.w(TAG, "VETO: Cloud said '$cleanCloudName' but phone sees $forceName. Forcing recovery.")
                        
                        val correctionResponse = withContext(Dispatchers.IO) {
                            RecipeRetrofitClient.api.getNutritionByFoodName(apiKey, forceName)
                        }
                        if (correctionResponse.isSuccessful && correctionResponse.body() != null) {
                            updateUIWithResult(forceName, correctionResponse.body()!!)
                            showResultCard(true)
                            return@launch
                        }
                    }
                    
                    // Clean and display Cloud Result
                    val displayName = cleanCloudName.split(" ").joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
                    updateUIWithResult(displayName, analysis.nutrition)
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

    private fun updateUIWithResult(foodName: String, nutrition: com.example.nutrisnapapp.data.models.Nutrition) {
        // Update UI with results
        binding.foodNameText.text = foodName.replaceFirstChar { 
            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() 
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
    }

    private fun uriToFile(context: Context, uri: Uri): File {
        // First, get the filename
        val returnCursor = context.contentResolver.query(uri, null, null, null, null)
        val nameIndex = returnCursor?.getColumnIndex(OpenableColumns.DISPLAY_NAME) ?: 0
        returnCursor?.moveToFirst()
        val fileName = returnCursor?.getString(nameIndex) ?: "temp_image.jpg"
        returnCursor?.close()

        val file = File(context.cacheDir, "processed_$fileName")
        
        // Load original bitmap
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeStream(inputStream, null, options)
        inputStream?.close()

        // Calculate sample size to avoid OOM
        val reqWidth = 1024
        val reqHeight = 1024
        var inSampleSize = 1
        if (options.outHeight > reqHeight || options.outWidth > reqWidth) {
            val halfHeight: Int = options.outHeight / 2
            val halfWidth: Int = options.outWidth / 2
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }

        // Decode with inSampleSize
        val decodeOptions = BitmapFactory.Options().apply {
            this.inSampleSize = inSampleSize
        }
        val finalInputStream = context.contentResolver.openInputStream(uri)
        var bitmap = BitmapFactory.decodeStream(finalInputStream, null, decodeOptions)
        finalInputStream?.close()

        // Handle rotation from EXIF
        bitmap?.let {
            val exifInputStream = context.contentResolver.openInputStream(uri)
            val exif = exifInputStream?.let { it1 -> ExifInterface(it1) }
            val orientation = exif?.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            exifInputStream?.close()

            val matrix = Matrix()
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            }
            
            if (orientation != ExifInterface.ORIENTATION_NORMAL && orientation != 0) {
                bitmap = Bitmap.createBitmap(it, 0, 0, it.width, it.height, matrix, true)
            }
        }

        // Compress and save
        val outputStream = FileOutputStream(file)
        bitmap?.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
        outputStream.close()
        
        Log.d(TAG, "Processed Image: $fileName, Final size: ${file.length()} bytes")
        
        return file
    }

    private fun String.containsAny(vararg keywords: String): Boolean {
        return keywords.any { this.contains(it, ignoreCase = true) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
