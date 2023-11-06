package ru.mclord.classic;

import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.utils.Disposable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;

public class Helper {
    public static final int PROTOCOL_STRING_LENGTH = 64;
    /* package-private */ static int PREFERRED_NETWORK_BUFFER_LENGTH = 8192;

    public static final int ATTR = VertexAttributes.Usage.Position |
            VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates;
    public static final String RIGHT_SIDE_NAME = "right";
    public static final String LEFT_SIDE_NAME = "left";
    public static final String TOP_SIDE_NAME = "top";
    public static final String BOTTOM_SIDE_NAME = "bottom";
    public static final String FRONT_SIDE_NAME = "front";
    public static final String BACK_SIDE_NAME = "back";

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

    @ShouldBeCalledBy(thread = "main")
    public static void setPreferredNetworkBufferLength(int bufferLength) {
        if (bufferLength <= PREFERRED_NETWORK_BUFFER_LENGTH) return;

        PREFERRED_NETWORK_BUFFER_LENGTH = bufferLength;
    }

    /*
     * Should be called by the main thread (just like any
     * network packet should be written by the main thread).
     *
     * But it would be better if it was never called.
     */
    @ShouldBeCalledBy(thread = "main")
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

    public static void dispose(Disposable disposable) {
        if (disposable != null) disposable.dispose();
    }

    public static byte[] toProtocolString(String str) {
        byte[] result = new byte[PROTOCOL_STRING_LENGTH];
        for (int i = 0; i < str.length(); i++) {
            result[i] = (byte) str.charAt(i);
        }
        for (int i = str.length(); i < PROTOCOL_STRING_LENGTH; i++) {
            result[i] = 0x20;
            // result[i] = ' '; // should be exactly the same thing
        }

        return result;
    }

    public static String readProtocolString(DataInputStream stream) throws IOException {
        byte[] buffer = new byte[PROTOCOL_STRING_LENGTH];
        stream.readFully(buffer);

        int end = -1;
        for (int i = PROTOCOL_STRING_LENGTH - 1; i >= 0; i--) {
            if (buffer[i] != 0x20) {
                end = i + 1;

                break;
            }
        }
        if (end == -1) return "";

        byte[] result = new byte[end];
        System.arraycopy(buffer, 0, result, 0, result.length);

        return new String(result);
    }
}
