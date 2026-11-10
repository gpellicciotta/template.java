---
id: T0034
owner: ~
needs: ['T0033']
branch: —
worktree: —
status: ready
started_at: —
ended_at: —
review: none
preview: none
protocol: adhoc
---
# T0034: Restructure devops.md into the v2 section layout, sync the README layout, and fix paths and placeholders

## Goals
Restructure docs/devops.md to open with `Development Environment` (Prerequisites, Setup) and `Versioning and Releasing` (Updating Versions, Making a Release), keeping the other sections afterwards.
State under Updating Versions that gradle.properties holds the version and `plan` task completion bumps it.
Replace the `C:\Dev-Projects` create examples with a `{{output-dir}}` placeholder, and fix links to renamed anchors.
Convert `<version>`, `{user.home}`, and `{name}` style placeholders in README.md, devops.md, and requirements.md to `{{kebab-case}}`.
Sync the README layout with the tracked tree, adding ScaffoldTest.java, library-publishing.md, .gitattributes, and .vscode/settings.json.

Completion check: `lint-markdown check README.md docs AGENTS.md` exits 0, plus review against the project-setup and general guidelines.

Size estimate: 5 files, 250 lines

### Declared Files
- docs/devops.md
- docs/requirements.md
- README.md
- AGENTS.md
- CHANGELOG.md

## Task Execution Steps
- [ ] **[Read]**      Read the project-setup devops.md section rules and the general placeholder and path rules.
- [ ] **[Doc]**       Reorder devops.md sections and add the Updating Versions and Making a Release content.
- [ ] **[Doc]**       Replace absolute paths and old-style placeholders, and fix links to renamed anchors.
- [ ] **[Doc]**       Sync the README directory layout with git ls-files output.
- [ ] **[Doc]**       Add a Docs entry under the active CHANGELOG.md heading.
- [ ] **[Verify]**    Run lint-markdown on README.md, docs, and AGENTS.md.

## Execution Log
- [2026-11-10] **[Doc]**
  Companion task file scaffolded at task creation.
