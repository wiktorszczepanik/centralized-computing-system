package Exceptions;

public class ArgumentException extends RuntimeException {
    public ArgumentException(String message) {
        super("(argument) ".concat(message));
    }
}
