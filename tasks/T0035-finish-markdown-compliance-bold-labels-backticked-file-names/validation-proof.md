## Walkthrough & Validation

### Summary
- Replaced 21 bold bullet labels in devops.md and requirements.md with plain bullets.
- Kept the `**[TODO]**` markers in AGENTS.md; they are status markers, not glossary labels.
- Linked backticked repository file names in prose across README.md, AGENTS.md, and docs.
- Replaced bold README section references with anchor links.
- Kept code spans for commands, untracked paths (`PLAN.md`, `.plan/`, `.venv`, `build/`), and derived-project paths.
- Left the `plan init` managed block in AGENTS.md untouched.

### Completion Check
Command: `.venv/Scripts/lint-markdown check README.md docs AGENTS.md` (exit code 0)

```text
All 7 markdown files passed linting.
lint exit=0
```

Command: `python scripts/devops.py check` (exit code 0), filtered to its lint-markdown and final Gradle lines.

```text
All 9 markdown files passed linting.
[... Gradle output omitted ...]
BUILD SUCCESSFUL in 3s
check exit=0
```

### Relative Link Check
Command: a Python scan resolving every relative link and Markdown anchor in the changed files (exit code 0).

```text
bad links: 0
```
