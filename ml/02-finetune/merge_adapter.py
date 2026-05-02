"""
LoRA 어댑터를 베이스 모델에 병합 (책 8.4).

병합 결과는 단일 모델 디렉토리이며, GGUF 변환 입력으로 그대로 넘길 수 있다.

사용:
    python ml/02-finetune/merge_adapter.py \\
        --adapter-dir ml/02-finetune/outputs/lora-init \\
        --output-dir  ml/02-finetune/outputs/merged
"""

import argparse
import shutil
from pathlib import Path

from peft import PeftModel
from transformers import AutoModelForCausalLM

# 머지 후 GGUF 변환기가 인식할 수 있도록 base에서 그대로 복사할 파일들.
# AutoModelForCausalLM 저장 시 멀티모달 config가 텍스트 전용으로 flatten되거나
# 토크나이저가 재직렬화되며 hash가 달라지는 문제를 회피한다.
_FILES_TO_COPY_FROM_BASE = (
    "config.json",
    "tokenizer.json",
    "tokenizer_config.json",
    "vocab.json",
    "merges.txt",
    "chat_template.jinja",
    "special_tokens_map.json",
)


def parse_args() -> argparse.Namespace:
    p = argparse.ArgumentParser()
    here = Path(__file__).resolve().parent
    p.add_argument(
        "--base-model",
        default=str(here.parent / "01-models" / "checkpoints" / "qwen3.5-0.8b"),
    )
    p.add_argument("--adapter-dir", required=True)
    p.add_argument("--output-dir", required=True)
    return p.parse_args()


def main() -> None:
    args = parse_args()

    output_dir = Path(args.output_dir)
    output_dir.mkdir(parents=True, exist_ok=True)

    print(f"[로드] base={args.base_model}")
    base = AutoModelForCausalLM.from_pretrained(args.base_model, dtype="auto")

    print(f"[로드] adapter={args.adapter_dir}")
    peft_model = PeftModel.from_pretrained(base, args.adapter_dir)

    print("[병합] merge_and_unload")
    merged = peft_model.merge_and_unload()

    print(f"[저장] {output_dir}")
    merged.save_pretrained(str(output_dir))

    # base의 원본 config·tokenizer 파일을 그대로 복사 (GGUF 변환 호환성)
    base_path = Path(args.base_model)
    print(f"[복사] base의 config·tokenizer → merged ({len(_FILES_TO_COPY_FROM_BASE)}개)")
    for name in _FILES_TO_COPY_FROM_BASE:
        src = base_path / name
        if src.exists():
            shutil.copy2(src, output_dir / name)


if __name__ == "__main__":
    main()
