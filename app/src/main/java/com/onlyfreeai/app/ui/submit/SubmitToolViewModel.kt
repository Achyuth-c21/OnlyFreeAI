package com.onlyfreeai.app.ui.submit

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onlyfreeai.app.data.model.Submission
import com.onlyfreeai.app.data.repository.SubmissionRepository
import com.onlyfreeai.app.data.repository.UserRepository
import com.onlyfreeai.app.util.UrlFetcher
import com.onlyfreeai.app.util.UrlMetadata
import kotlinx.coroutines.launch

class SubmitToolViewModel : ViewModel() {

    private val submissionRepository = SubmissionRepository()
    private val userRepository = UserRepository()

    private val _urlMetadata = MutableLiveData<UrlMetadata?>()
    val urlMetadata: LiveData<UrlMetadata?> = _urlMetadata

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _submitResult = MutableLiveData<Result<String>?>()
    val submitResult: LiveData<Result<String>?> = _submitResult

    private val _canSubmit = MutableLiveData<Boolean>()
    val canSubmit: LiveData<Boolean> = _canSubmit

    private var fetchedLogoUrl: String = ""

    init {
        checkSubmissionLimit()
    }

    private fun checkSubmissionLimit() {
        viewModelScope.launch {
            _canSubmit.value = userRepository.canSubmitToday()
        }
    }

    fun fetchUrlMetadata(url: String) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = UrlFetcher.fetchMetadata(url)
            result.onSuccess { metadata ->
                _urlMetadata.value = metadata
                fetchedLogoUrl = metadata.logoUrl
            }
            result.onFailure {
                _urlMetadata.value = null
            }
            _isLoading.value = false
        }
    }

    fun submitTool(
        url: String,
        name: String,
        description: String,
        category: String,
        whatsFree: List<String>
    ) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val userId = userRepository.getCurrentUserId() ?: throw Exception("Not signed in")

                if (!userRepository.canSubmitToday()) {
                    _submitResult.value = Result.failure(Exception("Daily submission limit reached"))
                    return@launch
                }

                // SECURITY: Validate URL before submission (M-4 fix)
                if (!url.startsWith("https://")) {
                    _submitResult.value = Result.failure(Exception("Only HTTPS URLs are allowed"))
                    return@launch
                }

                val submission = Submission(
                    submittedBy = userId,
                    websiteUrl = url,
                    name = name.take(com.onlyfreeai.app.util.Constants.MAX_TOOL_NAME_LENGTH),
                    description = description.take(com.onlyfreeai.app.util.Constants.MAX_DESCRIPTION_LENGTH),
                    logoUrl = fetchedLogoUrl,
                    category = category,
                    whatsFree = whatsFree.map { it.take(com.onlyfreeai.app.util.Constants.MAX_FREE_ITEM_LENGTH) },
                    status = Submission.STATUS_PENDING
                )

                // SECURITY (H-1 fix): Atomic batch write — creates the submission AND
                // updates the rate-limit counter in a single transaction to prevent
                // race-condition bypass where submission is created without incrementing the counter
                val id = submissionRepository.submitToolAtomic(submission, userId)

                _submitResult.value = Result.success(id)
            } catch (e: Exception) {
                _submitResult.value = Result.failure(e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}
