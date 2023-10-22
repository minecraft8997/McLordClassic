package ru.mclord.classic;

import java.io.DataInputStream;
import java.io.IOException;

public abstract class PacketHandler {
    /* package-private */ final byte packetId;
    /**
     * Packet length without the first packetId byte.
     * For example the length of PlayerIdentification (0x00)
     * packet should be 131 - 1 = 130 bytes.
     */
    /* package-private */ final int packetLength;
    /* package-private */ final boolean fastHandler;

    public PacketHandler(byte packetId, int packetLength) {
        this(packetId, packetLength, false);
    }

    public PacketHandler(byte packetId, int packetLength, boolean fastHandler) {
        this.packetId = packetId;
        this.packetLength = packetLength;
        this.fastHandler = fastHandler;
    }

    public final byte getPacketId() {
        return packetId;
    }

    public final int getPacketLength() {
        return packetLength;
    }

    public final boolean isFastHandler() {
        return fastHandler;
    }

    /*
     * If fastHandler == true, calling this method will most
     * likely cause a big hang-up inside the main/rendering thread.
     */
    public final void handle0(DataInputStream stream) {
        McLordClassic.game().addTask((NetworkingRunnable) () -> {
            try {
                handle(stream);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /*
     * If fastHandler == true, the method will be called by
     * NetworkingThread, otherwise it's going to be called by main thread.
     */
    public abstract void handle(DataInputStream stream) throws IOException;
}
