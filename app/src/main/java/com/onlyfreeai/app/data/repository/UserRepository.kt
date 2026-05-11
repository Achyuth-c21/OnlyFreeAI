package com.onlyfreeai.app.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.onlyfreeai.app.data.model.User
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class UserRepository {

    private val db = FirebaseFirestore.getInstance()
    private val usersRef = db.collection(User.COLLECTION)
    private val auth = FirebaseAuth.getInstance()

    fun getCurrentUserId(): String? = auth.currentUser?.uid

    suspend fun getCurrentUser(): User? {
        val uid = getCurrentUserId() ?: return null
        return usersRef.document(uid)
            .get()
            .await()
            .toObject(User::class.java)
    }

    suspend fun createOrUpdateUser(user: User) {
        usersRef.document(user.id).set(user).await()
    }

    suspend fun isAdmin(): Boolean {
        val user = getCurrentUser()
        return user?.isAdmin == true
    }

    suspend fun saveTool(toolId: String) {
        val uid = getCurrentUserId() ?: return
        usersRef.document(uid)
            .update("savedTools", FieldValue.arrayUnion(toolId))
            .await()
    }

    suspend fun unsaveTool(toolId: String) {
        val uid = getCurrentUserId() ?: return
        usersRef.document(uid)
            .update("savedTools", FieldValue.arrayRemove(toolId))
            .await()
    }

    suspend fun getSavedToolIds(): List<String> {
        val user = getCurrentUser()
        return user?.savedTools ?: emptyList()
    }

    suspend fun isToolSaved(toolId: String): Boolean {
        val savedTools = getSavedToolIds()
        return toolId in savedTools
    }

    suspend fun canSubmitToday(): Boolean {
        val user = getCurrentUser() ?: return false
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

        return if (user.lastSubmissionDate == today) {
            user.submissionsToday < User.MAX_SUBMISSIONS_PER_DAY
        } else {
            true
        }
    }

    suspend fun incrementSubmissionCount() {
        val uid = getCurrentUserId() ?: return
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val user = getCurrentUser() ?: return

        val newCount = if (user.lastSubmissionDate == today) {
            user.submissionsToday + 1
        } else {
            1
        }

        usersRef.document(uid).update(
            mapOf(
                "submissionsToday" to newCount,
                "lastSubmissionDate" to today
            )
        ).await()
    }

    suspend fun addSubmittedTool(toolId: String) {
        val uid = getCurrentUserId() ?: return
        usersRef.document(uid)
            .update("submittedTools", FieldValue.arrayUnion(toolId))
            .await()
    }
}
