package com.jdcoding.houbllaa.models

import java.util.Date

/**
 * Domain model representing a pregnancy-related event (appointment, milestone, etc.)
 */
data class Event(
    val eventId: String = "",
    val userId: String,
    val title: String,
    val date: Date,
    val type: String, // appointment, milestone, custom
    val description: String? = null,
    val location: String? = null,
    val reminder: Boolean = false,
    val reminderTime: Date? = null,
    val createdAt: Date? = null,
    val updatedAt: Date? = null
)
