package Logs;

import Logs.Dependencies.Site;

public class Logger {

    public static Site prefix = Site.NONE;
    public static int lineLength = 50;
    public static int lastLineLength = 0;

    public static void setPrefix(Site prefixType) {
        if (prefixType == Site.CLIENT) prefix = Site.CLIENT;
        else prefix = Site.SERVER;
    }

    public static void log(String message) {
        System.out.print("[" + prefix + "] " + message);
        lastLineLength = ("[" + prefix + "] " + message).length();
        System.out.flush();
    }

    public static void updateLastLine(int length) {
        lastLineLength += length;
    }

    public static void sendStatus(Logs status) {
        numberOfDots(lineLength - lastLineLength);
        System.out.println(status.getHeader());
        System.out.flush();
    }

    private static void numberOfDots(int till) {
        for (int i = 0; i < till; i++)
            System.out.print('.');
    }
}