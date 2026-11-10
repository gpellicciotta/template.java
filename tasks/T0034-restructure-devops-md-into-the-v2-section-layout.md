---
id: T0034
owner: "@claude-code"
needs: []
branch: —
worktree: —
status: completed
started_at: "2026-11-10T16:33:10Z"
ended_at: "2026-11-10T16:39:32Z"
review: none
preview: none
protocol: adhoc
completion-commit: 15cecc13534a1b233f19b4dcc22fdc1348195cf4
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
- [x] **[Read]**      Read the project-setup devops.md section rules and the general placeholder and path rules.
- [x] **[Doc]**       Reorder devops.md sections and add the Updating Versions and Making a Release content.
- [x] **[Doc]**       Replace absolute paths and old-style placeholders, and fix links to renamed anchors.
- [x] **[Doc]**       Sync the README directory layout with git ls-files output.
- [x] **[Doc]**       Add a Docs entry under the active CHANGELOG.md heading.
- [x] **[Verify]**    Run lint-markdown on README.md, docs, and AGENTS.md.

## Execution Log
- [2026-11-10] **[Doc]**
  Companion task file scaffolded at task creation.

- [2026-11-10] **[Doc]**
  Reordered devops.md to open with Development Environment and Versioning and Releasing, adding Updating Versions and step-by-step Making a Release.

- [2026-11-10] **[Decided]**
  Documented that plan bumps only pyproject.toml, since lifecycle.bump_project_version ignores gradle.properties; openNextPreRelease raises the patch here.

- [2026-11-10] **[Doc]**
  Converted old-style placeholders to kebab-case mustache, replaced absolute create paths, and pointed links at the renamed anchors.

- [2026-11-10] **[Doc]**
  Synced the README layout with tracked files and added a Docs entry to the active CHANGELOG.md heading.

- [2026-11-10] **[Verify]**
  Recorded task outcomes; lint-markdown and devops.py check passed.
  - Files touched: 5.
  - Lines touched: 166.
  - Reopenings: 0.

- [2026-11-10] **[Complete]**
  Task completed successfully.

