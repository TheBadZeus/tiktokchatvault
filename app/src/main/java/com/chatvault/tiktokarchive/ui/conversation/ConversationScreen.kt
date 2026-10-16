package com.chatvault.tiktokarchive.ui.conversation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.chatvault.tiktokarchive.data.entities.ArchivedMessage
import com.chatvault.tiktokarchive.domain.model.Direction
import com.chatvault.tiktokarchive.domain.model.MessageType
import com.chatvault.tiktokarchive.ui.components.Avatar
import com.chatvault.tiktokarchive.ui.components.CenteredMessage
import com.chatvault.tiktokarchive.ui.components.LoadingView
import com.chatvault.tiktokarchive.ui.components.SearchField
import com.chatvault.tiktokarchive.ui.components.TimeFormat
import com.chatvault.tiktokarchive.ui.components.VerificationIcon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationScreen(vm: ConversationViewModel, onBack: () -> Unit, onOpenDetails: (String) -> Unit) {
    val conversation by vm.conversation.collectAsStateWithLifecycle()
    val query by vm.query.collectAsStateWithLifecycle()
    val items = vm.messages.collectAsLazyPagingItems()
    var searching by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                },
                title = {
                    if (searching) {
                        SearchField(query, vm::setQuery, "Search in this chat")
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Avatar(conversation?.title ?: "?", size = 36.dp)
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text(
                                    conversation?.title ?: "…",
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text(
                                    if (conversation?.isDemo == true) "Demo data" else "Archived copy",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                },
                actions = {
                    IconButton(onClick = {
                        if (searching) vm.setQuery("")
                        searching = !searching
                    }) {
                        Icon(
                            if (searching) Icons.Filled.Close else Icons.Filled.Search,
                            contentDescription = if (searching) "Close search" else "Search in chat",
                        )
                    }
                },
            )
        },
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            Surface(color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Archived copy kept by this app. Tap a message for capture details; press and hold to save it.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                )
            }
            Box(Modifier.weight(1f).fillMaxWidth()) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    reverseLayout = true, // newest at the bottom; older pages load as you scroll up
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    items(count = items.itemCount, key = items.itemKey { it.localId }) { index ->
                        val msg = items[index]
                        if (msg != null) {
                            val older = if (index + 1 < items.itemCount) items.peek(index + 1) else null
                            val showDate = older == null || !TimeFormat.sameDay(older.sortTimestamp, msg.sortTimestamp)
                            Column {
                                if (showDate) DateSeparator(TimeFormat.dayLabel(msg.sortTimestamp))
                                MessageBubble(
                                    msg = msg,
                                    onClick = { onOpenDetails(msg.localId) },
                                    onLongClick = { vm.toggleStar(msg) },
                                )
                            }
                        }
                    }
                }

                val refresh = items.loadState.refresh
                when {
                    refresh is LoadState.Loading && items.itemCount == 0 -> LoadingView()
                    refresh is LoadState.Error -> CenteredMessage(
                        title = "Couldn't load messages",
                        body = refresh.error.message,
                        actionLabel = "Try again",
                        onAction = { items.retry() },
                    )
                    refresh is LoadState.NotLoading && items.itemCount == 0 -> CenteredMessage(
                        title = if (query.isBlank()) "No messages in this conversation" else "No matches",
                        body = if (query.isBlank()) null else "No message contains “$query”.",
                    )
                }
            }
        }
    }
}

@Composable
private fun DateSeparator(label: String) {
    Box(Modifier.fillMaxWidth().padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MessageBubble(msg: ArchivedMessage, onClick: () -> Unit, onLongClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val outgoing = msg.direction == Direction.OUTGOING
    val bubbleColor = if (outgoing) scheme.primary else scheme.surfaceVariant
    val textColor = if (outgoing) scheme.onPrimary else scheme.onSurface
    val shape = RoundedCornerShape(18.dp)

    Column(Modifier.fillMaxWidth(), horizontalAlignment = if (outgoing) Alignment.End else Alignment.Start) {
        Surface(
            color = bubbleColor,
            shape = shape,
            modifier = Modifier.widthIn(max = 300.dp).clip(shape).combinedClickable(onClick = onClick, onLongClick = onLongClick),
        ) {
            Column(Modifier.padding(horizontal = 14.dp, vertical = 9.dp)) {
                if (msg.messageType != MessageType.TEXT) {
                    // Attachment bytes are only shown if they were actually captured; here we show a placeholder.
                    Text(
                        "[${msg.messageType.name.lowercase().replaceFirstChar { it.uppercase() }} placeholder · tap for details]",
                        color = textColor,
                        fontStyle = FontStyle.Italic,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                msg.content?.let { if (it.isNotBlank()) Text(it, color = textColor) }
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            // Never present a capture time as if it were the original send time.
            val timeLabel = msg.originalTimestamp?.let { TimeFormat.clock(it) } ?: "captured ${TimeFormat.clock(msg.captureTimestamp)}"
            Text(timeLabel, style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant)
            VerificationIcon(msg.verificationStatus)
            if (msg.isStarred) {
                Icon(Icons.Filled.Star, contentDescription = "Saved", tint = Color(0xFFF5A623), modifier = Modifier.size(14.dp))
            }
        }
    }
}
