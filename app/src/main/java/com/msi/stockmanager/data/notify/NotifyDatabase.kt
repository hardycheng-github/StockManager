package com.msi.stockmanager.data.notify

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [NotifyEntity::class], version = 1, exportSchema = false)
abstract class NotifyDatabase : RoomDatabase() {
    abstract fun notifyDao(): NotifyDao
    
    companion object {
        @Volatile
        private var INSTANCE: NotifyDatabase? = null
        
        fun getDatabase(context: Context): NotifyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NotifyDatabase::class.java,
                    "notify_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
