---
id: T0004
owner: '@claude'
needs: []
branch: task/T0004-review-the-java-projects-hinolugi-support-java-and
worktree: work/T0004-review-the-java-projects-hinolugi-support-java-and
status: completed
started: 2026-09-06
ended: 2026-09-06
---

# T0004: Review hinolugi-support.java and hinolugi-auth for structural gaps against this template

## Goals

Compare hinolugi-support.java and hinolugi-auth's project structure against this template as if they'd
been scaffolded from it. File one backlog TODO per major topic, each flagged for @gio's decision, excluding
anything only relevant to one specific project.

## Task Execution Steps

- [x] **[Read]**      Diff directory trees, `.gitignore`, `settings.gradle`, `build.gradle`, `CHANGELOG.md`.
- [x] **[Read]**      Inspect hinolugi-auth's `buildSrc` convention plugins against `docs/multi-module.md`.
- [x] **[Read]**      Check template's `Cli.java`/test block/jar manifest against the Java/logging guidelines.
- [x] **[Decide]**    Judge which gaps recur in both real projects (generic) vs. appear in only one (project-specific).
- [x] **[Doc]**       File one backlog TODO per major topic (T0005-T0012), owner `@gio`, status `[?]`.
- [x] **[Verify]**    Have a sub-agent critically review the filed TODOs before integrating.

## Execution Log

- [2026-09-06] **[Read]**
  Compared file trees, `.gitignore`, `build.gradle`, `settings.gradle`/`buildSrc`, `Cli.java`, and `CHANGELOG.md`
  across all three projects.
  - Both real projects independently converged on several practices absent from the template.
  - `docs/multi-module.md`'s buildSrc pattern matches hinolugi-auth's actual convention plugins almost verbatim
    (only its private GitHub Packages repo credentials differ, correctly project-specific) — no gap there.

- [2026-09-06] **[Decide]**
  Selected 8 gaps against the template's own stated guidelines and filed them as T0005-T0012 in Backlog.
  - Excluded: hinolugi-auth's Dockerfile/Cloud SQL/social-identity-provider config, hinolugi-support.java's
    Maven-publish credentials and Jetty/mail dependencies — all single-project-specific, not template material.
  - Excluded: multi-module buildSrc pattern, since it already matches `docs/multi-module.md` almost verbatim.

- [2026-09-06] **[Doc]**
  Filed T0005 (Spotless formatting gate), T0006 (`testLogging.showStandardStreams`), T0007 (jar manifest
  commit-hash suffix), T0008 (no reference logging implementation despite the guideline),
  T0009 (`.gitignore` missing `pickup-work-loop*.log`), T0010 (`.idea/` blanket-ignore vs. real projects'
  selective checked-in pattern), T0011 (group/version/vendor metadata inlined vs. `gradle.properties`),
  T0012 (undocumented optional library-publishing pattern) — all `[?]`, owner `@gio`, in Backlog.

- [2026-09-06] **[Verify]**
  A review sub-agent caught that T0005 and T0007 as first drafted misattributed Spotless and manifest
  Specification-* attributes to "both real projects" when only hinolugi-support.java has them; corrected
  both TODOs to state accurate per-project attribution before integrating.

## Validation Record

No code changes to this template's build/runtime behavior were made — this task is a documentation/analysis
deliverable. `gradlew build` was not re-run since no source or build files changed.

**[Complete]** Filed 8 backlog TODOs (T0005-T0012) documenting structural gaps between this template and two
real projects derived from it, each awaiting @gio's decision before implementation.
