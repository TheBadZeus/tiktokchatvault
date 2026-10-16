package com.chatvault.tiktokarchive.ui.inbox

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chatvault.tiktokarchive.data.dao.InboxRow
import com.chatvault.tiktokarchive.ui.components.Avatar
import com.chatvault.tiktokarchive.ui.components.CenteredMessage
import com.chatvault.tiktokarchive.ui.components.DemoChip
import com.chatvault.tiktokarchive.ui.components.LoadingView
import com.chatvault.tiktokarchive.ui.components.SearchField
import com.chatvault.tiktokarchive.ui.components.TimeFormat
import com.chatvault.tiktokarchive.ui.components.UnreadBadge

@Composable
fun InboxScreen(vm: InboxViewModel, onOpenConversation: (String) -> Unit, onOpenSettings: () -> Unit) {
    val state by vm.state.collectAsStateWithLifecycle()
    val query by vm.query.collectAsStateWithLifecycle()
    InboxContent(
        state = state,
        query = query,
        onQueryChange = vm::setQuery,
        onOpenConversation = onOpenConversation,
        onOpenSettings = onOpenSettings,
        onRetry = vm::retry,
        onLoadDemo = vm::loadDemo,
    )
}

/** Stateless so it can be tested without a database. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InboxContent(
    state: InboxState,
    query: String,
    onQueryChange: (String) -> Unit,
    onOpenConversation: (String) -> Unit,
    onOpenSettings: () -> Unit,
    onRetry: () -> Unit,
    onLoadDemo: () -> Unit,
) {
    var searching by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (searching) {
                        SearchField(query, onQueryChange, "Search chats and messages")
                    } else {
                        Column {
                            Text("Chat Vault", fontWeight = FontWeight.Bold)
                            Text(
                                "Local archive · not the official TikTok app",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = {
                        if (searching) onQueryChange("")
                        searching = !searching
                    }) {
                        Icon(
                            if (searching) Icons.Filled.Close else Icons.Filled.Search,
                            contentDescription = if (searching) "Close search" else "Search",
                        )
                    }
                    if (!searching) {
                        IconButton(onClick = onOpenSettings) { Icon(Icons.Filled.Settings, contentDescription = "Settings") }
                    }
                },
            )
        },
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when (state) {
                InboxState.Loading -> LoadingView()
                is InboxState.Error -> CenteredMessage(
                    title = "Couldn't open the archive",
                    body = state.message,
                    actionLabel = "Try again",
                    onAction = onRetry,
                )
                is InboxState.Content -> when {
                    state.rows.isEmpty() && query.isNotBlank() ->
                        CenteredMessage("No results", body = "Nothing in the archive matches “$query”.")
                    state.rows.isEmpty() -> CenteredMessage(
                        title = "No archived conversations yet",
                        body = "Messages appear here after you import TikTok's data download or turn on notification capture " +
                            "(both arrive in later milestones). Nothing is captured automatically.",
                        actionLabel = "Load demo data (development only)",
                        onAction = onLoadDemo,
                    )
                    else -> LazyColumn(Modifier.fillMaxSize()) {
                        items(state.rows, key = { it.id }) { row ->
                            ConversationRow(row) { onOpenConversation(row.id) }
                            HorizontalDivider(Modifier.padding(start = 76.dp), color = MaterialTheme.colorScheme.surfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ConversationRow(row: InboxRow, onClick: () -> Unit) {
    val unread = row.unreadCount > 0
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Avatar(row.title)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    row.title,
                    fontWeight = if (unread) FontWeight.Bold else FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                if (row.isDemo) {
                    Spacer(Modifier.width(6.dp))
                    DemoChip()
                }
            }
            Text(
                row.lastMessagePreview ?: "No messages",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(horizontalAlignment = Alignment.End) {
            Text(
                row.lastMessageAt?.let { TimeFormat.inboxTime(it) } ?: "",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (row.hasStarred) {
                    Icon(Icons.Filled.Star, contentDescription = "Has saved messages", tint = Color(0xFFF5A623), modifier = Modifier.size(16.dp))
                }
                if (unread) UnreadBadge(row.unreadCount)
            }
        }
    }
}
