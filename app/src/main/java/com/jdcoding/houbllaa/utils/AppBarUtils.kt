package com.jdcoding.houbllaa.utils

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.jdcoding.houbllaa.R
import de.hdodenhof.circleimageview.CircleImageView

/**
 * Utility class to handle common app bar functionality across the app
 */
object AppBarUtils {
    
    /**
     * Setup the app bar with the given title
     * @param fragment Fragment that contains the app bar
     * @param title Title to display in the app bar
     * @param onMenuClickListener Optional listener for menu button click
     * @param onProfileClickListener Optional listener for profile image click
     */
    fun setupAppBar(
        fragment: Fragment,
        title: String,
        onMenuClickListener: (() -> Unit)? = null,
        onProfileClickListener: (() -> Unit)? = null
    ) {
        val view = fragment.requireView()
        
        // Set the page title
        val titleTextView = view.findViewById<TextView>(R.id.tvPageTitle)
        titleTextView.text = title
        
        // Setup menu button click listener
        val menuButton = view.findViewById<ImageView>(R.id.ivMenu)
        menuButton.setOnClickListener {
            if (onMenuClickListener != null) {
                onMenuClickListener.invoke()
            } else {
                // Default menu action
                Toast.makeText(fragment.requireContext(), "Menu clicked", Toast.LENGTH_SHORT).show()
            }
        }
        
        // Setup profile image click listener
        val profileImage = view.findViewById<CircleImageView>(R.id.ivProfilePic)
        profileImage.setOnClickListener {
            if (onProfileClickListener != null) {
                onProfileClickListener.invoke()
            } else {
                // Default profile action
                Toast.makeText(fragment.requireContext(), "Profile clicked", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    /**
     * Show/hide the menu button in the app bar
     * @param fragment Fragment that contains the app bar
     * @param show Whether to show or hide the menu button
     */
    fun showMenuButton(fragment: Fragment, show: Boolean) {
        val view = fragment.requireView()
        val menuButton = view.findViewById<ImageView>(R.id.ivMenu)
        menuButton.visibility = if (show) View.VISIBLE else View.GONE
    }
    
    /**
     * Show/hide the profile image in the app bar
     * @param fragment Fragment that contains the app bar
     * @param show Whether to show or hide the profile image
     */
    fun showProfileImage(fragment: Fragment, show: Boolean) {
        val view = fragment.requireView()
        val profileImage = view.findViewById<CircleImageView>(R.id.ivProfilePic)
        profileImage.visibility = if (show) View.VISIBLE else View.GONE
    }
}
