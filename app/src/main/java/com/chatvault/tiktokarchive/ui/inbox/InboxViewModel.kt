package com.chatvault.tiktokarchive.ui.inbox

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chatvault.tiktokarchive.data.dao.InboxRow
import com.chatvault.tiktokarchive.data.repository.ArchiveRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface InboxState {
    data object Loading : InboxState
    data class Error(val message: String) : InboxState
    data class Content(val rows: List<InboxRow>) : InboxState
}

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class InboxViewModel(private val repo: ArchiveRepository) : ViewModel() {
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()
    private val retryTick = MutableStateFlow(0)

    val state: StateFlow<InboxState> =
        combine(_query.debounce { if (it.isEmpty()) 0L else 250L }, retryTick) { q, _ -> q }
            .flatMapLatest { q ->
                repo.inbox(q)
                    .map<List<InboxRow>, InboxState> { InboxState.Content(it) }
                    .catch { emit(InboxState.Error(it.message ?: "Could not read the archive")) }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), InboxState.Loading)

    fun setQuery(q: String) { _query.value = q }
    fun retry() { retryTick.value += 1 }
    fun loadDemo() { viewModelScope.launch { repo.loadDemoData() } }
}
