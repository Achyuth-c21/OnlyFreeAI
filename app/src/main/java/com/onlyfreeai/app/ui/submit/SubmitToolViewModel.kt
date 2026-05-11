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

                val submission = Submission(
                    submittedBy = userId,
                    websiteUrl = url,
                    name = name,
                    description = description,
                    logoUrl = fetchedLogoUrl,
                    category = category,
                    whatsFree = whatsFree,
                    status = Submission.STATUS_PENDING
                )

                val id = submissionRepository.submitTool(submission)
                userRepository.incrementSubmissionCount()
                userRepository.addSubmittedTool(id)

                _submitResult.value = Result.success(id)
            } catch (e: Exception) {
                _submitResult.value = Result.failure(e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}
