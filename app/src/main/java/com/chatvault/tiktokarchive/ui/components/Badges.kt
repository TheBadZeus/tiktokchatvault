package com.chatvault.tiktokarchive.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chatvault.tiktokarchive.domain.model.VerificationStatus

@Composable
fun DemoChip() {
    val c = MaterialTheme.colorScheme.secondary
    Surface(color = c.copy(alpha = 0.16f), shape = RoundedCornerShape(6.dp)) {
        Text("DEMO", style = MaterialTheme.typography.labelSmall, color = c, modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp))
    }
}

@Composable
fun UnreadBadge(count: Int) {
    Box(
        Modifier
            .defaultMinSize(minWidth = 20.dp, minHeight = 20.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary)
            .padding(horizontal = 5.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            if (count > 99) "99+" else count.toString(),
            color = MaterialTheme.colorScheme.onPrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

/** Tick = hash re-checked and matched. Warning = hash re-checked and did NOT match. Info = not checked yet. */
@Composable
fun VerificationIcon(status: VerificationStatus, size: Dp = 14.dp) {
    val scheme = MaterialTheme.colorScheme
    when (status) {
        VerificationStatus.INTACT ->
            Icon(Icons.Filled.CheckCircle, "Integrity check passed", tint = scheme.secondary, modifier = Modifier.size(size))
        VerificationStatus.MODIFIED ->
            Icon(Icons.Filled.Warning, "Record changed since capture", tint = scheme.error, modifier = Modifier.size(size))
        VerificationStatus.UNVERIFIED ->
            Icon(Icons.Filled.Info, "Not verified yet", tint = scheme.onSurfaceVariant, modifier = Modifier.size(size))
    }
}
