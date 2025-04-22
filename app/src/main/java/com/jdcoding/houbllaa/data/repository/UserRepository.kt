package com.jdcoding.houbllaa.data.repository

import com.jdcoding.houbllaa.data.local.dao.UserDao
import com.jdcoding.houbllaa.data.local.entity.UserEntity
import com.jdcoding.houbllaa.data.remote.FirebaseAuthSource
import com.jdcoding.houbllaa.data.remote.FirestoreSource
import com.jdcoding.houbllaa.models.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Date

/**
 * Repository that handles user data operations, combining local and remote data sources
 */
class UserRepository(
    private val userDao: UserDao,
    private val firebaseAuthSource: FirebaseAuthSource,
    private val firestoreSource: FirestoreSource
) {
    // Get current authenticated user
    fun isUserAuthenticated() = firebaseAuthSource.isUserAuthenticated()
    
    // Get current user ID
    fun getCurrentUserId(): String? = firebaseAuthSource.currentUser?.uid
    
    // Get user profile from local database as Flow
    fun getUserProfile(userId: String): Flow<User?> {
        return userDao.getUserById(userId).map { userEntity ->
            userEntity?.toDomainModel()
        }
    }
    
    // Synchronize user data directly from Firestore
    suspend fun syncUserFromFirestore(userId: String): Result<User> {
        return firestoreSource.getUser(userId).fold(
            onSuccess = { user ->
                if (user != null) {
                    // Save to local database
                    userDao.insertUser(user.toLocalEntity())
                    Result.success(user)
                } else {
                    // User not found in Firestore
                    Result.failure(Exception("User not found in Firestore"))
                }
            },
            onFailure = { Result.failure(it) }
        )
    }
    
    // Sign in with email and password
    suspend fun signInWithEmailPassword(email: String, password: String): Result<User> {
        return firebaseAuthSource.signInWithEmailPassword(email, password).fold(
            onSuccess = { firebaseUser ->
                val userId = firebaseUser.uid
                firestoreSource.getUser(userId).fold(
                    onSuccess = { user ->
                        if (user != null) {
                            // Save to local database
                            userDao.insertUser(user.toLocalEntity())
                            Result.success(user)
                        } else {
                            // User exists in auth but not in Firestore, create a basic profile
                            val newUser = User(
                                userId = userId,
                                name = firebaseUser.displayName ?: "",
                                email = firebaseUser.email ?: "",
                                createdAt = Date(),
                                updatedAt = Date()
                            )
                            firestoreSource.createOrUpdateUser(newUser)
                            userDao.insertUser(newUser.toLocalEntity())
                            Result.success(newUser)
                        }
                    },
                    onFailure = { Result.failure(it) }
                )
            },
            onFailure = { Result.failure(it) }
        )
    }
    
    // Register with email and password
    suspend fun registerWithEmailPassword(name: String, email: String, password: String): Result<User> {
        return firebaseAuthSource.registerWithEmailPassword(email, password).fold(
            onSuccess = { firebaseUser ->
                val userId = firebaseUser.uid
                val newUser = User(
                    userId = userId,
                    name = name,
                    email = email,
                    createdAt = Date(),
                    updatedAt = Date()
                )
                
                firestoreSource.createOrUpdateUser(newUser).fold(
                    onSuccess = {
                        userDao.insertUser(newUser.toLocalEntity())
                        Result.success(newUser)
                    },
                    onFailure = { Result.failure(it) }
                )
            },
            onFailure = { Result.failure(it) }
        )
    }
    
    // Update user profile
    suspend fun updateUserProfile(user: User): Result<User> {
        return firestoreSource.createOrUpdateUser(user).fold(
            onSuccess = {
                userDao.updateUser(user.toLocalEntity())
                Result.success(user)
            },
            onFailure = { Result.failure(it) }
        )
    }
    
    // Sign out
    suspend fun signOut() {
        firebaseAuthSource.signOut()
        // Clear local user data if needed
        getCurrentUserId()?.let { userId ->
            val user = userDao.getUserByIdSync(userId)
            if (user != null) {
                // Optionally keep the local data for offline access
                // or delete it for security
                // userDao.deleteUser(userId)
            }
        }
    }
    
    // User preference updates
    suspend fun updateLanguagePreference(userId: String, language: String): Result<Unit> {
        val user = userDao.getUserByIdSync(userId)
        return if (user != null) {
            val updatedUser = user.copy(preferredLanguage = language, updatedAt = Date())
            userDao.updateUser(updatedUser)
            firestoreSource.createOrUpdateUser(updatedUser.toDomainModel()).map { Unit }.fold(
                onSuccess = { Result.success(Unit) },
                onFailure = { Result.failure(it) }
            )
        } else {
            Result.failure(Exception("User not found"))
        }
    }
    
    suspend fun updateDarkModePreference(userId: String, darkModeEnabled: Boolean): Result<Unit> {
        val user = userDao.getUserByIdSync(userId)
        return if (user != null) {
            val updatedUser = user.copy(darkModeEnabled = darkModeEnabled, updatedAt = Date())
            userDao.updateUser(updatedUser)
            firestoreSource.createOrUpdateUser(updatedUser.toDomainModel()).map { Unit }.fold(
                onSuccess = { Result.success(Unit) },
                onFailure = { Result.failure(it) }
            )
        } else {
            Result.failure(Exception("User not found"))
        }
    }
    
    // Extension functions for converting between domain and data models
    private fun UserEntity.toDomainModel(): User {
        return User(
            userId = userId,
            name = name,
            email = email,
            birthday = birthday,
            lastMenstrualPeriod = lastMenstrualPeriod,
            averageCycleLength = averageCycleLength,
            conceptionDate = conceptionDate,
            ultrasoundDate = ultrasoundDate,
            estimatedDueDate = estimatedDueDate,
            preferredLanguage = preferredLanguage,
            darkModeEnabled = darkModeEnabled,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
    
    private fun User.toLocalEntity(): UserEntity {
        return UserEntity(
            userId = userId,
            name = name,
            email = email,
            birthday = birthday,
            lastMenstrualPeriod = lastMenstrualPeriod,
            averageCycleLength = averageCycleLength,
            conceptionDate = conceptionDate,
            ultrasoundDate = ultrasoundDate,
            estimatedDueDate = estimatedDueDate,
            preferredLanguage = preferredLanguage,
            darkModeEnabled = darkModeEnabled,
            createdAt = createdAt ?: Date(),
            updatedAt = updatedAt ?: Date()
        )
    }
}
