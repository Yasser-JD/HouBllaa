package com.jdcoding.houbllaa.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Room entity representing the user profile information
 */
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val userId: String,
    val name: String,
    val email: String,
    val birthday: Date?,
    val lastMenstrualPeriod: Date?,
    val averageCycleLength: Int?,
    val conceptionDate: Date?,
    val ultrasoundDate: Date?,
    val estimatedDueDate: Date?,
    val preferredLanguage: String = "en",
    val darkModeEnabled: Boolean = false,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)
