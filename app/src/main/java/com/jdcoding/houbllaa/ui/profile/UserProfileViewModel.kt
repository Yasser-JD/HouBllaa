package com.jdcoding.houbllaa.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.jdcoding.houbllaa.models.User
import com.jdcoding.houbllaa.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.util.Date

/**
 * ViewModel for the UserProfileFragment, handles loading and updating user data from Firestore
 */
class UserProfileViewModel(
    private val userRepository: UserRepository
) : ViewModel() {
    
    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> = _user
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage
    
    init {
        loadUserProfile()
    }
    
    /**
     * Load the current user's profile from repository
     */
    private fun loadUserProfile() {
        _isLoading.value = true
        _errorMessage.value = null
        
        val userId = userRepository.getCurrentUserId()
        if (userId != null) {
            viewModelScope.launch {
                try {
                    userRepository.getUserProfile(userId).collect { user ->
                        _user.value = user
                        _isLoading.value = false
                    }
                } catch (e: Exception) {
                    _errorMessage.value = "Failed to load profile: ${e.message}"
                    _isLoading.value = false
                }
            }
        } else {
            _errorMessage.value = "Not logged in"
            _isLoading.value = false
        }
    }
    
    /**
     * Update the user profile with new information
     */
    fun updateUserProfile(name: String, email: String, estimatedDueDate: Date?) {
        _isLoading.value = true
        _errorMessage.value = null
        
        val userId = userRepository.getCurrentUserId()
        if (userId != null) {
            viewModelScope.launch {
                try {
                    val currentUser = _user.value
                    if (currentUser != null) {
                        // Create updated user with the same properties but updated values
                        val updatedUser = User(
                            userId = currentUser.userId,
                            name = name,
                            email = email,
                            birthday = currentUser.birthday,
                            lastMenstrualPeriod = currentUser.lastMenstrualPeriod,
                            averageCycleLength = currentUser.averageCycleLength,
                            conceptionDate = currentUser.conceptionDate,
                            ultrasoundDate = currentUser.ultrasoundDate,
                            estimatedDueDate = estimatedDueDate,
                            preferredLanguage = currentUser.preferredLanguage,
                            darkModeEnabled = currentUser.darkModeEnabled,
                            createdAt = currentUser.createdAt,
                            updatedAt = Date()
                        )
                        
                        // Use the repository's updateUserProfile method which returns a Result
                        val result = userRepository.updateUserProfile(updatedUser)
                        if (result.isSuccess) {
                            _user.value = updatedUser
                            _isLoading.value = false
                        } else {
                            _errorMessage.value = "Failed to update profile: ${result.exceptionOrNull()?.message}"
                            _isLoading.value = false
                        }
                    } else {
                        _errorMessage.value = "User profile not loaded"
                        _isLoading.value = false
                    }
                } catch (e: Exception) {
                    _errorMessage.value = "Failed to update profile: ${e.message}"
                    _isLoading.value = false
                }
            }
        } else {
            _errorMessage.value = "Not logged in"
            _isLoading.value = false
        }
    }
    
    /**
     * Logout the current user
     */
    fun logout() {
        viewModelScope.launch {
            try {
                // Sign out the user using repository method
                userRepository.signOut() 
                _user.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Failed to logout: ${e.message}"
            }
        }
    }
    
    /**
     * Factory for creating UserProfileViewModel with the repository
     */
    class Factory(private val userRepository: UserRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(UserProfileViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return UserProfileViewModel(userRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
