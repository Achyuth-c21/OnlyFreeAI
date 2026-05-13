package com.onlyfreeai.app.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

data class Tool(
    @DocumentId
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val logoUrl: String = "",
    val websiteUrl: String = "",
    val category: String = "",
    val isVerified: Boolean = false,
    val isFree: Boolean = true,
    val status: String = "pending", // live, pending, rejected, removed
    val saves: Int = 0,
    @ServerTimestamp
    val dateAdded: Timestamp? = null,
    val submittedBy: String = "",
    val whatsFree: List<String> = emptyList(),
    val whatsNotFree: List<String> = emptyList(),
    val bestFor: List<String> = emptyList()
) {
    companion object {
        const val COLLECTION = "tools"
        const val STATUS_LIVE = "live"
        const val STATUS_PENDING = "pending"
        const val STATUS_REJECTED = "rejected"
        const val STATUS_REMOVED = "removed"
        const val STATUS_FLAGGED = "flagged"
    }
}
