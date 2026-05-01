#!/usr/bin/env bash
#
# 책 12장 llama.cpp 안드로이드 빌드.
# 클론된 llama.cpp 소스를 Android NDK로 cross-compile하여
# arm64-v8a용 .so 산출물을 native/prebuilt/arm64-v8a/에 배치한다.
#
# Q4_K_M + ARMv8.6 i8mm SIMD 최적화 활성.
# KV 캐시는 별도 양자화 옵션을 주지 않아 기본 F16으로 유지된다.
#
# 사용:
#   LLAMA_CPP_DIR=$HOME/llama.cpp \
#   ANDROID_NDK_HOME=$HOME/Library/Android/sdk/ndk/27.0.12077973 \
#   bash native/scripts/build_llama.sh
#
# (선택) ABI=arm64-v8a       기본
# (선택) ANDROID_PLATFORM=android-26   minSdk와 일치
# (선택) BUILD_DIR=$LLAMA_CPP_DIR/build-android-${ABI}

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
NATIVE_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
REPO_ROOT="$(cd "$NATIVE_DIR/.." && pwd)"

: "${LLAMA_CPP_DIR:?LLAMA_CPP_DIR 환경변수를 설정해 주세요 (예: \$HOME/llama.cpp)}"
: "${ANDROID_NDK_HOME:?ANDROID_NDK_HOME 환경변수를 설정해 주세요}"

ABI="${ABI:-arm64-v8a}"
ANDROID_PLATFORM="${ANDROID_PLATFORM:-android-26}"
BUILD_DIR="${BUILD_DIR:-$LLAMA_CPP_DIR/build-android-${ABI}}"
PREBUILT_DIR="$NATIVE_DIR/prebuilt/${ABI}"

TOOLCHAIN="$ANDROID_NDK_HOME/build/cmake/android.toolchain.cmake"
if [[ ! -f "$TOOLCHAIN" ]]; then
  echo "Android NDK toolchain을 찾을 수 없습니다: $TOOLCHAIN" >&2
  exit 1
fi

# ARMv8.6 i8mm 활성. 미지원 디바이스는 런타임에서 자동 폴백되도록
# llama.cpp가 처리하므로 빌드 옵션으로 켜둬도 안전.
ARCH_FLAGS="-march=armv8.6-a+i8mm+dotprod"

echo "=========================================="
echo "  llama.cpp Android 빌드"
echo "=========================================="
echo "LLAMA_CPP_DIR  : $LLAMA_CPP_DIR"
echo "ANDROID_NDK    : $ANDROID_NDK_HOME"
echo "ABI            : $ABI"
echo "PLATFORM       : $ANDROID_PLATFORM"
echo "BUILD_DIR      : $BUILD_DIR"
echo "PREBUILT_DIR   : $PREBUILT_DIR"
echo "ARCH_FLAGS     : $ARCH_FLAGS"
echo

cmake -S "$LLAMA_CPP_DIR" -B "$BUILD_DIR" \
  -DCMAKE_TOOLCHAIN_FILE="$TOOLCHAIN" \
  -DANDROID_ABI="$ABI" \
  -DANDROID_PLATFORM="$ANDROID_PLATFORM" \
  -DCMAKE_BUILD_TYPE=Release \
  -DBUILD_SHARED_LIBS=ON \
  -DLLAMA_BUILD_TESTS=OFF \
  -DLLAMA_BUILD_EXAMPLES=OFF \
  -DLLAMA_BUILD_SERVER=OFF \
  -DLLAMA_BUILD_TOOLS=OFF \
  -DLLAMA_BUILD_COMMON=OFF \
  -DLLAMA_CURL=OFF \
  -DGGML_OPENMP=OFF \
  -DCMAKE_C_FLAGS="$ARCH_FLAGS" \
  -DCMAKE_CXX_FLAGS="$ARCH_FLAGS"

# llama 라이브러리 빌드만 (테스트·예제·서버·툴 제외)
cmake --build "$BUILD_DIR" --target llama -j"$(sysctl -n hw.ncpu 2>/dev/null || nproc)"

echo
echo "=========================================="
echo "  산출물 복사"
echo "=========================================="
mkdir -p "$PREBUILT_DIR"

# llama.cpp는 BUILD_SHARED_LIBS=ON일 때 다음 .so들을 만든다.
# (libggml-base, libggml-cpu, libggml, libllama, libllama-common 등)
# 앱이 필요로 하는 .so만 복사 (libmtmd, libllama-common 등 제외)
NEEDED=("libllama.so" "libggml.so" "libggml-base.so" "libggml-cpu.so")
STRIP_BIN="$ANDROID_NDK_HOME/toolchains/llvm/prebuilt/$(uname | tr '[:upper:]' '[:lower:]')-x86_64/bin/llvm-strip"

SO_COUNT=0
for name in "${NEEDED[@]}"; do
  src="$BUILD_DIR/bin/$name"
  if [[ ! -f "$src" ]]; then
    echo "필요한 산출물이 없습니다: $src" >&2
    exit 1
  fi
  dst="$PREBUILT_DIR/$name"
  cp "$src" "$dst"
  if [[ -x "$STRIP_BIN" ]]; then
    "$STRIP_BIN" --strip-all "$dst"
  fi
  SO_COUNT=$((SO_COUNT + 1))
done

echo
echo "[완료] $SO_COUNT개 .so 파일을 $PREBUILT_DIR/ 에 복사·strip"
ls -lh "$PREBUILT_DIR/"
