package ru.mclord.classic.events;

import ru.mclord.classic.Event;
import ru.mclord.classic.Player;

public class PlayerSpawnEvent extends Event {
    private final Player player;
    private final boolean firstSpawnOnCurrentLevel;

    private PlayerSpawnEvent(Player player, boolean firstSpawnOnCurrentLevel) {
        super(false);

        this.player = player;
        this.firstSpawnOnCurrentLevel = firstSpawnOnCurrentLevel;
    }

    public static PlayerSpawnEvent create(
            Player player, boolean firstSpawnOnCurrentLevel
    ) {
        return new PlayerSpawnEvent(player, firstSpawnOnCurrentLevel);
    }

    public boolean isFirstSpawnOnCurrentLevel() {
        return firstSpawnOnCurrentLevel;
    }

    public Player getPlayer() {
        return player;
    }
}
