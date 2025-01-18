package Logs.Dependencies;

public enum Site {

    CLIENT("Client"),
    SERVER("Server"),
    NONE("None");

    private final String textVersion;

    Site(String textVersion) {
        this.textVersion = textVersion;
    }

    public String getTextVersion() {
        return textVersion;
    }

}
