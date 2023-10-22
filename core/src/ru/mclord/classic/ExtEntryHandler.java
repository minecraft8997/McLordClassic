package ru.mclord.classic;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Set;

public class ExtEntryHandler extends PacketHandler {
    public static final byte PACKET_ID = 0x11;
    public static final int PACKET_LENGTH = 68;

    private boolean locked;

    public ExtEntryHandler() {
        // since we'll be activating extensions, we need
        // the handle() method to be called by the main thread

        super(PACKET_ID, PACKET_LENGTH, false);
    }

    @Override
    public void handle(DataInputStream stream) throws IOException {
        if (locked) {
            throw new IllegalStateException("Didn't expect to receive ExtEntry packets anymore");
        }
        String extName = Helper.readProtocolString(stream);
        int version = stream.readInt();

        String message;
        CPEManager manager = CPEManager.getInstance();
        if (manager.isExtensionSupported(extName, version)) {
            manager.activateExtension(extName, version);

            message = "Activated extension %s v%d";
        } else {
            message = "%s v%d is unsupported by the client";
        }
        message += System.lineSeparator();
        System.out.printf(message, extName, version);

        ExtInfoHandler extInfoHandler = (ExtInfoHandler) PacketManager
                .getInstance().getHandler(ExtInfoHandler.PACKET_ID, true);
        extInfoHandler.decrementExtensionsLeft();
        if (!extInfoHandler.anyExtensionsLeft()) {
            locked = true;

            writeOwnExtensions();
        }
    }

    private void writeOwnExtensions() throws IOException {
        ByteArrayOutputStream stream0 = new ByteArrayOutputStream();
        DataOutputStream stream = new DataOutputStream(stream0);

        // writing an ExtInfo
        stream.write(ExtInfoHandler.PACKET_ID);
        stream.write(Helper.toProtocolString(
                McLordClassic.APP_NAME + " v" + McLordClassic.VERSION));

        Set<CPE> extensionSet = CPEManager.getInstance().getSupportedExtensionsFast();

        stream.writeShort(extensionSet.size());

        // writing ExtEntries
        for (CPE extension : extensionSet) {
            stream.write(ExtEntryHandler.PACKET_ID);
            stream.write(Helper.toProtocolString(extension.name));
            stream.writeInt(extension.version);
        }

        Helper.writeRawDataAndFlush(stream0.toByteArray());
    }
}
