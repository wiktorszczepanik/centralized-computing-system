package Constants;

public enum ArithmeticOperations {

    ADD("ADD", "Addition"),
    SUB("SUB", "Subtraction"),
    MUL("MUL", "Multiplication"),
    DIV("DIV", "Division"),
    ERROR("ERR", "Error");

    private final String text;
    private final String description;

    ArithmeticOperations(String text, String description) {
        this.description = description;
        this.text = text;
    }

    public String getDescription() { return description; }
    public String getText() { return text; }
}
