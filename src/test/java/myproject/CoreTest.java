package myproject;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CoreTest {
  @Test
  void greet() {
    assertEquals("Hello, Gio", Core.greet("Gio"));
  }
}
