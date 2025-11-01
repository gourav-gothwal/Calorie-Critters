package com.example.nutrisnapapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.nutrisnapapp.databinding.FragmentRecipeDetailBinding

class RecipeDetailFragment : Fragment() {

    private var _binding: FragmentRecipeDetailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecipeDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ⚡ Placeholder logic
        binding.textTitle.text = "Recipe Title"
        binding.textSummary.text = "Recipe summary will appear here."
        binding.textInstructions.text = "Instructions will appear here."
        // For ImageView, you can use a placeholder drawable
        binding.imageRecipe.setImageResource(android.R.drawable.ic_menu_gallery)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(): RecipeDetailFragment {
            return RecipeDetailFragment()
        }
    }
}
