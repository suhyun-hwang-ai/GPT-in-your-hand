package gpt.`in`.your.hand.inference

import gpt.`in`.your.hand.nativ.LlamaJNI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

/**
 * 책 15장 추론 흐름 — JNI를 Kotlin Flow로 감싸 토큰 스트림을 노출.
 *
 * 13장 단계의 동기 `infer`를 백그라운드 스레드에서 호출하면서 토큰 콜백을
 * Flow로 변환한다. 이 단계는 내부 샘플러가 고정이므로 [config]는 보관만 하고
 * 16장에서 JNI에 전달하도록 확장한다.
 */
class InferenceEngine(private val jni: LlamaJNI) {

    fun stream(
        prompt: String,
        maxTokens: Int = 256,
        @Suppress("unused") config: SamplingConfig = SamplingConfig(),
    ): Flow<String> = callbackFlow {
        val callback = LlamaJNI.TokenCallback { piece ->
            trySend(piece)
        }

        val job = launch(Dispatchers.IO) {
            try {
                jni.infer(prompt, maxTokens, callback)
            } finally {
                close()
            }
        }

        awaitClose { job.cancel() }
    }.flowOn(Dispatchers.IO)

    /**
     * 책 17장 멀티턴 — 호출자가 ChatTemplate으로 만든 raw 프롬프트를 그대로 전달한다.
     */
    fun streamFormatted(
        formattedPrompt: String,
        maxTokens: Int = 256,
        @Suppress("unused") config: SamplingConfig = SamplingConfig(),
    ): Flow<String> = callbackFlow {
        val callback = LlamaJNI.TokenCallback { piece ->
            trySend(piece)
        }

        val job = launch(Dispatchers.IO) {
            try {
                jni.inferFormatted(formattedPrompt, maxTokens, callback)
            } finally {
                close()
            }
        }

        awaitClose { job.cancel() }
    }.flowOn(Dispatchers.IO)
}
