---
id: T0015
owner: '@gemini'
needs: []
branch: task/T0015-review-the-java-projects-hinolugi-support-java-hinolugi
worktree: work/T0015-review-the-java-projects-hinolugi-support-java-hinolugi
status: completed
started: 2026-09-07
ended: 2026-09-07
---

# T0015: Review hinolugi-support.java, hinolugi-counters, and hinolugi-auth for structural differences

## Goals

Review hinolugi-support.java, hinolugi-counters, and hinolugi-auth for generic structural gaps against this template. File separate backlog tasks for @gio review on each major topic, excluding single-project specifics.

## Task Execution Steps

- [x] **[Read]**      Inspect root configs, Gradle builds, conventions, docs, and scripts across all four projects.
- [x] **[Decide]**    Filter out single-project specifics and identify recurring generic structural gaps.
- [x] **[Verify]**    Verify attributions and generic suitability with an independent review subagent.
- [x] **[Doc]**       File 10 backlog tasks for @gio in TODO.md and document findings in this task file.

## Execution Log

- [2026-09-07] **[Read]**
  Inspected configurations, build files, convention plugins, docs, and scripts across all four repositories.
  - Analyzed hinolugi-support.java, hinolugi-counters, and hinolugi-auth against current template.

- [2026-09-07] **[Decide]**
  Identified 10 generic structural gaps and filtered out single-project specifics.
  - Excluded Cloud SQL, Cloud Run, Dockerfiles, and client SDKs as single-project specifics.

- [2026-09-07] **[Verify]**
  A review subagent verified attributions across all four codebases and flagged corrections.
  - Caught that hinolugi-auth never used its declared JetBrains nullability annotations dependency.
  - Identified additional gaps: .gitignore missing .env and bin/, and gradle.properties copyright.

- [2026-09-07] **[Doc]**
  Filed 10 backlog TODOs (T0020-T0029) for @gio review in TODO.md and updated Next ID.

## Validation Record

No code changes to template build or runtime behavior were made. All 10 tasks were validated against actual codebase contents.

**[Complete]** Filed 10 backlog TODOs (T0020-T0029) for @gio review documenting generic structural differences across projects.
