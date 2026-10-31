# Splitting into a multi-project build

Keep this template as one Gradle module until a distinct second module is necessary.
Then share build logic through three Groovy conventions: common Java defaults, reusable libraries, and runnable applications.

## The pattern, once you need it
Both `counters` and `auth` define this structure in their `buildSrc/src/main/groovy/` directories.
The library and application conventions each apply common; application does not inherit library.
For specialized packaging, such as WAR files, apply common with the appropriate packaging plugin.

Use this layout in a derived project, replacing `<group>` consistently with its plugin namespace, such as `com.example`:

```text
settings.gradle
gradle.properties
gradlew / gradlew.bat
gradle/
buildSrc/
  build.gradle
  src/main/groovy/
    <group>.java-common-conventions.gradle
    <group>.java-library-conventions.gradle
    <group>.java-application-conventions.gradle
clients/java/
  build.gradle
  src/main/java/
  src/test/java/
server/
  build.gradle
  src/main/java/
  src/test/java/
```

The examples use `clients/java` as a library and `server` as an application consuming it.
This dependency illustrates project wiring; choose dependencies according to your actual module responsibilities.

## Map logical projects to directories
`settings.gradle`:

```groovy
rootProject.name = 'example-project'

include 'server'
include 'clients.java'
project(':clients.java').projectDir = file('clients/java')
```

Both inspected projects use this explicit client mapping.
`:clients.java` is one logical project name containing a dot; its physical directory is `clients/java`.
Use that logical path in dependencies and task invocations, such as `:clients.java:build`.
`:clients:java` would instead declare a nested project hierarchy with a parent `:clients` project.
Create the included directories before running Gradle; Gradle 9 requires existing, writable project directories.
See Gradle's [multi-project build reference](https://docs.gradle.org/current/userguide/multi_project_builds.html) for project descriptors and dependencies.

Gradle discovers `buildSrc` automatically; do not add it to the root `include` list.

## Define shared conventions

### Compile the plugins
`buildSrc/build.gradle` — just enough to compile the convention plugins below:

```groovy
plugins {
  id 'groovy-gradle-plugin'
}

repositories {
  gradlePluginPortal()
}
```

### Common Java defaults
`buildSrc/src/main/groovy/<group>.java-common-conventions.gradle`:

```groovy
plugins {
  id 'java'
}

repositories {
  mavenCentral()
}

dependencies {
  testImplementation 'org.junit.jupiter:junit-jupiter-api:6.1.3'
  testRuntimeOnly    'org.junit.jupiter:junit-jupiter-engine:6.1.3'
  testRuntimeOnly    'org.junit.platform:junit-platform-launcher:6.1.3'
}

java {
  toolchain {
    languageVersion = JavaLanguageVersion.of(25)
  }
  withJavadocJar()
  withSourcesJar()
}

javadoc {
  options.addBooleanOption('html5', true)
  options.addStringOption('Xdoclint:none', '-quiet')
}

test {
  useJUnitPlatform()
  testLogging.showStandardStreams = true
}
```

Keep the JUnit versions together here, aligned with the template's [build configuration](../build.gradle).
Keep Java 25 aligned with the wrapper's [daemon toolchain](../gradle/gradle-daemon-jvm.properties) and CI.
The shared defaults retain test output streaming, source archives, and HTML5 Javadoc with quiet output and disabled doclint.
Each Java subproject's `build` produces its main jar, `-sources.jar`, and `-javadoc.jar` under its own `build/libs/`.

### Reusable libraries
`buildSrc/src/main/groovy/<group>.java-library-conventions.gradle`:

```groovy
plugins {
  id '<group>.java-common-conventions'
  id 'java-library'
}
```

Declare dependencies exposed through public signatures with `api`; use `implementation` for internal dependencies.
Publishing remains an optional concern covered in [Publishing a Reusable Library](library-publishing.md).

### Runnable applications
`buildSrc/src/main/groovy/<group>.java-application-conventions.gradle`:

```groovy
plugins {
  id '<group>.java-common-conventions'
  id 'application'
}
```

Configure each application's main class in its own build file.
The application plugin supplies `run`, start scripts, and distributions containing runtime dependencies.
Direct `java -jar` execution additionally needs manifest and dependency packaging configuration; retain the template's manifest logic when migrating.

These are [precompiled script plugins](https://docs.gradle.org/current/userguide/implementing_gradle_plugins_precompiled.html): each filename determines its plugin ID without `.gradle`.

## Apply conventions in each module
`clients/java/build.gradle`:

```groovy
plugins {
  id '<group>.java-library-conventions'
}
```

`server/build.gradle`:

```groovy
plugins {
  id '<group>.java-application-conventions'
}

dependencies {
  implementation project(':clients.java')
}

application {
  mainClass = 'com.example.server.Main'
}
```

Replace the example main class with your application's fully qualified entry point.
Keep module-specific dependencies and packaging in each module's build file.

## Migrate and verify
- Move production code, tests, and resources into the owning module's standard `src/main` and `src/test` trees.
- Keep the wrapper, daemon toolchain, and shared `gradle.properties` at the root.
- Move shared repository configuration into common, including private repositories needed by your dependencies.
- Preserve manifest metadata, formatting checks, and release wiring from the root build in the appropriate module or convention.
- Keep root `build.gradle` only for required build-wide tasks; remove Java application configuration after moving its sources.
- Update CI, launch commands, and release artifact paths to the module outputs.

Run these commands from the build root after adding source files and tests:

```shell
./gradlew projects
./gradlew :clients.java:build :server:build
./gradlew :server:run
```

On Windows, use `.\gradlew.bat` in place of `./gradlew`.
Confirm the project listing contains `:clients.java` and `:server`, both modules' tests pass, and the application starts.
Check all three archives in each module's `build/libs/` directory.
