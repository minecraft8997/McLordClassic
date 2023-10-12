package ru.mclord.classic;

public interface EventHandler<T extends Event> {
    void handleEvent(T event);
}
