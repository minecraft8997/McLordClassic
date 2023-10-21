package ru.mclord.classic.handlers;

import ru.mclord.classic.PacketHandler;

import java.io.DataInputStream;
import java.io.IOException;

public class ExtInfoHandler extends PacketHandler {
    public static final byte PACKET_ID = 0x10;
    public static final int PACKET_LENGTH = 66;

    public ExtInfoHandler() {
        super(PACKET_ID, PACKET_LENGTH, true);
    }

    @Override
    public void handle(DataInputStream stream) throws IOException {

    }
}
