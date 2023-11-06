package ru.mclord.classic.events;

import ru.mclord.classic.Event;

public class LevelDownloadingStartedEvent extends Event {
    private LevelDownloadingStartedEvent() {
        super(false);
    }

    public static LevelDownloadingStartedEvent create() {
        return new LevelDownloadingStartedEvent();
    }
}
