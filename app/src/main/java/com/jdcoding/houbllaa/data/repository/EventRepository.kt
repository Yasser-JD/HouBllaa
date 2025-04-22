package com.jdcoding.houbllaa.data.repository

import com.jdcoding.houbllaa.data.local.dao.EventDao
import com.jdcoding.houbllaa.data.local.entity.EventEntity
import com.jdcoding.houbllaa.data.remote.FirestoreSource
import com.jdcoding.houbllaa.models.Event
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Date

/**
 * Repository that manages pregnancy-related events (appointments, milestones, etc.)
 */
class EventRepository(
    private val eventDao: EventDao,
    private val firestoreSource: FirestoreSource
) {
    // Get events by user as Flow (from local database)
    fun getEventsByUser(userId: String): Flow<List<Event>> {
        return eventDao.getEventsByUser(userId).map { eventEntities ->
            eventEntities.map { it.toDomainModel() }
        }
    }
    
    // Get events by date range as Flow (from local database)
    fun getEventsByDateRange(userId: String, startDate: Date, endDate: Date): Flow<List<Event>> {
        return eventDao.getEventsByDateRange(userId, startDate, endDate).map { eventEntities ->
            eventEntities.map { it.toDomainModel() }
        }
    }
    
    // Get events by type as Flow (from local database)
    fun getEventsByType(userId: String, type: String): Flow<List<Event>> {
        return eventDao.getEventsByType(userId, type).map { eventEntities ->
            eventEntities.map { it.toDomainModel() }
        }
    }
    
    // Create a new event
    suspend fun createEvent(event: Event): Result<Event> {
        // First save to Firestore
        return firestoreSource.createEvent(event).fold(
            onSuccess = { remoteEvent ->
                // Then save to local database
                val eventEntity = EventEntity(
                    userId = remoteEvent.userId,
                    title = remoteEvent.title,
                    date = remoteEvent.date,
                    type = remoteEvent.type,
                    description = remoteEvent.description,
                    location = remoteEvent.location,
                    reminder = remoteEvent.reminder,
                    reminderTime = remoteEvent.reminderTime,
                    createdAt = remoteEvent.createdAt ?: Date(),
                    updatedAt = remoteEvent.updatedAt ?: Date()
                )
                val localId = eventDao.insertEvent(eventEntity)
                Result.success(remoteEvent.copy(eventId = localId.toString()))
            },
            onFailure = { Result.failure(it) }
        )
    }
    
    // Sync with remote data
    suspend fun syncEventsWithRemote(userId: String) {
        // Fetch from Firestore
        firestoreSource.getEventsByUser(userId).fold(
            onSuccess = { remoteEvents ->
                // Process remote events and sync with local database
                for (remoteEvent in remoteEvents) {
                    val eventEntity = EventEntity(
                        eventId = remoteEvent.eventId.toLongOrNull() ?: 0,
                        userId = remoteEvent.userId,
                        title = remoteEvent.title,
                        date = remoteEvent.date,
                        type = remoteEvent.type,
                        description = remoteEvent.description,
                        location = remoteEvent.location,
                        reminder = remoteEvent.reminder,
                        reminderTime = remoteEvent.reminderTime,
                        createdAt = remoteEvent.createdAt ?: Date(),
                        updatedAt = remoteEvent.updatedAt ?: Date()
                    )
                    
                    if (eventEntity.eventId == 0L) {
                        // New event from server, save to local
                        eventDao.insertEvent(eventEntity)
                    } else {
                        // Existing event, update local
                        eventDao.updateEvent(eventEntity)
                    }
                }
            },
            onFailure = { /* Handle error */ }
        )
    }
    
    // Extension functions for converting between domain and data models
    private fun EventEntity.toDomainModel(): Event {
        return Event(
            eventId = eventId.toString(),
            userId = userId,
            title = title,
            date = date,
            type = type,
            description = description,
            location = location,
            reminder = reminder,
            reminderTime = reminderTime,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}
