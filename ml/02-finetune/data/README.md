# 학습 데이터 양식

이 디렉토리는 Qwen 3.5 0.8B Instruct에 LoRA 파인튜닝용 데이터를 둔다.

## 파일 종류

| 위치 | 용도 | 상태 |
|---|---|---|
| `few_shots/*.jsonl` | 사용자가 직접 작성한 소규모 시드 예시 | 준비 중 |
| `sample.jsonl` | 양식 확인용 더미 예제 | ✓ |
| `general.jsonl` | 일반 챗 LoRA 학습 데이터 (main 브랜치) | 미생성 |
| `tarot.jsonl` | 타로 도메인 LoRA 학습 데이터 (tarot 브랜치) | 미생성 |

`few_shots/`에 사용자 시드가 들어오면 그것을 토대로 대량 데이터를 생성해 `general.jsonl` 또는 `tarot.jsonl`을 채운다 (생성 흐름은 책 8장에서 다룸).

가중치·체크포인트·생성된 학습 결과물은 모두 `.gitignore` 처리되며, 커밋되는 것은 본 README와 양식 예시(`sample.jsonl`)뿐이다. **실제 학습 데이터는 내용 검토 후 별도 커밋**한다.

## 양식

JSONL (한 줄에 한 JSON 객체). 객체 스키마는 다음과 같다.

```json
{
  "messages": [
    {"role": "system",    "content": "..."},
    {"role": "user",      "content": "..."},
    {"role": "assistant", "content": "..."}
  ]
}
```

### 필드 규칙

- **`messages`** (배열, 필수)
  - 각 원소는 `{role, content}` 형태
  - `role`은 다음 중 하나: `"system"`, `"user"`, `"assistant"`
  - `content`는 한국어/영어 평문. 줄바꿈은 `\n`으로 인코딩
- **`system`**
  - 0회 또는 1회. 들어가는 경우 `messages[0]`에 위치
  - 페르소나, 답변 형식, 거절 정책 등
  - 비워두면 모델 기본 시스템 프롬프트 사용
- **`user` / `assistant`**
  - 1회 이상 교대로 등장
  - 한 턴 학습은 `user` 1개 + `assistant` 1개
  - 멀티턴 학습은 `user`/`assistant` 쌍이 여러 번 반복

### 학습 시 동작

`train_lora.py`는 본 양식의 jsonl을 받아 모델의 chat template을 적용해 토큰화한다. `assistant` 응답 부분만 loss 계산 대상이며, `system`·`user` 부분은 마스킹된다.

## few-shots 작성 가이드 (사용자 작성용)

`few_shots/` 아래에 도메인별로 jsonl 파일을 둔다. 예:

- `few_shots/persona_general.jsonl` — 일반 챗 톤·말투 시드
- `few_shots/persona_tarot.jsonl` — 타로 답변 형식 시드

각 파일은 5~30개 정도의 고품질 예시로 구성한다. 파일 자체에 `system` 메시지를 일관되게 넣으면, 데이터 생성 단계에서 그 시스템 프롬프트를 보존한다.

## 검증

학습 전에 jsonl 파일을 빠르게 검증하려면 다음을 실행한다 (검증 스크립트는 8장 작업에 추가).

```bash
python ml/02-finetune/validate_data.py ml/02-finetune/data/general.jsonl
```
