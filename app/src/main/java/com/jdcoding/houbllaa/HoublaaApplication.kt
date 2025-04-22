package com.jdcoding.houbllaa

import android.app.Application
import com.jdcoding.houbllaa.data.local.AppDatabase
import com.jdcoding.houbllaa.data.remote.FirebaseAuthSource
import com.jdcoding.houbllaa.data.remote.FirestoreSource
import com.jdcoding.houbllaa.data.repository.EventRepository
import com.jdcoding.houbllaa.data.repository.NoteRepository
import com.jdcoding.houbllaa.data.repository.UserRepository
import com.google.firebase.FirebaseApp

/**
 * Application class that initializes core components
 */
class HoublaaApplication : Application() {
    // Database
    private val database by lazy { AppDatabase.getDatabase(this) }
    
    // DAOs
    private val userDao by lazy { database.userDao() }
    private val noteDao by lazy { database.noteDao() }
    private val eventDao by lazy { database.eventDao() }
    
    // Remote data sources
    private val firebaseAuthSource by lazy { FirebaseAuthSource() }
    private val firestoreSource by lazy { FirestoreSource() }
    
    // Repositories
    val userRepository by lazy { 
        UserRepository(userDao, firebaseAuthSource, firestoreSource)
    }
    val noteRepository by lazy { 
        NoteRepository(noteDao, firestoreSource)
    }
    val eventRepository by lazy { 
        EventRepository(eventDao, firestoreSource)
    }
    
    override fun onCreate() {
        super.onCreate()
        // Initialize Firebase
        FirebaseApp.initializeApp(this)
    }
    
    companion object {
        // For Singleton instantiation
        @Volatile private var instance: HoublaaApplication? = null
        
        fun getInstance(): HoublaaApplication {
            return instance ?: synchronized(this) {
                instance ?: throw IllegalStateException("Application not initialized")
            }
        }
    }
}
