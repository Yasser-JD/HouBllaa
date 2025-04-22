package com.jdcoding.houbllaa.di

import android.content.Context
import com.jdcoding.houbllaa.data.local.AppDatabase
import com.jdcoding.houbllaa.data.remote.FirebaseAuthSource
import com.jdcoding.houbllaa.data.remote.FirestoreSource
import com.jdcoding.houbllaa.data.repository.EventRepository
import com.jdcoding.houbllaa.data.repository.NoteRepository
import com.jdcoding.houbllaa.data.repository.UserRepository

/**
 * Helper class to provide repository instances with their dependencies
 */
object RepositoryProvider {
    
    private val firebaseAuthSource by lazy { FirebaseAuthSource() }
    private val firestoreSource by lazy { FirestoreSource() }
    
    // Database is lazily initialized when needed
    private var database: AppDatabase? = null
    
    // Initialize the database if not already done
    private fun getDatabase(context: Context): AppDatabase {
        return database ?: synchronized(this) {
            AppDatabase.getDatabase(context).also { database = it }
        }
    }
    
    // User repository provider
    fun provideUserRepository(context: Context): UserRepository {
        val db = getDatabase(context)
        return UserRepository(db.userDao(), firebaseAuthSource, firestoreSource)
    }
    
    // Note repository provider
    fun provideNoteRepository(context: Context): NoteRepository {
        val db = getDatabase(context)
        return NoteRepository(db.noteDao(), firestoreSource)
    }
    
    // Event repository provider
    fun provideEventRepository(context: Context): EventRepository {
        val db = getDatabase(context)
        return EventRepository(db.eventDao(), firestoreSource)
    }
}
