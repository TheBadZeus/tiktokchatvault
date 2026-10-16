package com.chatvault.tiktokarchive.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chatvault.tiktokarchive.data.entities.CaptureEvent
import com.chatvault.tiktokarchive.data.prefs.SettingsStore
import com.chatvault.tiktokarchive.data.repository.ArchiveRepository
import com.chatvault.tiktokarchive.domain.model.ThemeMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val messageCount: Int = 0,
    val conversationCount: Int = 0,
    val databaseBytes: Long = 0,
    val events: List<CaptureEvent> = emptyList(),
)

class SettingsViewModel(
    private val repo: ArchiveRepository,
    private val store: SettingsStore,
    private val databaseSize: () -> Long,
) : ViewModel() {

    val state: StateFlow<SettingsState> =
        combine(store.themeMode, repo.messageCount(), repo.conversationCount(), repo.recentEvents(20)) { theme, msgs, convs, events ->
            SettingsState(theme, msgs, convs, databaseSize(), events)
        }
            .flowOn(Dispatchers.IO)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsState())

    private val _notice = MutableStateFlow<String?>(null)
    val notice: StateFlow<String?> = _notice.asStateFlow()

    fun consumeNotice() { _notice.value = null }

    fun setTheme(mode: ThemeMode) { viewModelScope.launch { store.setThemeMode(mode) } }

    fun loadDemo() {
        viewModelScope.launch {
            val n = repo.loadDemoData()
            _notice.value = if (n > 0) "Loaded $n demo messages" else "Demo data was already loaded"
        }
    }

    fun removeDemo() {
        viewModelScope.launch {
            repo.deleteDemoData()
            _notice.value = "Demo data removed"
        }
    }

    fun tamperTest() {
        viewModelScope.launch {
            _notice.value = if (repo.tamperWithDemoMessage()) {
                "Edited the 2nd-oldest message in maya.demo without updating its hash. Open it and tap Verify now."
            } else {
                "Load the demo data first"
            }
        }
    }

    fun deleteAll() {
        viewModelScope.launch {
            repo.deleteAllData()
            _notice.value = "All archived data deleted"
        }
    }
}
