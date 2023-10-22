package ru.mclord.classic;

public class Location {
    /* package-private */ int x;
    /* package-private */ int y;
    /* package-private */ int z;

    public Location() {
        this(0, 0, 0);
    }

    public Location(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setZ(int z) {
        this.z = z;
    }

    public void addX(int x) {
        this.x += x;
    }

    public void addY(int y) {
        this.y += y;
    }

    public void addZ(int z) {
        this.z += z;
    }

    public int getBlockX() {
        return Math.round((x - 16) / 32.0f);
    }

    public int getBlockY() {
        return Math.round((y - 16) / 32.0f);
    }

    public int getBlockZ() {
        return Math.round((z - 16) / 32.0f);
    }
}
