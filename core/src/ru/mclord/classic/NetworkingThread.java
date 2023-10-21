package ru.mclord.classic;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.net.Socket;
import com.badlogic.gdx.net.SocketHints;
import ru.mclord.classic.events.DisconnectEvent;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

public class NetworkingThread extends Thread {
    public static final byte PLAYER_IDENTIFICATION = 0x00;

    private static boolean reportedDisconnected = false;

    @SuppressWarnings("FieldCanBeLocal") /* package-private */ DataInputStream input;
    @SuppressWarnings("FieldCanBeLocal") /* package-private */ DataOutputStream output;

    public NetworkingThread() {
        setName("Networking Thread");
        setDaemon(true);
    }

    @Override
    public void run() {
        PacketManager packets = PacketManager.getInstance();

        Socket socket = null;
        try {
            socket = Gdx.net.newClientSocket(
                    Net.Protocol.TCP,
                    GameParameters.getAddress(),
                    GameParameters.getPort(),
                    new SocketHints()
            );
            input = new DataInputStream(socket.getInputStream());
            output = new DataOutputStream(new BufferedOutputStream(socket
                    .getOutputStream(), Helper.PREFERRED_NETWORK_BUFFER_LENGTH));

            packets.writeAndFlush(true, output, PLAYER_IDENTIFICATION,
                    (byte) 0x07,
                    GameParameters.getUsername(),
                    GameParameters.getMppass(),
                    (byte) 0x42
            );

            //noinspection InfiniteLoopStatement
            while (true) {
                byte packetId = input.readByte();
                PacketHandler handler = packets.attemptHandleFastly(packetId, input);
                if (handler != null) { // means fastHandler == false
                    byte[] payload = new byte[handler.packetLength];
                    input.readFully(payload);
                    packets.handle(packetId, handler, payload);
                }
            }

            //game.addTask(() -> PluginManager.getInstance().initPlugins());
        } catch (Throwable t) {
            t.printStackTrace();

            reportDisconnected(t.toString());
        } finally {
            if (socket != null) {
                socket.dispose();
            }

            reportDisconnected("Disconnected");
        }
    }

    public static void reportDisconnected(String reason) {
        if (reportedDisconnected) return;

        EventManager.getInstance().fireEvent(new DisconnectEvent(reason));
        reportedDisconnected = true;
    }
}
