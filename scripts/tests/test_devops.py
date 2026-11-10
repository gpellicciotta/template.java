"""Tests of scripts/devops.py; run with `python -m unittest discover -s scripts/tests` after the bootstrap."""

from __future__ import annotations

import contextlib
import importlib.util
import io
import subprocess
import sys
import tempfile
import unittest
from pathlib import Path

from support_python.devops import DevopsCli, GradleProject

REPO_ROOT = Path(__file__).resolve().parents[2]
DEVOPS_SCRIPT = REPO_ROOT / "scripts" / "devops.py"


def load_devops():
    spec = importlib.util.spec_from_file_location("devops_script", DEVOPS_SCRIPT)
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


def write_versions(root: Path, gradle_version: str, changelog_version: str) -> None:
    (root / "gradle.properties").write_text(f"version={gradle_version}\n", encoding="utf-8")
    (root / "CHANGELOG.md").write_text(f"# Changelog\n\n## v{changelog_version}\n- Entry.\n", encoding="utf-8")


class DevopsScriptTest(unittest.TestCase):
    def test_builds_devops_cli_over_gradle_project_at_repo_root(self) -> None:
        cli = load_devops().build_cli()
        self.assertIsInstance(cli, DevopsCli)
        self.assertIsInstance(cli.project, GradleProject)
        self.assertEqual(REPO_ROOT, cli.project.root)

    def test_release_delegates_to_do_full_release_with_confirmation_property(self) -> None:
        project = load_devops().build_cli().project
        self.assertEqual("doFullRelease", project.release_task)
        self.assertEqual(["-PreleaseConfirmed=yes"], project.release_args)

    def test_help_lists_standard_actions(self) -> None:
        result = subprocess.run(
            [sys.executable, str(DEVOPS_SCRIPT), "help"], capture_output=True, text=True, encoding="utf-8", check=False
        )
        self.assertEqual(0, result.returncode, result.stderr)
        for action in ("check", "build", "test", "clean", "versions", "release", "lint", "format"):
            self.assertRegex(result.stdout, rf"(?m)^\s+{action}\b")

    def test_versions_exits_zero_when_versions_agree(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            write_versions(root, "1.2.3-pre", "1.2.3-pre")
            with contextlib.redirect_stdout(io.StringIO()), contextlib.redirect_stderr(io.StringIO()):
                self.assertEqual(0, load_devops().build_cli(root).run(["versions", "--log-file", str(root / "devops.log")]))

    def test_versions_exits_one_when_versions_drift(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            write_versions(root, "1.2.4-pre", "1.2.3-pre")
            with contextlib.redirect_stdout(io.StringIO()), contextlib.redirect_stderr(io.StringIO()):
                self.assertEqual(1, load_devops().build_cli(root).run(["versions", "--log-file", str(root / "devops.log")]))


if __name__ == "__main__":
    unittest.main()
