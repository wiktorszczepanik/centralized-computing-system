package Calculations;

public enum Statistics {

    CONNECTED_CLIENTS("Number of newly connected clients"),
    SUCCESS_COUNTER("Number of operations calculated"),
    ATTEMPTS_COUNTER("Any calculation attempt"),
    ERRORS_COUNTER("Number of error operations"),
    RESULT_SUM("Sum of results");

    private final String description;
    private int value;

    Statistics(String description) {
        this.description = description;
        this.value = 0;
    }

    public String getDescription() { return description; }
    public int getValue() { return value; }

    public void increment() { value++; }
    public void add(int amount) { value += amount; }
    public void reset() { value = 0; }

    @Override
    public String toString() {
        return description + " : " + value;
    }
}
