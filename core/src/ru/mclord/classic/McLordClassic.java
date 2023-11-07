package ru.mclord.classic;

import com.badlogic.gdx.Game;
import ru.mclord.classic.events.DisconnectEvent;
import ru.mclord.classic.events.LevelDownloadingFinishedEvent;

import java.util.ArrayDeque;
import java.util.Properties;
import java.util.Queue;

public class McLordClassic extends Game {
	public enum GameStage {
		INTERNAL_INITIALIZATION,
		PRE_INITIALIZATION,
		CONNECTING_TO_THE_SERVER,
		ENABLING_PROTOCOL_EXTENSIONS,
		INITIALIZATION,
		DOWNLOADING_THE_LEVEL,
		POST_INITIALIZATION,
		IN_GAME,
		DISCONNECTED
	}

	public static final boolean DEBUG = true;
	public static final String APP_NAME = "McLordClassic";
	public static final String VERSION = "0.1";
	public static final int VERSION_CODE = 1;

	private static final McLordClassic INSTANCE = new McLordClassic();

	private Properties gameProperties;
	private final Queue<Runnable> taskList = new ArrayDeque<>();
	/* package-private */ GameStage stage = GameStage.INTERNAL_INITIALIZATION;
	/* package-private */ NetworkingThread networkingThread;
	/* package-private */ Player thePlayer;
	/* package-private */ Level level;
	private boolean levelDownloadFinishedForTheFirstTime = true;
	/* package-private */ String disconnectReason;

	private McLordClassic() {
		if (DEBUG) GameParameters.setupDebugProperties();
		GameParameters.collectAndVerify();
		EventManager.getInstance().registerEventHandler(
				DisconnectEvent.class, this::handleDisconnect);
		EventManager.getInstance().registerEventHandler(
				LevelDownloadingFinishedEvent.class, this::handleLevelDownloadingFinished);
	}

	public static McLordClassic game() {
		return INSTANCE;
	}

	public McLordClassic linkProperties(Properties gameProperties) {
		if (this.gameProperties != null) {
			throw new IllegalStateException("Game properties are already set");
		}
		this.gameProperties = gameProperties;

		return this;
	}

	public void addTask(Runnable task) {
		synchronized (taskList) {
			taskList.offer(task);
		}
	}

	@Override
	public void create() {
		PluginManager.getInstance().loadPlugins();

		setStage(GameStage.CONNECTING_TO_THE_SERVER);
		networkingThread = new NetworkingThread();
		networkingThread.start();

		setScreen(new LoadingScreen());
	}

	@Override
	@SuppressWarnings("SynchronizeOnNonFinalField")
	public void render() {
		synchronized (taskList) {
			while (!taskList.isEmpty()) {
				Runnable task = taskList.poll();
				task.run();
				if (task instanceof NetworkingRunnable) {
					synchronized (networkingThread) {
						networkingThread.finishedExecuting = true;
						networkingThread.notify();
					}
				}
			}
		}

		super.render();
	}

	@ShouldBeCalledBy(thread = "main")
	public GameStage getStage() {
		return stage;
	}

	@ShouldBeCalledBy(thread = "main")
	public void setStage(GameStage stage) {
		GameStage old = this.stage;
		this.stage = stage;

		EventManager.getInstance().fireEvent(GameStageChangedEvent.create(old, stage));
	}

	private void handleDisconnect(DisconnectEvent event) {
		disconnectReason = event.getReason();
		setStage(GameStage.DISCONNECTED);

		setScreen(DisconnectedScreen.getInstance());
	}

	private void handleLevelDownloadingFinished(LevelDownloadingFinishedEvent event) {
		if (levelDownloadFinishedForTheFirstTime) {
			PluginManager.getInstance().postInitPlugins();

			levelDownloadFinishedForTheFirstTime = false;
		}

		setStage(GameStage.IN_GAME);
	}
	
	@Override
	public void dispose() {
	}
}
