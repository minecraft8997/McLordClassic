package ru.mclord.classic;

import java.io.DataInputStream;
import java.io.IOException;

public abstract class LevelDownloadDriver {
    /* package-private */ static LevelDownloadDriver driver;

    public abstract Level downloadLevel(DataInputStream stream) throws IOException;

    public synchronized static LevelDownloadDriver getDriver() {
        return driver;
    }

    public static synchronized void setDriver(LevelDownloadDriver driver) {
        if (LevelDownloadDriver.driver != null) {
            throw new IllegalStateException("LevelDownloadDriver is already set");
        }

        LevelDownloadDriver.driver = driver;
    }
}
