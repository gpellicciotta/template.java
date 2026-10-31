package myproject;

/** Sample business logic, kept free of CLI and IO concerns so it is usable and testable on its own. */
public final class Core {
  private Core() { }

  /**
   * Builds a greeting.
   *
   * @param name who to greet
   * @return the greeting text
   */
  public static String greet(String name) {
    return "Hello, " + name;
  }
}
