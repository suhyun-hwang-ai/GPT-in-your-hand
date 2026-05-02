package gpt.`in`.your.hand.chat

/**
 * 책 17장 컨텍스트와 멀티턴 관리.
 */
data class ChatMessage(
    val role: ChatRole,
    val content: String,
)

enum class ChatRole(val tag: String) {
    SYSTEM("system"),
    USER("user"),
    ASSISTANT("assistant"),
}
