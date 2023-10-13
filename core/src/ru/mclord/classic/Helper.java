package ru.mclord.classic;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;

public class Helper {
    /* package-private */ static int PREFERRED_NETWORK_BUFFER_LENGTH = 8192;

    private Helper() {
    }

    public static File[] listPlugins() {
        File pluginsDir = new File("./plugins/");
        if (!pluginsDir.isDirectory()) {
            if (!pluginsDir.mkdir()) {
                System.err.println("Failed to create \"plugins\" directory");

                return null;
            }
            System.out.println("Created \"plugins\" directory");
        }

        return pluginsDir.listFiles(file -> file.getName().endsWith(".jar"));
    }

    /*
     * Should be called by the main thread.
     */
    public static void setPreferredNetworkBufferLength(int bufferLength) {
        if (bufferLength <= PREFERRED_NETWORK_BUFFER_LENGTH) return;

        PREFERRED_NETWORK_BUFFER_LENGTH = bufferLength;
    }

    /*
     * Should be called by the main thread.
     *
     * But it would be better if it was never called.
     */
    public static boolean writeRawDataAndFlush(byte[] bytes) throws IOException {
        McLordClassic game = McLordClassic.game();
        NetworkingThread thread = game.networkingThread;
        if (thread == null) return false;

        DataOutputStream stream = thread.output;
        if (stream == null) return false;

        stream.write(bytes);
        stream.flush();

        return true;
    }
}
