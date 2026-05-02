package gpt.`in`.your.hand.chat

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * 책 17장 멀티턴 대화 상태 관리자.
 *
 * 책 17 단계의 단순 구현: 매 턴 전체 이력을 ChatTemplate로 직렬화해
 * 모델에 다시 보낸다. KV 캐시 재사용 최적화는 책 후속 절에서.
 */
class ConversationManager(
    initialSystemPrompt: String? = null,
    private val historyWindow: HistoryWindow = HistoryWindow(),
) {

    private val _messages = MutableStateFlow<List<ChatMessage>>(
        buildList {
            if (initialSystemPrompt != null) {
                add(ChatMessage(ChatRole.SYSTEM, initialSystemPrompt))
            }
        }
    )
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    fun setSystemPrompt(text: String) {
        _messages.update { current ->
            val withoutSystem = current.filter { it.role != ChatRole.SYSTEM }
            buildList {
                add(ChatMessage(ChatRole.SYSTEM, text))
                addAll(withoutSystem)
            }
        }
    }

    fun appendUser(text: String) {
        _messages.update { it + ChatMessage(ChatRole.USER, text) }
    }

    /** 새로운 assistant 응답 자리 추가 (스트리밍 시작 직전). */
    fun startAssistantTurn() {
        _messages.update { it + ChatMessage(ChatRole.ASSISTANT, "") }
    }

    /** 진행 중인 assistant 응답에 토큰 누적. */
    fun appendAssistantToken(token: String) {
        _messages.update { current ->
            if (current.isEmpty() || current.last().role != ChatRole.ASSISTANT) {
                current + ChatMessage(ChatRole.ASSISTANT, token)
            } else {
                val last = current.last()
                current.dropLast(1) + last.copy(content = last.content + token)
            }
        }
    }

    fun reset() {
        _messages.update { current ->
            current.filter { it.role == ChatRole.SYSTEM }
        }
    }

    /**
     * 모델에 보낼 프롬프트 직렬화.
     * 컨텍스트 한계를 고려해 이력을 트리밍한 뒤 ChatTemplate 적용.
     */
    fun buildPrompt(): String {
        val trimmed = historyWindow.trim(_messages.value)
        return ChatTemplate.format(trimmed)
    }
}
