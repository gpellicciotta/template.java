---
id: T0032
owner: "@claude-code"
needs: []
branch: —
worktree: —
status: active
started_at: "2026-11-10T16:20:14Z"
ended_at: —
review: agent
preview: none
protocol: standard
review_status: pending
review_round: 1
---
# T0032: Fix the nine pre-existing lint-markdown MD019 findings and run lint-markdown in devops.py check

## Goals
Fix the nine lint-markdown MD019 findings outside CHANGELOG.md by turning repository file references in prose into Markdown links.
Exclude CHANGELOG.md from lint-markdown through a root .lint-markdown.json, as decided on 2026-11-10, on top of lint-markdown's default excludes.
Make `devops.py lint` and `devops.py check` run `lint-markdown check` before their Gradle tasks, so CI enforces it.

## Task Execution Steps
- [x] **[Implement]** Add devops script tests requiring lint and check to run lint-markdown before Gradle.
- [x] **[Verify]**    Run the devops script tests and confirm the new tests fail, capturing the output.
- [x] **[Implement]** Run lint-markdown from devops.py lint and check, and add the CHANGELOG.md exclusion.
- [x] **[Doc]**       Fix the nine MD019 findings and document the lint step in devops.md and CHANGELOG.md.
- [x] **[Verify]**    Run the script tests, lint-markdown check, and devops.py check, recording output in validation-proof.md.
- [ ] **[Review]**    Obtain review approval of the finished work before completion.

## Execution Log
- [2026-11-10] **[Doc]**
  Companion task file scaffolded at task creation.

- [2026-11-10] **[Implement]**
  Added TemplateProject to devops.py so lint and check run lint-markdown first; added .lint-markdown.json excluding CHANGELOG.md; fixed the nine MD019 findings.

- [2026-11-10] **[Doc]**
  Touched undeclared README.md to name the Markdown lint step.
  - Size note: nine counted files cross the typical line by one.
  - Splitting off one README line would not merge usefully.

- [2026-11-10] **[Verify]**
  Recorded red and green runs in validation-proof.md; script tests, lint-markdown, and devops.py check all exit 0.
  - Files touched: 9.
  - Lines touched: 98.
  - Reopenings: 0.

- [2026-11-10] **[Review]**
  Round 1 requested (mode: agent).

- [2026-11-10] **[Implement]**
  Reworked per review: .lint-markdown.json lists only CHANGELOG.md, devops.md and the spec describe the merge correctly, and the lint-failure test checks the single call.
  - Files touched: 9.
  - Lines touched: 109.
  - Reopenings: 0.

