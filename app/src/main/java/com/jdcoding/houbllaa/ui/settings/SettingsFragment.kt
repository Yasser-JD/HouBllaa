package com.jdcoding.houbllaa.ui.settings

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.jdcoding.houbllaa.R
import com.jdcoding.houbllaa.SplashActivity
import com.jdcoding.houbllaa.utils.AppBarUtils
import com.jdcoding.houbllaa.utils.LocaleHelper

/**
 * SettingsFragment allows users to configure app preferences,
 * such as language, dark mode, and notification settings.
 */
class SettingsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up app bar components
        setupAppBarActions(view)

        // Set up language selector
        val languageSelector = view.findViewById<View>(R.id.language_selector)
        val currentLanguageText = view.findViewById<TextView>(R.id.tv_current_language)
        
        // Update displayed language
        updateCurrentLanguageDisplay(currentLanguageText)
        
        // Set up language selection click listener
        languageSelector.setOnClickListener {
            showLanguageSelectionDialog(currentLanguageText)
        }
        
        // Set up the sign out button with a simple click listener
        view.findViewById<Button>(R.id.btn_sign_out).setOnClickListener {
            // Sign out from Firebase
            FirebaseAuth.getInstance().signOut()
            
            // Redirect to splash activity
            val intent = Intent(requireContext(), SplashActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
    
    private fun updateCurrentLanguageDisplay(textView: TextView) {
        val currentLanguage = LocaleHelper.getLanguage(requireContext())
        textView.text = LocaleHelper.getDisplayLanguage(currentLanguage)
    }


    private fun setupAppBarActions(view: View) {
        // Use the AppBarUtils to set up the app bar
        AppBarUtils.setupAppBar(
            fragment = this,
            title = "Settings",
            onMenuClickListener = {
                // Open navigation drawer or show menu options
                Toast.makeText(requireContext(), "Menu clicked", Toast.LENGTH_SHORT).show()
            },
            onProfileClickListener = {
                // Navigate to user profile fragment
                findNavController().navigate(R.id.navigation_user_profile)
            }
        )
    }
    private fun showLanguageSelectionDialog(currentLanguageTextView: TextView) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_language_selection, null)
        
        val currentLanguage = LocaleHelper.getLanguage(requireContext())
        val radioGroup = dialogView.findViewById<RadioGroup>(R.id.language_radio_group)
        
        // Pre-select the current language
        val radioButtonId = when (currentLanguage) {
            "en" -> R.id.rb_english
            "fr" -> R.id.rb_french
            "ar" -> R.id.rb_arabic
            else -> R.id.rb_english
        }
        radioGroup.check(radioButtonId)
        
        // Create and show the dialog
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(R.string.language_label)
            .setView(dialogView)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                // Get the selected language
                val selectedId = radioGroup.checkedRadioButtonId
                val languageCode = when (selectedId) {
                    R.id.rb_english -> "en"
                    R.id.rb_french -> "fr"
                    R.id.rb_arabic -> "ar"
                    else -> "en"
                }
                
                // Only change if language is different
                if (languageCode != currentLanguage) {
                    // Apply the new language
                    LocaleHelper.setLocale(requireContext(), languageCode)
                    
                    // Update the UI to reflect language change
                    updateCurrentLanguageDisplay(currentLanguageTextView)
                    
                    // Restart the activity to apply changes
                    requireActivity().recreate()
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .create()
        
        dialog.show()
    }
}
