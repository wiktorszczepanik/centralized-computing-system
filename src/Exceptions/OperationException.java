package Exceptions;
public class OperationException extends Exception {
    public OperationException(String message) {
        super("(operation) ".concat(message));
    }
}
