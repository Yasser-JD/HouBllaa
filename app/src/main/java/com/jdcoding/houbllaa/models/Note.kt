package com.jdcoding.houbllaa.models

import java.util.Date

/**
 * Domain model representing a user's pregnancy journal note
 */
data class Note(
    val noteId: String = "",
    val userId: String,
    val date: Date,
    val content: String,
    val mood: String? = null,
    val createdAt: Date? = null,
    val updatedAt: Date? = null
)
