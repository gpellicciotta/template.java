---
id: T0035
owner: "@claude-code"
needs: []
branch: —
worktree: —
status: completed
started_at: "2026-11-10T17:15:13Z"
ended_at: "2026-11-10T17:17:56Z"
review: none
preview: none
protocol: adhoc
completion-commit: 93eb8260335690f70d9add71bc06936a1930d5ca
---
# T0035: Finish Markdown compliance: replace bold bullet labels and link backticked repository file names in prose.

## Goals
Replace the 23 bold bullet lead-in labels in docs/requirements.md, docs/devops.md, and agent files with plain bullets or nested lists.
Turn backticked repository file names in prose into `[basename](path)` links, including ones lint-markdown misses in README.md and docs.
Keep code spans for deleted files and inside code blocks.
CHANGELOG.md stays excluded from lint-markdown, as decided on 2026-11-10 and configured by T0032.

Completion check: `python scripts/devops.py check` and `lint-markdown check` both exit 0.

Size estimate: 5 files, 190 lines

### Declared Files
- README.md
- docs/devops.md
- docs/requirements.md
- docs/multi-module.md
- AGENTS.md

## Task Execution Steps
- [x] **[Doc]**       Replace bold bullet labels with plain bullets or nested lists.
- [x] **[Doc]**       Convert backticked repository file names in prose into Markdown links.
- [x] **[Verify]**    Run lint-markdown check and python scripts/devops.py check, both exiting 0.

## Execution Log
- [2026-11-10] **[Doc]**
  Companion task file scaffolded at task creation.

- [2026-11-10] **[Doc]**
  Replaced 21 bold bullet labels with plain bullets, keeping AGENTS.md TODO status markers, and turned bold README section references into anchor links.

- [2026-11-10] **[Doc]**
  Linked backticked repository file names in prose, keeping code spans for commands, untracked plan and build paths, and derived-project paths.

- [2026-11-10] **[Verify]**
  Recorded task outcomes; lint-markdown, devops.py check, and a relative link scan passed.
  - Files touched: 5.
  - Lines touched: 123.
  - Reopenings: 0.

- [2026-11-10] **[Complete]**
  Task completed successfully.

