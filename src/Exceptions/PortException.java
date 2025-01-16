package Exceptions;

public class PortException extends Exception {
    public PortException(String message) {
        super("(port) ".concat(message));
    }
}
