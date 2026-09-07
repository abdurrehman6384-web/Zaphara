package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entities.ApiConfigEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ApiConfigDao {
    @Query("SELECT * FROM api_configs ORDER BY priorityOrder ASC")
    fun getAllApiConfigs(): Flow<List<ApiConfigEntity>>

    @Query("SELECT * FROM api_configs ORDER BY priorityOrder ASC")
    suspend fun getAllApiConfigsDirect(): List<ApiConfigEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveApiConfig(config: ApiConfigEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveAllApiConfigs(configs: List<ApiConfigEntity>)
}
