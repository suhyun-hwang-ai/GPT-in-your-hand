package gpt.`in`.your.hand.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import gpt.`in`.your.hand.chat.ChatRole

@Composable
fun MessageBubble(
    role: ChatRole,
    content: String,
    modifier: Modifier = Modifier,
) {
    val (bg, fg, align) = when (role) {
        ChatRole.USER -> Triple(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.onPrimary,
            Alignment.CenterEnd,
        )
        ChatRole.ASSISTANT -> Triple(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant,
            Alignment.CenterStart,
        )
        ChatRole.SYSTEM -> Triple(
            Color.Transparent,
            MaterialTheme.colorScheme.onSurfaceVariant,
            Alignment.Center,
        )
    }

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = align,
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 320.dp)
                .background(bg, RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            val display = if (role == ChatRole.SYSTEM) "[시스템] $content" else content
            Text(
                text = display,
                color = fg,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}
