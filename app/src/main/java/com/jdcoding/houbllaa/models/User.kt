package com.jdcoding.houbllaa.models

import java.util.Date

/**
 * Domain model representing a user profile
 */
data class User(
    val userId: String,
    val name: String,
    val email: String,
    val birthday: Date? = null,
    val lastMenstrualPeriod: Date? = null,
    val averageCycleLength: Int? = null,
    val conceptionDate: Date? = null,
    val ultrasoundDate: Date? = null,
    val estimatedDueDate: Date? = null,
    val preferredLanguage: String = "en",
    val darkModeEnabled: Boolean = false,
    val createdAt: Date? = null,
    val updatedAt: Date? = null
)
