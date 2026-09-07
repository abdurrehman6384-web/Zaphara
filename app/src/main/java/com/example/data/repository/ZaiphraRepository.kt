package com.example.data.repository

import com.example.data.local.InteractionEntity
import com.example.data.local.MemoryEntity
import com.example.data.local.PreferenceEntity
import com.example.data.local.ZaiphraDao
import kotlinx.coroutines.flow.Flow

class ZaiphraRepository(private val dao: ZaiphraDao) {
    val allMemories: Flow<List<MemoryEntity>> = dao.getAllMemories()
    fun getMemoriesByCategory(category: String) = dao.getMemoriesByCategory(category)
    suspend fun insertMemory(memory: MemoryEntity) = dao.insertMemory(memory)
    suspend fun deleteMemory(id: Int) = dao.deleteMemoryById(id)

    val allPreferences: Flow<List<PreferenceEntity>> = dao.getAllPreferences()
    suspend fun getPreference(key: String) = dao.getPreference(key)
    suspend fun setPreference(key: String, value: String) {
        dao.insertPreference(PreferenceEntity(key = key, value = value))
    }

    val interactionHistory: Flow<List<InteractionEntity>> = dao.getInteractionHistory()
    suspend fun insertInteraction(interaction: InteractionEntity) = dao.insertInteraction(interaction)
}
