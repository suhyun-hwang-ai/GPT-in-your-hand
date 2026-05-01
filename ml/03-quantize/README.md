# 03-quantize — GGUF 변환·양자화

책 9~10장 대응.

## 파일 (예정)

| 파일 | 챕터 | 설명 |
|---|---|---|
| `convert_to_gguf.sh` | 10장 | HF 모델 → GGUF (Q4_K_M 권장) |
| `quantize_levels.sh` | 10장 | Q2_K~Q8_0 일괄 산출 (크기 비교) |
| `verify_model.py` | 10장 | 양자화된 GGUF로 추론 검증 |

## 사전 준비

llama.cpp 클론 필요. 본 저장소 루트의 `native/llama.cpp.version`에 명시된 커밋·태그 사용.

## 산출물

`.gguf` 파일은 저장소에 포함하지 않는다 (`.gitignore` 처리).
