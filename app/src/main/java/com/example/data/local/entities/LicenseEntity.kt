package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "license_info")
data class LicenseEntity(
    @PrimaryKey val id: Int = 1,
    val licenseKey: String,
    val tier: String, // STARTER, PRO, GOD_MODE
    val hwid: String,
    val isValid: Boolean,
    val registeredTo: String = "AJWAD DEVELOPER User",
    val activationTimestamp: Long = System.currentTimeMillis()
)
