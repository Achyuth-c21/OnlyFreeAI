package com.onlyfreeai.app.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

data class User(
    @DocumentId
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val photoUrl: String = "",
    @ServerTimestamp
    val dateJoined: Timestamp? = null,
    val savedTools: List<String> = emptyList(),
    val submittedTools: List<String> = emptyList(),
    val isAdmin: Boolean = false,
    val submissionsToday: Int = 0,
    val lastSubmissionDate: String = "",
    val isDeleted: Boolean = false,
    val fcmToken: String = ""
) {
    companion object {
        const val COLLECTION = "users"
        const val MAX_SUBMISSIONS_PER_DAY = com.onlyfreeai.app.util.Constants.MAX_SUBMISSIONS_PER_DAY
    }
}
