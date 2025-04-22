package com.jdcoding.houbllaa.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.jdcoding.houbllaa.data.repository.EventRepository
import com.jdcoding.houbllaa.data.repository.UserRepository
import com.jdcoding.houbllaa.network.PregnancyApiClient

class CalendarViewModelFactory(
    private val userRepository: UserRepository,
    private val eventRepository: EventRepository
) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CalendarViewModel::class.java)) {
            return CalendarViewModel(
                userRepository,
                eventRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
