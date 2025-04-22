package com.jdcoding.houbllaa.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.jdcoding.houbllaa.MainActivity
import com.jdcoding.houbllaa.R

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var tilEmail: TextInputLayout
    private lateinit var tilPassword: TextInputLayout
    private lateinit var btnLogin: Button
    private lateinit var tvRegister: TextView
    private lateinit var tvForgotPassword: TextView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Initialize views
        etEmail = findViewById(R.id.et_email)
        etPassword = findViewById(R.id.et_password)
        tilEmail = findViewById(R.id.til_email)
        tilPassword = findViewById(R.id.til_password)
        btnLogin = findViewById(R.id.btn_login)
        tvRegister = findViewById(R.id.tv_register)
        tvForgotPassword = findViewById(R.id.tv_forgot_password)
        progressBar = findViewById(R.id.progress_bar)

        // Set up click listeners
        btnLogin.setOnClickListener {
            loginUser()
        }

        tvRegister.setOnClickListener {
            // Navigate to register
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        tvForgotPassword.setOnClickListener {
            showPasswordResetDialog()
        }
    }

    private fun loginUser() {
        // Reset any previous errors
        tilEmail.error = null
        tilPassword.error = null

        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        // Validate input
        if (email.isEmpty()) {
            tilEmail.error = getString(R.string.email_required)
            return
        }

        if (password.isEmpty()) {
            tilPassword.error = getString(R.string.password_required)
            return
        }

        // Show progress bar
        progressBar.visibility = View.VISIBLE
        btnLogin.isEnabled = false

        // Authenticate with Firebase
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Login successful
                    val user = auth.currentUser
                    
                    // Check if user profile is complete
                    checkUserProfileComplete(user?.uid)
                } else {
                    // Login failed
                    val errorMessage = when {
                        task.exception?.message?.contains("password is invalid") == true -> 
                            getString(R.string.invalid_password)
                        task.exception?.message?.contains("no user record") == true -> 
                            getString(R.string.user_not_found)
                        else -> task.exception?.message ?: getString(R.string.login_failed)
                    }
                    
                    Toast.makeText(
                        this,
                        errorMessage,
                        Toast.LENGTH_LONG
                    ).show()
                    
                    // Hide progress bar and re-enable button
                    progressBar.visibility = View.GONE
                    btnLogin.isEnabled = true
                }
            }
    }

    private fun checkUserProfileComplete(userId: String?) {
        if (userId == null) {
            progressBar.visibility = View.GONE
            btnLogin.isEnabled = true
            return
        }
        
        // Check if user has a profile document in Firestore
        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists() && document.contains("profileComplete")) {
                    val profileComplete = document.getBoolean("profileComplete") ?: false
                    
                    if (profileComplete) {
                        // Profile is complete, go to main activity
                        val intent = Intent(this, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    } else {
                        // Profile is not complete, go to profile setup
                        val intent = Intent(this, ProfileSetupActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                } else {
                    // No profile document, go to profile setup
                    val intent = Intent(this, ProfileSetupActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
            .addOnFailureListener {
                // Error checking profile, default to profile setup
                Toast.makeText(
                    this,
                    getString(R.string.error_checking_profile),
                    Toast.LENGTH_SHORT
                ).show()
                
                val intent = Intent(this, ProfileSetupActivity::class.java)
                startActivity(intent)
                finish()
            }
            .addOnCompleteListener {
                // Always hide progress and re-enable button
                progressBar.visibility = View.GONE
                btnLogin.isEnabled = true
            }
    }

    private fun showPasswordResetDialog() {
        val email = etEmail.text.toString().trim()
        
        if (email.isEmpty()) {
            tilEmail.error = getString(R.string.email_for_reset)
            return
        }
        
        progressBar.visibility = View.VISIBLE
        
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                progressBar.visibility = View.GONE
                
                if (task.isSuccessful) {
                    Toast.makeText(
                        this,
                        getString(R.string.password_reset_sent),
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(
                        this,
                        task.exception?.message ?: getString(R.string.password_reset_failed),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }
}
