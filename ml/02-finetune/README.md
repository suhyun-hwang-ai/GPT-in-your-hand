# 02-finetune — LoRA 파인튜닝

책 7~8장 대응.

## 파일 (예정)

| 파일 | 챕터 | 설명 |
|---|---|---|
| `requirements.txt` | 8장 | transformers, peft, datasets, accelerate |
| `data/general_sample.jsonl` | 8장 | 일반 챗 학습 데이터 (main 브랜치) |
| `data/tarot_dataset.jsonl` | 19장 | 타로 도메인 데이터 (tarot 브랜치) |
| `train_lora.py` | 8장 | LoRA 학습 스크립트 |
| `merge_adapter.py` | 8장 | 어댑터를 베이스에 병합 |

## 산출물 위치

```
outputs/
└── lora-{timestamp}/    # 학습 체크포인트 (.gitignore)
```
