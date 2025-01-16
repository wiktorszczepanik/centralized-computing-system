package Exceptions;

public class FlagException extends Exception {
  public FlagException(String message) {
    super("(flag) ".concat(message));
  }
}