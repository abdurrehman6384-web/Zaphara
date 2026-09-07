package com.rgs.companion.companion

/**
 * Long-term memory store interface for companion facts.
 */
interface MemoryStore {
    suspend fun allFacts(): List<String>
}
