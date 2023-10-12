package ru.mclord.classic;

@SuppressWarnings("RedundantThrows")
public interface Plugin {
    default void preInit() throws Exception {}
    default void init() throws Exception {}
    default void postInit() throws Exception {}
    default void disable() throws Exception {}
}
