package com.chatvault.tiktokarchive.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val palette = listOf(
    Color(0xFF7C4DFF), Color(0xFF00A3BF), Color(0xFFE65100),
    Color(0xFF2E9E8F), Color(0xFFD81B60), Color(0xFF5C6BC0),
)

/** Initials avatar. Real profile pictures are not stored (data minimisation). */
@Composable
fun Avatar(name: String, size: Dp = 48.dp, modifier: Modifier = Modifier) {
    val color = palette[(name.hashCode() and Int.MAX_VALUE) % palette.size]
    Box(modifier.size(size).clip(CircleShape).background(color), contentAlignment = Alignment.Center) {
        Text(
            text = name.firstOrNull { it.isLetterOrDigit() }?.uppercase() ?: "?",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = (size.value * 0.4f).sp,
        )
    }
}
