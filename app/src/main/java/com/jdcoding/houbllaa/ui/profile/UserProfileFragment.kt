package com.jdcoding.houbllaa.ui.profile

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputEditText
import com.jdcoding.houbllaa.R
import com.jdcoding.houbllaa.di.RepositoryProvider
import com.jdcoding.houbllaa.models.User
import com.jdcoding.houbllaa.utils.AppBarUtils
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.*

/**
 * Fragment for displaying and editing user profile information
 * Data is fetched from Firestore
 */
class UserProfileFragment : Fragment() {

    private lateinit var nameEditText: TextInputEditText
    private lateinit var emailEditText: TextInputEditText
    private lateinit var dueDateEditText: TextInputEditText
    private lateinit var profileImageView: CircleImageView
    private lateinit var changePhotoButton: Button
    private lateinit var saveButton: Button
    private lateinit var logoutButton: Button
    
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    private var selectedDueDate: Date? = null
    
    private val viewModel by viewModels<UserProfileViewModel> {
        UserProfileViewModel.Factory(
            RepositoryProvider.provideUserRepository(requireContext())
        )
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_user_profile, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize views
        initViews(view)
        
        // Setup app bar
        setupAppBar()
        
        // Setup date picker
        setupDatePicker()
        
        // Setup buttons
        setupButtons()
        
        // Observe user data
        observeUserData()
    }
    
    private fun initViews(view: View) {
        nameEditText = view.findViewById(R.id.etName)
        emailEditText = view.findViewById(R.id.etEmail)
        dueDateEditText = view.findViewById(R.id.etDueDate)
        profileImageView = view.findViewById(R.id.ivUserPhoto)
        changePhotoButton = view.findViewById(R.id.btnChangePhoto)
        saveButton = view.findViewById(R.id.btnSaveProfile)
        logoutButton = view.findViewById(R.id.btnLogout)
    }
    
    private fun setupAppBar() {
        AppBarUtils.setupAppBar(
            this,
            "Profile",
            onMenuClickListener = {
                // Go back when menu (back) button is clicked
                findNavController().popBackStack()
            }
        )
    }
    
    private fun setupDatePicker() {
        dueDateEditText.setOnClickListener {
            showDatePicker()
        }
    }
    
    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        if (selectedDueDate != null) {
            calendar.time = selectedDueDate!!
        }
        
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                calendar.set(selectedYear, selectedMonth, selectedDay)
                selectedDueDate = calendar.time
                dueDateEditText.setText(dateFormat.format(selectedDueDate!!))
            },
            year,
            month,
            day
        )
        
        datePickerDialog.show()
    }
    
    private fun setupButtons() {
        saveButton.setOnClickListener {
            saveUserProfile()
        }
        
        logoutButton.setOnClickListener {
            viewModel.logout()
            // Navigate to login/welcome screen 
            // (would be implemented in a real app)
            Toast.makeText(requireContext(), "Logged out", Toast.LENGTH_SHORT).show()
        }
        
        changePhotoButton.setOnClickListener {
            // This would open image picker in a real app
            Toast.makeText(requireContext(), "Photo change not implemented yet", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun observeUserData() {
        viewModel.user.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                // Populate fields with user data
                nameEditText.setText(user.name)
                emailEditText.setText(user.email)
                
                if (user.estimatedDueDate != null) {
                    selectedDueDate = user.estimatedDueDate
                    dueDateEditText.setText(dateFormat.format(user.estimatedDueDate))
                }
                
                // Load profile picture if available
                // In a real app, this would use Glide or similar to load from URL
            }
        }
    }
    
    private fun saveUserProfile() {
        val name = nameEditText.text.toString().trim()
        val email = emailEditText.text.toString().trim()
        
        if (name.isEmpty()) {
            nameEditText.error = "Name cannot be empty"
            return
        }
        
        if (email.isEmpty()) {
            emailEditText.error = "Email cannot be empty"
            return
        }
        
        viewModel.updateUserProfile(name, email, selectedDueDate)
        Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show()
    }
}
