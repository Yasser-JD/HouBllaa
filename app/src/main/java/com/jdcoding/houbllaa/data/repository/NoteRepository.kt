package com.jdcoding.houbllaa.data.repository

import com.jdcoding.houbllaa.data.local.dao.NoteDao
import com.jdcoding.houbllaa.data.local.entity.NoteEntity
import com.jdcoding.houbllaa.data.remote.FirestoreSource
import com.jdcoding.houbllaa.models.Note
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Date

/**
 * Repository that manages pregnancy journal notes
 */
class NoteRepository(
    private val noteDao: NoteDao,
    private val firestoreSource: FirestoreSource
) {
    // Get notes by user as Flow (from local database)
    fun getNotesByUser(userId: String): Flow<List<Note>> {
        return noteDao.getNotesByUser(userId).map { noteEntities ->
            noteEntities.map { it.toDomainModel() }
        }
    }
    
    // Get notes by date range as Flow (from local database)
    fun getNotesByDateRange(userId: String, startDate: Date, endDate: Date): Flow<List<Note>> {
        return noteDao.getNotesByDateRange(userId, startDate, endDate).map { noteEntities ->
            noteEntities.map { it.toDomainModel() }
        }
    }
    
    // Create a new note
    suspend fun createNote(note: Note): Result<Note> {
        // First save to Firestore
        return firestoreSource.createNote(note).fold(
            onSuccess = { remoteNote ->
                // Then save to local database
                val noteEntity = NoteEntity(
                    userId = remoteNote.userId,
                    date = remoteNote.date,
                    content = remoteNote.content,
                    mood = remoteNote.mood,
                    createdAt = remoteNote.createdAt ?: Date(),
                    updatedAt = remoteNote.updatedAt ?: Date()
                )
                val localId = noteDao.insertNote(noteEntity)
                Result.success(remoteNote.copy(noteId = localId.toString()))
            },
            onFailure = { Result.failure(it) }
        )
    }
    
    // Update an existing note
    suspend fun updateNote(note: Note): Result<Note> {
        // First update in Firestore
        return firestoreSource.updateNote(note).fold(
            onSuccess = { remoteNote ->
                // Then update in local database
                val noteEntity = NoteEntity(
                    noteId = remoteNote.noteId.toLongOrNull() ?: 0,
                    userId = remoteNote.userId,
                    date = remoteNote.date,
                    content = remoteNote.content,
                    mood = remoteNote.mood,
                    updatedAt = Date()
                )
                noteDao.updateNote(noteEntity)
                Result.success(remoteNote)
            },
            onFailure = { Result.failure(it) }
        )
    }
    
    // Sync with remote data
    suspend fun syncNotesWithRemote(userId: String) {
        // Fetch from Firestore
        firestoreSource.getNotesByUser(userId).fold(
            onSuccess = { remoteNotes ->
                // Process remote notes and sync with local database
                for (remoteNote in remoteNotes) {
                    val noteEntity = NoteEntity(
                        noteId = remoteNote.noteId.toLongOrNull() ?: 0,
                        userId = remoteNote.userId,
                        date = remoteNote.date,
                        content = remoteNote.content,
                        mood = remoteNote.mood,
                        createdAt = remoteNote.createdAt ?: Date(),
                        updatedAt = remoteNote.updatedAt ?: Date()
                    )
                    
                    if (noteEntity.noteId == 0L) {
                        // New note from server, save to local
                        noteDao.insertNote(noteEntity)
                    } else {
                        // Existing note, update local
                        noteDao.updateNote(noteEntity)
                    }
                }
            },
            onFailure = { /* Handle error */ }
        )
    }
    
    // Extension functions for converting between domain and data models
    private fun NoteEntity.toDomainModel(): Note {
        return Note(
            noteId = noteId.toString(),
            userId = userId,
            date = date,
            content = content,
            mood = mood,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}
