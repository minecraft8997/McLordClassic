package ru.mclord.classic;

import java.io.DataInputStream;

public abstract class LevelDownloadDriver {
    private static LevelDownloadDriver driver;

    public abstract void downloadLevel(DataInputStream stream);

    public static void setDriver(LevelDownloadDriver driver) {
        if (LevelDownloadDriver.driver != null) {
            throw new IllegalStateException("LevelDownloadDriver is already set");
        }

        LevelDownloadDriver.driver = driver;
    }
}
