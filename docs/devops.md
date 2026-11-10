# DevOps and Operations

Practical guidance on building, testing, developing, and releasing for `template.java`.

---

## Development Environment

### Prerequisites
- **JDK**: Java 25 (auto-provisioned by Gradle toolchain if not present locally).
- **Gradle**: Gradle 9.x (managed via the included Gradle wrapper `./gradlew` / `gradlew.bat`).
- **Git**: Git 2.30+ supporting worktrees (`git worktree`).
- **PowerShell / Bash**: PowerShell on Windows, standard POSIX shell on macOS/Linux.
- **Python**: Python 3.9+ for [bootstrap-dev-environment.py](../scripts/bootstrap-dev-environment.py) (standard library only) and [devops.py](../scripts/devops.py).
  `devops.py` runs on [support-python](https://github.com/gpellicciotta/support.python), pinned in `scripts/requirements.txt`.
- **GitHub Packages access**: `build.gradle` depends on `com.pellicciotta:support.java`, hosted on the
  `gpellicciotta/support.java` GitHub Packages repo. Local builds need a GitHub PAT with `read:packages`
  set as `github.token` (and `github.username`) in `{{user-home}}/.gradle/gradle.properties`, or the
  `GITHUB_USERNAME`/`GITHUB_TOKEN` env vars. CI reads the PAT from the `PACKAGES_TOKEN` repo secret.
- **Dependency versions**: All dependency and plugin versions live in [libs.versions.toml](../gradle/libs.versions.toml).

### Setup
Clone the repository and run the bootstrap script:
```shell
python scripts/bootstrap-dev-environment.py
```
This initializes a git repo (if one doesn't already exist), then creates `.venv` and installs `scripts/requirements.txt` into it.
It then runs `gradlew build`.
A project-local `.venv` keeps the pinned support-python apart from other projects pinning other versions.
Rerun the bootstrap after changing `scripts/requirements.txt`.

The shared [.vscode/settings.json](../.vscode/settings.json) enables automatic Java build configuration updates.
Keep shared settings portable; configure machine-specific JDK paths and personal preferences in VS Code user settings.
Other files and directories under `.vscode/` remain ignored by default, including in nested modules.

---

## Versioning and Releasing

### Updating Versions
[gradle.properties](../gradle.properties) holds the authoritative `version`, without a `v` prefix.
The top [CHANGELOG.md](../CHANGELOG.md) heading must match it exactly, `-pre` suffix included, with a `v` prefix.
[build.gradle](../build.gradle) stamps the version into the jar manifest with `+{{commit}}` build metadata.
`Cli.version()` reads it back at runtime.

Bump the version as part of the change that requires it:
- Breaking change: raise the major version and reset minor and patch, unless the `-pre` version already did.
- New functionality: raise the minor version and reset the patch, unless the `-pre` version already did.
- Rename `version` and the top `CHANGELOG.md` heading together, in the same commit as the change.

`plan` task completion raises the patch of Python projects in `pyproject.toml`, but it does not read `gradle.properties` yet.
In this project, the release pipeline's `openNextPreRelease` step raises the patch instead.
Run `python scripts/devops.py versions` to fail fast when the two versions drift apart.

### Making a Release
A release pushes to `origin` and creates a GitHub release, so it only runs on explicit request.
1. Confirm every change of the version is recorded under the top `-pre` heading of `CHANGELOG.md`.
2. Run `python scripts/devops.py versions` and `python scripts/devops.py check`, and fix any failure.
3. Commit and push `main`, then wait until CI on `origin/main` is green.
4. Authenticate the [GitHub CLI](https://cli.github.com/) with `gh auth login`, if not done yet.
5. Preview the pipeline with `python scripts/devops.py release --dry-run`.
6. Run `python scripts/devops.py release` and type the requested confirmation; `--yes` skips it.
7. Check the new `v{{version}}` tag, the GitHub release, and the next `-pre` heading on `origin/main`.

```shell
python scripts/devops.py release --dry-run   # list the pipeline tasks only
python scripts/devops.py release             # asks for a typed confirmation, then runs doFullRelease -PreleaseConfirmed=yes
python scripts/devops.py release --yes       # skips the typed confirmation
```

#### Gradle `doFullRelease` Pipeline
`devops.py release` runs this pipeline, which can also run directly:
```powershell
.\gradlew.bat doFullRelease -PreleaseConfirmed=yes
```
It refuses to start without `-PreleaseConfirmed=yes`. It runs `build` (compile, Spotless, Checkstyle, tests), then in order:
- `checkReleasePreconditions` - fails unless confirmed, the tree is clean, the branch is `main`, `CHANGELOG.md`
  has a matching un-released heading, `gh` is authenticated, and HEAD equals `origin/main` with green CI.
- `finalizeRelease` - strips `-pre` from `gradle.properties`, marks the CHANGELOG heading
  `[released: {{date}}]`, and commits.
- `publishReleaseArtifacts` - runs `publishMavenPublicationToGithubPackagesRepository` in a nested Gradle run if this
  project has opted into `maven-publish` per [library-publishing.md](library-publishing.md); otherwise a no-op (this template ships
  as an application, not a library).
- `tagAndPushRelease` - tags the release commit `v{{version}}` and pushes the branch and tag to `origin`.
- `createGithubRelease` - runs `gh release create` for the new tag, using the CHANGELOG section as notes.
- `openNextPreRelease` - bumps `gradle.properties` to the next patch `-pre` version, opens a matching
  `CHANGELOG.md` heading, and commits and pushes.

Any individual step can also be run on its own (e.g. `.\gradlew.bat checkReleasePreconditions`) to diagnose or
resume a failed release without repeating the steps already done.

---

## Development Workflows

### Local Environment Files and Compiler Output
[.gitignore](../.gitignore) excludes `.env`, `**/*.env`, and `bin/` at the root and within nested modules.
These patterns cover local environment secrets, named files such as `production.env`, and compiler output directories.
Sanitized examples such as `.env.example` remain trackable; exclude real credentials from examples.
Ignore rules do not remove files already tracked by Git.

### Line Endings and Binary Assets
[.gitattributes](../.gitattributes) normalizes text to LF in Git and checks out `.bat` and `.cmd` scripts with CRLF.
Shell scripts (`.sh`) and `gradlew` use LF; `gradlew.bat` uses CRLF.
[.editorconfig](../.editorconfig) applies matching line endings when saving scripts in compatible editors.

Git treats `.png`, `.jpg`, `.jpeg`, `.gif`, `.ico`, `.pdf`, `.docx`, `.jar`, `.war`, `.class`,
`.woff`, `.woff2`, `.ttf`, and `.eot` files as binary, disabling text conversion, text diffs, and text merging.
The general `.jar` rule also protects the Gradle wrapper JAR.

### Deploying a Service
This template ships as a standalone CLI jar with no production service, so `devops.py` has no `deploy` action.
Derived projects that deploy a service enable it with `DevopsCli.set_deploy` in `build_cli()`, keeping the deploy steps project-local.
See the [CLI guidelines](https://github.com/gpellicciotta/dev-guidelines/blob/main/guidelines/cli-guidelines.md#devops-scripts).

### Operations Runner `devops.py`
[devops.py](../scripts/devops.py) is the single entry point to typical operations; run `python scripts/devops.py help --verbose`.
It is a thin `DevopsCli` over a `GradleProject` from support-python's `support_python.devops`.
When `.venv` exists, it reruns itself with the `.venv` interpreter, so plain `python` works.

```shell
python scripts/devops.py check      # every CI validation step: lint-markdown check, then gradlew build (Spotless, Checkstyle, tests, jars)
python scripts/devops.py lint       # lint-markdown check, then spotlessCheck, checkstyleMain, checkstyleTest
python scripts/devops.py format     # spotlessApply
python scripts/devops.py test myproject.CoreTest   # each selection becomes a Gradle --tests filter
python scripts/devops.py versions   # fail when gradle.properties and CHANGELOG.md versions drift apart
python scripts/devops.py release --dry-run
```

`lint` and `check` run `lint-markdown check` first and stop when it fails.
[.lint-markdown.json](../.lint-markdown.json) adds `CHANGELOG.md`, whose finalized entries must stay unedited, to lint-markdown's default excludes.
Add project-specific actions in `build_cli()` with `cli.add_action(name, summary, handler)`.
See support-python's [DevOps scripts tutorial](https://github.com/gpellicciotta/support.python/blob/v2.0.0/docs/tutorials/devops-scripts.md).
Test the script itself with `.venv/Scripts/python -m unittest discover -s scripts/tests` (`.venv/bin/python` outside Windows).
CI installs `scripts/requirements.txt`, runs those tests, then runs `python scripts/devops.py check`.

### Building and Testing
The root [gradle.properties](../gradle.properties) defines shared build defaults:
- `org.gradle.caching=true` reuses eligible task outputs through Gradle's local build cache.
- `org.gradle.warning.mode=all` displays each warning.
- `org.gradle.console=rich` enables colored output and progress indicators.
- `org.gradle.logging.level=info` includes detailed build diagnostics.

For plain automation logs, run `./gradlew build --console=plain` (Windows: `.\gradlew.bat build --console=plain`).
Use `--no-build-cache`, `--warning-mode=summary`, or `--quiet` to override individual defaults for one invocation.
See the [Gradle build environment reference](https://docs.gradle.org/current/userguide/build_environment.html) for property precedence and supported values.

The same properties file stores `author` and a literal `copyright` notice for derived projects and build consumers.
Update both when changing ownership; `copyright` does not interpolate `author`.
The existing CLI copyright and JAR vendor continue to derive from `author`.

```powershell
# Run tests only
.\gradlew.bat test

# Full compile, test, and assembly of application, source, and Javadoc jars
.\gradlew.bat build

# Run application during development
.\gradlew.bat run --args="help"
.\gradlew.bat run --args="greet Gio"
```

The build writes the main jar, `-sources.jar`, and `-javadoc.jar` to `build/libs/`.
Run `sourcesJar` or `javadocJar` to generate either companion archive individually.
Javadoc explicitly uses HTML5 output with `-Xdoclint:none` and `-quiet`.
This disables documentation lint diagnostics and suppresses generation progress messages; Javadoc errors still fail the build.

### JAR Build Timestamp
The application JAR manifest includes `Build-Time` by default, formatted as an ISO-8601 UTC instant ending in `Z`.
The value records when Gradle configures the manifest, rather than when compilation or packaging finishes.
Source and Javadoc JARs do not receive this attribute.

This deliberately favors build traceability over byte-for-byte reproducibility.
Each fresh configuration changes the manifest input, causing `jar` to execute again even when source code is unchanged.
Distributions containing that JAR also change.
Gradle does not cache `Jar` tasks by default; enabling caching explicitly would still encounter changing timestamp inputs.
Compilation and other eligible tasks retain their normal incremental and build-cache behavior.
Derived projects requiring reproducible archives should remove the `Build-Time` attribute or replace it with a fixed release value.
See Gradle's [incremental build](https://docs.gradle.org/current/userguide/incremental_build.html) and
[build cache](https://docs.gradle.org/current/userguide/build_cache.html) documentation.

### Task Coordination Protocol
Tasks live in `PLAN.md` and `.plan/tasks/` and are managed only through the `plan` CLI; see `AGENTS.md` and the
[cross-project development guidelines](https://github.com/gpellicciotta/dev-guidelines).
Mainline is `main`.

---

## Scaffolding a New Project
Generated projects start at `0.1.0-pre` in `gradle.properties`, with a matching `## v0.1.0-pre` changelog heading.
`create` skips the template's plan store: `PLAN.md`, `.plan/`, and `plan.code-workspace`.
After the bootstrap has run `git init`, create the new project's store with `plan init --store branch`.
It keeps plan data on an orphan `plan` branch, as the copied `.gitignore` expects.
`create` finds the template root by `build.gradle`, `settings.gradle`, `CHANGELOG.md`, and `src/main/java/myproject/`.
It needs no plan data, so it works in CI checkouts of `main`.

To create a new project from this template:
```powershell
# Using the CLI
java -jar build/libs/template.java-{{version}}.jar create my-new-tool -o {{output-dir}}

# Or via Gradle run
.\gradlew.bat run --args="create my-new-tool -o {{output-dir}}"
```

---

## Continuous Integration
The GitHub Actions workflow in [ci.yml](../.github/workflows/ci.yml) triggers on push and pull requests, executing:
- Java JDK 25 and Python 3 setup.
- `python -m pip install -r scripts/requirements.txt`, installing support-python into the runner's Python, without a `.venv`.
- `python -m unittest discover -s scripts/tests`, testing [devops.py](../scripts/devops.py) itself.
- `python scripts/devops.py check`, which runs `lint-markdown check` and then `./gradlew build` (compilation, Spotless, Checkstyle, unit testing, packaging).
