package ru.mclord.classic;

import java.io.DataOutputStream;
import java.io.IOException;

public abstract class PacketWriter {
    public static class ParameterContainer {
        private final Object[] params;
        private int currentIdx;

        public ParameterContainer(Object[] params) {
            this.params = params;
        }

        public int getLength() {
            return params.length;
        }

        public Object get(int i) {
            return params[i];
        }
    }

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

    @SuppressWarnings("unchecked")
    protected final <T> T nextParameter(ParameterContainer container) {
        return (T) container.params[container.currentIdx++];
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
    public final void write0(DataOutputStream stream, Object... params) throws IOException {
        write(stream, new ParameterContainer(params));
    }

    public void write(
            DataOutputStream stream, ParameterContainer params
    ) throws IOException {
        stream.write(packetId);
    }
}
