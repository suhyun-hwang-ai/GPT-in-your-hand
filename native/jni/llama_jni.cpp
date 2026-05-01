// 책 13장: JNI로 코틀린과 C++ 잇기.
// llama.cpp C API를 코틀린에서 호출 가능하도록 감싼다.
//
// 13장 단계: 동기 추론 + 토큰 단위 콜백
// (15장에서 prefill·decode 분리, 16장에서 샘플러 외부 설정으로 확장)

#include <jni.h>
#include <string>
#include <vector>
#include <cstring>

#include "llama.h"

namespace {

struct LlamaHandle {
    llama_model* model = nullptr;
    llama_context* ctx = nullptr;
    const llama_vocab* vocab = nullptr;
    llama_sampler* sampler = nullptr;
};

// 안드로이드 logcat용 간단한 로깅 헬퍼 대신,
// 예외를 자바로 던지는 방식 사용
void throwJavaException(JNIEnv* env, const char* msg) {
    jclass cls = env->FindClass("java/lang/RuntimeException");
    if (cls != nullptr) {
        env->ThrowNew(cls, msg);
    }
}

}  // namespace

extern "C" {

JNIEXPORT jlong JNICALL
Java_gpt_in_your_hand_nativ_LlamaJNI_nativeLoadModel(
    JNIEnv* env, jobject /* thiz */, jstring path_jstr) {

    const char* path = env->GetStringUTFChars(path_jstr, nullptr);

    llama_backend_init();

    auto* h = new LlamaHandle();

    llama_model_params mparams = llama_model_default_params();
    mparams.use_mmap = true;
    h->model = llama_model_load_from_file(path, mparams);
    env->ReleaseStringUTFChars(path_jstr, path);

    if (h->model == nullptr) {
        delete h;
        throwJavaException(env, "llama_model_load_from_file 실패");
        return 0;
    }
    h->vocab = llama_model_get_vocab(h->model);

    llama_context_params cparams = llama_context_default_params();
    cparams.n_ctx = 2048;
    cparams.n_batch = 512;
    cparams.no_perf = false;
    h->ctx = llama_init_from_model(h->model, cparams);

    if (h->ctx == nullptr) {
        llama_model_free(h->model);
        delete h;
        throwJavaException(env, "llama_init_from_model 실패");
        return 0;
    }

    // 13장 단계의 단순 샘플러 체인 (16장에서 옵션화)
    auto sparams = llama_sampler_chain_default_params();
    h->sampler = llama_sampler_chain_init(sparams);
    llama_sampler_chain_add(h->sampler, llama_sampler_init_top_k(40));
    llama_sampler_chain_add(h->sampler, llama_sampler_init_top_p(0.9f, 1));
    llama_sampler_chain_add(h->sampler, llama_sampler_init_temp(0.7f));
    llama_sampler_chain_add(h->sampler, llama_sampler_init_dist(LLAMA_DEFAULT_SEED));

    return reinterpret_cast<jlong>(h);
}

JNIEXPORT void JNICALL
Java_gpt_in_your_hand_nativ_LlamaJNI_nativeFreeModel(
    JNIEnv* /* env */, jobject /* thiz */, jlong handle_jl) {

    auto* h = reinterpret_cast<LlamaHandle*>(handle_jl);
    if (h == nullptr) return;

    if (h->sampler) llama_sampler_free(h->sampler);
    if (h->ctx)     llama_free(h->ctx);
    if (h->model)   llama_model_free(h->model);
    delete h;
}

// 채팅 템플릿 적용 (Qwen3.5 등 모델 내장 템플릿 사용)
static std::string apply_chat_template(
    const llama_model* model,
    const std::string& user_text) {

    const char* tmpl = llama_model_chat_template(model, /*name=*/nullptr);

    llama_chat_message msgs[] = {
        {"user", user_text.c_str()},
    };

    std::vector<char> buf(4096);
    int32_t len = llama_chat_apply_template(
        tmpl, msgs, /*n_msg=*/1, /*add_ass=*/true,
        buf.data(), static_cast<int32_t>(buf.size()));

    if (len > static_cast<int32_t>(buf.size())) {
        buf.resize(len);
        len = llama_chat_apply_template(
            tmpl, msgs, 1, true, buf.data(), len);
    }
    if (len < 0) return user_text;  // 폴백: 원문 그대로
    return std::string(buf.data(), len);
}

JNIEXPORT jint JNICALL
Java_gpt_in_your_hand_nativ_LlamaJNI_nativeInfer(
    JNIEnv* env, jobject /* thiz */,
    jlong handle_jl,
    jstring prompt_jstr,
    jint max_tokens,
    jobject callback) {

    auto* h = reinterpret_cast<LlamaHandle*>(handle_jl);
    if (h == nullptr || h->ctx == nullptr) {
        throwJavaException(env, "유효하지 않은 핸들");
        return 0;
    }

    // 콜백 메서드 ID
    jclass cb_cls = env->GetObjectClass(callback);
    jmethodID mid_on_token = env->GetMethodID(
        cb_cls, "onToken", "(Ljava/lang/String;)V");
    if (mid_on_token == nullptr) {
        throwJavaException(env, "콜백 onToken(String) 메서드를 찾을 수 없음");
        return 0;
    }

    // 프롬프트 → 채팅 템플릿 적용 → 토큰화
    const char* prompt_cstr = env->GetStringUTFChars(prompt_jstr, nullptr);
    std::string formatted = apply_chat_template(h->model, prompt_cstr);
    env->ReleaseStringUTFChars(prompt_jstr, prompt_cstr);

    int32_t n_prompt = -llama_tokenize(
        h->vocab, formatted.c_str(), static_cast<int32_t>(formatted.size()),
        nullptr, 0, /*add_special=*/true, /*parse_special=*/true);

    std::vector<llama_token> prompt_tokens(n_prompt);
    if (llama_tokenize(
            h->vocab, formatted.c_str(), static_cast<int32_t>(formatted.size()),
            prompt_tokens.data(), n_prompt, true, true) < 0) {
        throwJavaException(env, "프롬프트 토큰화 실패");
        return 0;
    }

    // KV 캐시 초기화 (이전 호출 흔적 제거)
    llama_memory_clear(llama_get_memory(h->ctx), true);

    // 프리필
    llama_batch batch = llama_batch_get_one(prompt_tokens.data(), n_prompt);
    if (llama_decode(h->ctx, batch) != 0) {
        throwJavaException(env, "프리필 llama_decode 실패");
        return 0;
    }

    int32_t n_generated = 0;
    llama_token tok = 0;
    char piece_buf[256];

    for (int i = 0; i < max_tokens; ++i) {
        tok = llama_sampler_sample(h->sampler, h->ctx, -1);

        if (llama_vocab_is_eog(h->vocab, tok)) break;

        int32_t plen = llama_token_to_piece(
            h->vocab, tok, piece_buf, sizeof(piece_buf), 0, /*special=*/false);
        if (plen < 0) plen = 0;

        std::string piece(piece_buf, plen);
        jstring jpiece = env->NewStringUTF(piece.c_str());
        env->CallVoidMethod(callback, mid_on_token, jpiece);
        env->DeleteLocalRef(jpiece);

        if (env->ExceptionCheck()) {
            // 콜백 안에서 예외가 던져졌으면 즉시 중단
            return n_generated;
        }

        ++n_generated;

        // 디코드 다음 토큰
        llama_batch step = llama_batch_get_one(&tok, 1);
        if (llama_decode(h->ctx, step) != 0) break;
    }

    return n_generated;
}

}  // extern "C"
