# ml/ — 파이썬 ML 파이프라인

책 5~10장에서 다루는 모델 다운로드, 토크나이저 탐색, LoRA 파인튜닝,
GGUF 양자화 스크립트가 들어 있다.

| 디렉토리 | 책 챕터 | 내용 |
|---|---|---|
| `01-models/` | 5~6장 | 모델 다운로드, 토크나이저 탐색 |
| `02-finetune/` | 7~8장 | LoRA 학습, 어댑터 병합 |
| `03-quantize/` | 9~10장 | GGUF 변환, 양자화 레벨 비교 |

## 환경

Python 3.10 이상. 가상환경 권장.

```bash
cd ml/02-finetune
python -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
```

가중치·체크포인트·중간 산출물은 저장소에 포함하지 않는다 (`.gitignore` 처리).
