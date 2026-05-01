package gpt.`in`.your.hand.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import gpt.`in`.your.hand.inference.InferenceEngine
import gpt.`in`.your.hand.inference.SamplingConfig
import gpt.`in`.your.hand.nativ.LlamaJNI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

private const val TAG = "SingleTurnVM"

data class SingleTurnUiState(
    val modelStatus: ModelStatus = ModelStatus.Idle,
    val inputText: String = "",
    val outputText: String = "",
    val isGenerating: Boolean = false,
    val sampling: SamplingConfig = SamplingConfig(),
)

sealed interface ModelStatus {
    data object Idle : ModelStatus
    data object Loading : ModelStatus
    data object Ready : ModelStatus
    data class Error(val message: String) : ModelStatus
}

class SingleTurnViewModel(app: Application) : AndroidViewModel(app) {

    private val jni = LlamaJNI()
    private val engine = InferenceEngine(jni)

    private val _uiState = MutableStateFlow(SingleTurnUiState())
    val uiState: StateFlow<SingleTurnUiState> = _uiState.asStateFlow()

    private var generationJob: Job? = null

    fun loadModel(path: String) {
        if (_uiState.value.modelStatus is ModelStatus.Loading ||
            _uiState.value.modelStatus is ModelStatus.Ready
        ) {
            return
        }
        _uiState.update { it.copy(modelStatus = ModelStatus.Loading) }
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) { jni.loadModel(path) }
                _uiState.update { it.copy(modelStatus = ModelStatus.Ready) }
            } catch (t: Throwable) {
                Log.e(TAG, "모델 로드 실패", t)
                _uiState.update {
                    it.copy(modelStatus = ModelStatus.Error(t.message ?: "unknown"))
                }
            }
        }
    }

    fun onInputChange(text: String) {
        _uiState.update { it.copy(inputText = text) }
    }

    fun onSamplingChange(sampling: SamplingConfig) {
        _uiState.update { it.copy(sampling = sampling) }
    }

    fun submit() {
        val state = _uiState.value
        if (state.modelStatus !is ModelStatus.Ready) return
        if (state.isGenerating) return
        val prompt = state.inputText.trim()
        if (prompt.isEmpty()) return

        _uiState.update { it.copy(isGenerating = true, outputText = "") }

        generationJob?.cancel()
        generationJob = viewModelScope.launch {
            engine.stream(prompt, maxTokens = 256, config = state.sampling).collect { piece ->
                _uiState.update { it.copy(outputText = it.outputText + piece) }
            }
            _uiState.update { it.copy(isGenerating = false) }
        }
    }

    override fun onCleared() {
        generationJob?.cancel()
        jni.close()
        super.onCleared()
    }
}

/**
 * 모델 파일 위치 헬퍼. 17장 이전 단계에서는 외부 저장소에 직접 푸시한
 * GGUF를 사용한다. (책에서 안내)
 */
fun defaultModelPath(app: Application): String {
    return File(app.filesDir, "model.Q4_K_M.gguf").absolutePath
}
