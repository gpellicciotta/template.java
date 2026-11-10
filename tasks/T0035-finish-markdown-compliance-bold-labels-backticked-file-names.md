---
id: T0035
owner: ~
needs: ['T0032', 'T0033']
branch: —
worktree: —
status: ready
started_at: —
ended_at: —
review: none
preview: none
protocol: adhoc
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
- [ ] **[Doc]**       Replace bold bullet labels with plain bullets or nested lists.
- [ ] **[Doc]**       Convert backticked repository file names in prose into Markdown links.
- [ ] **[Verify]**    Run lint-markdown check and python scripts/devops.py check, both exiting 0.

## Execution Log
- [2026-11-10] **[Doc]**
  Companion task file scaffolded at task creation.
