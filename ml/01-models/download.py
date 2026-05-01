"""
Qwen 3.5 0.8B Instruct 모델을 허깅페이스에서 다운로드한다.

책 5.4절 "허깅페이스에서 모델 받기" 예제.

사용:
    pip install -r ml/01-models/requirements.txt
    python ml/01-models/download.py
"""

from pathlib import Path

from huggingface_hub import snapshot_download


MODEL_ID = "Qwen/Qwen3.5-0.8B"
LOCAL_DIR = Path(__file__).parent / "checkpoints" / "qwen3.5-0.8b"


def human_size(num_bytes: int) -> str:
    for unit in ("B", "KB", "MB", "GB"):
        if num_bytes < 1024:
            return f"{num_bytes:,.1f} {unit}"
        num_bytes /= 1024
    return f"{num_bytes:,.1f} TB"


def main() -> None:
    LOCAL_DIR.mkdir(parents=True, exist_ok=True)
    print(f"[다운로드] {MODEL_ID}")
    print(f"          → {LOCAL_DIR}")
    print()

    snapshot_download(
        repo_id=MODEL_ID,
        local_dir=str(LOCAL_DIR),
    )

    print()
    print("[완료] 다운로드된 파일")
    total = 0
    for f in sorted(LOCAL_DIR.rglob("*")):
        if not f.is_file():
            continue
        size = f.stat().st_size
        total += size
        rel = f.relative_to(LOCAL_DIR)
        print(f"  {rel}  ({human_size(size)})")
    print()
    print(f"총 크기: {human_size(total)}")


if __name__ == "__main__":
    main()
