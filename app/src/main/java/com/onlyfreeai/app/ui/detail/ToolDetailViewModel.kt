package com.onlyfreeai.app.ui.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onlyfreeai.app.data.model.Tool
import com.onlyfreeai.app.data.repository.ToolRepository
import com.onlyfreeai.app.data.repository.UserRepository
import com.onlyfreeai.app.util.SingleLiveEvent
import kotlinx.coroutines.launch

class ToolDetailViewModel : ViewModel() {

    private val toolRepository = ToolRepository()
    private val userRepository = UserRepository()

    private val _tool = MutableLiveData<Tool?>()
    val tool: LiveData<Tool?> = _tool

    private val _isSaved = MutableLiveData<Boolean>()
    val isSaved: LiveData<Boolean> = _isSaved

    private val _error = SingleLiveEvent<String>()
    val error: LiveData<String> = _error

    private val _message = SingleLiveEvent<String>()
    val message: LiveData<String> = _message

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private var isTogglingSave = false
    private var isFlagging = false

    fun loadTool(toolId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                _tool.value = toolRepository.getToolById(toolId)
                _isSaved.value = userRepository.isToolSaved(toolId)
            } catch (e: Exception) {
                _tool.value = null
                _error.value = "Failed to load tool details."
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleSave(toolId: String) {
        if (isTogglingSave) return
        isTogglingSave = true
        viewModelScope.launch {
            try {
                if (userRepository.getCurrentUserId() == null) throw Exception("Must be logged in to save tools")
                val currentlySaved = _isSaved.value ?: false
                if (currentlySaved) {
                    userRepository.unsaveTool(toolId)
                    toolRepository.decrementSaves(toolId)
                } else {
                    userRepository.saveTool(toolId)
                    toolRepository.incrementSaves(toolId)
                }
                _isSaved.value = !currentlySaved
                // Reload tool to get updated save count
                _tool.value = toolRepository.getToolById(toolId)
            } catch (e: Exception) {
                _error.value = "Failed to update save status: ${e.message}"
            } finally {
                isTogglingSave = false
            }
        }
    }

    fun flagAsPaid(toolId: String) {
        if (isFlagging) return
        isFlagging = true
        viewModelScope.launch {
            try {
                if (userRepository.getCurrentUserId() == null) throw Exception("Must be logged in to flag tools")
                // In a full implementation, this would create a report
                // For v1, we just mark it for admin review
                toolRepository.updateToolStatus(toolId, Tool.STATUS_FLAGGED)
                _message.value = "Tool successfully flagged for review."
            } catch (e: Exception) {
                _error.value = "Failed to flag tool: ${e.message}"
            } finally {
                isFlagging = false
            }
        }
    }
}
