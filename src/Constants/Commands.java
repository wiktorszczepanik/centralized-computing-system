package Constants;

public enum Commands {

    DISCOVER("CSS_DISCOVER"),
    FOUND("CSS_FOUND"),
    ERROR("ERROR: ");

    private final String description;

    Commands(String description) {
        this.description = description;
    }

    public String getDescription() { return description; }
}
