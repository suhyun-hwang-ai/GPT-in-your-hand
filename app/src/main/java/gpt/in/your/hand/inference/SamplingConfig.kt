package gpt.`in`.your.hand.inference

/**
 * 책 16장 샘플링 전략 — 사용자 설정 모델.
 *
 * 13장 단계의 JNI는 내부에 고정 샘플러를 갖지만, 16장에서 이 설정을 외부로 노출하면서
 * 사용자가 슬라이더로 조정할 수 있도록 한다.
 */
data class SamplingConfig(
    val temperature: Float = 0.7f,
    val topK: Int = 40,
    val topP: Float = 0.9f,
    val repeatPenalty: Float = 1.1f,
)
