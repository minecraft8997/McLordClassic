package ru.mclord.classic;

public class Rotation {
    /* package-private */ int yaw;
    /* package-private */ int pitch;

    public Rotation() {
        this(0, 0);
    }

    public Rotation(int yaw, int pitch) {
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public int getYaw() {
        return yaw;
    }

    public int getPitch() {
        return pitch;
    }

    public void setYaw(int yaw) {
        this.yaw = yaw;
    }

    public void setPitch(int pitch) {
        this.pitch = pitch;
    }
}
