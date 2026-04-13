#!/usr/bin/env python3
from __future__ import annotations

import argparse
from pathlib import Path
import shutil

from PIL import Image


SUPPORTED_EXTENSIONS = {".jpg", ".jpeg", ".png", ".webp", ".bmp", ".gif", ".tiff"}


def resize_if_needed(image_path: Path, target_width: int, output_dir: Path | None) -> None:
    output_path = image_path if output_dir is None else output_dir / image_path.name

    with Image.open(image_path) as image:
        width, height = image.size

        if width <= target_width:
            if output_dir is not None:
                shutil.copy2(image_path, output_path)
                print(f"[COPIED] {image_path.name}: {width}x{height} -> {output_path}")
                return

            print(f"[SKIP] {image_path.name}: {width}x{height}")
            return

        scale = target_width / width
        target_height = int(round(height * scale))
        resized = image.resize((target_width, target_height), Image.Resampling.LANCZOS)
        resized.save(output_path)

        print(
            f"[RESIZED] {image_path.name}: {width}x{height} -> {target_width}x{target_height} ({output_path})"
        )


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Resize images in current directory if width exceeds target."
    )
    parser.add_argument(
        "target_width",
        nargs="?",
        default=512,
        type=int,
        help="Target max width in pixels (default: 512).",
    )
    parser.add_argument(
        "-o",
        "--output-dir",
        type=str,
        default=None,
        help="Output directory for resized/copied images. If omitted, images are overwritten.",
    )
    parser.add_argument(
        "-i",
        "--input-dir",
        type=str,
        default=None,
        help="Input directory containing images. Defaults to script directory.",
    )
    return parser.parse_args()


def main() -> None:
    args = parse_args()
    if args.target_width <= 0:
        raise ValueError("target_width must be a positive integer.")

    base_dir = Path(__file__).resolve().parent
    input_dir = base_dir
    if args.input_dir:
        input_dir = Path(args.input_dir)
        if not input_dir.is_absolute():
            input_dir = base_dir / input_dir
    if not input_dir.exists() or not input_dir.is_dir():
        raise ValueError(f"input_dir does not exist or is not a directory: {input_dir}")

    output_dir: Path | None = None
    if args.output_dir:
        output_dir = Path(args.output_dir)
        if not output_dir.is_absolute():
            output_dir = base_dir / output_dir
        output_dir.mkdir(parents=True, exist_ok=True)

    image_files = sorted(
        path
        for path in input_dir.iterdir()
        if path.is_file() and path.suffix.lower() in SUPPORTED_EXTENSIONS
    )

    if not image_files:
        print("No supported images found in current directory.")
        return

    for image_path in image_files:
        resize_if_needed(image_path, args.target_width, output_dir)


if __name__ == "__main__":
    main()
