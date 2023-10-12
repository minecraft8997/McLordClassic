package ru.mclord.classic;

import java.util.Objects;

public class GameParameters {
    private static String username;
    private static String mppass;
    private static String address;
    private static int port;

    private static boolean ignoreInvalidParameters;

    private GameParameters() {
    }

    public static void setupDebugProperties() {
        System.setProperty("mclordUsername", "deewend");
        System.setProperty("mclordAddress", "localhost");
        System.setProperty("mclordPort", "25565");
    }

    public static void collectAndVerify() {
        ignoreInvalidParameters = Boolean.parseBoolean(
                System.getProperty("mclordIgnoreInvalidParameters", "false"));

        username = Objects.requireNonNull(System.getProperty("mclordUsername"));
        mppass = System.getProperty("mclordMppass");
        address = Objects.requireNonNull(System.getProperty("mclordAddress"));
        port = Integer.parseInt(System.getProperty("mclordPort"));

        if (!username.matches("^[a-zA-Z0-9_.]{2,16}$")) {
            report("Got an invalid username");
        }
        if (mppass != null && !mppass.matches("^[a-f0-9]{32}$")) {
            report("Got an invalid mppass");
        }
        if (mppass == null) mppass = "";
        if (port < 0 || port > 65535) {
            //noinspection DataFlowIssue
            report("Got an invalid port", true);
        }
    }

    private static void report(String message) {
        report(message, false);
    }

    private static void report(String message, boolean forceTerminate) {
        if (forceTerminate || !ignoreInvalidParameters) {
            throw new RuntimeException(message);
        } else {
            System.err.println("[Warning] " + message);
        }
    }

    public static String getUsername() {
        return username;
    }

    public static String getMppass() {
        return mppass;
    }

    public static String getAddress() {
        return address;
    }

    public static int getPort() {
        return port;
    }
}

