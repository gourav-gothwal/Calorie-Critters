package com.example.nutrisnapapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.nutrisnapapp.databinding.FragmentProfilePageBinding

class ProfilePage : Fragment() {

    // ViewBinding
    private var _binding: FragmentProfilePageBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfilePageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // TODO: Add your functionality here
        // Example: binding.textView.text = "User Profile"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
