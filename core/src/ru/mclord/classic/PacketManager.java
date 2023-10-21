package ru.mclord.classic;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class PacketManager implements Manager {
    private static final PacketManager INSTANCE = new PacketManager();

    private final Map<Byte, PacketWriter> writerMap = new HashMap<>();
    private final Map<Byte, PacketHandler> handlerMap = new HashMap<>();

    private PacketManager() {
    }

    public static PacketManager getInstance() {
        return INSTANCE;
    }

    public void registerWriter(PacketWriter writer) {
        if (!checkStage()) {
            throw new IllegalStateException(
                    "Cannot register packet writers during current game stage");
        }
        if (writerMap.containsKey(writer.packetId)) {
            throw new IllegalArgumentException("A PacketWriter for " +
                    "packetId=" + writer.packetId + " is already registered");
        }

        writerMap.put(writer.packetId, writer);
    }

    public boolean isWriterRegistered(byte packetId) {
        return writerMap.containsKey(packetId);
    }

    public void registerHandler(PacketHandler handler) {
        if (!checkStage()) {
            throw new IllegalStateException(
                    "Cannot register packet handlers during current game stage");
        }
        if (handlerMap.containsKey(handler.packetId)) {
            throw new IllegalArgumentException("A PacketHandler for " +
                    "packetId=" + handler.packetId + " is already registered");
        }

        handlerMap.put(handler.packetId, handler);
    }

    public boolean isHandlerRegistered(byte packetId) {
        return getHandler(packetId, false) != null;
    }

    /* package-private */ PacketHandler getHandler(byte packetId, boolean throwException) {
        PacketHandler handler = handlerMap.get(packetId);
        if (handler == null && throwException) {
            throw new IllegalArgumentException("Could not " +
                    "find an appropriate handler for packetId=" + packetId);
        }

        return handler;
    }

    /*
     * If you're calling the method from any thread other
     * than the main one, you should leave the first ("queue")
     * parameter equal to <code>true</code>.
     */
    public void writeAndFlush(
            boolean queue,
            DataOutputStream stream,
            byte packetId,
            Object... parameters
    ) throws IOException {
        if (queue) {
            queueWrite(stream, true, packetId, parameters);

            return;
        }
        write(stream, packetId, parameters);
        stream.flush();
    }

    /*
     * Can be called by any thread.
     */
    public void queueWrite(
            DataOutputStream stream, boolean flush, byte packetId, Object... parameters
    ) {
        McLordClassic.game().addTask(() -> {
            try {
                write(stream, packetId, parameters);
                if (flush) stream.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /*
     * Should be called by the main thread.
     */
    public void write(
            DataOutputStream stream, byte packetId, Object... parameters
    ) throws IOException {
        if (checkStage()) {
            throw new IllegalStateException("Cannot write packets during current game stage");
        }

        PacketWriter writer = writerMap.get(packetId);
        if (writer == null) {
            throw new IllegalArgumentException("Could not " +
                    "find an appropriate writer for packetId=" + packetId);
        }
        if (!writer.fastWriter) {
            Class<?>[] parameterClasses = new Class<?>[parameters.length];
            for (int i = 0; i < parameterClasses.length; i++) {
                parameterClasses[i] = parameters[i].getClass();
            }
            if (!writer.verifyParameterClasses(parameterClasses)) {
                throw new IllegalStateException("Invalid parameters");
            }
        }
        writer.write0(stream, parameters);
    }

    public PacketHandler attemptHandleFastly(byte packetId, DataInputStream stream) {
        PacketHandler handler = getHandler(packetId, true);
        if (!handler.fastHandler) return handler;

        handler.handle0(stream);

        return null;
    }

    public void handle(byte packetId, PacketHandler handler, byte[] payload) {
        if (handler == null) {
            handler = getHandler(packetId, true);
        }
        ByteArrayInputStream stream0 = new ByteArrayInputStream(payload);
        DataInputStream stream = new DataInputStream(stream0);
        handler.handle0(stream);

        // we don't have to close a ByteArrayInputStream
    }

    @Override
    public boolean checkStage() {
        McLordClassic.GameStage stage = McLordClassic.game().stage;

        return stage == McLordClassic.GameStage.PRE_INITIALIZATION ||
                stage == McLordClassic.GameStage.ENABLING_PROTOCOL_EXTENSIONS;
    }
}
