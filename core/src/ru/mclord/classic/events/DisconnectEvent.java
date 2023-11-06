package ru.mclord.classic.events;

import ru.mclord.classic.Event;

public class DisconnectEvent extends Event {
    /*
     * Marking the field as package-private is useless.
     * Main game classes are located in a different package.
     * This applies to any other Event subclass located in the
     * "events" package.
     */
    private final String reason;

    private DisconnectEvent(String reason) {
        super(false);

        this.reason = reason;
    }

    public static DisconnectEvent create(String reason) {
        return new DisconnectEvent(reason);
    }

    public String getReason() {
        return reason;
    }
}
