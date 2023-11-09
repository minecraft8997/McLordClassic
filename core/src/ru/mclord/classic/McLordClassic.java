package ru.mclord.classic;

import com.badlogic.gdx.Game;
import ru.mclord.classic.events.DisconnectEvent;
import ru.mclord.classic.events.LevelDownloadingFinishedEvent;
import ru.mclord.classic.events.PlayerSpawnEvent;

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
	/* package-private */ volatile NetworkingThread networkingThread;
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
		EventManager.getInstance().registerEventHandler(
				PlayerSpawnEvent.class, this::handlePlayerSpawn);
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

	@ShouldBeCalledBy(thread = "main")
	public static Player getPlayer() {
		Level level = McLordClassic.game().level;
		if (level == null) return null;

		return level.getPlayer((byte) -1);
	}

	public void addTask(Runnable task) {
		synchronized (taskList) {
			taskList.offer(task);
		}
	}

	@Override
	public void create() {
		System.out.println("Loading texture pack");
		String configTexturePack = gameProperties.getProperty("texturePack");
		if (configTexturePack == null || configTexturePack.isEmpty()) {
			configTexturePack = TextureManager.DEFAULT_TEXTURE_PACK;
		}
		TextureManager.getInstance()
				.load(configTexturePack, true, true);

		PluginManager.getInstance().loadPlugins();

		setStage(GameStage.CONNECTING_TO_THE_SERVER);
		networkingThread = new NetworkingThread();
		networkingThread.start();

		setScreen(LoadingScreen.getInstance());
	}

	@Override
	@SuppressWarnings("SynchronizeOnNonFinalField")
	public void render() {
		synchronized (taskList) {
			int size = taskList.size();
			for (int i = 0; i < size; i++) {
				Runnable task = taskList.poll();
				//noinspection DataFlowIssue
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
	public Level getLevel() {
		return level;
	}

	@ShouldBeCalledBy(thread = "main")
	public String getDisconnectReason() {
		return disconnectReason;
	}

	@ShouldBeCalledBy(thread = "main")
	public void setStage(GameStage stage) {
		GameStage old = this.stage;
		if (old == stage) return;
		this.stage = stage;

		switch (stage) {
			case PRE_INITIALIZATION: {
				LoadingScreen.getInstance().setStatus("Completing Pre-Initialization");

				break;
			}
			case CONNECTING_TO_THE_SERVER: {
				LoadingScreen.getInstance().setStatus("Connecting to " +
						GameParameters.getAddress() + ":" + GameParameters.getPort());

				break;
			}
			case ENABLING_PROTOCOL_EXTENSIONS: {
				LoadingScreen.getInstance().setStatus("Enabling protocol extensions");

				break;
			}
			case INITIALIZATION: {
				LoadingScreen.getInstance().setStatus("Initializing");

				break;
			}
			case DOWNLOADING_THE_LEVEL: {
				LoadingScreen.getInstance().setStatus("Downloading the level");

				Helper.dispose(level); level = null;
				if (!(getScreen() instanceof LoadingScreen)) {
					setScreen(LoadingScreen.getInstance());
				}

				break;
			}
			case POST_INITIALIZATION: {
				LoadingScreen.getInstance().setStatus("Completing Post-Initialization");

				break;
			}
			case IN_GAME: {
				LoadingScreen.getInstance().setStatus("Preparing level");
				setScreen(InGameScreen.getInstance());

				break;
			}
			case DISCONNECTED: {
				setScreen(DisconnectedScreen.getInstance());

				break;
			}
		}

		EventManager.getInstance().fireEvent(GameStageChangedEvent.create(old, stage));
	}

	private void handleDisconnect(DisconnectEvent event) {
		disconnectReason = event.getReason();

		setStage(GameStage.DISCONNECTED);
	}

	private void handleLevelDownloadingFinished(LevelDownloadingFinishedEvent event) {
		level = event.getLevel();

		InGameScreen gameScreen = InGameScreen.getInstance();
		gameScreen.dispose();
		gameScreen.setLevel(level);

		if (levelDownloadFinishedForTheFirstTime) {
			PluginManager.getInstance().postInitPlugins();

			levelDownloadFinishedForTheFirstTime = false;
		}
	}

	private void handlePlayerSpawn(PlayerSpawnEvent event) {
		Player player = event.getPlayer();
		if (player.isMe() && event.isFirstSpawnOnCurrentLevel()) {
			setStage(GameStage.IN_GAME);
		}
	}
	
	@Override
	public void dispose() {
		PluginManager.getInstance().disablePlugins();

		Helper.dispose(level); level = null;
		for (Block block : BlockManager.getInstance().enumerateBlocksFast()) {
			block.dispose();
		}
		TextureManager.getInstance().dispose();

		System.out.println("Goodbye!");
	}
}
