package ru.mclord.classic;

@SuppressWarnings("RedundantThrows")
public interface Plugin {
    // lifecycle

    default void preInit() throws Exception {}
    default void init() throws Exception {}
    default void postInit() throws Exception {}
    default void disable() throws Exception {}

    // miscellaneous

    default void message(Object message) throws Exception {
        message(null, message);
    }

    default void message(String description, Object message) throws Exception {}
}
