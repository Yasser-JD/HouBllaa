package com.jdcoding.houbllaa.models

/**
 * Data class representing information for a week of pregnancy 
 * to be displayed in the timeline
 */
data class PregnancyWeekInfo(
    val week: Int,
    val title: String,
    val description: String,
    val isCurrentWeek: Boolean = false
)
