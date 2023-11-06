package ru.mclord.classic.events;

import ru.mclord.classic.CancellableEvent;

public class KeyPressedEvent extends CancellableEvent {
    private KeyPressedEvent() {
    }

    public static KeyPressedEvent create() {
        return new KeyPressedEvent();
    }
}
