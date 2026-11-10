# Project Planning
An overview of all tasks and their planning.

---

## Next Milestone
- [x] T0032 Fix the nine pre-existing lint-markdown MD019 findings and run lint-markdown in devops.py check.
- [x] T0031 Make create work in checkouts without tracked plan data, so the scaffold test passes in CI again.
- [x] T0033 Move agent instructions into AGENTS.md per the v2 structure, reduce CLAUDE.md to an import, and fix deploy advice.
- [x] T0034 Restructure devops.md into the v2 section layout, sync the README layout, and fix paths and placeholders.
- [x] T0035 Finish Markdown compliance: replace bold bullet labels and link backticked repository file names in prose.
- [x] T0036 Fix the active changelog entries and give the scaffolded changelog entry an area prefix.
- [ ] T0037 Align the CLI with v2: exit code 2, logging flags, --format, create --dry-run, and copyright line.
- [ ] T0038 Write log file timestamps in UTC.
- [ ] T0039 [needs: T0037, T0038] Split tests into fast and slow suites with timeouts, parallel safety, a jar end-to-end test, and leak guard.
- [ ] T0040 Move the devops script tests to pytest, add Ruff checks, and test the minimum supported Python in CI.
- [ ] T0041 Move multi-module.md and library-publishing.md into docs/tutorials/ and update every link to them.

---

## Backlog
- [x] T0030 Replace scripts/devops.py with a thin DevopsCli script on the GradleProject adapter, pinning the released support-python v2.0.0.
