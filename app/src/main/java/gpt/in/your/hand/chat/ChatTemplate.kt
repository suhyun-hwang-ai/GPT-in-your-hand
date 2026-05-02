package gpt.`in`.your.hand.chat

/**
 * Qwen3.5 ChatML 형식으로 메시지 배열을 단일 프롬프트 문자열로 변환.
 *
 * 생성 시작을 위해 마지막에 `<|im_start|>assistant` 토큰을 붙이고
 * `<|im_end|>`는 모델이 출력하도록 둔다.
 */
object ChatTemplate {
    fun format(messages: List<ChatMessage>): String = buildString {
        for (msg in messages) {
            append("<|im_start|>")
            append(msg.role.tag)
            append('\n')
            append(msg.content)
            append("<|im_end|>\n")
        }
        append("<|im_start|>assistant\n")
    }
}
