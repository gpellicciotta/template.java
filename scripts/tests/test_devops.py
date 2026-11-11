"""Tests of scripts/devops.py; run with `.venv/Scripts/python -m pytest scripts/tests` after the bootstrap."""

from __future__ import annotations

import importlib.util
import json
import py_compile
import re
import subprocess
import sys
from pathlib import Path
from types import ModuleType

import pytest
from support_python.devops import DevopsCli, GradleProject

REPO_ROOT = Path(__file__).resolve().parents[2]
DEVOPS_SCRIPT = REPO_ROOT / "scripts" / "devops.py"
BOOTSTRAP_SCRIPT = REPO_ROOT / "scripts" / "bootstrap-dev-environment.py"


@pytest.fixture(autouse=True)
def isolate_home(tmp_path: Path, monkeypatch: pytest.MonkeyPatch) -> None:
    home = tmp_path / "home"
    (home / ".config").mkdir(parents=True)
    (home / "AppData" / "Roaming").mkdir(parents=True)
    monkeypatch.setenv("HOME", str(home))
    monkeypatch.setenv("USERPROFILE", str(home))
    monkeypatch.setenv("XDG_CONFIG_HOME", str(home / ".config"))
    monkeypatch.setenv("APPDATA", str(home / "AppData" / "Roaming"))


def load_devops() -> ModuleType:
    spec = importlib.util.spec_from_file_location("devops_script", DEVOPS_SCRIPT)
    assert spec is not None and spec.loader is not None
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


def write_versions(root: Path, gradle_version: str, changelog_version: str) -> None:
    (root / "gradle.properties").write_text(f"version={gradle_version}\n", encoding="utf-8")
    (root / "CHANGELOG.md").write_text(f"# Changelog\n\n## v{changelog_version}\n- Entry.\n", encoding="utf-8")


def recording_project(tmp_path: Path, failing: str = "") -> tuple[list[list[str]], GradleProject]:
    calls: list[list[str]] = []

    def runner(argv, cwd, logger):
        calls.append(list(argv))
        return 1 if failing and Path(argv[0]).stem == failing else 0

    root = tmp_path / "project"
    root.mkdir()
    (root / ("gradlew.bat" if sys.platform == "win32" else "gradlew")).write_text("", encoding="utf-8")
    project = load_devops().build_cli(root).project
    project.runner = runner
    return calls, project


def command_names(argv: list[str]) -> list[str]:
    return [Path(argv[0]).stem, *argv[1:]]


def test_builds_devops_cli_over_gradle_project_at_repo_root() -> None:
    cli = load_devops().build_cli()
    assert isinstance(cli, DevopsCli)
    assert isinstance(cli.project, GradleProject)
    assert cli.project.root == REPO_ROOT


def test_release_delegates_to_do_full_release_with_confirmation_property() -> None:
    project = load_devops().build_cli().project
    assert project.release_task == "doFullRelease"
    assert project.release_args == ["-PreleaseConfirmed=yes"]


def test_lint_runs_lint_markdown_before_gradle_lint_tasks(tmp_path: Path) -> None:
    calls, project = recording_project(tmp_path)
    assert project.lint() is True
    assert command_names(calls[0]) == ["lint-markdown", "check"]
    assert calls[1][-3:] == ["spotlessCheck", "checkstyleMain", "checkstyleTest"]


def test_check_runs_lint_markdown_before_gradle_build(tmp_path: Path) -> None:
    calls, project = recording_project(tmp_path)
    assert project.check() is True
    assert command_names(calls[0]) == ["lint-markdown", "check"]
    assert calls[1][-1:] == ["build"]


def test_check_skips_gradle_build_when_lint_markdown_fails(tmp_path: Path) -> None:
    calls, project = recording_project(tmp_path, failing="lint-markdown")
    assert project.check() is False
    assert [command_names(call) for call in calls] == [["lint-markdown", "check"]]


def test_lint_markdown_config_excludes_changelog_and_sets_github_owners() -> None:
    config = json.loads((REPO_ROOT / ".lint-markdown.json").read_text(encoding="utf-8"))
    assert config == {"exclude": ["CHANGELOG.md"], "github-owners": ["gpellicciotta"]}


def test_help_lists_standard_actions() -> None:
    result = subprocess.run(
        [sys.executable, str(DEVOPS_SCRIPT), "help"],
        capture_output=True,
        text=True,
        encoding="utf-8",
        check=False,
    )
    assert result.returncode == 0, result.stderr
    for action in ("check", "build", "test", "clean", "versions", "release", "lint", "format"):
        assert re.search(rf"(?m)^\s+{action}\b", result.stdout)


def test_versions_exits_zero_when_versions_agree(tmp_path: Path, capsys: pytest.CaptureFixture[str]) -> None:
    root = tmp_path / "repo"
    root.mkdir()
    write_versions(root, "1.2.3-pre", "1.2.3-pre")
    exit_code = load_devops().build_cli(root).run(["versions", "--log-file", str(root / "devops.log")])
    capsys.readouterr()
    assert exit_code == 0


def test_versions_exits_one_when_versions_drift(tmp_path: Path, capsys: pytest.CaptureFixture[str]) -> None:
    root = tmp_path / "repo"
    root.mkdir()
    write_versions(root, "1.2.4-pre", "1.2.3-pre")
    exit_code = load_devops().build_cli(root).run(["versions", "--log-file", str(root / "devops.log")])
    capsys.readouterr()
    assert exit_code == 1


def test_missing_support_python_exits_with_v3_bootstrap_hint(monkeypatch: pytest.MonkeyPatch) -> None:
    for name in [key for key in sys.modules if key == "support_python" or key.startswith("support_python.")]:
        monkeypatch.delitem(sys.modules, name)
    monkeypatch.setitem(sys.modules, "support_python", None)
    monkeypatch.setitem(sys.modules, "support_python.devops", None)
    with pytest.raises(SystemExit) as exc_info:
        load_devops()
    assert "support-python v3 is missing" in str(exc_info.value)


def test_bootstrap_script_compiles(tmp_path: Path) -> None:
    output = py_compile.compile(str(BOOTSTRAP_SCRIPT), cfile=str(tmp_path / "bootstrap.pyc"), doraise=True)
    assert Path(output).is_file()
