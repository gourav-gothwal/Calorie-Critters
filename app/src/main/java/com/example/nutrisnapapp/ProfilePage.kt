package com.example.nutrisnapapp

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.example.nutrisnapapp.databinding.FragmentProfilePageBinding
import com.google.firebase.auth.FirebaseAuth

class ProfilePage : Fragment() {

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

        setupUserInfo()
        setupClickListeners()
        animateEntrance()
    }

    private fun setupUserInfo() {
        // Get current user from Firebase
        val currentUser = FirebaseAuth.getInstance().currentUser
        
        currentUser?.let { user ->
            binding.userName.text = user.displayName ?: "User"
            binding.userEmail.text = user.email ?: "No email"
            
            // Load profile photo if available
            user.photoUrl?.let { photoUrl ->
                // You can use Glide or Coil to load the image
                // Glide.with(this).load(photoUrl).into(binding.profileAvatar)
            }
        }
    }

    private fun setupClickListeners() {
        // Edit Profile
        binding.menuEditProfile.setOnClickListener {
            // Navigate to edit profile screen
            Toast.makeText(context, "Edit Profile", Toast.LENGTH_SHORT).show()
        }

        // Edit Avatar
        binding.editAvatarButton.setOnClickListener {
            // Open image picker for profile photo
            Toast.makeText(context, "Change Profile Photo", Toast.LENGTH_SHORT).show()
        }

        // Preferences
        binding.menuPreferences.setOnClickListener {
            Toast.makeText(context, "Preferences", Toast.LENGTH_SHORT).show()
        }

        // Dark Mode Toggle
        binding.darkModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }

        // About
        binding.menuAbout.setOnClickListener {
            Toast.makeText(context, "Calorie Critters v1.0.0", Toast.LENGTH_SHORT).show()
        }

        // Terms
        binding.menuTerms.setOnClickListener {
            Toast.makeText(context, "Terms of Service", Toast.LENGTH_SHORT).show()
        }

        // Privacy
        binding.menuPrivacy.setOnClickListener {
            Toast.makeText(context, "Privacy Policy", Toast.LENGTH_SHORT).show()
        }

        // Logout
        binding.menuLogout.setOnClickListener {
            performLogout()
        }
    }

    private fun performLogout() {
        FirebaseAuth.getInstance().signOut()
        
        // Navigate to login screen
        val intent = Intent(requireContext(), LoginScreen::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        activity?.finish()
    }

    private fun animateEntrance() {
        // Subtle fade-in animation for minimalist feel
        binding.root.alpha = 0f
        binding.root.animate()
            .alpha(1f)
            .setDuration(300)
            .start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
