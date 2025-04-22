package com.jdcoding.houbllaa.auth

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.jdcoding.houbllaa.MainActivity
import com.jdcoding.houbllaa.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ProfileSetupActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    
    private lateinit var etFullName: TextInputEditText
    private lateinit var etDate: TextInputEditText
    private lateinit var etHeight: TextInputEditText
    private lateinit var etWeight: TextInputEditText
    private lateinit var tilFullName: TextInputLayout
    private lateinit var tilDate: TextInputLayout
    private lateinit var tilHeight: TextInputLayout
    private lateinit var tilWeight: TextInputLayout
    private lateinit var rgDateType: RadioGroup
    private lateinit var rbDueDate: RadioButton
    private lateinit var rbLastPeriod: RadioButton
    private lateinit var rbConceptionDate: RadioButton
    private lateinit var cbFirstPregnancy: CheckBox
    private lateinit var btnPickDate: Button
    private lateinit var btnSaveProfile: Button
    private lateinit var progressBar: ProgressBar
    
    private val calendar = Calendar.getInstance()
    private var selectedDate: Date? = null
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_setup)
        
        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        
        // Initialize views
        etFullName = findViewById(R.id.et_full_name)
        etDate = findViewById(R.id.et_date)
        etHeight = findViewById(R.id.et_height)
        etWeight = findViewById(R.id.et_weight)
        tilFullName = findViewById(R.id.til_full_name)
        tilDate = findViewById(R.id.til_date)
        tilHeight = findViewById(R.id.til_height)
        tilWeight = findViewById(R.id.til_weight)
        rgDateType = findViewById(R.id.rg_date_type)
        rbDueDate = findViewById(R.id.rb_due_date)
        rbLastPeriod = findViewById(R.id.rb_last_period)
        rbConceptionDate = findViewById(R.id.rb_conception_date)
        cbFirstPregnancy = findViewById(R.id.cb_first_pregnancy)
        btnPickDate = findViewById(R.id.btn_pick_date)
        btnSaveProfile = findViewById(R.id.btn_save_profile)
        progressBar = findViewById(R.id.progress_bar)
        
        // Set up date selector
        btnPickDate.setOnClickListener {
            showDatePicker()
        }
        
        // Update hint when date type changes
        rgDateType.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rb_due_date -> tilDate.hint = getString(R.string.due_date)
                R.id.rb_last_period -> tilDate.hint = getString(R.string.last_period_date)
                R.id.rb_conception_date -> tilDate.hint = getString(R.string.conception_date)
            }
            
            // Clear the selected date
            etDate.setText("")
            selectedDate = null
        }
        
        // Save profile button
        btnSaveProfile.setOnClickListener {
            saveUserProfile()
        }
        
        // Pre-fill data if user is coming back to edit profile
        loadUserData()
    }
    
    private fun showDatePicker() {
        val dateListener = DatePickerDialog.OnDateSetListener { _, year, month, day ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, day)
            updateDateLabel()
        }
        
        val dialog = DatePickerDialog(
            this,
            dateListener,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        
        // Set date range based on selection type
        when (rgDateType.checkedRadioButtonId) {
            R.id.rb_due_date -> {
                // Due date should be in the future (up to 10 months)
                val minDate = Calendar.getInstance()
                val maxDate = Calendar.getInstance()
                maxDate.add(Calendar.MONTH, 10)
                dialog.datePicker.minDate = minDate.timeInMillis
                dialog.datePicker.maxDate = maxDate.timeInMillis
            }
            R.id.rb_last_period -> {
                // Last period should be in the past (up to 9 months)
                val minDate = Calendar.getInstance()
                minDate.add(Calendar.MONTH, -9)
                val maxDate = Calendar.getInstance()
                dialog.datePicker.minDate = minDate.timeInMillis
                dialog.datePicker.maxDate = maxDate.timeInMillis
            }
            R.id.rb_conception_date -> {
                // Conception date should be in the past (up to 9 months)
                val minDate = Calendar.getInstance()
                minDate.add(Calendar.MONTH, -9)
                val maxDate = Calendar.getInstance()
                dialog.datePicker.minDate = minDate.timeInMillis
                dialog.datePicker.maxDate = maxDate.timeInMillis
            }
        }
        
        dialog.show()
    }
    
    private fun updateDateLabel() {
        selectedDate = calendar.time
        etDate.setText(dateFormat.format(calendar.time))
    }
    
    private fun saveUserProfile() {
        // Reset errors
        tilFullName.error = null
        tilDate.error = null
        
        // Get values
        val fullName = etFullName.text.toString().trim()
        val height = etHeight.text.toString().trim()
        val weight = etWeight.text.toString().trim()
        val isFirstPregnancy = cbFirstPregnancy.isChecked
        
        // Validate required fields
        if (fullName.isEmpty()) {
            tilFullName.error = getString(R.string.name_required)
            return
        }
        
        if (selectedDate == null) {
            tilDate.error = getString(R.string.date_required)
            return
        }
        
        // Show progress
        progressBar.visibility = View.VISIBLE
        btnSaveProfile.isEnabled = false
        
        // Get current user
        val user = auth.currentUser
        if (user == null) {
            Toast.makeText(this, getString(R.string.user_not_authenticated), Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }
        
        // Create user profile data
        val userData = HashMap<String, Any>()
        userData["name"] = fullName
        userData["profileComplete"] = true
        userData["updatedAt"] = FieldValue.serverTimestamp()
        
        // Add date based on type
        when (rgDateType.checkedRadioButtonId) {
            R.id.rb_due_date -> userData["estimatedDueDate"] = selectedDate!!
            R.id.rb_last_period -> userData["lastMenstrualPeriod"] = selectedDate!!
            R.id.rb_conception_date -> userData["conceptionDate"] = selectedDate!!
        }
        
        // Add optional data
        if (height.isNotEmpty()) {
            userData["height"] = height.toDoubleOrNull() ?: 0.0
        }
        
        if (weight.isNotEmpty()) {
            userData["weight"] = weight.toDoubleOrNull() ?: 0.0
        }
        
        userData["isFirstPregnancy"] = isFirstPregnancy
        
        // Save to Firestore
        db.collection("users").document(user.uid)
            .set(userData)
            .addOnSuccessListener {
                Toast.makeText(this, getString(R.string.profile_saved), Toast.LENGTH_SHORT).show()
                
                // Navigate to main activity
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, getString(R.string.profile_save_failed) + e.message, Toast.LENGTH_LONG).show()
                progressBar.visibility = View.GONE
                btnSaveProfile.isEnabled = true
            }
    }
    
    private fun loadUserData() {
        val user = auth.currentUser ?: return
        
        progressBar.visibility = View.VISIBLE
        
        db.collection("users").document(user.uid)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    // Fill name
                    etFullName.setText(document.getString("name") ?: "")
                    
                    // Fill height and weight
                    val height = document.getDouble("height")
                    if (height != null && height > 0) {
                        etHeight.setText(height.toInt().toString())
                    }
                    
                    val weight = document.getDouble("weight")
                    if (weight != null && weight > 0) {
                        etWeight.setText(weight.toString())
                    }
                    
                    // Set first pregnancy checkbox
                    cbFirstPregnancy.isChecked = document.getBoolean("isFirstPregnancy") ?: false
                    
                    // Handle dates
                    val dueDate = document.getDate("estimatedDueDate")
                    val lmpDate = document.getDate("lastMenstrualPeriod")
                    val conceptionDate = document.getDate("conceptionDate")
                    
                    when {
                        dueDate != null -> {
                            rbDueDate.isChecked = true
                            tilDate.hint = getString(R.string.due_date)
                            selectedDate = dueDate
                            calendar.time = dueDate
                            etDate.setText(dateFormat.format(dueDate))
                        }
                        lmpDate != null -> {
                            rbLastPeriod.isChecked = true
                            tilDate.hint = getString(R.string.last_period_date)
                            selectedDate = lmpDate
                            calendar.time = lmpDate
                            etDate.setText(dateFormat.format(lmpDate))
                        }
                        conceptionDate != null -> {
                            rbConceptionDate.isChecked = true
                            tilDate.hint = getString(R.string.conception_date)
                            selectedDate = conceptionDate
                            calendar.time = conceptionDate
                            etDate.setText(dateFormat.format(conceptionDate))
                        }
                    }
                }
            }
            .addOnCompleteListener {
                progressBar.visibility = View.GONE
            }
    }
}
