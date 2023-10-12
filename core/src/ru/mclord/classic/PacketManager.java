package ru.mclord.classic;

public class PacketManager implements Manager {
    private static final PacketManager INSTANCE = new PacketManager();

    private PacketManager() {
    }

    public static PacketManager getInstance() {
        return INSTANCE;
    }


    @Override
    public boolean checkStage() {
        return false;
    }
}
