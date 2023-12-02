package ru.mclord.classic;

import com.badlogic.gdx.utils.Disposable;

public class Player implements Locatable, Disposable {
    public static final int MAX_CLICK_DISTANCE = 5;

    /* package-private */ final byte id;
    /* package-private */ final String username;
    /* package-private */ final Location location;
    /* package-private */ final Location spawnLocation;
    /* package-private */ final Rotation rotation;
    /* package-private */ final Rotation spawnRotation;
    /* package-private */ boolean op;

    /* package-private */ Player(byte id, String username) {
        this.id = id;
        this.username = username;
        this.location = new Location();
        this.spawnLocation = new Location();
        this.rotation = new Rotation();
        this.spawnRotation = new Rotation();
    }

    public static Player create(byte id, String username) {
        return new Player(id, username);
    }

    public boolean isMe() {
        return (id == -1);
    }

    public void render() {

    }

    public byte getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    @Override
    public Location getLoc() {
        return location;
    }

    public Location getSpawnLoc() {
        return spawnLocation;
    }

    public Rotation getRot() {
        return rotation;
    }

    public Rotation getSpawnRot() {
        return rotation;
    }

    public boolean isOp() {
        return op;
    }

    public void setOp(boolean op) {
        this.op = op;
    }

    @Override
    public void dispose() {

    }
}
