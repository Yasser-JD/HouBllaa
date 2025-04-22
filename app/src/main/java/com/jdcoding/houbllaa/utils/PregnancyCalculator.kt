package com.jdcoding.houbllaa.utils

import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit

/**
 * Utility class for pregnancy-related calculations
 */
class PregnancyCalculator {
    companion object {
        // Average pregnancy duration in days (40 weeks)
        const val AVERAGE_PREGNANCY_DURATION_DAYS = 280
        
        // Average time from conception to birth in days (38 weeks)
        const val CONCEPTION_TO_BIRTH_DAYS = 266
        
        /**
         * Calculate estimated due date based on last menstrual period (LMP)
         * Formula: LMP + 280 days (40 weeks)
         */
        fun calculateDueDateFromLMP(lmpDate: Date): Date {
            val calendar = Calendar.getInstance()
            calendar.time = lmpDate
            calendar.add(Calendar.DAY_OF_YEAR, AVERAGE_PREGNANCY_DURATION_DAYS)
            return calendar.time
        }
        
        /**
         * Calculate estimated due date based on conception date
         * Formula: Conception date + 266 days (38 weeks)
         */
        fun calculateDueDateFromConception(conceptionDate: Date): Date {
            val calendar = Calendar.getInstance()
            calendar.time = conceptionDate
            calendar.add(Calendar.DAY_OF_YEAR, CONCEPTION_TO_BIRTH_DAYS)
            return calendar.time
        }
        
        /**
         * Estimate conception date based on LMP and cycle length
         * Formula: LMP + (cycle length - 14) days
         * Default cycle length is 28 days if not provided
         */
        fun estimateConceptionDate(lmpDate: Date, cycleLength: Int = 28): Date {
            val calendar = Calendar.getInstance()
            calendar.time = lmpDate
            
            // Adjust for cycle length
            val ovulationDay = cycleLength - 14
            calendar.add(Calendar.DAY_OF_YEAR, ovulationDay)
            
            return calendar.time
        }
        
        /**
         * Calculate current pregnancy week
         * Returns a Pair of (week, day) where week is 1-40 and day is 0-6
         */
        fun getCurrentPregnancyWeek(lmpDate: Date, currentDate: Date = Date()): Pair<Int, Int> {
            val diffInMillis = currentDate.time - lmpDate.time
            val diffInDays = TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS).toInt()
            
            // If before LMP, return 0
            if (diffInDays < 0) return Pair(0, 0)
            
            val week = (diffInDays / 7) + 1
            val day = diffInDays % 7
            
            return Pair(week, day)
        }
        
        /**
         * Calculate days left until estimated due date
         */
        fun getDaysUntilDueDate(dueDate: Date, currentDate: Date = Date()): Int {
            val diffInMillis = dueDate.time - currentDate.time
            return TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS).toInt()
        }
        
        /**
         * Calculate trimester (1, 2, or 3) based on current week
         */
        fun getCurrentTrimester(currentWeek: Int): Int {
            return when {
                currentWeek < 14 -> 1
                currentWeek < 28 -> 2
                else -> 3
            }
        }
    }
}
