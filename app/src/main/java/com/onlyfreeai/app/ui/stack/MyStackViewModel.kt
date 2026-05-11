package com.onlyfreeai.app.ui.stack

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onlyfreeai.app.data.model.Tool
import com.onlyfreeai.app.data.repository.ToolRepository
import com.onlyfreeai.app.data.repository.UserRepository
import kotlinx.coroutines.launch

class MyStackViewModel : ViewModel() {

    private val toolRepository = ToolRepository()
    private val userRepository = UserRepository()

    private val _savedTools = MutableLiveData<List<Tool>>()
    val savedTools: LiveData<List<Tool>> = _savedTools

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun loadSavedTools() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val savedIds = userRepository.getSavedToolIds()
                val tools = savedIds.mapNotNull { toolId ->
                    toolRepository.getToolById(toolId)
                }
                _savedTools.value = tools
            } catch (e: Exception) {
                _savedTools.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }
}
