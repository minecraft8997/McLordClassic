package ru.mclord.classic;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.net.Socket;
import com.badlogic.gdx.net.SocketHints;
import com.badlogic.gdx.utils.GdxRuntimeException;
import ru.mclord.classic.events.DisconnectEvent;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ConnectException;

public class NetworkingThread extends Thread {
    private boolean reportedDisconnected = false;

    private DataInputStream input;
    private DataOutputStream output;

    @Override
    public void run() {
        McLordClassic game = McLordClassic.game();

        Socket socket = null;
        try {
            socket = Gdx.net.newClientSocket(
                    Net.Protocol.TCP,
                    GameParameters.getAddress(),
                    GameParameters.getPort(),
                    new SocketHints()
            );
            input = new DataInputStream(socket.getInputStream());
            output = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));

            output.write();

            // todo init cpe
            game.addTask(() -> PluginManager.getInstance().initPlugins());
        } catch (GdxRuntimeException e) {
            e.printStackTrace();

            if (e.getCause() instanceof ConnectException) {
                reportDisconnected(e.toString());
            }
        } finally {
            if (socket != null) {
                socket.dispose();
            }

            reportDisconnected("Disconnected");
        }
    }

    private void reportDisconnected(String reason) {
        if (reportedDisconnected) return;

        EventManager.getInstance().fireEvent(new DisconnectEvent(reason));
        reportedDisconnected = true;
    }
}
