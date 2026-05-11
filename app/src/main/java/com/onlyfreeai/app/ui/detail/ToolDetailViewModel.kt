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
        viewModelScope.launch {
            try {
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
                // Handle error
            }
        }
    }

    fun flagAsPaid(toolId: String) {
        viewModelScope.launch {
            try {
                // In a full implementation, this would create a report
                // For v1, we just mark it for admin review
                toolRepository.updateToolStatus(toolId, "flagged")
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}
