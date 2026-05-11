package com.onlyfreeai.app.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onlyfreeai.app.data.model.Tool
import com.onlyfreeai.app.data.repository.ToolRepository
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val toolRepository = ToolRepository()

    private val _tools = MutableLiveData<List<Tool>>()
    val tools: LiveData<List<Tool>> = _tools

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun loadTools() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val result = toolRepository.getLiveTools()
                _tools.value = result
            } catch (e: Exception) {
                _tools.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun searchTools(query: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val result = toolRepository.searchTools(query)
                _tools.value = result
            } catch (e: Exception) {
                _tools.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun filterByCategory(category: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val result = toolRepository.getToolsByCategory(category)
                _tools.value = result
            } catch (e: Exception) {
                _tools.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }
}
