package com.jdcoding.houbllaa.data.local.util

import androidx.room.TypeConverter
import java.util.Date

/**
 * Type converter for Room to handle Date objects
 */
class DateTypeConverter {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}
