package myproject;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LogTest {
  private final ByteArrayOutputStream outBuf = new ByteArrayOutputStream();
  private final ByteArrayOutputStream errBuf = new ByteArrayOutputStream();

  private void configure(boolean debug, Path logFile) throws Exception {
    Log.configure(debug, logFile, new PrintStream(outBuf, true, "UTF-8"), new PrintStream(errBuf, true, "UTF-8"));
  }

  @AfterEach
  void resetToRealStreams() {
    Log.configure(false, null, System.out, System.err);
  }

  @Test
  void infoOmitsIndicatorAndTimestampOnConsole() throws Exception {
    configure(false, null);
    Log.info("hello there");
    assertEquals("hello there" + System.lineSeparator(), outBuf.toString(StandardCharsets.UTF_8));
  }

  @Test
  void warnAndErrorKeepIndicatorOnConsoleButNoTimestamp() throws Exception {
    configure(false, null);
    Log.warn("careful");
    Log.error("boom");
    String errOut = errBuf.toString(StandardCharsets.UTF_8);
    assertTrue(errOut.contains("**[WARN]**  careful"));
    assertTrue(errOut.contains("**[ERROR]** boom"));
    assertFalse(errOut.matches("(?s).*\\d{4}-\\d{2}-\\d{2}.*"));
  }

  @Test
  void debugIsDiscardedUnlessEnabled() throws Exception {
    configure(false, null);
    Log.debug("hidden");
    assertEquals("", outBuf.toString(StandardCharsets.UTF_8));

    configure(true, null);
    Log.debug("visible");
    assertTrue(outBuf.toString(StandardCharsets.UTF_8).contains("**[DEBUG]** visible"));
  }

  @Test
  void fileEntriesArePrefixedWithTimestampAndIndicator(@TempDir Path tmpDir) throws Exception {
    Path logFile = tmpDir.resolve("app.log");
    configure(false, logFile);
    Log.info("started up");

    String logged = Files.readString(logFile, StandardCharsets.UTF_8);
    assertTrue(logged.matches("(?s)^\\[\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\] \\*\\*\\[INFO\\]\\*\\*\\s+started up.*"));
  }

  @Test
  void multiLineMessagesIndentContinuationsToMatchFirstLine() throws Exception {
    configure(false, null);
    Log.warn("first line\nsecond line");
    String[] lines = errBuf.toString(StandardCharsets.UTF_8).split("\\R");
    assertEquals("**[WARN]**  first line", lines[0]);
    int indent = lines[0].indexOf("first line");
    assertEquals(" ".repeat(indent) + "second line", lines[1]);
  }

  @Test
  void originIsRenderedInAngleBrackets(@TempDir Path tmpDir) throws Exception {
    Path logFile = tmpDir.resolve("app.log");
    configure(false, logFile);
    Log.info("Scaffold", "copying files");

    String logged = Files.readString(logFile, StandardCharsets.UTF_8);
    assertTrue(logged.contains("<Scaffold> copying files"));
  }
}
