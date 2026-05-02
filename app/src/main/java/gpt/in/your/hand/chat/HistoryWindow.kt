package gpt.`in`.your.hand.chat

/**
 * 책 17.1 컨텍스트 윈도우 한계 관리.
 *
 * 단순 정책: system 메시지는 항상 보존, 나머지 user/assistant 쌍은
 * 가장 오래된 것부터 잘라낸다.
 *
 * 추정 토큰 길이는 character / [charsPerToken] 으로 근사한다 (정확도 미사용).
 * 실측 기반 트리밍은 책 17.3에서 다룬다.
 */
class HistoryWindow(
    private val maxApproxTokens: Int = 1500,
    private val charsPerToken: Int = 3,
) {

    fun trim(messages: List<ChatMessage>): List<ChatMessage> {
        if (messages.isEmpty()) return messages
        if (estimateTokens(messages) <= maxApproxTokens) return messages

        val system = messages.firstOrNull { it.role == ChatRole.SYSTEM }
        val turns = messages.filter { it.role != ChatRole.SYSTEM }.toMutableList()

        // user/assistant 쌍 단위로 앞에서 잘라낸다
        while (turns.isNotEmpty()) {
            val approx = (system?.let { estimateTokens(listOf(it)) } ?: 0) +
                    estimateTokens(turns)
            if (approx <= maxApproxTokens) break
            // 보통 user 1 + assistant 1 한 쌍 제거
            turns.removeFirst()
            if (turns.isNotEmpty() && turns.first().role == ChatRole.ASSISTANT) {
                turns.removeFirst()
            }
        }

        return buildList {
            system?.let { add(it) }
            addAll(turns)
        }
    }

    private fun estimateTokens(messages: List<ChatMessage>): Int {
        // role 태그·줄바꿈 등 오버헤드를 +8자 가산
        return messages.sumOf { (it.content.length + 8) / charsPerToken }
    }
}
