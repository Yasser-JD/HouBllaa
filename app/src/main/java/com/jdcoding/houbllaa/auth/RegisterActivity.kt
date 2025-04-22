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
import com.jdcoding.houbllaa.R

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var etConfirmPassword: TextInputEditText
    private lateinit var tilEmail: TextInputLayout
    private lateinit var tilPassword: TextInputLayout
    private lateinit var tilConfirmPassword: TextInputLayout
    private lateinit var btnRegister: Button
    private lateinit var tvLogin: TextView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Initialize views
        etEmail = findViewById(R.id.et_email)
        etPassword = findViewById(R.id.et_password)
        etConfirmPassword = findViewById(R.id.et_confirm_password)
        tilEmail = findViewById(R.id.til_email)
        tilPassword = findViewById(R.id.til_password)
        tilConfirmPassword = findViewById(R.id.til_confirm_password)
        btnRegister = findViewById(R.id.btn_register)
        tvLogin = findViewById(R.id.tv_login)
        progressBar = findViewById(R.id.progress_bar)

        // Set up click listeners
        btnRegister.setOnClickListener {
            registerUser()
        }

        tvLogin.setOnClickListener {
            // Navigate to login
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun registerUser() {
        // Reset any previous errors
        tilEmail.error = null
        tilPassword.error = null
        tilConfirmPassword.error = null

        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val confirmPassword = etConfirmPassword.text.toString().trim()

        // Validate input
        if (email.isEmpty()) {
            tilEmail.error = getString(R.string.email_required)
            return
        }

        if (password.isEmpty()) {
            tilPassword.error = getString(R.string.password_required)
            return
        }

        if (password.length < 6) {
            tilPassword.error = getString(R.string.password_length)
            return
        }

        if (confirmPassword.isEmpty()) {
            tilConfirmPassword.error = getString(R.string.confirm_password_required)
            return
        }

        if (password != confirmPassword) {
            tilConfirmPassword.error = getString(R.string.passwords_not_match)
            return
        }

        // Show progress bar
        progressBar.visibility = View.VISIBLE
        btnRegister.isEnabled = false

        // Create user in Firebase
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Registration successful, navigate to profile setup
                    val user = auth.currentUser
                    Toast.makeText(
                        this,
                        getString(R.string.registration_successful),
                        Toast.LENGTH_SHORT
                    ).show()
                    
                    // Navigate to profile setup
                    val intent = Intent(this, ProfileSetupActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                } else {
                    // Registration failed
                    val exception = task.exception
                    val errorMessage = when {
                        exception?.message?.contains("email address is already in use") == true -> 
                            getString(R.string.email_already_in_use)
                        exception?.message?.contains("email address is badly formatted") == true -> 
                            getString(R.string.invalid_email)
                        else -> exception?.message ?: getString(R.string.registration_failed)
                    }
                    
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                    
                    // Hide progress bar and re-enable button
                    progressBar.visibility = View.GONE
                    btnRegister.isEnabled = true
                }
            }
    }
}
