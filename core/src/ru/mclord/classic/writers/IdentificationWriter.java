package ru.mclord.classic.writers;

import ru.mclord.classic.Helper;
import ru.mclord.classic.PacketWriter;

import java.io.DataOutputStream;
import java.io.IOException;

public class IdentificationWriter extends PacketWriter {
    public static final byte PACKET_ID = 0x00;

    public IdentificationWriter() {
        super(PACKET_ID);
    }

    @Override
    public void write(DataOutputStream stream, ParameterContainer params) throws IOException {
        super.write(stream, params); // PacketID is written here

        byte protocol = nextParameter(params);
        String username = nextParameter(params);
        String mppass = nextParameter(params);
        byte padding = nextParameter(params);

        stream.write(protocol);
        stream.write(Helper.toProtocolString(username));
        stream.write(Helper.toProtocolString(mppass));
        stream.write(padding);
    }
}
