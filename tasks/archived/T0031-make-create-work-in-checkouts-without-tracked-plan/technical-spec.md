# Scaffold Without Tracked Plan Data

## Technical Specifications
### Tests
- Unit: `ScaffoldTest` calls `findTemplateRoot(start)` below a temporary template tree without `PLAN.md` and gets its root.
- Unit: `createScaffoldsRenamedProject` asserts the destination has no `PLAN.md`, `.plan/`, or `plan.code-workspace`.
- Unit: `helpVerboseOptionIncludesDetails` asserts the `create` details name `plan init --store branch`, not `PLAN.md`.
- Integration: CI on main runs `python scripts/devops.py check` green on a checkout without plan data.

Completion check: `python scripts/devops.py check`, then green CI on main.

### Declared Files
- `src/main/java/myproject/Scaffold.java`
- `src/test/java/myproject/CliTest.java`
- `src/test/java/myproject/ScaffoldTest.java` (new)
- `src/main/java/myproject/Cli.java`
- `docs/requirements.md`
- `gradle.properties` (version bump to 3.0.0-pre)
- `README.md`
- `docs/devops.md`
- `CLAUDE.md`
- `CHANGELOG.md`
