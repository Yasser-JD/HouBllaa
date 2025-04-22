package com.jdcoding.houbllaa.ui.timeline

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.jdcoding.houbllaa.R
import com.jdcoding.houbllaa.adapters.TimelineAdapter
import com.jdcoding.houbllaa.data.PregnancyTimelineData
import com.jdcoding.houbllaa.di.RepositoryProvider
import com.jdcoding.houbllaa.utils.AppBarUtils

/**
 * TimelineFragment displays a week-by-week timeline of the pregnancy,
 * showing baby development milestones and changes in the mother's body.
 */
class TimelineFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var timelineAdapter: TimelineAdapter
    private lateinit var currentWeekText: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    
    private var currentWeek: Int = 16 // Default week if we can't calculate from user data
    
    private val viewModel by viewModels<TimelineViewModel> {
        TimelineViewModel.Factory(
            RepositoryProvider.provideUserRepository(requireContext())
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_timeline, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Initialize views
        recyclerView = view.findViewById(R.id.recycler_view_timeline)
        currentWeekText = view.findViewById(R.id.tv_current_week)
        progressBar = view.findViewById(R.id.progress_bar)
        
        // Set up app bar components
        setupAppBarActions(view)
        
        // Set up RecyclerView
        setupRecyclerView()
        
        // Load current pregnancy week from Firestore
        loadCurrentWeekFromFirestore()
        
        // Observe ViewModel data
        observeViewModelData()
    }
    
    private fun setupRecyclerView() {
        timelineAdapter = TimelineAdapter(emptyList())
        recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = timelineAdapter
        }
    }
    
    private fun loadCurrentWeekFromFirestore() {
        progressBar.visibility = View.VISIBLE
        
        val userId = auth.currentUser?.uid
        if (userId != null) {
            db.collection("users").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        // Get pregnancy information to calculate current week
                        val estimatedDueDate = document.getTimestamp("estimatedDueDate")?.toDate()
                        val lastMenstrualPeriod = document.getTimestamp("lastMenstrualPeriod")?.toDate()
                        
                        if (estimatedDueDate != null) {
                            // Calculate pregnancy week based on due date
                            val today = java.util.Calendar.getInstance().time
                            val daysUntilDue = ((estimatedDueDate.time - today.time) / (1000 * 60 * 60 * 24)).toInt()
                            val daysOfPregnancy = 280 - daysUntilDue // Assuming 40 weeks = 280 days
                            currentWeek = (daysOfPregnancy / 7) + 1
                        } else if (lastMenstrualPeriod != null) {
                            // Calculate pregnancy week based on LMP
                            val today = java.util.Calendar.getInstance().time
                            val daysOfPregnancy = ((today.time - lastMenstrualPeriod.time) / (1000 * 60 * 60 * 24)).toInt()
                            currentWeek = (daysOfPregnancy / 7) + 1
                        }
                        
                        // Update UI
                        updateTimelineWithCurrentWeek(currentWeek)
                    }
                    
                    progressBar.visibility = View.GONE
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Error loading data: ${e.message}", Toast.LENGTH_SHORT).show()
                    progressBar.visibility = View.GONE
                    
                    // Use default week in case of error
                    updateTimelineWithCurrentWeek(currentWeek)
                }
        } else {
            progressBar.visibility = View.GONE
            // Use default week if user is not logged in
            updateTimelineWithCurrentWeek(currentWeek)
        }
    }
    
    private fun updateTimelineWithCurrentWeek(week: Int) {
        // Bound the week between 1 and 42
        val boundedWeek = week.coerceIn(1, 42)
        
        // Update the week display
        currentWeekText.text = "Current Week: $boundedWeek"
        
        // Get timeline data with current week highlighted
        val timelineData = PregnancyTimelineData.getPregnancyWeeks(boundedWeek)
        
        // Update the adapter
        timelineAdapter.updateData(timelineData)
        
        // Scroll to the current week
        (recyclerView.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(boundedWeek - 1, 0)
    }
    
    private fun observeViewModelData() {
        viewModel.currentWeek.observe(viewLifecycleOwner, Observer { week ->
            week?.let {
                currentWeek = it
                updateTimelineWithCurrentWeek(it)
            }
        })
    }

    private fun setupAppBarActions(view: View) {
        // Use the AppBarUtils to set up the app bar
        AppBarUtils.setupAppBar(
            fragment = this,
            title = "Timeline",
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
}
