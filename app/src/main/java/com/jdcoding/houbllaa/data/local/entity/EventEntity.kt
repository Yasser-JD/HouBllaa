package com.jdcoding.houbllaa.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Room entity representing pregnancy-related events (appointments, milestones, etc.)
 */
@Entity(
    tableName = "events",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("userId")]
)
data class EventEntity(
    @PrimaryKey(autoGenerate = true)
    val eventId: Long = 0,
    val userId: String,
    val title: String,
    val date: Date,
    val type: String, // appointment, milestone, custom
    val description: String? = null,
    val location: String? = null,
    val reminder: Boolean = false,
    val reminderTime: Date? = null,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)
