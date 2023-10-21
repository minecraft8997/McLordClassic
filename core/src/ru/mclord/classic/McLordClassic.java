package ru.mclord.classic;

import com.badlogic.gdx.Game;
import ru.mclord.classic.events.DisconnectEvent;
import ru.mclord.classic.events.PluginInitializationFinishedEvent;
import ru.mclord.classic.writers.IdentificationWriter;

import java.util.*;

public class McLordClassic extends Game {
	public enum GameStage {
		INTERNAL_INITIALIZATION,
		PRE_INITIALIZATION,
		CONNECTING_TO_THE_SERVER,
		ENABLING_PROTOCOL_EXTENSIONS,
		INITIALIZATION,
		POST_INITIALIZATION,
		DOWNLOADING_THE_LEVEL,
		IN_GAME,
		DISCONNECTED
	}

	public static final boolean DEBUG = true;
	private static final McLordClassic INSTANCE = new McLordClassic();

	private Properties gameProperties;
	private final Queue<Runnable> taskList = new ArrayDeque<>();
	@SuppressWarnings("FieldCanBeLocal")
	/* package-private */ NetworkingThread networkingThread;
	/* package-private */ Player thePlayer;
	/* package-private */ GameStage stage = GameStage.INTERNAL_INITIALIZATION;
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

		PacketManager.getInstance().registerWriter(new IdentificationWriter());

		stage = GameStage.CONNECTING_TO_THE_SERVER;
		networkingThread = new NetworkingThread();
		networkingThread.start();

		setScreen(new LoadingScreen());
	}

	@Override
	public void render() {
		synchronized (taskList) {
			while (!taskList.isEmpty()) {
				taskList.poll().run();
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
