package com.jdcoding.houbllaa.ui.timeline

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.jdcoding.houbllaa.data.repository.UserRepository
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit

/**
 * ViewModel for the Timeline Fragment.
 * Handles the pregnancy week calculation and provides data for the view.
 */
class TimelineViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _currentWeek = MutableLiveData<Int>()
    val currentWeek: LiveData<Int> = _currentWeek

    init {
        loadUserData()
    }

    private fun loadUserData() {
        val userId = userRepository.getCurrentUserId()
        if (userId != null) {
            viewModelScope.launch {
                userRepository.getUserProfile(userId).collect { user ->
                    user?.let {
                        calculateCurrentWeek(it.estimatedDueDate, it.lastMenstrualPeriod)
                    }
                }
            }
        }
    }

    private fun calculateCurrentWeek(dueDate: Date?, lastPeriodDate: Date?) {
        val today = Calendar.getInstance().time

        when {
            dueDate != null -> {
                // Calculate based on due date (EDD)
                val daysUntilDue = TimeUnit.DAYS.convert(
                    dueDate.time - today.time,
                    TimeUnit.MILLISECONDS
                ).toInt()
                val daysOfPregnancy = 280 - daysUntilDue // 40 weeks = 280 days
                val currentWeek = (daysOfPregnancy / 7) + 1
                
                // Ensure week is within valid pregnancy range (1-42)
                _currentWeek.value = currentWeek.coerceIn(1, 42)
            }
            lastPeriodDate != null -> {
                // Calculate based on last menstrual period (LMP)
                val daysOfPregnancy = TimeUnit.DAYS.convert(
                    today.time - lastPeriodDate.time,
                    TimeUnit.MILLISECONDS
                ).toInt()
                val currentWeek = (daysOfPregnancy / 7) + 1
                
                // Ensure week is within valid pregnancy range (1-42)
                _currentWeek.value = currentWeek.coerceIn(1, 42)
            }
            else -> {
                // Default to a middle-pregnancy week if no dates available
                _currentWeek.value = 20
            }
        }
    }

    /**
     * Factory for creating a TimelineViewModel with a dependency
     */
    class Factory(private val userRepository: UserRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(TimelineViewModel::class.java)) {
                return TimelineViewModel(userRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
