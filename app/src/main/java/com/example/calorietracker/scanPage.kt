import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class ScanPage : Fragment() {

    private lateinit var imageViewPreview: ImageView
    private lateinit var buttonSelectImage: Button
    private lateinit var textViewCalorieInfo: TextView
    private lateinit var progressBar: ProgressBar // Add a ProgressBar for loading state

    private val viewModel: ScanViewModel by viewModels()

    // Modern way to handle activity results
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val bitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, it)
            imageViewPreview.setImageBitmap(bitmap)
            viewModel.processImage(bitmap)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_scan_page, container, false)

        imageViewPreview = view.findViewById(R.id.imageViewPreview)
        buttonSelectImage = view.findViewById(R.id.buttonSelectImage)
        textViewCalorieInfo = view.findViewById(R.id.textViewCalorieInfo)
        progressBar = view.findViewById(R.id.progressBar) // Initialize your ProgressBar

        buttonSelectImage.setOnClickListener {
            pickImageLauncher.launch("image/*") // Launch the modern image picker
        }

        observeViewModel()

        return view
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is UiState.Idle -> {
                        progressBar.visibility = View.GONE
                        textViewCalorieInfo.text = "Select an image to analyze."
                    }
                    is UiState.Loading -> {
                        progressBar.visibility = View.VISIBLE
                        textViewCalorieInfo.text = "Analyzing..."
                    }
                    is UiState.Success -> {
                        progressBar.visibility = View.GONE
                        val calorieInfo = state.calorieResponse
                        if (calorieInfo.foods.isNotEmpty()) {
                            val food = calorieInfo.foods[0]
                            textViewCalorieInfo.text = "Food: ${food.foodName}\nCalories: ${food.calories}"
                        } else {
                            textViewCalorieInfo.text = "Calorie information not found."
                        }
                    }
                    is UiState.Error -> {
                        progressBar.visibility = View.GONE
                        textViewCalorieInfo.text = state.message
                    }
                }
            }
        }
    }
}