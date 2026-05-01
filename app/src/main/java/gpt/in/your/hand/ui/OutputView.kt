package gpt.`in`.your.hand.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun OutputView(
    text: String,
    isGenerating: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 200.dp)
            .background(
                MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(8.dp),
            )
            .padding(12.dp)
            .verticalScroll(rememberScrollState()),
    ) {
        val display = when {
            text.isNotEmpty() -> text + if (isGenerating) "▌" else ""
            isGenerating -> "생성 중..."
            else -> "여기 응답이 표시됩니다."
        }
        Text(text = display, style = MaterialTheme.typography.bodyMedium)
    }
}
