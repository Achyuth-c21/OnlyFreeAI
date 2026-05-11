package com.onlyfreeai.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.onlyfreeai.app.data.model.Tool
import kotlinx.coroutines.tasks.await

class ToolRepository {

    private val db = FirebaseFirestore.getInstance()
    private val toolsRef = db.collection(Tool.COLLECTION)

    suspend fun getLiveTools(): List<Tool> {
        return toolsRef
            .whereEqualTo("status", Tool.STATUS_LIVE)
            .orderBy("dateAdded", Query.Direction.DESCENDING)
            .get()
            .await()
            .toObjects(Tool::class.java)
    }

    suspend fun getToolsByCategory(category: String): List<Tool> {
        return toolsRef
            .whereEqualTo("status", Tool.STATUS_LIVE)
            .whereEqualTo("category", category)
            .orderBy("dateAdded", Query.Direction.DESCENDING)
            .get()
            .await()
            .toObjects(Tool::class.java)
    }

    suspend fun searchTools(query: String): List<Tool> {
        // Firestore doesn't support full-text search natively
        // We fetch all live tools and filter client-side for v1
        val allTools = getLiveTools()
        val lowerQuery = query.lowercase()
        return allTools.filter {
            it.name.lowercase().contains(lowerQuery) ||
            it.description.lowercase().contains(lowerQuery) ||
            it.category.lowercase().contains(lowerQuery)
        }
    }

    suspend fun getToolById(toolId: String): Tool? {
        return toolsRef.document(toolId)
            .get()
            .await()
            .toObject(Tool::class.java)
    }

    suspend fun incrementSaves(toolId: String) {
        val ref = toolsRef.document(toolId)
        db.runTransaction { transaction ->
            val snapshot = transaction.get(ref)
            val currentSaves = snapshot.getLong("saves") ?: 0
            transaction.update(ref, "saves", currentSaves + 1)
        }.await()
    }

    suspend fun decrementSaves(toolId: String) {
        val ref = toolsRef.document(toolId)
        db.runTransaction { transaction ->
            val snapshot = transaction.get(ref)
            val currentSaves = snapshot.getLong("saves") ?: 0
            if (currentSaves > 0) {
                transaction.update(ref, "saves", currentSaves - 1)
            }
        }.await()
    }

    suspend fun createToolFromSubmission(tool: Tool): String {
        val docRef = toolsRef.add(tool).await()
        return docRef.id
    }

    suspend fun updateToolStatus(toolId: String, status: String) {
        toolsRef.document(toolId)
            .update("status", status)
            .await()
    }

    suspend fun getCategories(): List<String> {
        val tools = getLiveTools()
        return tools.map { it.category }.distinct().sorted()
    }

    suspend fun getTrendingTools(limit: Int = 10): List<Tool> {
        return toolsRef
            .whereEqualTo("status", Tool.STATUS_LIVE)
            .orderBy("saves", Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .get()
            .await()
            .toObjects(Tool::class.java)
    }
}
