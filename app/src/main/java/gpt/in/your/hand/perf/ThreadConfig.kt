package gpt.`in`.your.hand.perf

/**
 * 책 20.2 스레드 수와 배치 사이즈 — 사용자 설정 모델.
 *
 * 현재 단계의 JNI는 스레드 수를 외부 노출하지 않으므로
 * 본 클래스는 후속 작업의 자리표시 역할이다.
 */
data class ThreadConfig(
    val nThreads: Int = 4,
    val nThreadsBatch: Int = 4,
    val nBatch: Int = 512,
)
