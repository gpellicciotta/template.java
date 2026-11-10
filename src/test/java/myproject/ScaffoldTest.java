package myproject;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ScaffoldTest {
  private static Path makeTemplateTree(Path root, String... markers) throws Exception {
    for (String marker : markers) {
      Files.writeString(root.resolve(marker), "");
    }
    Files.createDirectories(root.resolve("src/main/java/myproject"));
    return Files.createDirectories(root.resolve("build/classes/java/main"));
  }

  @Test
  void findTemplateRootNeedsNoPlanData(@TempDir Path tmpDir) throws Exception {
    Path start = makeTemplateTree(tmpDir, "build.gradle", "settings.gradle", "CHANGELOG.md");
    assertEquals(tmpDir, Scaffold.findTemplateRoot(start));
  }

  @Test
  void findTemplateRootRejectsTreesWithoutSettingsGradle(@TempDir Path tmpDir) throws Exception {
    Path start = makeTemplateTree(tmpDir, "build.gradle", "CHANGELOG.md");
    assertThrows(Scaffold.ScaffoldException.class, () -> Scaffold.findTemplateRoot(start));
  }
}
