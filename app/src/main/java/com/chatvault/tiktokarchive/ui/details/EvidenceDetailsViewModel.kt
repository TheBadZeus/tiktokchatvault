package com.chatvault.tiktokarchive.ui.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chatvault.tiktokarchive.data.entities.ArchivedMessage
import com.chatvault.tiktokarchive.data.entities.MessageAttachment
import com.chatvault.tiktokarchive.data.repository.ArchiveRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DetailsState(
    val loaded: Boolean = false,
    val message: ArchivedMessage? = null,
    val attachments: List<MessageAttachment> = emptyList(),
    val verifying: Boolean = false,
)

class EvidenceDetailsViewModel(private val repo: ArchiveRepository, private val localId: String) : ViewModel() {
    private val _state = MutableStateFlow(DetailsState())
    val state: StateFlow<DetailsState> = _state.asStateFlow()

    init { reload() }

    private fun reload() {
        viewModelScope.launch {
            val m = repo.message(localId)
            val a = repo.attachments(localId)
            _state.update { it.copy(loaded = true, message = m, attachments = a, verifying = false) }
        }
    }

    fun verify() {
        viewModelScope.launch {
            _state.update { it.copy(verifying = true) }
            repo.verifyMessage(localId)
            reload()
        }
    }

    fun toggleStar() {
        val m = _state.value.message ?: return
        viewModelScope.launch {
            repo.setStarred(m.localId, !m.isStarred)
            reload()
        }
    }
}
