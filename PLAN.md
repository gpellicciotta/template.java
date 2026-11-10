# Project Planning
An overview of all tasks and their planning.

---

## Next Milestone
- [ ] T0032 Fix the nine pre-existing lint-markdown MD019 findings and run lint-markdown in devops.py check.
- [x] T0031 Make create work in checkouts without tracked plan data, so the scaffold test passes in CI again.
- [ ] T0033 Move agent instructions into AGENTS.md per the v2 structure, reduce CLAUDE.md to an import, and fix deploy advice.
- [ ] T0034 [needs: T0033] Restructure devops.md into the v2 section layout, sync the README layout, and fix paths and placeholders.
- [ ] T0035 [needs: T0032, T0033] Finish Markdown compliance: bold labels, backticked file names, and a lint exclusion for finalized changelog sections.
- [ ] T0036 Fix the active changelog entries and give the scaffolded changelog entry an area prefix.
- [ ] T0037 Align the CLI with v2: exit code 2, logging flags, --format, create --dry-run, and copyright line.
- [ ] T0038 Write log file timestamps in UTC.
- [ ] T0039 [needs: T0037, T0038, T0033] Split tests into fast and slow suites with timeouts, parallel safety, a jar end-to-end test, and leak guard.
- [ ] T0040 [needs: T0033] Move the devops script tests to pytest, add Ruff checks, and test the minimum supported Python in CI.

---

## Backlog
- [x] T0030 Replace scripts/devops.py with a thin DevopsCli script on the GradleProject adapter, pinning the released support-python v2.0.0.
