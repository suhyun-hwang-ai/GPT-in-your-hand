package gpt.`in`.your.hand.perf

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * 책 20.1 추론 속도 측정.
 *
 * 단순 래퍼: 토큰 스트림 첫 토큰까지 시간(prefill 근사), 이후 평균 tok/s.
 */
data class ProfileResult(
    val firstTokenMs: Long,
    val totalTokens: Int,
    val totalMs: Long,
    val tokensPerSecond: Double,
)

class InferenceProfiler {

    fun measure(stream: Flow<String>): Flow<TokenWithStats> = flow {
        val start = System.nanoTime()
        var firstTokenNs: Long? = null
        var count = 0

        stream.collect { piece ->
            if (firstTokenNs == null) firstTokenNs = System.nanoTime()
            count++
            val now = System.nanoTime()
            emit(
                TokenWithStats(
                    piece = piece,
                    firstTokenMs = ((firstTokenNs!! - start) / 1_000_000L),
                    elapsedMs = ((now - start) / 1_000_000L),
                    tokenIndex = count,
                )
            )
        }
    }
}

data class TokenWithStats(
    val piece: String,
    val firstTokenMs: Long,
    val elapsedMs: Long,
    val tokenIndex: Int,
) {
    val tokensPerSecond: Double
        get() {
            val genMs = (elapsedMs - firstTokenMs).coerceAtLeast(1)
            return (tokenIndex - 1) * 1000.0 / genMs
        }
}
