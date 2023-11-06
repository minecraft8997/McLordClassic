package ru.mclord.classic.events;

import ru.mclord.classic.Event;

public class PluginInitializationFinishedEvent extends Event {
    private PluginInitializationFinishedEvent() {
        super(false);
    }

    public static PluginInitializationFinishedEvent create() {
        return new PluginInitializationFinishedEvent();
    }
}
