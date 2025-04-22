package com.jdcoding.houbllaa.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.jdcoding.houbllaa.data.repository.UserRepository
import com.jdcoding.houbllaa.models.User
import com.jdcoding.houbllaa.utils.PregnancyCalculator
import kotlinx.coroutines.launch
import java.util.Date

/**
 * ViewModel for managing user profile information and pregnancy data
 */
class ProfileViewModel(private val userRepository: UserRepository) : ViewModel() {
    
    private val _profileState = MutableLiveData<ProfileState>()
    val profileState: LiveData<ProfileState> = _profileState
    
    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> = _user
    
    private val _estimatedDueDate = MutableLiveData<Date?>()
    val estimatedDueDate: LiveData<Date?> = _estimatedDueDate
    
    init {
        loadUserProfile()
    }
    
    /**
     * Load the current user's profile
     */
    private fun loadUserProfile() {
        val userId = userRepository.getCurrentUserId()
        if (userId != null) {
            viewModelScope.launch {
                _profileState.value = ProfileState.LOADING
                
                try {
                    // Using Flow collection instead of direct sync call
                    userRepository.getUserProfile(userId).collect { userProfile ->
                        _user.value = userProfile
                        _estimatedDueDate.value = userProfile?.estimatedDueDate
                        
                        if (userProfile?.lastMenstrualPeriod != null || 
                            userProfile?.conceptionDate != null || 
                            userProfile?.estimatedDueDate != null) {
                            _profileState.value = ProfileState.PROFILE_COMPLETE
                        } else {
                            _profileState.value = ProfileState.PROFILE_INCOMPLETE
                        }
                    }
                } catch (e: Exception) {
                    _profileState.value = ProfileState.ERROR
                }
            }
        } else {
            _profileState.value = ProfileState.UNAUTHENTICATED
        }
    }
    
    /**
     * Update user's basic information
     */
    fun updateBasicInfo(name: String, birthday: Date?) {
        val userId = userRepository.getCurrentUserId() ?: return
        
        val currentUser = _user.value
        val updatedUser = currentUser?.copy(
            name = name,
            birthday = birthday
        ) ?: User(
            userId = userId,
            name = name,
            email = "", // This should be fetched from auth
            birthday = birthday
        )
        
        updateUserProfile(updatedUser)
    }
    
    /**
     * Update pregnancy information based on last menstrual period
     */
    fun updatePregnancyInfoFromLMP(lmpDate: Date, cycleLength: Int) {
        val userId = userRepository.getCurrentUserId() ?: return
        
        // Calculate estimated due date based on LMP
        val dueDate = PregnancyCalculator.calculateDueDateFromLMP(lmpDate)
        
        // Estimate conception date based on cycle length
        val conceptionDate = PregnancyCalculator.estimateConceptionDate(lmpDate, cycleLength)
        
        val currentUser = _user.value
        val updatedUser = currentUser?.copy(
            lastMenstrualPeriod = lmpDate,
            averageCycleLength = cycleLength,
            conceptionDate = conceptionDate,
            estimatedDueDate = dueDate
        ) ?: User(
            userId = userId,
            name = "",
            email = "",
            lastMenstrualPeriod = lmpDate,
            averageCycleLength = cycleLength,
            conceptionDate = conceptionDate,
            estimatedDueDate = dueDate
        )
        
        updateUserProfile(updatedUser)
        _estimatedDueDate.value = dueDate
    }
    
    /**
     * Update pregnancy information based on known conception date
     */
    fun updatePregnancyInfoFromConception(conceptionDate: Date) {
        val userId = userRepository.getCurrentUserId() ?: return
        
        // Calculate estimated due date based on conception
        val dueDate = PregnancyCalculator.calculateDueDateFromConception(conceptionDate)
        
        val currentUser = _user.value
        val updatedUser = currentUser?.copy(
            conceptionDate = conceptionDate,
            estimatedDueDate = dueDate
        ) ?: User(
            userId = userId,
            name = "",
            email = "",
            conceptionDate = conceptionDate,
            estimatedDueDate = dueDate
        )
        
        updateUserProfile(updatedUser)
        _estimatedDueDate.value = dueDate
    }
    
    /**
     * Update pregnancy information based on ultrasound date
     */
    fun updatePregnancyInfoFromUltrasound(ultrasoundDate: Date, estimatedDueDate: Date) {
        val userId = userRepository.getCurrentUserId() ?: return
        
        val currentUser = _user.value
        val updatedUser = currentUser?.copy(
            ultrasoundDate = ultrasoundDate,
            estimatedDueDate = estimatedDueDate
        ) ?: User(
            userId = userId,
            name = "",
            email = "",
            ultrasoundDate = ultrasoundDate,
            estimatedDueDate = estimatedDueDate
        )
        
        updateUserProfile(updatedUser)
        _estimatedDueDate.value = estimatedDueDate
    }
    
    /**
     * Update language preference
     */
    fun updateLanguagePreference(language: String) {
        val userId = userRepository.getCurrentUserId() ?: return
        
        viewModelScope.launch {
            userRepository.updateLanguagePreference(userId, language)
            loadUserProfile() // Reload user data
        }
    }
    
    /**
     * Update dark mode preference
     */
    fun updateDarkModePreference(darkModeEnabled: Boolean) {
        val userId = userRepository.getCurrentUserId() ?: return
        
        viewModelScope.launch {
            userRepository.updateDarkModePreference(userId, darkModeEnabled)
            loadUserProfile() // Reload user data
        }
    }
    
    /**
     * Helper function to update user profile
     */
    private fun updateUserProfile(user: User) {
        viewModelScope.launch {
            _profileState.value = ProfileState.UPDATING
            
            userRepository.updateUserProfile(user).fold(
                onSuccess = {
                    _user.value = it
                    _profileState.value = ProfileState.PROFILE_COMPLETE
                },
                onFailure = {
                    _profileState.value = ProfileState.ERROR
                }
            )
        }
    }
    
    /**
     * Enum representing different profile states
     */
    enum class ProfileState {
        LOADING,
        UPDATING,
        PROFILE_COMPLETE,
        PROFILE_INCOMPLETE,
        UNAUTHENTICATED,
        ERROR
    }
    
    /**
     * Factory for creating ProfileViewModel with the necessary dependencies
     */
    class Factory(private val userRepository: UserRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
                return ProfileViewModel(userRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
