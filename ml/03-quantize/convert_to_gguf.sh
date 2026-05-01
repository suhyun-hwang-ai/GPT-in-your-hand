#!/usr/bin/env bash
#
# 책 10장 GGUF 변환·양자화 실습.
# HF 포맷 모델을 F16 GGUF로 변환한 뒤 Q4_K_M으로 양자화한다.
#
# 사용:
#   LLAMA_CPP_DIR=$HOME/llama.cpp bash ml/03-quantize/convert_to_gguf.sh
#   (선택) MODEL_DIR=path/to/model     기본: ml/01-models/checkpoints/qwen3.5-0.8b
#   (선택) OUT_DIR=path/to/output      기본: ml/03-quantize/output
#   (선택) QUANT_TYPE=Q4_K_M           기본: Q4_K_M

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"

: "${LLAMA_CPP_DIR:?LLAMA_CPP_DIR 환경변수를 설정해 주세요 (예: \$HOME/llama.cpp)}"
MODEL_DIR="${MODEL_DIR:-$REPO_ROOT/ml/01-models/checkpoints/qwen3.5-0.8b}"
OUT_DIR="${OUT_DIR:-$REPO_ROOT/ml/03-quantize/output}"
QUANT_TYPE="${QUANT_TYPE:-Q4_K_M}"

CONVERT_SCRIPT="$LLAMA_CPP_DIR/convert_hf_to_gguf.py"
QUANTIZE_BIN="$LLAMA_CPP_DIR/build/bin/llama-quantize"

if [[ ! -f "$CONVERT_SCRIPT" ]]; then
  echo "convert_hf_to_gguf.py를 찾을 수 없습니다: $CONVERT_SCRIPT" >&2
  exit 1
fi
if [[ ! -x "$QUANTIZE_BIN" ]]; then
  echo "llama-quantize 빌드 산출물을 찾을 수 없습니다: $QUANTIZE_BIN" >&2
  echo "먼저 llama.cpp를 호스트에서 빌드해 주세요." >&2
  exit 1
fi
if [[ ! -d "$MODEL_DIR" ]]; then
  echo "모델 디렉토리를 찾을 수 없습니다: $MODEL_DIR" >&2
  echo "ml/01-models/download.py를 먼저 실행해 주세요." >&2
  exit 1
fi

mkdir -p "$OUT_DIR"

F16_FILE="$OUT_DIR/model.F16.gguf"
QUANT_FILE="$OUT_DIR/model.${QUANT_TYPE}.gguf"

echo "=========================================="
echo "  1단계: HF → GGUF F16"
echo "=========================================="
echo "입력:  $MODEL_DIR"
echo "출력:  $F16_FILE"
echo

python3 "$CONVERT_SCRIPT" \
  "$MODEL_DIR" \
  --outfile "$F16_FILE" \
  --outtype f16

echo
echo "=========================================="
echo "  2단계: F16 → ${QUANT_TYPE} 양자화"
echo "=========================================="
echo "입력:  $F16_FILE"
echo "출력:  $QUANT_FILE"
echo

"$QUANTIZE_BIN" "$F16_FILE" "$QUANT_FILE" "$QUANT_TYPE"

echo
echo "=========================================="
echo "  완료"
echo "=========================================="
ls -lh "$F16_FILE" "$QUANT_FILE"
