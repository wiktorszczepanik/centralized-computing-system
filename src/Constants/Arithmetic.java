package Constants;

import Exceptions.ArgumentException;
import Exceptions.FlagException;

public enum Arithmetic {

    ADD("ADD", "Addition"),
    SUB("SUB", "Subtraction"),
    MUL("MUL", "Multiplication"),
    DIV("DIV", "Division");
    // ERROR("ERR", "Error");

    private final String text;
    private final String description;

    Arithmetic(String text, String description) {
        this.description = description;
        this.text = text;
    }

    public String getDescription() { return description; }
    public String getText() { return text; }

    public static Arithmetic getToken(String text) throws ArgumentException {
        for (Arithmetic operation : Arithmetic.values()) {
            if (operation.getText().equals(text))
                return operation;
        }
        throw new ArgumentException("Invalid operation token.");
    }
}
