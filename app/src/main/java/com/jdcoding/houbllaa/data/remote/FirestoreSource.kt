package com.jdcoding.houbllaa.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.jdcoding.houbllaa.models.Event
import com.jdcoding.houbllaa.models.Note
import com.jdcoding.houbllaa.models.User
import kotlinx.coroutines.tasks.await
import java.util.Date

/**
 * Firestore data source for handling cloud data storage
 */
class FirestoreSource {
    private val firestore = FirebaseFirestore.getInstance()
    
    // Collection references
    private val usersCollection = firestore.collection("users")
    private val notesCollection = firestore.collection("notes")
    private val eventsCollection = firestore.collection("events")
    
    // User operations
    suspend fun createOrUpdateUser(user: User): Result<User> {
        return try {
            val userMap = hashMapOf(
                "name" to user.name,
                "email" to user.email,
                "birthday" to user.birthday,
                "lastMenstrualPeriod" to user.lastMenstrualPeriod,
                "averageCycleLength" to user.averageCycleLength,
                "conceptionDate" to user.conceptionDate,
                "ultrasoundDate" to user.ultrasoundDate,
                "estimatedDueDate" to user.estimatedDueDate,
                "preferredLanguage" to user.preferredLanguage,
                "darkModeEnabled" to user.darkModeEnabled,
                "updatedAt" to Date()
            )
            
            if (user.createdAt == null) {
                userMap["createdAt"] = Date()
            }
            
            usersCollection.document(user.userId)
                .set(userMap, SetOptions.merge())
                .await()
                
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getUser(userId: String): Result<User?> {
        return try {
            val documentSnapshot = usersCollection.document(userId).get().await()
            if (documentSnapshot.exists()) {
                val userData = documentSnapshot.data
                val user = User(
                    userId = userId,
                    name = userData?.get("name") as? String ?: "",
                    email = userData?.get("email") as? String ?: "",
                    birthday = userData?.get("birthday") as? Date,
                    lastMenstrualPeriod = userData?.get("lastMenstrualPeriod") as? Date,
                    averageCycleLength = (userData?.get("averageCycleLength") as? Long)?.toInt(),
                    conceptionDate = userData?.get("conceptionDate") as? Date,
                    ultrasoundDate = userData?.get("ultrasoundDate") as? Date,
                    estimatedDueDate = userData?.get("estimatedDueDate") as? Date,
                    preferredLanguage = userData?.get("preferredLanguage") as? String ?: "en",
                    darkModeEnabled = userData?.get("darkModeEnabled") as? Boolean ?: false,
                    createdAt = userData?.get("createdAt") as? Date,
                    updatedAt = userData?.get("updatedAt") as? Date
                )
                Result.success(user)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Note operations
    suspend fun createNote(note: Note): Result<Note> {
        return try {
            val noteMap = hashMapOf(
                "userId" to note.userId,
                "date" to note.date,
                "content" to note.content,
                "mood" to note.mood,
                "createdAt" to Date(),
                "updatedAt" to Date()
            )
            
            val documentRef = notesCollection.document()
            documentRef.set(noteMap).await()
            
            Result.success(note.copy(noteId = documentRef.id))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateNote(note: Note): Result<Note> {
        return try {
            // Create map using mapOf instead of hashMapOf to avoid type mismatch
            val updates = mapOf<String, Any>(
                "content" to note.content,
                "mood" to (note.mood ?: ""),
                "updatedAt" to Date()
            )
            
            notesCollection.document(note.noteId)
                .update(updates)
                .await()
                
            Result.success(note)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getNotesByUser(userId: String): Result<List<Note>> {
        return try {
            val querySnapshot = notesCollection
                .whereEqualTo("userId", userId)
                .orderBy("date")
                .get()
                .await()
                
            val notes = querySnapshot.documents.mapNotNull { document ->
                val data = document.data ?: return@mapNotNull null
                
                Note(
                    noteId = document.id,
                    userId = data["userId"] as String,
                    date = data["date"] as Date,
                    content = data["content"] as String,
                    mood = data["mood"] as? String,
                    createdAt = data["createdAt"] as? Date,
                    updatedAt = data["updatedAt"] as? Date
                )
            }
            
            Result.success(notes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Event operations
    suspend fun createEvent(event: Event): Result<Event> {
        return try {
            val eventMap = hashMapOf(
                "userId" to event.userId,
                "title" to event.title,
                "date" to event.date,
                "type" to event.type,
                "description" to event.description,
                "location" to event.location,
                "reminder" to event.reminder,
                "reminderTime" to event.reminderTime,
                "createdAt" to Date(),
                "updatedAt" to Date()
            )
            
            val documentRef = eventsCollection.document()
            documentRef.set(eventMap).await()
            
            Result.success(event.copy(eventId = documentRef.id))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getEventsByUser(userId: String): Result<List<Event>> {
        return try {
            val querySnapshot = eventsCollection
                .whereEqualTo("userId", userId)
                .orderBy("date")
                .get()
                .await()
                
            val events = querySnapshot.documents.mapNotNull { document ->
                val data = document.data ?: return@mapNotNull null
                
                Event(
                    eventId = document.id,
                    userId = data["userId"] as String,
                    title = data["title"] as String,
                    date = data["date"] as Date,
                    type = data["type"] as String,
                    description = data["description"] as? String,
                    location = data["location"] as? String,
                    reminder = data["reminder"] as? Boolean ?: false,
                    reminderTime = data["reminderTime"] as? Date,
                    createdAt = data["createdAt"] as? Date,
                    updatedAt = data["updatedAt"] as? Date
                )
            }
            
            Result.success(events)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
