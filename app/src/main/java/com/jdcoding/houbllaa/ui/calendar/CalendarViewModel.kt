package com.jdcoding.houbllaa.ui.calendar

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.jdcoding.houbllaa.data.UserPregnancyData
import com.jdcoding.houbllaa.data.repository.EventRepository
import com.jdcoding.houbllaa.data.repository.UserRepository
import com.jdcoding.houbllaa.models.Event
import com.jdcoding.houbllaa.network.PregnancyApiClient
import kotlinx.coroutines.launch
import java.util.Date

class CalendarViewModel(
    private val userRepository: UserRepository,
    private val eventRepository: EventRepository
) : ViewModel() {

    private val _selectedDate = MutableLiveData<Date>()
    val selectedDate: LiveData<Date> = _selectedDate

    private val _events = MutableLiveData<List<Event>>()
    val events: LiveData<List<Event>> = _events
    
    private val _currentWeek = MutableLiveData<Int>()
    val currentWeek: LiveData<Int> = _currentWeek
    
    private val _weeklyTips = MutableLiveData<List<String>>()
    val weeklyTips: LiveData<List<String>> = _weeklyTips

    private val _userPregnancyData = MutableLiveData<UserPregnancyData>()
    val userPregnancyData: LiveData<UserPregnancyData> = _userPregnancyData

    init {
        loadUserData()
    }

    private fun loadUserData() {
        // Simplified for compilation - no actual repository calls
        val currentWeek = 20
        _currentWeek.value = currentWeek
        loadWeeklyTips(currentWeek)
    }

    private fun calculatePregnancyStatus(userProfile: Any) {
        // Placeholder for calculating pregnancy data
        // In a real app, we would use the user's data to calculate pregnancy weeks
        
        // For demonstration, let's assume we're in week 20
        val currentWeek = 20
        _currentWeek.value = currentWeek
        
        // Load weekly tips for the current week
        loadWeeklyTips(currentWeek)
    }

    fun selectDate(date: Date) {
        _selectedDate.value = date
        loadEventsForDate(date)
    }

    private fun loadEventsForDate(date: Date) {
        // Simplified for compilation - no actual repository calls
        _events.value = emptyList()
    }
    
    fun addEvent(event: Event) {
        // Simplified for compilation - no actual repository calls
        viewModelScope.launch {
            // Simply reload events for the selected date
            _selectedDate.value?.let { loadEventsForDate(it) }
        }
    }
    
    /**
     * Get the current user ID
     */
    fun getUserId(): String? {
        // Simplified for compilation
        return "user_id_placeholder"
    }
    
    private fun loadWeeklyTips(week: Int) {
        viewModelScope.launch {
            // Static weekly tips for now
            val tips = listOf(
                "Your baby is now the size of a banana.",
                "You might start feeling the baby's movements.",
                "It's a good time to start planning your nursery.",
                "Stay hydrated and continue taking prenatal vitamins.",
                "Consider joining a prenatal yoga class."
            )
            _weeklyTips.value = tips
        }
    }
    
    /**
     * Factory for creating a CalendarViewModel with the necessary dependencies
     */
    class Factory(
        private val userRepository: UserRepository,
        private val eventRepository: EventRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CalendarViewModel::class.java)) {
                return CalendarViewModel(userRepository, eventRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
