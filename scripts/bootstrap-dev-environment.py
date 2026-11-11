#!/usr/bin/env python3
"""One-shot dev environment bootstrap: init git if needed, install scripts/requirements.txt into .venv, run a full Gradle build.

Standard library only, so it runs on a bare checkout before support-python is installed.
"""

import subprocess
import sys
from collections.abc import Sequence
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
VENV_DIR = ROOT / ".venv"
VENV_PYTHON = VENV_DIR / ("Scripts/python.exe" if sys.platform == "win32" else "bin/python")


def run(argv: Sequence[object]) -> None:
    """Print and run one command from the repository root, raising on non-zero exit."""
    print(f"$ {' '.join(str(arg) for arg in argv)}", flush=True)
    subprocess.run([str(arg) for arg in argv], cwd=ROOT, check=True)


def main() -> int:
    """Initialize Git if needed, populate .venv from scripts/requirements.txt, and run a Gradle build."""
    if not (ROOT / ".git").exists():
        run(["git", "init"])

    if not VENV_PYTHON.is_file():
        run([sys.executable, "-m", "venv", VENV_DIR])
    requirements = ROOT / "scripts" / "requirements.txt"
    run([VENV_PYTHON, "-m", "pip", "install", "--quiet", "--disable-pip-version-check", "-r", requirements])

    gradlew = ROOT / ("gradlew.bat" if sys.platform == "win32" else "gradlew")
    run([gradlew, "build"])
    return 0


if __name__ == "__main__":
    try:
        sys.exit(main())
    except subprocess.CalledProcessError as exc:
        print(f"**[ERROR]** Bootstrap step failed with exit code {exc.returncode}: {' '.join(map(str, exc.cmd))}", file=sys.stderr)
        sys.exit(1)
