package gpt.`in`.your.hand.nativ

/**
 * llama.cpp JNI 바인딩 (책 13장).
 *
 * 13장 단계: 동기 추론 + 토큰 단위 콜백.
 * 이후 15·16장에서 prefill/decode 분리, 샘플러 외부 설정으로 확장.
 */
class LlamaJNI : AutoCloseable {

    fun interface TokenCallback {
        fun onToken(text: String)
    }

    private var handle: Long = 0L

    fun loadModel(path: String) {
        check(handle == 0L) { "이미 로드된 모델이 있습니다" }
        handle = nativeLoadModel(path)
        check(handle != 0L) { "모델 로드 실패: $path" }
    }

    /**
     * 동기 추론. 단일 user 메시지를 받아 모델 내장 chat template을 적용한 뒤 추론한다.
     * 토큰이 생성될 때마다 [callback]이 호출된다.
     *
     * @return 생성된 토큰 수
     */
    fun infer(prompt: String, maxTokens: Int = 256, callback: TokenCallback): Int {
        check(handle != 0L) { "모델이 로드되지 않았습니다" }
        return nativeInfer(handle, prompt, maxTokens, callback)
    }

    /**
     * 동기 추론. 호출자가 이미 chat template을 적용한 raw 프롬프트를 전달한다.
     * 멀티턴 시 사용 (책 17장).
     */
    fun inferFormatted(formattedPrompt: String, maxTokens: Int = 256, callback: TokenCallback): Int {
        check(handle != 0L) { "모델이 로드되지 않았습니다" }
        return nativeInferFormatted(handle, formattedPrompt, maxTokens, callback)
    }

    override fun close() {
        if (handle != 0L) {
            nativeFreeModel(handle)
            handle = 0L
        }
    }

    private external fun nativeLoadModel(path: String): Long
    private external fun nativeFreeModel(handle: Long)
    private external fun nativeInfer(
        handle: Long,
        prompt: String,
        maxTokens: Int,
        callback: TokenCallback,
    ): Int

    private external fun nativeInferFormatted(
        handle: Long,
        prompt: String,
        maxTokens: Int,
        callback: TokenCallback,
    ): Int

    companion object {
        init {
            // 의존성 순서대로 명시 로드 (Android 동적 링커 호환)
            System.loadLibrary("ggml-base")
            System.loadLibrary("ggml-cpu")
            System.loadLibrary("ggml")
            System.loadLibrary("llama")
            System.loadLibrary("llama_jni")
        }
    }
}
