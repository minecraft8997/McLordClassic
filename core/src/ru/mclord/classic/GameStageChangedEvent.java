package ru.mclord.classic;

public class GameStageChangedEvent extends Event {
    private final McLordClassic.GameStage previous;
    private final McLordClassic.GameStage current;

    private GameStageChangedEvent(
            McLordClassic.GameStage previous, McLordClassic.GameStage current
    ) {
        super(false);

        this.previous = previous;
        this.current = current;
    }

    /* package-private */ static GameStageChangedEvent create(
            McLordClassic.GameStage previous, McLordClassic.GameStage current
    ) {
        return new GameStageChangedEvent(previous, current);
    }

    public McLordClassic.GameStage getPrevious() {
        return previous;
    }

    public McLordClassic.GameStage getCurrent() {
        return current;
    }
}
