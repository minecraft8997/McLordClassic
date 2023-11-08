package ru.mclord.classic.events;

import ru.mclord.classic.Event;
import ru.mclord.classic.Player;

public class PlayerSpawnEvent extends Event {
    private final Player player;

    private PlayerSpawnEvent(Player player) {
        super(false);

        this.player = player;
    }

    public static PlayerSpawnEvent create(Player player) {
        return new PlayerSpawnEvent(player);
    }

    public boolean isFirstSpawnOnCurrentLevel() {
        return (player != null);
    }

    public Player getPlayer() {
        return player;
    }
}
