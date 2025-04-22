package com.jdcoding.houbllaa.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.jdcoding.houbllaa.data.local.entity.EventEntity
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface EventDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: EventEntity): Long
    
    @Update
    suspend fun updateEvent(event: EventEntity)
    
    @Delete
    suspend fun deleteEvent(event: EventEntity)
    
    @Query("SELECT * FROM events WHERE userId = :userId ORDER BY date ASC")
    fun getEventsByUser(userId: String): Flow<List<EventEntity>>
    
    @Query("SELECT * FROM events WHERE userId = :userId AND date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    fun getEventsByDateRange(userId: String, startDate: Date, endDate: Date): Flow<List<EventEntity>>
    
    @Query("SELECT * FROM events WHERE eventId = :eventId")
    fun getEventById(eventId: Long): Flow<EventEntity?>
    
    @Query("SELECT * FROM events WHERE userId = :userId AND type = :type ORDER BY date ASC")
    fun getEventsByType(userId: String, type: String): Flow<List<EventEntity>>
    
    @Query("DELETE FROM events WHERE userId = :userId")
    suspend fun deleteAllUserEvents(userId: String)
}
