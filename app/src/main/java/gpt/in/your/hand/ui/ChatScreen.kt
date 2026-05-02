package gpt.`in`.your.hand.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import gpt.`in`.your.hand.chat.ChatRole

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    modifier: Modifier = Modifier,
    viewModel: ChatViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    LaunchedEffect(state.messages.size, state.messages.lastOrNull()?.content) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.size - 1)
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("타로봇") },
                actions = {
                    IconButton(onClick = viewModel::resetConversation) {
                        Icon(Icons.Default.Refresh, contentDescription = "대화 초기화")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ModelStatusRow(state.modelStatus)

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(state.messages) { msg ->
                    MessageBubble(role = msg.role, content = msg.content)
                }
            }

            TarotActionRow(
                onDrawCard = { viewModel.drawCardAndAsk("이 카드의 의미를 풀어 주세요.") },
                enabled = state.modelStatus is ModelStatus.Ready && !state.isGenerating,
            )

            MessageInput(
                value = state.inputText,
                onValueChange = viewModel::onInputChange,
                onSubmit = viewModel::submit,
                enabled = state.modelStatus is ModelStatus.Ready && !state.isGenerating,
            )
        }
    }
}

@Composable
private fun ModelStatusRow(status: ModelStatus) {
    val text = when (status) {
        ModelStatus.Idle -> "모델 미로드"
        ModelStatus.Loading -> "모델 로드 중..."
        ModelStatus.Ready -> ""
        is ModelStatus.Error -> "모델 오류: ${status.message}"
    }
    if (text.isNotEmpty()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text)
        }
    }
}

@Composable
private fun TarotActionRow(
    onDrawCard: () -> Unit,
    enabled: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
    ) {
        androidx.compose.material3.OutlinedButton(
            onClick = onDrawCard,
            enabled = enabled,
        ) {
            Text("카드 한 장 뽑기")
        }
    }
}
