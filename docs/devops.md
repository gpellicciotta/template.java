# DevOps and Operations

Practical guidance on building, testing, developing, and releasing for `template.java`.

---

## Prerequisites and Environment
- **JDK**: Java 25 (auto-provisioned by Gradle toolchain if not present locally).
- **Gradle**: Gradle 9.x (managed via the included Gradle wrapper `./gradlew` / `gradlew.bat`).
- **Git**: Git 2.30+ supporting worktrees (`git worktree`).
- **PowerShell / Bash**: PowerShell on Windows, standard POSIX shell on macOS/Linux.
- **Python**: Python 3.9+ for [bootstrap-dev-environment.py](../scripts/bootstrap-dev-environment.py) (standard library only) and [devops.py](../scripts/devops.py).
  `devops.py` runs on [support-python](https://github.com/gpellicciotta/support.python), pinned in `scripts/requirements.txt`.
- **GitHub Packages access**: `build.gradle` depends on `com.pellicciotta:support.java`, hosted on the
  `gpellicciotta/support.java` GitHub Packages repo. Local builds need a GitHub PAT with `read:packages`
  set as `github.token` (and `github.username`) in `{user.home}/.gradle/gradle.properties`, or the
  `GITHUB_USERNAME`/`GITHUB_TOKEN` env vars. CI reads the PAT from the `PACKAGES_TOKEN` repo secret.
- **Dependency versions**: All dependency and plugin versions live in [libs.versions.toml](../gradle/libs.versions.toml).

---

## Development Workflows

### Initial Bootstrap
Clone the repository and run the bootstrap script:
```shell
python scripts/bootstrap-dev-environment.py
```
This initializes a git repo (if one doesn't already exist), then creates `.venv` and installs `scripts/requirements.txt` into it.
It then runs `gradlew build`.
A project-local `.venv` keeps the pinned support-python apart from other projects pinning other versions.
Rerun the bootstrap after changing `scripts/requirements.txt`.

### VS Code Workspace Settings
The shared [.vscode/settings.json](../.vscode/settings.json) enables automatic Java build configuration updates.
Keep shared settings portable; configure machine-specific JDK paths and personal preferences in VS Code user settings.
Other files and directories under `.vscode/` remain ignored by default, including in nested modules.

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
This template ships as a standalone CLI jar with no production service, so `devops.py` has no `production`
subcommand. Projects derived from this template that deploy a service should add one per the dev-guidelines
CLI conventions.

### Operations Runner `devops.py`
`scripts/devops.py` is the single entry point to typical operations; run `python scripts/devops.py help --verbose`.
It is a thin `DevopsCli` over a `GradleProject` from support-python's `support_python.devops`.
When `.venv` exists, it reruns itself with the `.venv` interpreter, so plain `python` works.

```shell
python scripts/devops.py check      # every CI validation step: gradlew build (Spotless, Checkstyle, tests, jars)
python scripts/devops.py lint       # spotlessCheck, checkstyleMain, checkstyleTest
python scripts/devops.py format     # spotlessApply
python scripts/devops.py test myproject.CoreTest   # each selection becomes a Gradle --tests filter
python scripts/devops.py versions   # fail when gradle.properties and CHANGELOG.md versions drift apart
python scripts/devops.py release --dry-run
```

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
Their fresh `PLAN.md` has empty Next Milestone and Backlog sections, and `.plan/tasks` starts empty.

To create a new project from this template:
```powershell
# Using the CLI
java -jar build/libs/template.java-<version>.jar create my-new-tool -o C:\Dev-Projects

# Or via Gradle run
.\gradlew.bat run --args="create my-new-tool -o C:\Dev-Projects"
```

---

## Release Process
Ongoing work accumulates under the top `CHANGELOG.md` heading while it carries a `-pre` SemVer suffix (e.g.
`## v1.1.1-pre`), which must always match `version` in `gradle.properties` (the single source of truth) exactly,
`-pre` included.

### Releasing with `devops.py`
```shell
python scripts/devops.py release --dry-run   # list the pipeline tasks only
python scripts/devops.py release             # asks for a typed confirmation, then runs doFullRelease -PreleaseConfirmed=yes
python scripts/devops.py release --yes       # skips the typed confirmation
```

The release pushes to `origin` and creates a GitHub release, so it only runs on explicit request.

### Gradle `doFullRelease` Pipeline
```powershell
.\gradlew.bat doFullRelease -PreleaseConfirmed=yes
```
It refuses to start without `-PreleaseConfirmed=yes`. It runs `build` (compile, Spotless, Checkstyle, tests), then in order:
- `checkReleasePreconditions` - fails unless confirmed, the tree is clean, the branch is `main`, `CHANGELOG.md`
  has a matching un-released heading, `gh` is authenticated, and HEAD equals `origin/main` with green CI.
- `finalizeRelease` - strips `-pre` from `gradle.properties`, marks the CHANGELOG heading
  `[released: <date>]`, and commits.
- `publishReleaseArtifacts` - runs `publishMavenPublicationToGithubPackagesRepository` in a nested Gradle run if this
  project has opted into `maven-publish` per `docs/library-publishing.md`; otherwise a no-op (this template ships
  as an application, not a library).
- `tagAndPushRelease` - tags the release commit `v<version>` and pushes the branch and tag to `origin`.
- `createGithubRelease` - runs `gh release create` for the new tag, using the CHANGELOG section as notes
  (requires the [GitHub CLI](https://cli.github.com/) installed and authenticated via `gh auth login`).
- `openNextPreRelease` - bumps `gradle.properties` to the next patch `-pre` version, opens a matching
  `CHANGELOG.md` heading, and commits and pushes.

Any individual step can also be run on its own (e.g. `.\gradlew.bat checkReleasePreconditions`) to diagnose or
resume a failed release without repeating the steps already done.

---

## Continuous Integration
The GitHub Actions workflow in `.github/workflows/ci.yml` triggers on push and pull requests, executing:
- Java JDK 25 and Python 3 setup.
- `python -m pip install -r scripts/requirements.txt`, installing support-python into the runner's Python, without a `.venv`.
- `python -m unittest discover -s scripts/tests`, testing `scripts/devops.py` itself.
- `python scripts/devops.py check`, which runs `./gradlew build` (compilation, Spotless, Checkstyle, unit testing, packaging).
