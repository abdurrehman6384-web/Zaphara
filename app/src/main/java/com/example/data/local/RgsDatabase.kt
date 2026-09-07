package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.ApiConfigDao
import com.example.data.local.dao.ChatDao
import com.example.data.local.dao.LicenseDao
import com.example.data.local.entities.ApiConfigEntity
import com.example.data.local.entities.ChatMessageEntity
import com.example.data.local.entities.ChatSessionEntity
import com.example.data.local.entities.LicenseEntity

@Database(
    entities = [
        ChatMessageEntity::class,
        ChatSessionEntity::class,
        LicenseEntity::class,
        ApiConfigEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class RgsDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
    abstract fun licenseDao(): LicenseDao
    abstract fun apiConfigDao(): ApiConfigDao

    companion object {
        @Volatile
        private var INSTANCE: RgsDatabase? = null

        fun getDatabase(context: Context): RgsDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RgsDatabase::class.java,
                    "rgs_ai_mobile.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
