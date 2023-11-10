package ru.mclord.classic.events;

import com.badlogic.gdx.graphics.g3d.Environment;
import ru.mclord.classic.Event;

public class CustomizeEnvironmentEvent extends Event {
    private final Environment environment;

    private CustomizeEnvironmentEvent(Environment environment) {
        super(false);

        this.environment = environment;
    }

    public static CustomizeEnvironmentEvent create(Environment environment) {
        return new CustomizeEnvironmentEvent(environment);
    }

    public Environment getEnvironment() {
        return environment;
    }
}
