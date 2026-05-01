# native/ — NDK·JNI 코드, llama.cpp 빌드

책 11~14장 대응.

## 디렉토리

| 경로 | 책 챕터 | 내용 |
|---|---|---|
| `scripts/build_llama.sh` | 12장 | llama.cpp 안드로이드 빌드 자동화 |
| `jni/llama_jni.cpp` | 13장 | JNI 바인딩 원본 코드 |
| `jni/CMakeLists.txt` | 13장 | JNI 빌드 설정 |
| `prebuilt/{ABI}/libllama.so` | 12장 | ABI별 빌드된 산출물 (그대로 사용 가능) |
| `llama.cpp.version` | 12장 | 사용한 llama.cpp 커밋·태그 |

## 빌드 방법

### 직접 빌드 (12장 학습 흐름)

```bash
git clone https://github.com/ggerganov/llama.cpp /path/to/llama.cpp
cd /path/to/llama.cpp
git checkout $(cat /path/to/this/repo/native/llama.cpp.version)

cd /path/to/this/repo
LLAMA_CPP_DIR=/path/to/llama.cpp \
ANDROID_NDK_HOME=/path/to/ndk \
bash native/scripts/build_llama.sh
```

### 빠른 실행

`prebuilt/{ABI}/libllama.so`를 그대로 사용. 빌드 단계 생략.

## 라이선스

llama.cpp 부분은 원저작자(MIT)의 라이선스를 따른다.
