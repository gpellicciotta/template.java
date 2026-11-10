## Walkthrough & Validation
Moved both guides into `docs/tutorials/` with `git mv` and fixed every reference.
The guides' cross-link stays valid; `multi-module.md` repository links gained one `../` level.
`docs/index.md` renamed its "Architecture & Design Notes" section to "Tutorials".

```text
> .venv/Scripts/lint-markdown check
All 9 markdown files passed linting.
EXIT=0
```

```text
> .venv/Scripts/python scripts/devops.py check
[... output before the build summary omitted ...]
> Task :check
Skipping task ':check' as it has no actions.
> Task :build
Skipping task ':build' as it has no actions.
BUILD SUCCESSFUL in 9s
16 actionable tasks: 6 executed, 10 up-to-date
EXIT=0
```

```text
> git grep -n -E "docs/(multi-module|library-publishing)\.md|\]\((multi-module|library-publishing)\.md\)" -- ':!CHANGELOG.md' ':!docs/tutorials'
GREP_EXIT=1
```

```text
> for each relative link in AGENTS.md, README.md, CHANGELOG.md, docs/*.md, docs/tutorials/*.md: test -e target
done
```
No `BROKEN` lines were reported.
