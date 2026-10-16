package com.chatvault.tiktokarchive.ui.conversation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.chatvault.tiktokarchive.data.entities.ArchivedMessage
import com.chatvault.tiktokarchive.data.entities.Conversation
import com.chatvault.tiktokarchive.data.repository.ArchiveRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class ConversationViewModel(private val repo: ArchiveRepository, private val conversationId: String) : ViewModel() {
    val conversation: StateFlow<Conversation?> =
        repo.conversation(conversationId).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    /** Database-backed paging: only a window of messages is ever held in memory. */
    val messages: Flow<PagingData<ArchivedMessage>> =
        _query
            .debounce { if (it.isEmpty()) 0L else 250L }
            .distinctUntilChanged()
            .flatMapLatest { q -> repo.messages(conversationId, q) }
            .cachedIn(viewModelScope)

    init {
        viewModelScope.launch { repo.markRead(conversationId) }
    }

    fun setQuery(q: String) { _query.value = q }

    fun toggleStar(m: ArchivedMessage) {
        viewModelScope.launch { repo.setStarred(m.localId, !m.isStarred) }
    }
}
