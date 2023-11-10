package ru.mclord.classic;

import java.util.*;

public class EventManager implements Manager {
    private static final EventManager INSTANCE = new EventManager();

    private final Map<Class<? extends Event>, List<EventHandler<?>>> eventHandlerMap;

    private EventManager() {
        this.eventHandlerMap = new HashMap<>();
    }

    public static EventManager getInstance() {
        return INSTANCE;
    }

    public synchronized <T extends Event> void registerEventHandler(
            Class<T> eventClass, EventHandler<T> eventHandler
    ) {
        Objects.requireNonNull(eventClass);
        Objects.requireNonNull(eventHandler);

        List<EventHandler<? extends Event>> eventHandlerList =
                eventHandlerMap.computeIfAbsent(eventClass, (key) -> new ArrayList<>());
        eventHandlerList.add(eventHandler);
    }

    @SuppressWarnings("unchecked")
    public synchronized <T extends Event> boolean fireEvent(T event) {
        Objects.requireNonNull(event);

        if ("true".equalsIgnoreCase(System.getProperty("mclordDebugEvents"))) {
            System.out.println("Firing event " + event.getClass().getName());
        }

        Class<? extends Event> eventClass = event.getClass();
        if (!eventHandlerMap.containsKey(eventClass)) return true;

        List<EventHandler<? extends Event>> handlerList =
                eventHandlerMap.get(eventClass);
        for (EventHandler<? extends Event> eventHandler : handlerList) {
            Runnable task = () -> ((EventHandler<T>) eventHandler).handleEvent(event);
            if (Helper.isOnMainThread()) {
                if (event.isCancellable()) {
                    task.run();
                } else {
                    McLordClassic.game().addTask(task);
                }
            } else {
                Object lock = McLordClassic.game().addTask(() ->
                        ((EventHandler<T>) eventHandler)
                                .handleEvent(event), event.isCancellable());
                if (event.isCancellable()) {
                    try {
                        Helper.join(lock);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();

                        throw new RuntimeException(e);
                    }
                }
            }
            if (event.isCancellable() && event.isCancelled()) return false;
        }

        return true;
    }

    @Override
    public boolean checkStage() {
        return true;
    }
}
