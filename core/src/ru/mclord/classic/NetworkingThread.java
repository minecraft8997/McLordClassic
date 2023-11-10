package ru.mclord.classic;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.net.Socket;
import com.badlogic.gdx.net.SocketHints;
import ru.mclord.classic.events.DisconnectEvent;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class NetworkingThread extends Thread {
    public static final byte PLAYER_IDENTIFICATION = 0x00;

    /* package-private */ volatile boolean finishedExecuting;
    private static boolean reportedDisconnected = false;

    @SuppressWarnings("FieldCanBeLocal") /* package-private */ volatile DataInputStream input;
    @SuppressWarnings("FieldCanBeLocal") /* package-private */ volatile DataOutputStream output;

    public NetworkingThread() {
        setName("Networking Thread");
        setDaemon(true);
    }

    @Override
    public void run() {
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

            PacketManager.getInstance().writeAndFlush(true, output,
                    PLAYER_IDENTIFICATION,

                    (byte) 0x07,
                    GameParameters.getUsername(),
                    GameParameters.getMppass(),
                    (byte) 0x42
            );

            while (!Thread.currentThread().isInterrupted()) {
                byte packetId = input.readByte();
                handlePacket(packetId);
            }
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

    /* package-private */ void handlePacket(
            byte packetId
    ) throws IOException, InterruptedException {
        PacketHandler handler =
                PacketManager.getInstance().attemptHandleFastly(packetId, input);
        if (handler != null) { // means fastHandler == false
            byte[] payload = new byte[handler.packetLength];
            input.readFully(payload);
            PacketManager.getInstance().handle(this, packetId, handler, payload);
        }
    }

    public static synchronized void reportDisconnected(String reason) {
        if (reportedDisconnected) return;

        System.out.println("Disconnected: " + reason);
        McLordClassic.game().networkingThread.interrupt();
        EventManager.getInstance().fireEvent(DisconnectEvent.create(reason));

        reportedDisconnected = true;
    }
}
