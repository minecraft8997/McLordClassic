package ru.mclord.classic.events;

import ru.mclord.classic.Event;

public class PluginPreInitializationFinishedEvent extends Event {
    private PluginPreInitializationFinishedEvent() {
        super(false);
    }

    public static PluginPreInitializationFinishedEvent create() {
        return new PluginPreInitializationFinishedEvent();
    }
}
