# Scaffold Without Tracked Plan Data

## Technical Specifications
### Tests
- Unit: `ScaffoldTest` calls `findTemplateRoot(start)` below a temporary template tree without `PLAN.md` and gets its root.
- Unit: `createScaffoldsRenamedProject` asserts the destination has a fresh `PLAN.md` and no `.plan/` or `plan.code-workspace`.
- Integration: CI on main runs `python scripts/devops.py check` green on a checkout without plan data.

Completion check: `python scripts/devops.py check`, then green CI on main.

### Declared Files
- `src/main/java/myproject/Scaffold.java`
- `src/test/java/myproject/CliTest.java`
- `src/test/java/myproject/ScaffoldTest.java` (new)
- `README.md`
- `docs/devops.md`
- `CLAUDE.md`
- `CHANGELOG.md`
