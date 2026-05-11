package com.onlyfreeai.app.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

data class Submission(
    @DocumentId
    val id: String = "",
    val submittedBy: String = "",
    val websiteUrl: String = "",
    val name: String = "",
    val description: String = "",
    val logoUrl: String = "",
    val category: String = "",
    val whatsFree: List<String> = emptyList(),
    val status: String = "pending", // pending, approved, rejected
    @ServerTimestamp
    val dateSubmitted: Timestamp? = null,
    val reviewedBy: String = "",
    val reviewNote: String = ""
) {
    companion object {
        const val COLLECTION = "submissions"
        const val STATUS_PENDING = "pending"
        const val STATUS_APPROVED = "approved"
        const val STATUS_REJECTED = "rejected"
    }
}
