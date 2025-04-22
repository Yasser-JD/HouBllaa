package com.jdcoding.houbllaa

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.jdcoding.houbllaa.auth.LoginActivity
import com.jdcoding.houbllaa.auth.OnboardingActivity
import com.jdcoding.houbllaa.auth.ProfileSetupActivity

/**
 * SplashActivity is the entry point of the application.
 * It displays the app logo and then navigates to the appropriate screen
 * based on authentication status.
 */
class SplashActivity : AppCompatActivity() {

    private val SPLASH_DELAY = 2000L // 2 seconds
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()
        
        // Configure splash background animation
        val splashBgAnimation = findViewById<com.airbnb.lottie.LottieAnimationView>(R.id.splashAnimation)
        splashBgAnimation.apply {
            // Ensure animation fills the screen properly
            //scaleType = com.airbnb.lottie.LottieAnimationView.ScaleType.CENTER_CROP
            
            // Set animation speed
            speed = 0.8f
        }

        // Delayed navigation
        Handler(Looper.getMainLooper()).postDelayed({
            navigateToNextScreen()
        }, SPLASH_DELAY)
    }

    private fun navigateToNextScreen() {
        // Check if user is signed in (non-null)
        val currentUser = auth.currentUser
        
        if (currentUser != null) {
            // User is already signed in - check if profile is complete
            checkProfileCompletion(currentUser.uid)
        } else {
            // User not signed in - check if they've seen onboarding
            val sharedPreferences = getSharedPreferences("HouBlaaPrefs", MODE_PRIVATE)
            val hasSeenOnboarding = sharedPreferences.getBoolean("hasSeenOnboarding", false)
            
            if (!hasSeenOnboarding) {
                // First time user - show onboarding flow
                startActivity(Intent(this, OnboardingActivity::class.java))
                finish()
            } else {
                // Returning user but not signed in - show login
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }
    }
    
    /**
     * Checks if the user has completed their profile setup by querying Firestore
     */
    private fun checkProfileCompletion(userId: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists() && document.contains("profileComplete")) {
                    val profileComplete = document.getBoolean("profileComplete") ?: false
                    
                    if (profileComplete) {
                        // Profile is complete - go to dashboard
                        startActivity(Intent(this, MainActivity::class.java))
                    } else {
                        // Profile is incomplete - go to profile setup
                        startActivity(Intent(this, ProfileSetupActivity::class.java))
                    }
                } else {
                    // No profile data found - go to profile setup
                    startActivity(Intent(this, ProfileSetupActivity::class.java))
                }
                
                // Close splash activity
                finish()
            }
            .addOnFailureListener { exception ->
                // On error, just proceed to main activity
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
    }
}
