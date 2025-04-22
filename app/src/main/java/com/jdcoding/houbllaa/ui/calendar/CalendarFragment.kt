package com.jdcoding.houbllaa.ui.calendar

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CalendarView
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.jdcoding.houbllaa.R
import com.jdcoding.houbllaa.di.RepositoryProvider
import com.jdcoding.houbllaa.models.Event
import com.jdcoding.houbllaa.network.PregnancyApiClient
import com.jdcoding.houbllaa.utils.AppBarUtils
import java.util.Calendar
import java.util.Date

/**
 * CalendarFragment displays a calendar view with pregnancy milestones,
 * appointments, and other important dates. It also shows weekly pregnancy tips.
 */
class CalendarFragment : Fragment() {

    private lateinit var calendarView: CalendarView
    private lateinit var weeklyTipsRecyclerView: RecyclerView
    private lateinit var eventsRecyclerView: RecyclerView
    private lateinit var noEventsText: TextView
    private lateinit var addEventButton: FloatingActionButton
    private lateinit var currentWeekText: TextView
    
    private lateinit var weeklyTipAdapter: WeeklyTipAdapter
    // Use the view model initialization with Factory pattern
    private val viewModel by viewModels<CalendarViewModel> {
        CalendarViewModel.Factory(
            RepositoryProvider.provideUserRepository(requireContext()),
            RepositoryProvider.provideEventRepository(requireContext())
        )
    }
    private val selectedDate = Calendar.getInstance()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_calendar, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Set up app bar
        AppBarUtils.setupAppBar(
            this,
            "Calendar",
            onProfileClickListener = {
                // Navigate to user profile fragment
                findNavController().navigate(R.id.navigation_user_profile)
            }
        )
        
        // ViewModel is initialized automatically via viewModels delegate with the Factory
        
        // Initialize views
        initializeViews(view)
        
        // Set up calendar
        setupCalendar()
        
        // Set up weekly tips recycler view
        setupWeeklyTipsRecyclerView()
        
        // Set up add event button
        setupAddEventButton()
        
        // Observe ViewModel data
        observeViewModelData()
    }
    
    private fun initializeViews(view: View) {
        calendarView = view.findViewById(R.id.calendar_view)
        weeklyTipsRecyclerView = view.findViewById(R.id.recycler_view_tips)
        eventsRecyclerView = view.findViewById(R.id.recycler_view_events)
        noEventsText = view.findViewById(R.id.tv_no_events)
        addEventButton = view.findViewById(R.id.fab_add_event)
        currentWeekText = view.findViewById(R.id.tv_current_week)
    }
    
    private fun setupCalendar() {
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            selectedDate.set(year, month, dayOfMonth)
            viewModel.selectDate(selectedDate.time)
        }
        
        // Initialize with current date
        viewModel.selectDate(Date())
    }
    
    private fun setupWeeklyTipsRecyclerView() {
        weeklyTipAdapter = WeeklyTipAdapter()
        weeklyTipsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = weeklyTipAdapter
        }
    }
    
    private fun setupAddEventButton() {
        addEventButton.setOnClickListener {
            showAddEventDialog()
        }
    }
    
    private fun observeViewModelData() {
        // Observe selected date events
        viewModel.events.observe(viewLifecycleOwner) { events ->
            // Update the events recycler view
            noEventsText.visibility = if (events.isEmpty()) View.VISIBLE else View.GONE
            eventsRecyclerView.visibility = if (events.isEmpty()) View.GONE else View.VISIBLE
            
            // TODO: Update events adapter when implemented
        }
        
        // Observe weekly tips
        viewModel.weeklyTips.observe(viewLifecycleOwner) { tips ->
            weeklyTipAdapter.updateTips(tips)
        }
        
        // Observe current pregnancy week
        viewModel.currentWeek.observe(viewLifecycleOwner) { week ->
            currentWeekText.text = "Week $week"
        }
    }
    
    private fun showAddEventDialog() {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_add_event)
        
        val titleInput = dialog.findViewById<EditText>(R.id.et_event_title)
        val descriptionInput = dialog.findViewById<EditText>(R.id.et_event_description)
        val typeGroup = dialog.findViewById<RadioGroup>(R.id.rg_event_type)
        val saveButton = dialog.findViewById<Button>(R.id.btn_save_event)
        val cancelButton = dialog.findViewById<Button>(R.id.btn_cancel)
        
        // Set the date to the currently selected date
        val dateText = dialog.findViewById<TextView>(R.id.tv_selected_date)
        val dateFormat = java.text.SimpleDateFormat("MMMM dd, yyyy", java.util.Locale.getDefault())
        dateText.text = dateFormat.format(selectedDate.time)
        
        // Set up date picker
        dateText.setOnClickListener {
            showDatePicker(dateText)
        }
        
        // Set up save button
        saveButton.setOnClickListener {
            val title = titleInput.text.toString()
            val description = descriptionInput.text.toString()
            
            if (title.isBlank()) {
                titleInput.error = "Title is required"
                return@setOnClickListener
            }
            
            // Get selected type
            val eventType = when(typeGroup.checkedRadioButtonId) {
                R.id.rb_appointment -> "appointment"
                R.id.rb_milestone -> "milestone"
                else -> "custom"
            }
            
            // Create event object
            val userId = viewModel.getUserId() ?: ""
            if (userId.isBlank()) {
                Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
                return@setOnClickListener
            }
            
            val event = Event(
                userId = userId,
                title = title,
                description = description,
                date = selectedDate.time,
                type = eventType,
                location = null,
                reminder = false
            )
            
            // Save event
            viewModel.addEvent(event)
            
            Toast.makeText(requireContext(), "Event added", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }
        
        // Set up cancel button
        cancelButton.setOnClickListener {
            dialog.dismiss()
        }
        
        dialog.show()
    }
    
    private fun showDatePicker(dateText: TextView) {
        val calendar = Calendar.getInstance()
        calendar.time = selectedDate.time
        
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                calendar.set(selectedYear, selectedMonth, selectedDay)
                selectedDate.time = calendar.time
                
                val dateFormat = java.text.SimpleDateFormat("MMMM dd, yyyy", java.util.Locale.getDefault())
                dateText.text = dateFormat.format(selectedDate.time)
            },
            year,
            month,
            day
        )
        
        datePickerDialog.show()
    }
}
