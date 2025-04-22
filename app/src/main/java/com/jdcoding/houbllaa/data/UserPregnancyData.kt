package com.jdcoding.houbllaa.data

import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Data class to hold user and pregnancy information
 */
data class UserPregnancyData(
    val userId: String,
    val userName: String,
    val lmpDate: LocalDate? = null,
    val conceptionDate: LocalDate? = null,
    val ultrasoundDueDate: LocalDate? = null,
    val cycleLength: Int = 28
) {
    /**
     * Calculate due date based on available information
     * Priority: Ultrasound > Conception Date > LMP
     */
    fun calculateDueDate(): LocalDate? {
        return when {
            ultrasoundDueDate != null -> ultrasoundDueDate
            conceptionDate != null -> conceptionDate.plusDays(266)
            lmpDate != null -> lmpDate.plusDays(280)
            else -> null
        }
    }
    
    /**
     * Calculate the current pregnancy week
     * @return current week of pregnancy (1-42) or null if unable to calculate
     */
    fun calculateCurrentWeek(): Int? {
        val today = LocalDate.now()
        
        // Use the appropriate date for calculation based on what's available
        val referenceDate = when {
            lmpDate != null -> lmpDate
            conceptionDate != null -> conceptionDate.minusDays(14) // Approximate LMP from conception
            ultrasoundDueDate != null -> ultrasoundDueDate.minusDays(280) // Approximate LMP from due date
            else -> return null
        }
        
        // Calculate days since reference date
        val daysSinceReference = ChronoUnit.DAYS.between(referenceDate, today)
        
        // Calculate week number (add 1 since weeks start from 1, not 0)
        val weekNumber = (daysSinceReference / 7) + 1
        
        // Return week number if within valid range (1-42), otherwise null
        return if (weekNumber in 1..42) weekNumber.toInt() else null
    }
    
    /**
     * Calculate days remaining until due date
     */
    fun calculateDaysRemaining(): Int? {
        val dueDate = calculateDueDate() ?: return null
        val today = LocalDate.now()
        val daysRemaining = ChronoUnit.DAYS.between(today, dueDate)
        return if (daysRemaining >= 0) daysRemaining.toInt() else 0
    }
    
    /**
     * Determine which trimester the pregnancy is in
     * @return Trimester (1, 2, or 3) or null if unable to calculate
     */
    fun getCurrentTrimester(): Int? {
        val week = calculateCurrentWeek() ?: return null
        return when {
            week <= 12 -> 1
            week <= 26 -> 2
            else -> 3
        }
    }
}
