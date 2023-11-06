package ru.mclord.classic.events;

import ru.mclord.classic.Event;
import ru.mclord.classic.Level;

public class LevelDownloadingFinishedEvent extends Event {
    private final Level level;

    private LevelDownloadingFinishedEvent(Level level) {
        super(false);

        this.level = level;
    }

    public static LevelDownloadingFinishedEvent create(Level level) {
        return new LevelDownloadingFinishedEvent(level);
    }

    public Level getLevel() {
        return level;
    }
}
