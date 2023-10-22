package ru.mclord.classic;

import java.io.DataInputStream;
import java.io.IOException;

public class ExtInfoHandler extends PacketHandler {
    public static final byte PACKET_ID = 0x10;
    public static final int PACKET_LENGTH = 66;

    private int extensionCount;
    private int extensionsLeft;

    public ExtInfoHandler() {
        // since ExtEntryHandler will be handling packets inside
        // the main thread and will use "extensionLeft" field, we
        // should handle ExtInfo inside the main thread as well

        super(PACKET_ID, PACKET_LENGTH, false);
    }

    @Override
    public void handle(DataInputStream stream) throws IOException {
        String appName = Helper.readProtocolString(stream);
        extensionCount = stream.readUnsignedShort();
        extensionsLeft = extensionCount;
        McLordClassic.game().stage = McLordClassic.GameStage.ENABLING_PROTOCOL_EXTENSIONS;

        System.out.println("Server AppName: " + appName);
        System.out.println("Extension count: " + extensionsLeft);
    }

    public int getExtensionCount() {
        return extensionCount;
    }

    public int getExtensionsLeft() {
        return extensionsLeft;
    }

    public void decrementExtensionsLeft() {
        extensionsLeft--;
    }

    public boolean anyExtensionsLeft() {
        return extensionsLeft > 0;
    }
}
