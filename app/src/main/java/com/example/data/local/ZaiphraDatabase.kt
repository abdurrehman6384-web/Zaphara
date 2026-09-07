package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [MemoryEntity::class, PreferenceEntity::class, InteractionEntity::class],
    version = 1,
    exportSchema = false
)
abstract class ZaiphraDatabase : RoomDatabase() {
    abstract fun zaiphraDao(): ZaiphraDao

    companion object {
        @Volatile
        private var INSTANCE: ZaiphraDatabase? = null

        fun getDatabase(context: Context): ZaiphraDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ZaiphraDatabase::class.java,
                    "zaiphra_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
