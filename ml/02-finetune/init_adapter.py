"""
학습 없는 LoRA 어댑터 초기화 (책 8.1 보조).

용도: 학습 데이터 없이 LoRA → 병합 → GGUF 변환 → 모바일 추론까지의
전체 파이프라인을 검증한다. peft의 LoRA는 B 행렬을 0으로 초기화하므로
B·A = 0이 되어, 학습되지 않은 어댑터는 베이스 모델과 동일한 출력을 낸다.

사용:
    python ml/02-finetune/init_adapter.py
"""

import argparse
from pathlib import Path

from peft import LoraConfig, get_peft_model
from transformers import AutoModelForCausalLM


def parse_args() -> argparse.Namespace:
    p = argparse.ArgumentParser()
    here = Path(__file__).resolve().parent
    p.add_argument(
        "--base-model",
        default=str(here.parent / "01-models" / "checkpoints" / "qwen3.5-0.8b"),
    )
    p.add_argument(
        "--output-dir",
        default=str(here / "outputs" / "lora-init"),
    )
    p.add_argument("--lora-r", type=int, default=16)
    p.add_argument("--lora-alpha", type=int, default=32)
    p.add_argument(
        "--target-modules",
        nargs="+",
        default=["q_proj", "v_proj"],
        help="LoRA를 적용할 어텐션 프로젝션 이름",
    )
    p.add_argument("--lora-dropout", type=float, default=0.0)
    return p.parse_args()


def main() -> None:
    args = parse_args()

    output_dir = Path(args.output_dir)
    output_dir.mkdir(parents=True, exist_ok=True)

    print(f"[로드] base={args.base_model}")
    model = AutoModelForCausalLM.from_pretrained(args.base_model, dtype="auto")

    config = LoraConfig(
        r=args.lora_r,
        lora_alpha=args.lora_alpha,
        target_modules=list(args.target_modules),
        lora_dropout=args.lora_dropout,
        bias="none",
        task_type="CAUSAL_LM",
    )

    peft_model = get_peft_model(model, config)
    peft_model.print_trainable_parameters()

    print(f"[저장] {output_dir}")
    peft_model.save_pretrained(str(output_dir))


if __name__ == "__main__":
    main()
