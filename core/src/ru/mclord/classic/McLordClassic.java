package ru.mclord.classic;

import com.badlogic.gdx.Game;
import ru.mclord.classic.events.DisconnectEvent;
import ru.mclord.classic.events.PluginInitializationFinishedEvent;

import java.util.*;

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
	public static final byte PROTOCOL_VERSION = 7;

	private static final McLordClassic INSTANCE = new McLordClassic();

	private Properties gameProperties;
	private final Queue<Runnable> taskList = new ArrayDeque<>();
	/* package-private */ GameStage stage = GameStage.INTERNAL_INITIALIZATION;
	/* package-private */ NetworkingThread networkingThread;
	/* package-private */ Player thePlayer;
	/* package-private */ Level level;
	/* package-private */ String disconnectReason;

	private McLordClassic() {
		if (DEBUG) GameParameters.setupDebugProperties();
		GameParameters.collectAndVerify();
		EventManager.getInstance().registerEventHandler(
				DisconnectEvent.class, this::handleDisconnect);
		EventManager.getInstance().registerEventHandler(
				PluginInitializationFinishedEvent.class, this::handleInitFinished);
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

		PacketManager.getInstance().registerWriter(new PlayerIdentificationWriter());

		PacketManager.getInstance().registerHandler(new ExtInfoHandler());
		PacketManager.getInstance().registerHandler(new ExtEntryHandler());
		PacketManager.getInstance().registerHandler(new PingPacketHandler());
		PacketManager.getInstance().registerHandler(new ServerIdentificationHandler());

		stage = GameStage.CONNECTING_TO_THE_SERVER;
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

	private void handleDisconnect(DisconnectEvent event) {
		disconnectReason = event.getReason();
		stage = GameStage.DISCONNECTED;

		setScreen(DisconnectedScreen.getInstance());
	}

	private void handleInitFinished(PluginInitializationFinishedEvent event) {
		PluginManager.getInstance().postInitPlugins();
	}
	
	@Override
	public void dispose() {
	}
}
