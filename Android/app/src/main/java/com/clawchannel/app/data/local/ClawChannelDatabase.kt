package com.clawchannel.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [MessageEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class ClawChannelDatabase : RoomDatabase() {
    
    abstract fun messageDao(): MessageDao
    
    companion object {
        @Volatile
        private var INSTANCE: ClawChannelDatabase? = null
        
        fun getDatabase(context: Context): ClawChannelDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ClawChannelDatabase::class.java,
                    "claw_channel_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
