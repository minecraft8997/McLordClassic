package ru.mclord.classic;

import ru.mclord.classic.events.LevelDownloadingFinishedEvent;
import ru.mclord.classic.events.LevelDownloadingStartedEvent;

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

    public static void startDownloading(DataInputStream stream) throws IOException {
        LevelDownloadDriver driver = LevelDownloadDriver.getDriver();
        if (driver == null) {
            throw new IllegalStateException("LevelDownloadDriver was not set");
        }
        McLordClassic game = McLordClassic.game();
        game.addTask(() -> game.setStage(McLordClassic.GameStage.DOWNLOADING_THE_LEVEL));

        EventManager.getInstance().fireEvent(LevelDownloadingStartedEvent.create());
        System.out.println("Invoking LevelDownloadDriver (" + driver.getClass() + ")");
        Level level = driver.downloadLevel(stream);
        EventManager.getInstance().fireEvent(LevelDownloadingFinishedEvent.create(level));

        System.out.printf("Successfully downloaded a %dx%dx%d level%s",
                level.getSizeX(), level.getSizeY(), level.getSizeZ(), System.lineSeparator());

        game.addTask(() -> game.level = level);
    }
}
