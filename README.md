# 내 손 안의 챗봇 — 안드로이드 온디바이스 LLM 타로봇 만들기

도서 『내 손 안의 챗봇』의 예제 저장소입니다.
허깅페이스의 Qwen 3.5 0.8B를 LoRA로 미세조정하고, GGUF로 양자화한 뒤,
llama.cpp를 NDK로 빌드해 안드로이드 앱에 탑재하기까지의 전 과정을 다룹니다.

---

## 브랜치 안내

| 브랜치 | 내용 | 책 챕터 |
|---|---|---|
| `main` | 기초적인 한 턴 대화 앱 (입력 → 응답 한 번) | 1~16장 |
| `tarot` | 멀티턴 + 타로봇 특화 (`main`에서 분기) | 17~22장 |

```bash
git clone <repo-url>
cd <repo-name>

# 한 턴 대화 앱 (main)
git checkout main

# 멀티턴 + 타로봇 (tarot)
git checkout tarot
```

---

## 디렉토리 구조

```
.
├── app/                  # 안드로이드 앱 모듈
├── native/               # NDK·JNI 코드, llama.cpp 빌드 스크립트·산출물
│   ├── jni/              # JNI 바인딩 원본 코드
│   ├── scripts/          # 빌드 스크립트
│   ├── prebuilt/         # ABI별 빌드된 .so
│   └── llama.cpp.version # 사용한 llama.cpp 커밋·태그
├── ml/                   # 파이썬 ML 파이프라인
│   ├── 01-models/        # 모델 다운로드·토크나이저 탐색
│   ├── 02-finetune/      # LoRA 학습·어댑터 병합
│   └── 03-quantize/      # GGUF 변환·양자화
└── docs/                 # 챕터별 보충 자료
```

---

## 환경 준비

### 안드로이드
- Android Studio Iguana 이상
- Android NDK r26 이상
- minSdk 26 (Android 8.0) 이상

### Python (ML 파이프라인)
- Python 3.10 이상
- 가상환경 권장
  ```bash
  cd ml/02-finetune
  python -m venv .venv
  source .venv/bin/activate
  pip install -r requirements.txt
  ```

### llama.cpp
본 저장소에는 llama.cpp 소스가 포함되지 않습니다. 직접 클론합니다.

```bash
git clone https://github.com/ggerganov/llama.cpp
cd llama.cpp
git checkout $(cat ../native/llama.cpp.version)
```

빌드는 `native/scripts/build_llama.sh`를 사용하거나, 시간을 절약하려면 `native/prebuilt/`의 산출물을 그대로 사용합니다.

---

## 빠른 시작

```bash
# 1. 모델 다운로드 (Qwen 3.5 0.8B)
python ml/01-models/download.py

# 2. (선택) LoRA 학습
python ml/02-finetune/train_lora.py \
  --base-model ml/01-models/checkpoints/qwen3.5-0.8b \
  --data ml/02-finetune/data/general_sample.jsonl \
  --output-dir ml/02-finetune/outputs/

# 3. 어댑터 병합
python ml/02-finetune/merge_adapter.py

# 4. GGUF 변환·양자화 (Q4_K_M 권장)
bash ml/03-quantize/convert_to_gguf.sh

# 5. llama.cpp 안드로이드 빌드 (또는 prebuilt 사용)
bash native/scripts/build_llama.sh

# 6. Android Studio에서 app 모듈 실행
```

---

## 라이선스

본 저장소의 코드는 (라이선스 결정 예정)에 따라 배포됩니다.
llama.cpp 부분은 원저작자(MIT)의 라이선스를 따릅니다.

---

## 문의·기여

도서 정오표·예제 코드 관련 이슈는 GitHub Issues에 등록 부탁드립니다.
