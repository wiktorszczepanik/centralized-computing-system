package Logs;

public enum Logs {

    CORRECT("<CORRECT>"),
    DONE("<DONE>"),
    RECEIVED("<RECEIVED>"),
    ERROR("<ERROR>");

    private final String header;

    Logs(String header) {
        this.header = header;
    }

    public String getHeader() {
        return header;
    }

}