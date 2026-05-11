package com.onlyfreeai.app.ui.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onlyfreeai.app.data.model.Tool
import com.onlyfreeai.app.data.repository.ToolRepository
import com.onlyfreeai.app.data.repository.UserRepository
import kotlinx.coroutines.launch

class ToolDetailViewModel : ViewModel() {

    private val toolRepository = ToolRepository()
    private val userRepository = UserRepository()

    private val _tool = MutableLiveData<Tool?>()
    val tool: LiveData<Tool?> = _tool

    private val _isSaved = MutableLiveData<Boolean>()
    val isSaved: LiveData<Boolean> = _isSaved

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private var isTogglingSave = false
    private var isFlagging = false

    fun loadTool(toolId: String) {
        viewModelScope.launch {
            try {
                _tool.value = toolRepository.getToolById(toolId)
                _isSaved.value = userRepository.isToolSaved(toolId)
            } catch (e: Exception) {
                _tool.value = null
            }
        }
    }

    fun toggleSave(toolId: String) {
        if (isTogglingSave) return
        isTogglingSave = true
        viewModelScope.launch {
            try {
                val userId = userRepository.getCurrentUserId() ?: throw Exception("Must be logged in to save tools")
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
                val userId = userRepository.getCurrentUserId() ?: throw Exception("Must be logged in to flag tools")
                // In a full implementation, this would create a report
                // For v1, we just mark it for admin review
                toolRepository.updateToolStatus(toolId, "flagged")
                _error.value = "Tool successfully flagged for review."
            } catch (e: Exception) {
                _error.value = "Failed to flag tool: ${e.message}"
            } finally {
                isFlagging = false
            }
        }
    }
}
