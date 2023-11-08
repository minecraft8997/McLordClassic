package ru.mclord.classic;

import com.badlogic.gdx.utils.Disposable;

public class Player implements Locatable, Disposable {
    /* package-private */ final byte id;
    /* package-private */ final String username;
    /* package-private */ final Location location;
    /* package-private */ final Rotation rotation;
    /* package-private */ boolean op;

    /* package-private */ Player(byte id, String username) {
        this.id = id;
        this.username = username;
        this.location = new Location();
        this.rotation = new Rotation();
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

    public Rotation getRot() {
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
