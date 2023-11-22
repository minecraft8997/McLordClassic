package ru.mclord.classic;

import com.badlogic.gdx.Game;
import ru.mclord.classic.events.DisconnectEvent;
import ru.mclord.classic.events.LevelDownloadingFinishedEvent;
import ru.mclord.classic.events.PlayerSpawnEvent;

import java.util.ArrayDeque;
import java.util.Date;
import java.util.Properties;
import java.util.Queue;

public class McLordClassic extends Game {
	/* package-private */ static class TaskContainer {
		private final Runnable task;
		private final Thread thread;
		private final StackTraceElement[] stacktrace;
		private final boolean subscribed;
		private final long timestamp;
		/* package-private */ volatile boolean finished;

		public TaskContainer(
				Runnable task,
				Thread thread,
				StackTraceElement[] stacktrace,
				boolean subscribed
		) {
			this.task = task;
			this.thread = thread;
			this.stacktrace = stacktrace;
			this.subscribed = subscribed;
			timestamp = System.currentTimeMillis();
		}

		public void printStackTrace() {
			for (int i = 1; i < stacktrace.length; i++) {
				StackTraceElement element = stacktrace[i];

				System.err.println("        at " + element.toString());
			}
		}
	}

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
	public static final String VERSION = "0.1.2";
	public static final int VERSION_CODE = 2;

	private static final McLordClassic INSTANCE = new McLordClassic();

	/* package-private */ final Thread mainThread;
	private Properties gameProperties;
	/* package-private */ final Queue<TaskContainer> taskList = new ArrayDeque<>();
	/* package-private */ GameStage stage;
	/* package-private */ volatile NetworkingThread networkingThread;
	/* package-private */ Level level;
	private boolean levelDownloadFinishedForTheFirstTime = true;
	/* package-private */ String disconnectReason;

	private McLordClassic() {
		this.mainThread = Thread.currentThread();
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

	@ShouldBeCalledBy(thread = "main")
	public static String getProperty(String key) {
		return game().gameProperties.getProperty(key);
	}

	public void addTask(Runnable task) {
		addTask(task, false);
	}

	public Object addTask(Runnable task, boolean subscribe) {
		Thread currentThread = Thread.currentThread();
		StackTraceElement[] stacktrace = currentThread.getStackTrace();

		TaskContainer container = new TaskContainer(
				task, currentThread, stacktrace, subscribe);
		synchronized (taskList) {
			taskList.offer(container);
		}

		return (subscribe ? container : null);
	}

	@Override
	public void create() {
		setStage(GameStage.INTERNAL_INITIALIZATION);

		if (DEBUG) GameParameters.setupDebugProperties();
		GameParameters.collectAndVerify();

		EventManager.getInstance().registerEventHandler(
				DisconnectEvent.class, this::handleDisconnect);
		EventManager.getInstance().registerEventHandler(
				LevelDownloadingFinishedEvent.class, this::handleLevelDownloadingFinished);
		EventManager.getInstance().registerEventHandler(
				PlayerSpawnEvent.class, this::handlePlayerSpawn);

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
	@SuppressWarnings({"SynchronizeOnNonFinalField",
			"SynchronizationOnLocalVariableOrMethodParameter"})
	public void render() {
		synchronized (taskList) {
			int size = taskList.size();
			for (int i = 0; i < size; i++) {
				TaskContainer container = taskList.poll();
				//noinspection DataFlowIssue
				Runnable task = container.task;
				try {
					task.run();
				} catch (Throwable t) {
					System.err.println("Failed to complete a task:");
					t.printStackTrace();
					System.err.println("Details of the thread which " +
							"enqueued the task: " + container.thread.toString());
					System.err.println("Stacktrace snapshot of " +
							"the thread at the moment the task was enqueued:");
					container.printStackTrace();
					System.err.println("Date added: " + (new Date(container.timestamp)));
					System.err.println("The game will be terminated");

					System.exit(-1);
				}
				if (container.subscribed) {
					synchronized (container) {
						container.finished = true;
						container.notifyAll();
					}
				}
				if (task instanceof NetworkingRunnable) {
					synchronized (networkingThread) {
						networkingThread.finishedExecuting = true;
						networkingThread.notifyAll();
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
				addTask(() -> setScreen(InGameScreen.getInstance()));

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
