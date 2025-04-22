package com.jdcoding.houbllaa.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.jdcoding.houbllaa.data.local.dao.EventDao
import com.jdcoding.houbllaa.data.local.dao.NoteDao
import com.jdcoding.houbllaa.data.local.dao.UserDao
import com.jdcoding.houbllaa.data.local.entity.EventEntity
import com.jdcoding.houbllaa.data.local.entity.NoteEntity
import com.jdcoding.houbllaa.data.local.entity.UserEntity
import com.jdcoding.houbllaa.data.local.util.DateTypeConverter

/**
 * Main database for the Houblaa pregnancy tracking app
 */
@Database(
    entities = [
        UserEntity::class,
        NoteEntity::class,
        EventEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(DateTypeConverter::class)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun userDao(): UserDao
    abstract fun noteDao(): NoteDao
    abstract fun eventDao(): EventDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "houblaa_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
