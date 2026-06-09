package com.onlyfreeai.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.onlyfreeai.app.data.model.Submission
import kotlinx.coroutines.tasks.await

class SubmissionRepository {

    private val db = FirebaseFirestore.getInstance()
    private val submissionsRef = db.collection(Submission.COLLECTION)

    suspend fun submitTool(submission: Submission): String {
        val docRef = submissionsRef.add(submission).await()
        return docRef.id
    }

    suspend fun submitToolAtomic(submission: Submission, userId: String): String {
        return db.runTransaction { transaction ->
            val userDocRef = db.collection(com.onlyfreeai.app.data.model.User.COLLECTION).document(userId)
            val userSnapshot = transaction.get(userDocRef)
            
            val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
            val submissionsToday = userSnapshot.getLong("submissionsToday") ?: 0
            val lastSubmissionDate = userSnapshot.getString("lastSubmissionDate") ?: ""
            
            val newCount = if (lastSubmissionDate == today) {
                if (submissionsToday >= com.onlyfreeai.app.data.model.User.MAX_SUBMISSIONS_PER_DAY) {
                    throw Exception("Daily submission limit reached")
                }
                submissionsToday + 1
            } else {
                1
            }
            
            val submissionDocRef = submissionsRef.document()
            transaction.set(submissionDocRef, submission)
            transaction.update(userDocRef, mapOf(
                "submissionsToday" to newCount,
                "lastSubmissionDate" to today
            ))
            submissionDocRef.id
        }.await()
    }

    suspend fun getPendingSubmissions(): List<Submission> {
        return submissionsRef
            .whereEqualTo("status", Submission.STATUS_PENDING)
            .orderBy("dateSubmitted", Query.Direction.DESCENDING)
            .get()
            .await()
            .toObjects(Submission::class.java)
    }

    suspend fun getApprovedSubmissions(): List<Submission> {
        return submissionsRef
            .whereEqualTo("status", Submission.STATUS_APPROVED)
            .orderBy("dateSubmitted", Query.Direction.DESCENDING)
            .get()
            .await()
            .toObjects(Submission::class.java)
    }

    suspend fun getRejectedSubmissions(): List<Submission> {
        return submissionsRef
            .whereEqualTo("status", Submission.STATUS_REJECTED)
            .orderBy("dateSubmitted", Query.Direction.DESCENDING)
            .get()
            .await()
            .toObjects(Submission::class.java)
    }

    suspend fun getUserSubmissions(userId: String): List<Submission> {
        return submissionsRef
            .whereEqualTo("submittedBy", userId)
            .orderBy("dateSubmitted", Query.Direction.DESCENDING)
            .get()
            .await()
            .toObjects(Submission::class.java)
    }

    suspend fun approveSubmission(submissionId: String, adminId: String) {
        submissionsRef.document(submissionId)
            .update(
                mapOf(
                    "status" to Submission.STATUS_APPROVED,
                    "reviewedBy" to adminId
                )
            )
            .await()
    }

    suspend fun rejectSubmission(submissionId: String, adminId: String, reason: String) {
        submissionsRef.document(submissionId)
            .update(
                mapOf(
                    "status" to Submission.STATUS_REJECTED,
                    "reviewedBy" to adminId,
                    "reviewNote" to reason
                )
            )
            .await()
    }
}
