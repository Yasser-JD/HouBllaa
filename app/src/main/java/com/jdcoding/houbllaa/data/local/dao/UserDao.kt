package com.jdcoding.houbllaa.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.jdcoding.houbllaa.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)
    
    @Update
    suspend fun updateUser(user: UserEntity)
    
    @Query("SELECT * FROM users WHERE userId = :userId")
    fun getUserById(userId: String): Flow<UserEntity?>
    
    @Query("DELETE FROM users WHERE userId = :userId")
    suspend fun deleteUser(userId: String)
    
    @Query("SELECT * FROM users WHERE userId = :userId")
    suspend fun getUserByIdSync(userId: String): UserEntity?
}
