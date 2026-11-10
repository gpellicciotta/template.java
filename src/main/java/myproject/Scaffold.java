package myproject;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/** Creates a new project as a renamed copy of this template project. */
public final class Scaffold {
  private Scaffold() { }

  /** Signals that a project could not be scaffolded. */
  public static final class ScaffoldException extends Exception {
    /**
     * Creates the exception.
     *
     * @param message what went wrong
     */
    public ScaffoldException(String message) {
      super(message);
    }
  }

  static final String PACKAGE_NAME = "myproject";
  static final String PROJECT_SLUG = "template.java";
  static final String TITLE_PLACEHOLDER = "Java Template Project";

  private static final String INITIAL_VERSION = "0.1.0-pre";

  // Tracked files only: plan data lives on the separate plan branch, so CI checkouts have no PLAN.md.
  private static final List<String> MARKER_FILES = List.of("build.gradle", "settings.gradle", "CHANGELOG.md");
  // The template's plan store is not inherited; new projects start from a fresh PLAN.md and run `plan init`.
  private static final Set<String> EXCLUDED_NAMES = Set.of(
      ".git", ".gradle", "build", "out", ".idea", ".vscode", ".venv", "work", "__pycache__", ".plan", "plan.code-workspace"
  );

  /**
   * Walk up from where this class was loaded from (e.g. {@code build/classes/java/main} when run via
   * {@code gradlew run}) to find this template project's root directory - the Java equivalent of
   * the Python template's {@code Path(__file__).resolve()}-based search, since there is no installed
   * package metadata to introspect the way Python's editable install provides.
   */
  static Path findTemplateRoot() throws ScaffoldException {
    Path start;
    try {
      start = Paths.get(Cli.class.getProtectionDomain().getCodeSource().getLocation().toURI());
    }
    catch (URISyntaxException | NullPointerException | SecurityException e) {
      start = Paths.get(System.getProperty("user.dir"));
    }
    return findTemplateRoot(start);
  }

  /** Walk up from {@code start} to the first directory holding the marker files and the template package. */
  static Path findTemplateRoot(Path start) throws ScaffoldException {
    for (Path candidate = start; candidate != null; candidate = candidate.getParent()) {
      Path candidateDir = candidate;
      boolean hasMarkers = MARKER_FILES.stream().allMatch(name -> Files.isRegularFile(candidateDir.resolve(name)));
      if (hasMarkers && Files.isDirectory(candidateDir.resolve("src/main/java/" + PACKAGE_NAME))) {
        return candidateDir;
      }
    }
    throw new ScaffoldException(
        "Could not locate the template project root (expected build.gradle, settings.gradle, CHANGELOG.md and "
        + "src/main/java/" + PACKAGE_NAME + "/ in a parent directory). 'create' must be run from within a "
        + "checkout of " + PROJECT_SLUG + " (e.g. via 'gradlew run --args=\"create ...\"')."
    );
  }

  private static String toPackageName(String projectName) throws ScaffoldException {
    String packageName = projectName.replaceAll("[^0-9a-zA-Z]+", "_")
                                     .replaceAll("^_+|_+$", "")
                                     .toLowerCase();
    if (!Pattern.matches("^[a-z_][a-z0-9_]*$", packageName)) {
      throw new ScaffoldException("Cannot derive a valid Java package name from '" + projectName + "'.");
    }
    return packageName;
  }

  private static String toTitle(String projectName) {
    String[] words = projectName.replace('-', ' ').replace('_', ' ').split(" +");
    StringBuilder title = new StringBuilder();
    for (String word : words) {
      if (word.isEmpty()) {
        continue;
      }
      if (title.length() > 0) {
        title.append(' ');
      }
      title.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1).toLowerCase());
    }
    return title.toString();
  }

  private static void copyTree(Path source, Path destination) throws ScaffoldException {
    try (Stream<Path> walk = Files.walk(source)) {
      walk.forEach(path -> {
        Path relative = source.relativize(path);
        for (Path part : relative) {
          if (EXCLUDED_NAMES.contains(part.toString()) || part.toString().endsWith(".egg-info")) {
            return; // Skip anything under an excluded directory.
          }
        }
        Path target = destination.resolve(relative);
        try {
          if (Files.isDirectory(path)) {
            Files.createDirectories(target);
          }
          else {
            Files.createDirectories(target.getParent());
            Files.copy(path, target, StandardCopyOption.COPY_ATTRIBUTES);
          }
        }
        catch (IOException e) {
          throw new UncheckedIOException(e);
        }
      });
    }
    catch (IOException | UncheckedIOException e) {
      throw new ScaffoldException("Failed to copy template tree: " + e.getMessage());
    }
  }

  private static void renamePackageDir(Path destination, String sourceSet, String packageName) throws ScaffoldException {
    Path oldDir = destination.resolve("src").resolve(sourceSet).resolve("java").resolve(PACKAGE_NAME);
    if (!packageName.equals(PACKAGE_NAME) && Files.isDirectory(oldDir)) {
      Path newDir = oldDir.resolveSibling(packageName);
      try {
        Files.move(oldDir, newDir);
      }
      catch (IOException e) {
        throw new ScaffoldException("Failed to rename " + oldDir + ": " + e.getMessage());
      }
    }
  }

  /** old -> new replacements, applied in order; order matters when one placeholder contains another. */
  private static void rewriteTextFiles(Path root, List<String[]> replacements) throws ScaffoldException {
    try (Stream<Path> walk = Files.walk(root)) {
      for (Path path : (Iterable<Path>) walk.filter(Files::isRegularFile)::iterator) {
        // Never touch the binary Gradle wrapper jar - a text rewrite would corrupt it.
        if (path.toString().endsWith(".jar") || path.toString().endsWith(".class")) {
          continue;
        }
        String text;
        try {
          text = Files.readString(path, StandardCharsets.UTF_8);
        }
        catch (IOException e) {
          continue; // Not valid UTF-8 text (e.g. a binary file) - leave it untouched.
        }
        String newText = text;
        for (String[] replacement : replacements) {
          newText = newText.replace(replacement[0], replacement[1]);
        }
        if (!newText.equals(text)) {
          Files.writeString(path, newText, StandardCharsets.UTF_8);
        }
      }
    }
    catch (IOException e) {
      throw new ScaffoldException("Failed to rewrite template placeholders: " + e.getMessage());
    }
  }

  private static void writeFreshChangelogMd(Path destination, String title) throws ScaffoldException {
    String content = "# Versioned Changes\n\n"
        + "A summarized overview of all changes, per version of this project.\n\n"
        + "> Entries will be added in reverse chronological order, so with the most recent at the top.\n"
        + ">\n"
        + "> An active development version carries a `-pre` suffix in this heading and `gradle.properties`.\n"
        + "> On release, the suffix is replaced with a status tag:\n"
        + "> - `[{{date}}]` - frozen/finalized on {{date}}\n"
        + "> - `[released: {{date}}]` - released to package manager or production on {{date}}\n"
        + "> - `[broken]` - considered broken and not be used\n\n"
        + "---\n\n"
        + "## v" + INITIAL_VERSION + "\n\n"
        + "- Initial release of the " + title + " project.\n";
    try {
      Files.writeString(destination.resolve("CHANGELOG.md"), content, StandardCharsets.UTF_8);
    }
    catch (IOException e) {
      throw new ScaffoldException("Failed to write CHANGELOG.md: " + e.getMessage());
    }
  }

  private static void writeFreshPlanMd(Path destination) throws ScaffoldException {
    String content = "# Project Planning\n"
        + "An overview of all tasks and their planning.\n\n"
        + "---\n\n"
        + "## Next Milestone\n"
        + "*(Currently no tasks)*\n\n"
        + "---\n\n"
        + "## Backlog\n"
        + "*(Currently no tasks)*\n";
    try {
      Files.writeString(destination.resolve("PLAN.md"), content, StandardCharsets.UTF_8);
    }
    catch (IOException e) {
      throw new ScaffoldException("Failed to write PLAN.md: " + e.getMessage());
    }
  }

  private static void resetGradlePropertiesVersion(Path destination) throws ScaffoldException {
    Path gradleProperties = destination.resolve("gradle.properties");
    try {
      String text = Files.readString(gradleProperties, StandardCharsets.UTF_8);
      String newText = text.replaceFirst("(?m)^version\\s*=\\s*.*$", "version=" + INITIAL_VERSION);
      Files.writeString(gradleProperties, newText, StandardCharsets.UTF_8);
    }
    catch (IOException e) {
      throw new ScaffoldException("Failed to reset gradle.properties's version: " + e.getMessage());
    }
  }

  /**
   * Creates a new project at {@code outputDir/projectName}, as a renamed copy of this template.
   *
   * <p>Automates the manual steps documented in this template's README under "Starting a new project from
   * this template": copy the tree, rename the {@code myproject} package (in both {@code src/main} and
   * {@code src/test}), replace the {@code template.java} name placeholder
   * throughout, and reset {@code CHANGELOG.md}, {@code PLAN.md}, and {@code gradle.properties}'s {@code version} -
   * the new project starts its own history rather than inheriting the template's.
   *
   * @param projectName name of the new project, also used for its directory
   * @param outputDir directory in which to create the project
   * @return the created project directory
   * @throws ScaffoldException if the template cannot be located, the destination exists, or copying fails
   */
  public static Path createProject(String projectName, String outputDir) throws ScaffoldException {
    Path templateRoot = findTemplateRoot();

    Path destination = Paths.get(outputDir).toAbsolutePath().normalize().resolve(projectName);
    if (Files.exists(destination)) {
      throw new ScaffoldException("Destination " + destination + " already exists.");
    }

    String packageName = toPackageName(projectName);
    String title = toTitle(projectName);

    try {
      Files.createDirectories(destination.getParent());
    }
    catch (IOException e) {
      throw new ScaffoldException("Failed to create " + destination.getParent() + ": " + e.getMessage());
    }
    copyTree(templateRoot, destination);

    renamePackageDir(destination, "main", packageName);
    renamePackageDir(destination, "test", packageName);

    rewriteTextFiles(destination, List.of(
        new String[]{PROJECT_SLUG, projectName},
        new String[]{TITLE_PLACEHOLDER, title},
        new String[]{PACKAGE_NAME, packageName}
    ));

    writeFreshChangelogMd(destination, title);
    writeFreshPlanMd(destination);
    resetGradlePropertiesVersion(destination);

    return destination;
  }
}
