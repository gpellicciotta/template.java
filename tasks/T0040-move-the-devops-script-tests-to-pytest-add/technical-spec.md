### Declared Files
- scripts/tests/test_devops.py
- scripts/requirements.txt
- scripts/devops.py
- scripts/bootstrap-dev-environment.py
- scripts/pyproject.toml
- .github/workflows/ci.yml
- docs/devops.md
- AGENTS.md
- README.md
- CHANGELOG.md

### Tests
- Unit: the converted `scripts/tests/test_devops.py` cases pass under `pytest -n auto --timeout=30`.
- Integration: `.venv/Scripts/python -m pytest scripts/tests`, `ruff check scripts`, and `ruff format --check scripts` all exit 0; CI runs them on 3.9 and 3.x after an approved push.

Size estimate: 10 files, 180 lines

Size note: Above the typical file count because most files get only small command or lint fixes; splitting would leave CI inconsistent.

Completion check: `.venv/Scripts/python -m pytest scripts/tests`, `ruff check scripts`, and `ruff format --check scripts` exit 0.
