package com.jdcoding.houbllaa.data

import java.util.Date
import java.util.UUID

/**
 * Represents an event in the pregnancy calendar
 */
data class Event(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val date: Date,
    val type: EventType,
    val reminderTime: Long? = null // Time in minutes before the event to remind
)

/**
 * Types of events in the pregnancy app
 */
enum class EventType {
    APPOINTMENT,
    MILESTONE,
    REMINDER,
    CUSTOM
}
