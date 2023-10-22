package ru.mclord.classic;

public class Level {
    private final short[][][] blocks;
    /* package-private */ final int sizeX;
    /* package-private */ final int sizeY;
    /* package-private */ final int sizeZ;

    public Level(int sizeX, int sizeY, int sizeZ) {
        this.blocks = new short[sizeX][sizeY][sizeZ];
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
    }

    public int getSizeX() {
        return sizeX;
    }

    public int getSizeY() {
        return sizeY;
    }

    public int getSizeZ() {
        return sizeZ;
    }
}
