package ru.mclord.classic;

public abstract class Event {
    private final boolean cancellable;

    private boolean cancelled;

    public Event(boolean cancellable) {
        this.cancellable = cancellable;
    }

    /*
     * Always use this method to determine whether a
     * random event is cancellable. Don't do an instanceof
     * check against ru.mclord.classic.CancellableEvent.
     */
    public final boolean isCancellable() {
        return cancellable;
    }

    public final boolean isCancelled() {
        return cancelled;
    }

    public final void setCancelled() {
        if (!cancellable) {
            throw new IllegalStateException("This event cannot be cancelled");
        }

        cancelled = true;
    }
}
