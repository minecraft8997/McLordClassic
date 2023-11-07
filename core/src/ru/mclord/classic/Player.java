package ru.mclord.classic;

public class Player implements Locatable {
    /* package-private */ final String username;
    /* package-private */ final Location location;
    /* package-private */ final Rotation rotation;
    /* package-private */ boolean op;

    public Player(String username) {
        this.username = username;
        this.location = new Location();
        this.rotation = new Rotation();
    }

    public void render() {

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

    // @Override
    public void dispose() {

    }
}
