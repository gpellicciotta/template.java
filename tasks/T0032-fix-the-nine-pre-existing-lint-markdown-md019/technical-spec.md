### Declared Files
- scripts/devops.py
- scripts/tests/test_devops.py
- .lint-markdown.json
- CLAUDE.md
- docs/devops.md
- docs/multi-module.md
- docs/requirements.md
- CHANGELOG.md

### Requirements
- Subclass `GradleProject` in devops.py; `lint` and `check` run `lint-markdown check` in the repository root first.
- Stop at the first failure: a failing Markdown lint skips the Gradle tasks and fails the action.
- Resolve `lint-markdown` from the running interpreter's scripts directory, so `.venv` and CI both work.
- `.lint-markdown.json` lists only `CHANGELOG.md`; lint-markdown adds config excludes to its own defaults.

### Tests
- Unit: with a recording runner, `lint` runs `lint-markdown check` and then the Gradle lint tasks.
- Unit: with a recording runner, `check` runs `lint-markdown check` and then `build`; a lint failure skips `build`.
- Integration: `python scripts/devops.py check` runs lint-markdown and the Gradle build, exiting 0.

Size estimate: 8 files, 120 lines

Completion check: `.venv/Scripts/python -m unittest discover -s scripts/tests` and `python scripts/devops.py check` exit 0.
