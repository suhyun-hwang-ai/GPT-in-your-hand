package gpt.`in`.your.hand.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import gpt.`in`.your.hand.chat.ChatMessage
import gpt.`in`.your.hand.chat.ConversationManager
import gpt.`in`.your.hand.chat.SystemPrompt
import gpt.`in`.your.hand.inference.InferenceEngine
import gpt.`in`.your.hand.inference.SamplingConfig
import gpt.`in`.your.hand.nativ.LlamaJNI
import gpt.`in`.your.hand.tarot.TarotPromptBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "ChatVM"

data class ChatUiState(
    val modelStatus: ModelStatus = ModelStatus.Idle,
    val messages: List<ChatMessage> = emptyList(),
    val inputText: String = "",
    val isGenerating: Boolean = false,
    val sampling: SamplingConfig = SamplingConfig(),
)

class ChatViewModel(app: Application) : AndroidViewModel(app) {

    private val jni = LlamaJNI()
    private val engine = InferenceEngine(jni)

    private val systemPrompt = runCatching {
        SystemPrompt.load(app, "tarot_system")
    }.onFailure { Log.w(TAG, "타로 시스템 프롬프트 로드 실패", it) }
        .getOrDefault("당신은 친절한 한국어 비서입니다.")

    private val conversation = ConversationManager(initialSystemPrompt = systemPrompt)
    val tarotBuilder = TarotPromptBuilder(app)

    private val _local = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = combine(
        _local,
        conversation.messages,
    ) { local, messages -> local.copy(messages = messages) }
        .let { flow ->
            // StateFlow로 변환을 위해 stateIn 대신 단순 임시 캐시 사용
            val state = MutableStateFlow(ChatUiState())
            viewModelScope.launch {
                flow.collect { state.value = it }
            }
            state.asStateFlow()
        }

    private var generationJob: Job? = null

    fun loadModel(path: String) {
        if (_local.value.modelStatus is ModelStatus.Loading ||
            _local.value.modelStatus is ModelStatus.Ready
        ) return
        _local.update { it.copy(modelStatus = ModelStatus.Loading) }
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) { jni.loadModel(path) }
                _local.update { it.copy(modelStatus = ModelStatus.Ready) }
            } catch (t: Throwable) {
                Log.e(TAG, "모델 로드 실패", t)
                _local.update {
                    it.copy(modelStatus = ModelStatus.Error(t.message ?: "unknown"))
                }
            }
        }
    }

    fun onInputChange(text: String) {
        _local.update { it.copy(inputText = text) }
    }

    fun onSamplingChange(sampling: SamplingConfig) {
        _local.update { it.copy(sampling = sampling) }
    }

    fun submit() {
        val state = _local.value
        if (state.modelStatus !is ModelStatus.Ready || state.isGenerating) return
        val text = state.inputText.trim()
        if (text.isEmpty()) return

        conversation.appendUser(text)
        conversation.startAssistantTurn()
        _local.update { it.copy(inputText = "", isGenerating = true) }

        val prompt = conversation.buildPrompt()
        generationJob?.cancel()
        generationJob = viewModelScope.launch {
            engine.streamFormatted(prompt, maxTokens = 384, config = state.sampling)
                .collect { piece ->
                    conversation.appendAssistantToken(piece)
                }
            _local.update { it.copy(isGenerating = false) }
        }
    }

    fun resetConversation() {
        generationJob?.cancel()
        conversation.reset()
        _local.update { it.copy(isGenerating = false) }
    }

    /** 사용자가 카드를 뽑은 시점 호출. 카드 정보를 user 메시지로 자동 추가. */
    fun drawCardAndAsk(question: String) {
        val state = _local.value
        if (state.modelStatus !is ModelStatus.Ready || state.isGenerating) return
        val card = tarotBuilder.drawRandomCard() ?: return

        conversation.appendUser(tarotBuilder.formatUserTurn(question, card))
        conversation.startAssistantTurn()
        _local.update { it.copy(inputText = "", isGenerating = true) }

        val prompt = conversation.buildPrompt()
        generationJob?.cancel()
        generationJob = viewModelScope.launch {
            engine.streamFormatted(prompt, maxTokens = 384, config = state.sampling)
                .collect { piece -> conversation.appendAssistantToken(piece) }
            _local.update { it.copy(isGenerating = false) }
        }
    }

    override fun onCleared() {
        generationJob?.cancel()
        jni.close()
        super.onCleared()
    }
}
