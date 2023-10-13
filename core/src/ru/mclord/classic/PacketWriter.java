package ru.mclord.classic;

import java.io.DataOutputStream;

public abstract class PacketWriter {
    /* package-private */ final byte packetId;
    /* package-private */ final boolean fastWriter;

    public PacketWriter(byte packetId) {
        this(packetId, true);
    }

    public PacketWriter(byte packetId, boolean fastWriter) {
        this.packetId = packetId;
        this.fastWriter = fastWriter;
    }

    public final byte getPacketId() {
        return packetId;
    }

    public final boolean isFastWriter() {
        return fastWriter;
    }

    /*
     * Won't be called if fastWriter == true.
     */
    public boolean verifyParameterClasses(Class<?>[] classes) {
        return true;
    }

    /*
     * No need to flush the stream after writing the data.
     */
    public abstract void write(DataOutputStream stream, Object... parameters);
}
