package gpt.`in`.your.hand.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SingleTurnScreen(
    modifier: Modifier = Modifier,
    viewModel: SingleTurnViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(title = { Text("내 손 안의 챗봇 (한 턴)") })
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ModelStatusBar(state.modelStatus)
            MessageInput(
                value = state.inputText,
                onValueChange = viewModel::onInputChange,
                onSubmit = viewModel::submit,
                enabled = state.modelStatus is ModelStatus.Ready && !state.isGenerating,
            )
            SamplingPanel(
                config = state.sampling,
                onChange = viewModel::onSamplingChange,
                enabled = !state.isGenerating,
            )
            OutputView(
                text = state.outputText,
                isGenerating = state.isGenerating,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun ModelStatusBar(status: ModelStatus) {
    val text = when (status) {
        ModelStatus.Idle -> "모델 미로드"
        ModelStatus.Loading -> "모델 로드 중..."
        ModelStatus.Ready -> "모델 준비됨"
        is ModelStatus.Error -> "모델 오류: ${status.message}"
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text)
    }
}
