# Publishing a reusable library (optional)

This template ships as an application (`plugins { id 'application' }`), not a library, and deliberately does
not include publishing configuration by default — see CLAUDE.md's "Favour simplicity over ingenuity". Add the
pattern below only if your project derived from this template is itself a reusable library consumed by other
projects (e.g. `support.java`, consumed by `auth`).

## The pattern, once you need it
Apply `maven-publish` alongside (or instead of) `application`, and configure a library publication.
Retain the template's existing source and Javadoc archive configuration:

```groovy
plugins {
  id 'java-library'
  id 'maven-publish'
}

java {
  toolchain {
    languageVersion = JavaLanguageVersion.of(25)
  }
  withJavadocJar()
  withSourcesJar()
}

publishing {
  publications {
    maven(MavenPublication) {
      from components.java
    }
  }
  repositories {
    maven {
      name = 'GithubPackages'
      url = uri("https://maven.pkg.github.com/<owner>/<repo>")
      credentials {
        username = project.findProperty('gpr.user') ?: System.getenv('GITHUB_ACTOR')
        password = project.findProperty('gpr.token') ?: System.getenv('GITHUB_TOKEN')
      }
    }
  }
}
```

Notes:
- The template already enables `withJavadocJar()` and `withSourcesJar()`; `from components.java` includes both
  companion archives in the publication.
- The GitHub Packages repository block above is a template — swap in your own `<owner>/<repo>`, and never
  commit real credentials; read them from Gradle properties or environment variables as shown.
- Publishing to a GitHub Packages repository that is private requires consumers to authenticate too (a PAT
  with `read:packages`), which only works if consumers actually have access to that private repository or
  package. Don't bake a private registry into a public template's default build — document it here instead so
  each derived project opts in deliberately.
- Consuming projects add a matching `repositories { maven { url = ...; credentials { ... } } }` block pointing
  at the same GitHub Packages URL, plus the dependency itself (e.g.
  `implementation 'com.pellicciotta:support.java:<version>'`).
- Run `gradle publish` (or the repository-scoped task, e.g.
  `gradle publishMavenPublicationToGithubPackagesRepository`) to publish once credentials are configured.
