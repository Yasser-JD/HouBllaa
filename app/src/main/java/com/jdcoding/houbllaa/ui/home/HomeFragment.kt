package com.jdcoding.houbllaa.ui.home

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.jdcoding.houbllaa.R
import com.jdcoding.houbllaa.adapters.DaySelectorAdapter
import com.jdcoding.houbllaa.data.PregnancyStaticData
import com.jdcoding.houbllaa.data.UserPregnancyData
import com.jdcoding.houbllaa.data.WeeklyBabyMetrics
import com.jdcoding.houbllaa.di.RepositoryProvider
import com.jdcoding.houbllaa.models.User
import com.jdcoding.houbllaa.network.PregnancyApiClient
import com.jdcoding.houbllaa.utils.AppBarUtils
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/**
 * HomeFragment displays the main dashboard of the pregnancy tracking app,
 * including current week, days left, baby size comparison, and tips.
 */
class HomeFragment : Fragment() {

    // UI components
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var greetingText: TextView
    private lateinit var pregnancyWeekText: TextView
    private lateinit var daySelector: RecyclerView
    private lateinit var babySizeText: TextView
    private lateinit var babyHeightText: TextView
    private lateinit var babyWeightText: TextView
    private lateinit var daysLeftText: TextView
    private lateinit var babyDevelopmentText: TextView
    private lateinit var babyDevelopmentReadMore: TextView
    private lateinit var bodyChangesText: TextView
    private lateinit var bodyChangesReadMore: TextView
    private lateinit var tipOfTheDayText: TextView
    private lateinit var tipReadMore: TextView
    private lateinit var noteContent: EditText
    private lateinit var moodSpinner: Spinner
    private lateinit var saveNoteButton: Button
    private lateinit var babyIllustration: ImageView
    
    // Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    
    // ViewModel
    private val viewModel by viewModels<HomeViewModel> { 
        HomeViewModel.Factory(
            RepositoryProvider.provideUserRepository(requireContext()),
            RepositoryProvider.provideNoteRepository(requireContext()),
            RepositoryProvider.provideEventRepository(requireContext())
        )
    }
    
    // Data
    private var currentWeek: Int = 16  // Default to week 16 for now
    private var selectedDate: LocalDate = LocalDate.now()
    private var weekData: PregnancyApiClient.WeeklyPregnancyData? = null
    private var babyMetrics: WeeklyBabyMetrics? = null
    private var userName: String = ""
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        
        // Initialize UI components
        initializeViews(view)
        
        // Set up swipe to refresh
        swipeRefreshLayout.setOnRefreshListener {
            loadData()
        }
        
        // Set up day selector
        setupDaySelector()
        
        // Observe ViewModel data
        observeViewModelData()
        
        // Directly load user data from Firestore
        loadUserDataFromFirestore()
        
        // Load data
        loadData()
    }
    
    private fun observeViewModelData() {
        // Update greeting text with user's name
        viewModel.user.observe(viewLifecycleOwner, Observer { user ->
            if (user != null) {
                userName = user.name ?: "there"
                updateGreetingText()
            }
        })
        
        // Observe pregnancy week
        viewModel.currentWeek.observe(viewLifecycleOwner, Observer { week ->
            week?.let {
                currentWeek = it
                updatePregnancyWeekText()
                updateStaticData()
                fetchApiData()
            }
        })
        
        // Observe days left
        viewModel.daysLeft.observe(viewLifecycleOwner, Observer { days ->
            days?.let {
                updateDaysLeftText(it)
            }
        })
        
        // Observe tip of the day
        viewModel.tipOfTheDay.observe(viewLifecycleOwner, Observer { tip ->
            tip?.let {
                tipOfTheDayText.text = it
            }
        })
    }
    
    private fun updateGreetingText() {
        val currentHour = java.time.LocalTime.now().hour
        val greeting = when (currentHour) {
            in 0..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            else -> "Good evening"
        }
        greetingText.text = "$greeting, $userName"
    }
    
    private fun updatePregnancyWeekText() {
        // Format week number with the correct ordinal suffix (1st, 2nd, 3rd, etc.)
        val suffix = when {
            currentWeek % 100 in 11..13 -> "th"
            currentWeek % 10 == 1 -> "st"
            currentWeek % 10 == 2 -> "nd"
            currentWeek % 10 == 3 -> "rd"
            else -> "th"
        }
        pregnancyWeekText.text = "${currentWeek}$suffix Week of Pregnancy"
    }
    
    private fun updateDaysLeftText(days: Int) {
        daysLeftText.text = "$days days"
    }
    
    private fun initializeViews(view: View) {
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)
        greetingText = view.findViewById(R.id.greeting_text)
        pregnancyWeekText = view.findViewById(R.id.tvPregnancyWeek)
        daySelector = view.findViewById(R.id.rvDaySelector)
        babySizeText = view.findViewById(R.id.tvBabySize)
        babyHeightText = view.findViewById(R.id.tvBabyHeight)
        babyWeightText = view.findViewById(R.id.tvBabyWeight)
        daysLeftText = view.findViewById(R.id.tvDaysLeft)
        babyDevelopmentText = view.findViewById(R.id.tvBabyDevelopment)
        babyDevelopmentReadMore = view.findViewById(R.id.tvBabyDevelopmentReadMore)
        bodyChangesText = view.findViewById(R.id.tvBodyChanges)
        bodyChangesReadMore = view.findViewById(R.id.tvBodyChangesReadMore)
        tipOfTheDayText = view.findViewById(R.id.tvTipOfTheDay)
        tipReadMore = view.findViewById(R.id.tvTipReadMore)
        noteContent = view.findViewById(R.id.etNoteContent)
        moodSpinner = view.findViewById(R.id.spinnerMood)
        saveNoteButton = view.findViewById(R.id.btnSaveNote)
        babyIllustration = view.findViewById(R.id.baby_illustration)
        
        // Set up app bar components
        setupAppBarActions(view)
        
        // Set up read more click listeners
        setupReadMoreListeners()
        
        // Set up save note button
        saveNoteButton.setOnClickListener {
            saveNote()
        }
    }
    
    private fun setupAppBarActions(view: View) {
        // Use the AppBarUtils to set up the app bar
        AppBarUtils.setupAppBar(
            this,
            "Home",
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
    
    private fun setupDaySelector() {
        // Generate week days for the selector
        val today = LocalDate.now()
        val startOfWeek = today.minusDays(today.dayOfWeek.value - 1L) // Monday of current week
        val days = (0..6).map { startOfWeek.plusDays(it.toLong()) }
        
        // Create and set adapter
        val adapter = DaySelectorAdapter(
            requireContext(),
            days,
            selectedDate
        ) { date ->
            // Day selection callback
            selectedDate = date
            setupDaySelector() // Refresh the selector
        }
        
        daySelector.adapter = adapter
        daySelector.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
    }
    
    private fun loadData() {
        // Refresh data from ViewModel
        viewModel.refreshDashboard()
        
        // Also directly load user data from Firestore
        loadUserDataFromFirestore()
        
        // Update UI with static data based on the current week
        updateStaticData()
        
        // Fetch API data
        fetchApiData()
    }
    
    private fun loadUserDataFromFirestore() {
        val userId = auth.currentUser?.uid ?: return
        
        // Show loading indicator
        swipeRefreshLayout.isRefreshing = true
        
        // Get user data directly from Firestore
        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    // Get user name
                    val name = document.getString("name") ?: "there"
                    userName = name
                    updateGreetingText()
                    
                    // Get pregnancy information
                    val estimatedDueDate = document.getTimestamp("estimatedDueDate")?.toDate()
                    val lastMenstrualPeriod = document.getTimestamp("lastMenstrualPeriod")?.toDate()
                    
                    if (estimatedDueDate != null) {
                        // Calculate pregnancy week based on due date
                        val today = java.util.Calendar.getInstance().time
                        val daysUntilDue = ((estimatedDueDate.time - today.time) / (1000 * 60 * 60 * 24)).toInt()
                        val daysOfPregnancy = 280 - daysUntilDue // Assuming 40 weeks = 280 days
                        currentWeek = (daysOfPregnancy / 7) + 1
                        
                        updatePregnancyWeekText()
                        updateDaysLeftText(daysUntilDue)
                    } else if (lastMenstrualPeriod != null) {
                        // Calculate pregnancy week based on LMP
                        val today = java.util.Calendar.getInstance().time
                        val daysOfPregnancy = ((today.time - lastMenstrualPeriod.time) / (1000 * 60 * 60 * 24)).toInt()
                        currentWeek = (daysOfPregnancy / 7) + 1
                        
                        // Calculate due date from LMP
                        val calendar = java.util.Calendar.getInstance()
                        calendar.time = lastMenstrualPeriod
                        calendar.add(java.util.Calendar.DAY_OF_YEAR, 280)
                        val calculatedDueDate = calendar.time
                        
                        val daysUntilDue = ((calculatedDueDate.time - today.time) / (1000 * 60 * 60 * 24)).toInt()
                        
                        updatePregnancyWeekText()
                        updateDaysLeftText(daysUntilDue)
                    }
                    
                    // Update UI with the week information
                    updateStaticData()
                }
                
                swipeRefreshLayout.isRefreshing = false
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error loading user data: ${e.message}", Toast.LENGTH_SHORT).show()
                swipeRefreshLayout.isRefreshing = false
            }
    }
    
    private fun updateStaticData() {
        // Get metrics for current week
        babyMetrics = PregnancyStaticData.getMetricsForWeek(currentWeek)
        
        // Update baby status card with metrics
        if (babyMetrics != null) {
            val metrics = babyMetrics as WeeklyBabyMetrics
            babySizeText.text = "Your baby is the size of ${metrics.sizeComparison}"
            babyHeightText.text = "${metrics.heightCm} cm"
            babyWeightText.text = "${metrics.weightGrams.toInt()} gr"
            // Days left is observed from the ViewModel
        }
    }
    
    private fun fetchApiData() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                swipeRefreshLayout.isRefreshing = true
                
                // Fetch data for current week
                weekData = PregnancyApiClient.fetchWeekData(currentWeek)
                
                // Update UI with API data
                weekData?.let { data ->
                    // Baby development
                    babyDevelopmentText.text = data.babyDevelopment.take(150) + "..."
                    
                    // Body changes
                    bodyChangesText.text = data.bodyChanges.take(150) + "..."
                    
                    // Tip of the day (using first tip from the list)
                    if (data.tips.isNotEmpty()) {
                        tipOfTheDayText.text = data.tips[0]
                    }
                    
                    // Load baby illustration if available
                    if (data.imageUrl.isNotEmpty()) {
                        Glide.with(requireContext())
                            .load(data.imageUrl)
                            .placeholder(R.drawable.baby_placeholder)
                            .error(R.drawable.baby_placeholder)
                            .into(babyIllustration)
                    }
                }
            } catch (e: Exception) {
                // Handle error
                babyDevelopmentText.text = "Could not load development data. Please try again."
                bodyChangesText.text = "Could not load body changes data. Please try again."
            } finally {
                swipeRefreshLayout.isRefreshing = false
            }
        }
    }
    
    private fun setupReadMoreListeners() {
        babyDevelopmentReadMore.setOnClickListener {
            // Show baby development in a dialog
            weekData?.let { data ->
                showExpandableContentDialog(
                    title = "Baby Development: Week $currentWeek",
                    content = data.babyDevelopment
                )
            }
        }
        
        bodyChangesReadMore.setOnClickListener {
            // Show body changes in a dialog
            weekData?.let { data ->
                showExpandableContentDialog(
                    title = "Body Changes: Week $currentWeek",
                    content = data.bodyChanges
                )
            }
        }
        
        tipReadMore.setOnClickListener {
            // Show all tips in a dialog
            weekData?.let { data ->
                val tipsList = data.tips.joinToString("\n\n• ", "• ")
                showExpandableContentDialog(
                    title = "Tips for Week $currentWeek",
                    content = tipsList
                )
            }
        }
    }
    
    /**
     * Show a dialog with expandable content
     */
    private fun showExpandableContentDialog(title: String, content: String) {
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(content)
            .setPositiveButton("Close") { dialog, _ -> dialog.dismiss() }
            .create()
        
        // Set window to be wider
        dialog.show()
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }
    
    private fun saveNote() {
        val note = noteContent.text.toString()
        val mood = moodSpinner.selectedItem.toString()
        
        if (note.isNotEmpty()) {
            // Save note to database using the ViewModel
            viewModel.saveDailyNote(note, mood)
            noteContent.text.clear()
            // Show confirmation
            Toast.makeText(requireContext(), "Note saved successfully", Toast.LENGTH_SHORT).show()
        }
    }
}
