package com.chatvault.tiktokarchive.ui.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chatvault.tiktokarchive.data.entities.ArchivedMessage
import com.chatvault.tiktokarchive.domain.model.CaptureMethod
import com.chatvault.tiktokarchive.domain.model.VerificationStatus
import com.chatvault.tiktokarchive.ui.components.CenteredMessage
import com.chatvault.tiktokarchive.ui.components.LoadingView
import com.chatvault.tiktokarchive.ui.components.TimeFormat
import com.chatvault.tiktokarchive.ui.components.VerificationIcon
import com.chatvault.tiktokarchive.ui.components.label

private const val NOT_PROVIDED = "Not provided by the source"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EvidenceDetailsScreen(vm: EvidenceDetailsViewModel, onBack: () -> Unit) {
    val state by vm.state.collectAsStateWithLifecycle()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Evidence details") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                },
            )
        },
    ) { padding ->
        val msg = state.message
        when {
            !state.loaded -> LoadingView(Modifier.padding(padding))
            msg == null -> CenteredMessage("This record no longer exists", Modifier.padding(padding))
            else -> Column(
                Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (msg.captureMethod == CaptureMethod.DEMO) {
                    Notice("This is invented demo data, not captured evidence.")
                }
                Section("Message") {
                    Field("Content", msg.content ?: "No text content")
                    Field("Type", msg.messageType.label())
                    Field("Direction", msg.direction.label())
                }
                Section("Identifiers") {
                    Field("Conversation ID", msg.conversationId)
                    Field("Sender ID", msg.senderId ?: NOT_PROVIDED)
                    Field("Sender username", msg.senderUsername ?: NOT_PROVIDED)
                    Field("Source message ID", msg.sourceMessageId ?: NOT_PROVIDED)
                    Field("Local record ID", msg.localId)
                }
                Section("Timestamps") {
                    Field("Original time (reported by the source)", msg.originalTimestamp?.let { TimeFormat.full(it) } ?: NOT_PROVIDED)
                    Field("Captured by this app", TimeFormat.full(msg.captureTimestamp))
                }
                Section("Capture") {
                    Field("Method", msg.captureMethod.label())
                    Field("Source", msg.captureSource)
                }
                IntegritySection(msg, state.verifying, onVerify = vm::verify)
                Section("Attachments") {
                    if (state.attachments.isEmpty()) {
                        Text("No attachment records for this message.", style = MaterialTheme.typography.bodyMedium)
                    } else {
                        state.attachments.forEach { a ->
                            Field(
                                a.kind.label() + (a.fileName?.let { " · $it" } ?: ""),
                                a.status.label() + (a.sha256?.let { "\nSHA-256: $it" } ?: ""),
                            )
                        }
                    }
                }
                OutlinedButton(onClick = vm::toggleStar, modifier = Modifier.fillMaxWidth()) {
                    Text(if (msg.isStarred) "Remove from saved" else "Save this message")
                }
            }
        }
    }
}

@Composable
private fun IntegritySection(msg: ArchivedMessage, verifying: Boolean, onVerify: () -> Unit) {
    Section("Integrity") {
        Row(verticalAlignment = Alignment.CenterVertically) {
            VerificationIcon(msg.verificationStatus, size = 20.dp)
            Spacer(Modifier.width(8.dp))
            Text(msg.verificationStatus.label(), fontWeight = FontWeight.SemiBold)
        }
        if (msg.verificationStatus == VerificationStatus.MODIFIED) {
            Text(
                "The stored content no longer matches the hash recorded when it was saved.",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        Field("Last checked", msg.lastVerifiedAt?.let { TimeFormat.full(it) } ?: "Never")
        Field("SHA-256 recorded at capture", msg.integrityHash, mono = true)
        Button(onClick = onVerify, enabled = !verifying) { Text(if (verifying) "Checking…" else "Verify now") }
        Text(
            "A matching hash only shows this record is unchanged since this app saved it. It does not prove the message " +
                "is authentic or who sent it, and it does not by itself make a record admissible as evidence.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun Notice(text: String) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.16f))) {
        Text(text, modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
            HorizontalDivider()
            content()
        }
    }
}

@Composable
private fun Field(label: String, value: String, mono: Boolean = false) {
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        SelectionContainer {
            Text(
                value,
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = if (mono) FontFamily.Monospace else null,
                fontSize = if (mono) 12.sp else MaterialTheme.typography.bodyMedium.fontSize,
            )
        }
    }
}
