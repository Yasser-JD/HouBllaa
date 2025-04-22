package com.jdcoding.houbllaa.auth

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.viewfinder.core.ScaleType
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.jdcoding.houbllaa.R
import com.airbnb.lottie.LottieAnimationView

class OnboardingActivity : AppCompatActivity() {

    private lateinit var onboardingAdapter: OnboardingAdapter
    private lateinit var viewPager: ViewPager2
    private lateinit var layoutIndicators: LinearLayout
    private lateinit var btnNext: Button
    private lateinit var btnSkip: Button
    
    // Onboarding content - using Lottie animations from raw folder
    private val onboardingItems = listOf(
        OnboardingItem(
            "pregnantwomen.json", // Animation file in raw folder
            R.string.onboarding_title_1,
            R.string.onboarding_description_1
        ),
        OnboardingItem(
            "mintorhealth.json", // Animation file in raw folder
            R.string.onboarding_title_2,
            R.string.onboarding_description_2
        ),
        OnboardingItem(
            "importantdays.json", // Animation file in raw folder
            R.string.onboarding_title_3,
            R.string.onboarding_description_3
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)
        
        initViews()
        setupOnboardingItems()
        setupIndicators()
        setCurrentIndicator(0)
        
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                setCurrentIndicator(position)
                
                // Change Next button to Get Started on last page
                if (position == onboardingItems.size - 1) {
                    btnNext.text = getString(R.string.get_started)
                } else {
                    btnNext.text = getString(R.string.next)
                }
            }
        })
        
        btnNext.setOnClickListener {
            if (viewPager.currentItem + 1 < onboardingItems.size) {
                viewPager.currentItem += 1
            } else {
                // We're on the last page, navigate to login
                navigateToLogin()
            }
        }
        
        btnSkip.setOnClickListener {
            navigateToLogin()
        }
    }
    
    private fun navigateToLogin() {
        // Save that user has seen onboarding
        val sharedPreferences = getSharedPreferences("HouBlaaPrefs", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean("hasSeenOnboarding", true)
        editor.apply()
        
        // Navigate to login screen
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
    
    private fun initViews() {
        viewPager = findViewById(R.id.viewPager)
        layoutIndicators = findViewById(R.id.layoutIndicators)
        btnNext = findViewById(R.id.btnNext)
        btnSkip = findViewById(R.id.btnSkip)
    }
    
    private fun setupOnboardingItems() {
        onboardingAdapter = OnboardingAdapter(onboardingItems)
        viewPager.adapter = onboardingAdapter
    }
    
    private fun setupIndicators() {
        val indicators = arrayOfNulls<View>(onboardingItems.size)
        val layoutParams = LinearLayout.LayoutParams(
            resources.getDimensionPixelSize(R.dimen.indicator_size),
            resources.getDimensionPixelSize(R.dimen.indicator_size)
        )
        layoutParams.setMargins(8, 0, 8, 0)
        
        for (i in indicators.indices) {
            indicators[i] = View(applicationContext)
            indicators[i]?.setBackgroundResource(R.drawable.indicator_inactive)
            indicators[i]?.layoutParams = layoutParams
            layoutIndicators.addView(indicators[i])
        }
        
        // Set first indicator as active
        if (indicators.isNotEmpty()) {
            setCurrentIndicator(0)
        }
    }
    
    private fun setCurrentIndicator(position: Int) {
        val childCount = layoutIndicators.childCount
        for (i in 0 until childCount) {
            val indicator = layoutIndicators.getChildAt(i)
            val params = indicator.layoutParams as LinearLayout.LayoutParams
            
            // Ensure fixed size for indicators
            params.width = resources.getDimensionPixelSize(R.dimen.indicator_size)
            params.height = resources.getDimensionPixelSize(R.dimen.indicator_size)
            indicator.layoutParams = params
            
            // Set background resource based on position
            if (i == position) {
                indicator.setBackgroundResource(R.drawable.indicator_active)
            } else {
                indicator.setBackgroundResource(R.drawable.indicator_inactive)
            }
        }
    }
    
    // OnboardingAdapter class
    private class OnboardingAdapter(private val onboardingItems: List<OnboardingItem>) :
        RecyclerView.Adapter<OnboardingAdapter.OnboardingViewHolder>() {
        
        inner class OnboardingViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            private val lottieAnimation = view.findViewById<LottieAnimationView>(R.id.lottieAnimation)
            private val textTitle = view.findViewById<TextView>(R.id.textTitle)
            private val textDescription = view.findViewById<TextView>(R.id.textDescription)
            
            fun bind(onboardingItem: OnboardingItem) {
                // Configure Lottie animation from raw folder
                val resourceId = itemView.context.resources.getIdentifier(
                    onboardingItem.animationFile.replace(".json", ""),
                    "raw", 
                    itemView.context.packageName
                )
                
                // Set the animation resource with proper settings
                lottieAnimation.apply {
                    setAnimation(resourceId)
                    
                    // Important: Clear any background color that might be in the animation
                    setBackgroundColor(android.graphics.Color.TRANSPARENT)
                    
                    // Allow the animation to be masked by the parent view
                    clipToOutline = true
                    
                    // Set appropriate scale type
                    //scaleType = com.airbnb.lottie.LottieAnimationView.ScaleType.CENTER_INSIDE
                }
                
                // Set text resources
                textTitle.setText(onboardingItem.titleResId)
                textDescription.setText(onboardingItem.descriptionResId)
                
                // Apply the app color scheme
                textTitle.setTextColor(ContextCompat.getColor(itemView.context, R.color.text_primary))
                textDescription.setTextColor(ContextCompat.getColor(itemView.context, R.color.text_secondary))
            }
        }
        
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OnboardingViewHolder {
            return OnboardingViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.item_onboarding_page,
                    parent,
                    false
                )
            )
        }
        
        override fun onBindViewHolder(holder: OnboardingViewHolder, position: Int) {
            holder.bind(onboardingItems[position])
        }
        
        override fun getItemCount(): Int {
            return onboardingItems.size
        }
    }

    // Data class for onboarding items
    data class OnboardingItem(
        val animationFile: String, // JSON file name in the raw folder
        val titleResId: Int,
        val descriptionResId: Int
    )
}
