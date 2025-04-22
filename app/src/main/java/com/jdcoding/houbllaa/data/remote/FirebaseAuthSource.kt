package com.jdcoding.houbllaa.data.remote

import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
import java.lang.Exception

/**
 * Firebase Authentication data source
 */
class FirebaseAuthSource {
    private val firebaseAuth = FirebaseAuth.getInstance()

    // Current user
    val currentUser: FirebaseUser? 
        get() = firebaseAuth.currentUser

    // Check if user is authenticated
    fun isUserAuthenticated() = firebaseAuth.currentUser != null

    // Sign in with email and password
    suspend fun signInWithEmailPassword(email: String, password: String): Result<FirebaseUser> {
        return try {
            val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            authResult.user?.let {
                Result.success(it)
            } ?: Result.failure(Exception("Authentication failed"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Register with email and password
    suspend fun registerWithEmailPassword(email: String, password: String): Result<FirebaseUser> {
        return try {
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            authResult.user?.let {
                Result.success(it)
            } ?: Result.failure(Exception("Registration failed"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Sign in with Google
    suspend fun signInWithGoogle(googleSignInAccount: GoogleSignInAccount): Result<FirebaseUser> {
        return try {
            val credential = GoogleAuthProvider.getCredential(googleSignInAccount.idToken, null)
            val authResult = firebaseAuth.signInWithCredential(credential).await()
            authResult.user?.let {
                Result.success(it)
            } ?: Result.failure(Exception("Google authentication failed"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Sign out
    fun signOut() {
        firebaseAuth.signOut()
    }

    // Reset password
    suspend fun resetPassword(email: String): Result<Unit> {
        return try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
