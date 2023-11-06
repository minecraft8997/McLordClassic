package ru.mclord.classic.events;

import ru.mclord.classic.Event;

public class PluginPostInitializationFinishedEvent extends Event {
    private PluginPostInitializationFinishedEvent() {
        super(false);
    }

    public static PluginPostInitializationFinishedEvent create() {
        return new PluginPostInitializationFinishedEvent();
    }
}
