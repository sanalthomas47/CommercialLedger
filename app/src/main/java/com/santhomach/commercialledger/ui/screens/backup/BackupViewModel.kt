package com.santhomach.commercialledger.ui.screens.backup

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.santhomach.commercialledger.data.repository.LedgerRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BackupViewModel(private val repository: LedgerRepository) : ViewModel() {

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun clearMessage() { _message.value = null }

    fun exportTo(resolver: ContentResolver, uri: Uri) {
        if (_isProcessing.value) return
        viewModelScope.launch {
            _isProcessing.value = true
            runCatching {
                val json = repository.exportBackup()
                withContext(Dispatchers.IO) {
                    resolver.openOutputStream(uri, "wt")?.use { it.write(json.toByteArray(Charsets.UTF_8)) }
                        ?: error("Could not open file for writing")
                }
            }.onSuccess {
                _message.value = "Backup saved"
            }.onFailure { e ->
                _message.value = "Export failed: ${e.message ?: "unknown error"}"
            }
            _isProcessing.value = false
        }
    }

    fun importFrom(resolver: ContentResolver, uri: Uri) {
        if (_isProcessing.value) return
        viewModelScope.launch {
            _isProcessing.value = true
            runCatching {
                withContext(Dispatchers.IO) {
                    resolver.openInputStream(uri)?.use { it.readBytes().toString(Charsets.UTF_8) }
                        ?: error("Could not open file")
                }
            }.mapCatching { json ->
                repository.importFromBackup(json).getOrThrow()
            }.onSuccess {
                _message.value = "Data restored from backup"
            }.onFailure { e ->
                _message.value = "Restore failed: ${e.message ?: "invalid backup file"}"
            }
            _isProcessing.value = false
        }
    }

    companion object {
        fun factory(repository: LedgerRepository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                BackupViewModel(repository) as T
        }
    }
}
