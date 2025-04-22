package com.jdcoding.houbllaa.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.jdcoding.houbllaa.data.local.entity.NoteEntity
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface NoteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity): Long
    
    @Update
    suspend fun updateNote(note: NoteEntity)
    
    @Delete
    suspend fun deleteNote(note: NoteEntity)
    
    @Query("SELECT * FROM notes WHERE userId = :userId ORDER BY date DESC")
    fun getNotesByUser(userId: String): Flow<List<NoteEntity>>
    
    @Query("SELECT * FROM notes WHERE userId = :userId AND date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getNotesByDateRange(userId: String, startDate: Date, endDate: Date): Flow<List<NoteEntity>>
    
    @Query("SELECT * FROM notes WHERE noteId = :noteId")
    fun getNoteById(noteId: Long): Flow<NoteEntity?>
    
    @Query("DELETE FROM notes WHERE userId = :userId")
    suspend fun deleteAllUserNotes(userId: String)
}
