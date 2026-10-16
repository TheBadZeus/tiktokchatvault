package com.chatvault.tiktokarchive.ui.settings

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chatvault.tiktokarchive.domain.model.ThemeMode
import com.chatvault.tiktokarchive.ui.components.TimeFormat
import com.chatvault.tiktokarchive.ui.components.formatBytes

private fun notificationAccessGranted(context: Context): Boolean =
    NotificationManagerCompat.getEnabledListenerPackages(context).contains(context.packageName)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(vm: SettingsViewModel, onBack: () -> Unit) {
    val state by vm.state.collectAsStateWithLifecycle()
    val notice by vm.notice.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val context = LocalContext.current
    var notificationAccess by remember { mutableStateOf(notificationAccessGranted(context)) }
    var confirmDeleteAll by remember { mutableStateOf(false) }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { notificationAccess = notificationAccessGranted(context) }
    LaunchedEffect(notice) {
        notice?.let {
            snackbar.showSnackbar(it)
            vm.consumeNotice()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                },
            )
        },
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState())) {

            Section("Appearance") {
                ThemeMode.entries.forEach { mode ->
                    Row(
                        Modifier.fillMaxWidth()
                            .selectable(selected = state.themeMode == mode, role = Role.RadioButton, onClick = { vm.setTheme(mode) })
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected = state.themeMode == mode, onClick = null)
                        Spacer(Modifier.width(12.dp))
                        Text(
                            when (mode) {
                                ThemeMode.SYSTEM -> "Follow system"
                                ThemeMode.LIGHT -> "Light"
                                ThemeMode.DARK -> "Dark"
                            },
                        )
                    }
                }
            }

            Section("Archive & storage") {
                InfoRow("Conversations", state.conversationCount.toString())
                InfoRow("Archived messages", state.messageCount.toString())
                InfoRow("Database size on this phone", formatBytes(state.databaseBytes))
            }

            Section("Capture permissions") {
                InfoRow(
                    "Notification access",
                    if (notificationAccess) "Granted in Android settings" else "Not granted",
                    "Notification capture is not built yet (Milestone 4). Nothing is being captured, whatever this says.",
                )
                InfoRow(
                    "Official TikTok API",
                    "Not connected",
                    "Not yet investigated. No endpoints or permissions are assumed.",
                )
                InfoRow("TikTok data download import", "Not built yet", "Planned for Milestone 3.")
            }

            Section("Security") {
                SwitchRow("App lock (biometric / device credential)", "Planned for Milestone 5")
                InfoRow(
                    "Database encryption",
                    "Not enabled yet (Milestone 5)",
                    "Today the database is plain SQLite inside this app's private storage, protected by Android's app sandbox " +
                        "and your phone's screen lock, but not encrypted by this app.",
                )
            }

            Section("Export, import & backup") {
                InfoRow("Export (JSON, CSV, HTML, ZIP with manifest)", "Planned for Milestone 5")
                InfoRow("Import", "Planned for Milestone 3")
                InfoRow(
                    "Backup status",
                    "No backup exists",
                    "Android cloud backup and device-to-device transfer are switched off for this app so messages are never " +
                        "uploaded automatically. If you uninstall the app or clear its data, the archive is gone.",
                )
            }

            Section("Privacy") {
                Text(
                    "• This build has no internet permission and cannot send data anywhere.\n" +
                        "• Messages are stored only on this phone.\n" +
                        "• No analytics, ads or tracking.\n" +
                        "• This is an independent app. It is not made by or connected to TikTok.\n" +
                        "• A hash check detects accidental changes to the app's own records. It does not prove a message is genuine.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            Section("Development tools") {
                Text(
                    "Demo data is invented and always labelled DEMO. It never mixes with real captures.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OutlinedButton(onClick = vm::loadDemo, modifier = Modifier.fillMaxWidth()) { Text("Load demo data") }
                OutlinedButton(onClick = vm::tamperTest, modifier = Modifier.fillMaxWidth()) { Text("Tamper test: edit a demo message") }
                OutlinedButton(onClick = vm::removeDemo, modifier = Modifier.fillMaxWidth()) { Text("Remove demo data only") }
            }

            Section("Data deletion") {
                OutlinedButton(onClick = { confirmDeleteAll = true }, modifier = Modifier.fillMaxWidth()) {
                    Text("Delete ALL archived data", color = MaterialTheme.colorScheme.error)
                }
            }

            Section("Activity log (newest first)") {
                if (state.events.isEmpty()) {
                    Text("Nothing recorded yet.", style = MaterialTheme.typography.bodyMedium)
                }
                state.events.forEach { e ->
                    Column(Modifier.padding(vertical = 4.dp)) {
                        Text(
                            "${TimeFormat.compact(e.timestamp)} · ${e.type.name}" + if (e.success) "" else " · check failed",
                            style = MaterialTheme.typography.labelMedium,
                        )
                        e.detail?.let {
                            Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
            Spacer(Modifier.padding(16.dp))
        }
    }

    if (confirmDeleteAll) {
        AlertDialog(
            onDismissRequest = { confirmDeleteAll = false },
            title = { Text("Delete all archived data?") },
            text = {
                Text(
                    "This permanently removes every conversation, message, attachment record and the activity log from this phone. " +
                        "It cannot be undone, and there is no backup.",
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    confirmDeleteAll = false
                    vm.deleteAll()
                }) { Text("Delete everything", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { confirmDeleteAll = false }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        content()
        HorizontalDivider(Modifier.padding(top = 6.dp))
    }
}

@Composable
private fun InfoRow(title: String, value: String? = null, note: String? = null) {
    Column {
        Text(title, style = MaterialTheme.typography.bodyLarge)
        if (value != null) Text(value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (note != null) Text(note, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun SwitchRow(title: String, note: String) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(note, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = false, onCheckedChange = null, enabled = false)
    }
}
