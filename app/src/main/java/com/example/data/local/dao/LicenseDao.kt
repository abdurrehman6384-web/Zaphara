package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entities.LicenseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LicenseDao {
    @Query("SELECT * FROM license_info WHERE id = 1")
    fun getLicenseInfo(): Flow<LicenseEntity?>

    @Query("SELECT * FROM license_info WHERE id = 1")
    suspend fun getLicenseDirect(): LicenseEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveLicenseInfo(license: LicenseEntity)
}
