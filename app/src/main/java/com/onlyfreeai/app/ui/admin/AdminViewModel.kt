package com.onlyfreeai.app.ui.admin

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.onlyfreeai.app.data.model.Submission
import com.onlyfreeai.app.data.model.Tool
import com.onlyfreeai.app.data.repository.SubmissionRepository
import com.onlyfreeai.app.data.repository.ToolRepository
import com.onlyfreeai.app.util.SingleLiveEvent
import kotlinx.coroutines.launch

class AdminViewModel : ViewModel() {

    private val submissionRepository = SubmissionRepository()
    private val toolRepository = ToolRepository()
    private val auth = FirebaseAuth.getInstance()

    private val _submissions = MutableLiveData<List<Submission>>()
    val submissions: LiveData<List<Submission>> = _submissions

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun loadPendingSubmissions() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                _submissions.value = submissionRepository.getPendingSubmissions()
            } catch (e: Exception) {
                _submissions.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadApprovedSubmissions() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                _submissions.value = submissionRepository.getApprovedSubmissions()
            } catch (e: Exception) {
                _submissions.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadRejectedSubmissions() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                _submissions.value = submissionRepository.getRejectedSubmissions()
            } catch (e: Exception) {
                _submissions.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    private val _error = SingleLiveEvent<String>()
    val error: LiveData<String> = _error

    fun approveSubmission(submission: Submission) {
        viewModelScope.launch {
            try {
                val adminId = auth.currentUser?.uid ?: throw IllegalStateException("Not logged in")

                // Approve the submission
                submissionRepository.approveSubmission(submission.id, adminId)

                // Create a live tool from the submission
                val tool = Tool(
                    name = submission.name,
                    description = submission.description,
                    logoUrl = submission.logoUrl,
                    websiteUrl = submission.websiteUrl,
                    category = submission.category,
                    isVerified = true,
                    isFree = true,
                    status = Tool.STATUS_LIVE,
                    submittedBy = submission.submittedBy,
                    whatsFree = submission.whatsFree
                )
                toolRepository.createToolFromSubmission(tool)

                // Reload list
                loadPendingSubmissions()
            } catch (e: Exception) {
                _error.value = "Failed to approve: ${e.message}"
            }
        }
    }

    fun rejectSubmission(submissionId: String, reason: String) {
        viewModelScope.launch {
            try {
                val adminId = auth.currentUser?.uid ?: throw IllegalStateException("Not logged in")
                submissionRepository.rejectSubmission(submissionId, adminId, reason)
                loadPendingSubmissions()
            } catch (e: Exception) {
                _error.value = "Failed to reject: ${e.message}"
            }
        }
    }
}
