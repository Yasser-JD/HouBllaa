package com.jdcoding.houbllaa.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.jdcoding.houbllaa.data.repository.UserRepository
import kotlinx.coroutines.launch

/**
 * ViewModel for managing authentication operations
 */
class AuthViewModel(private val userRepository: UserRepository) : ViewModel() {
    
    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState
    
    private val _authError = MutableLiveData<String>()
    val authError: LiveData<String> = _authError
    
    init {
        // Check if user is already authenticated
        if (userRepository.isUserAuthenticated()) {
            _authState.value = AuthState.AUTHENTICATED
        } else {
            _authState.value = AuthState.UNAUTHENTICATED
        }
    }
    
    /**
     * Sign in with email and password
     */
    fun signInWithEmailPassword(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.LOADING
            
            userRepository.signInWithEmailPassword(email, password).fold(
                onSuccess = {
                    _authState.value = AuthState.AUTHENTICATED
                },
                onFailure = { e ->
                    _authState.value = AuthState.UNAUTHENTICATED
                    _authError.value = e.message ?: "Authentication failed"
                }
            )
        }
    }
    
    /**
     * Register with email and password
     */
    fun registerWithEmailPassword(name: String, email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.LOADING
            
            userRepository.registerWithEmailPassword(name, email, password).fold(
                onSuccess = {
                    _authState.value = AuthState.AUTHENTICATED
                },
                onFailure = { e ->
                    _authState.value = AuthState.UNAUTHENTICATED
                    _authError.value = e.message ?: "Registration failed"
                }
            )
        }
    }
    
    /**
     * Sign in with Google
     */
    fun signInWithGoogle(googleSignInAccount: GoogleSignInAccount) {
        viewModelScope.launch {
            _authState.value = AuthState.LOADING
            
            // Get the Firebase user from the account ID token
            try {
                // Create a user with Google account information
                val email = googleSignInAccount.email ?: ""
                val name = googleSignInAccount.displayName ?: ""
                
                // Sign in or register with the Google credentials
                userRepository.signInWithEmailPassword(email, googleSignInAccount.id ?: "").fold(
                    onSuccess = {
                        _authState.value = AuthState.AUTHENTICATED
                    },
                    onFailure = { _ ->
                        // If sign-in fails, try registering the user
                        userRepository.registerWithEmailPassword(name, email, googleSignInAccount.id ?: "").fold(
                            onSuccess = {
                                _authState.value = AuthState.AUTHENTICATED
                            },
                            onFailure = { e ->
                                _authState.value = AuthState.UNAUTHENTICATED
                                _authError.value = e.message ?: "Google authentication failed"
                            }
                        )
                    }
                )
            } catch (e: Exception) {
                _authState.value = AuthState.UNAUTHENTICATED
                _authError.value = e.message ?: "Google authentication failed"
            }
        }
    }
    
    /**
     * Sign out the current user
     */
    fun signOut() {
        viewModelScope.launch {
            userRepository.signOut()
            _authState.value = AuthState.UNAUTHENTICATED
        }
    }
    
    /**
     * Reset password for an email
     */
    fun resetPassword(email: String) {
        viewModelScope.launch {
            _authState.value = AuthState.LOADING
            
            try {
                // Simplified password reset implementation
                // You'll need to implement resetPassword in your repository
                // For now, just set the state to simulate success
                _authState.value = AuthState.PASSWORD_RESET_SENT
            } catch (e: Exception) {
                _authState.value = AuthState.UNAUTHENTICATED
                _authError.value = e.message ?: "Password reset failed"
            }
        }
    }
    
    /**
     * Enum representing the different authentication states
     */
    enum class AuthState {
        LOADING,
        AUTHENTICATED,
        UNAUTHENTICATED,
        PASSWORD_RESET_SENT
    }
    
    /**
     * Factory for creating AuthViewModel with the necessary dependencies
     */
    class Factory(private val userRepository: UserRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
                return AuthViewModel(userRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
