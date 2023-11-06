package ru.mclord.classic;

import ru.mclord.classic.events.PluginInitializationFinishedEvent;
import ru.mclord.classic.events.PluginPostInitializationFinishedEvent;
import ru.mclord.classic.events.PluginPreInitializationFinishedEvent;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class PluginManager {
    public static final class Key {
        private Key() {
        }
    }

    private static final PluginManager INSTANCE = new PluginManager();
    private static final int MAX_ATTEMPTS = Integer
            .parseInt(System.getProperty("mclordMaxPluginAttempts", "100"));

    private final Map<String, Plugin> pluginMap = new HashMap<>();
    private Integer keyHashCode;

    static {
        if (MAX_ATTEMPTS < 2) {
            throw new IllegalArgumentException(
                    "\"mclordMaxPluginAttempts\" cannot be lower than 2");
        }
    }

    private PluginManager() {
    }

    public static PluginManager getInstance() {
        return INSTANCE;
    }

    /**
     * Loads plugins and calls the preInit() method.
     */
    @SuppressWarnings("BusyWait")
    /* package-private */ void loadPlugins() {
        McLordClassic game = McLordClassic.game();
        if (game.stage != McLordClassic.GameStage.INTERNAL_INITIALIZATION) {
            throw new IllegalStateException();
        }
        game.stage = McLordClassic.GameStage.PRE_INITIALIZATION;

        System.out.println("Loading and calling preInit() on plugins");

        File[] files = Helper.listPlugins();
        if (files == null || files.length == 0) {
            System.err.println("Could not locate any plugins, " +
                    "the game will continue loading without them. " +
                    "Since the game is mostly based on plugins, it probably won't be playable");

            return;
        }
        URL[] urls = new URL[files.length];

        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            try {
                urls[i] = file.toURI().toURL();
            } catch (MalformedURLException e) {
                System.err.println(
                        "Failed to construct a plugin file URL (" + file.getName() + ")");
                e.printStackTrace();
                System.err.println("The game will be terminated");

                System.exit(-1);
            }
        }
        System.out.println("Located plugins: " + urls.length);

        try (URLClassLoader child = new URLClassLoader(urls, getClass().getClassLoader())) {
            int attempt = 0;
            Set<File> loadedFiles = new HashSet<>();
            Set<String> knownIdentifiers = new HashSet<>();
            boolean fullyLoaded;
            while (true) {
                fullyLoaded = true;
                for (File file : files) {
                    if (loadedFiles.contains(file)) continue;
                    loadedFiles.add(file);

                    String loadingMessage;
                    if (attempt == 0) {
                        loadingMessage = "Loading " + file.getName();
                    } else {
                        loadingMessage = "Trying to load " + file.getName() + " again";
                    }
                    System.out.println(loadingMessage);

                    try (ZipFile zipFile = new ZipFile(file)) {
                        ZipEntry entry = zipFile.getEntry("plugin.properties");
                        if (entry == null) {
                            System.err.println("Could not find \"/plugin.properties\" file " +
                                    "in " + file.getName() + ". The jarfile will just be " +
                                    "stored in classpath");

                            continue;
                        }
                        Properties properties = new Properties();
                        try (InputStream stream = zipFile.getInputStream(entry)) {
                            properties.load(stream);
                        }

                        String name = properties.getProperty("name");
                        String version = properties.getProperty("version");
                        String author = properties.getProperty("author");
                        String mainClass = properties.getProperty("mainClass");
                        boolean corePlugin = Boolean
                                .parseBoolean(properties.getProperty("corePlugin"));
                        String dependsOnUnparsed = properties.getProperty("dependsOn");

                        if (name == null || version == null ||
                                author == null || mainClass == null) {
                            System.err.println("\"/plugin.properties\" file in " + file
                                    .getName() + " is missing one or multiple required " +
                                    "fields. The jarfile will just be stored in classpath");

                            continue;
                        }
                        if (attempt == 0) {
                            knownIdentifiers.add(name);
                        }
                        if (attempt == 0 && !corePlugin) {
                            System.out.println("Currently we're searching for " +
                                    "core plugins, skipping loading the plugin for now");
                            loadedFiles.remove(file);
                            // we don't have to set fullyLoaded = false; at attempt=0

                            continue;
                        }

                        if (dependsOnUnparsed != null) {
                            String[] dependsOn = dependsOnUnparsed.split(", ");

                            if (!corePlugin) {
                                boolean dependenciesOk = true;
                                for (String dependency : dependsOn) {
                                    if (!knownIdentifiers.contains(dependency)) {
                                        throw new IllegalStateException("Could not " +
                                                "find dependency \"" + dependency + "\"");
                                    }
                                    if (getPlugin(dependency) == null) {
                                        dependenciesOk = false;

                                        break;
                                    }
                                }
                                if (!dependenciesOk) {
                                    System.out.println("Required dependencies are " +
                                            "not loaded, skipping loading the plugin for now");
                                    loadedFiles.remove(file);
                                    fullyLoaded = false;

                                    continue;
                                }
                            } else if (dependsOn.length > 0) {
                                throw new RuntimeException(
                                        "A core plugin cannot depend on other plugins");
                            }
                        }
                        if (pluginMap.containsKey(name)) {
                            throw new RuntimeException("Detected " +
                                    "two plugins having exactly the same identifiers");
                        }
                        if (corePlugin && keyHashCode != null) {
                            throw new IllegalStateException("Found two core plugins");
                        }

                        System.out.printf("Enabling %s v%s (from %s) by %s%s",
                                name, version, file.getName(), author, System.lineSeparator());

                        Class<?> clazz = Class.forName(mainClass, true, child);
                        Object pluginInstanceObj = clazz.newInstance();
                        if (!(pluginInstanceObj instanceof Plugin)) {
                            throw new RuntimeException(mainClass + " from " + file
                                    .getName() + " does not implement ru.mclord." +
                                    "classic.Plugin interface");
                        }
                        Plugin plugin = (Plugin) pluginInstanceObj;
                        try {
                            if (corePlugin) {
                                Key key = new Key();
                                keyHashCode = key.hashCode();

                                plugin.message(key);
                            }
                            plugin.preInit();
                        } catch (Throwable t) {
                            System.err.println(
                                    "Failed to pre-initialize the plugin \"" + name + "\":");
                            t.printStackTrace();
                            System.err.println("The game will be terminated");

                            System.exit(-1);
                        }

                        pluginMap.put(name, plugin);
                    }
                }
                if (attempt == 0 && keyHashCode == null) {
                    System.err.println("Could not find a core plugin. " +
                            "init() and postInit() won't be called on plugins");
                }
                if (attempt >= 1 && fullyLoaded) break;
                if (attempt >= 2) Thread.sleep(100);

                attempt++;
                if (attempt >= MAX_ATTEMPTS) {
                    throw new RuntimeException("Too many plugin pre-initializing " +
                            "attempts. Either MAX_ATTEMPTS=" + MAX_ATTEMPTS + " is " +
                            "too low for current setup or plugin dependencies produce " +
                            "a some kind of deadlock");
                }
            }
        } catch (Exception e) {
            System.err.println("A serious issue occurred while loading plugins:");
            e.printStackTrace();
            System.err.println("The game will be terminated");

            System.exit(-1);
        }

        EventManager.getInstance().fireEvent(PluginPreInitializationFinishedEvent.create());
    }

    @ShouldBeCalledBy(thread = "main")
    public Plugin getPlugin(String name) {
        return pluginMap.get(name);
    }

    // is called by a plugin
    public void initPlugins(Key key) {
        if (keyHashCode == null) {
            throw new IllegalStateException(
                    "There are no core plugins that are currently loaded");
        }
        if (key != null && key.hashCode() != keyHashCode) {
            throw new SecurityException("Wrong key");
        }
        McLordClassic game = McLordClassic.game();
        if (game.stage != McLordClassic.GameStage.ENABLING_PROTOCOL_EXTENSIONS &&
                game.stage != McLordClassic.GameStage.CONNECTING_TO_THE_SERVER) {
            throw new IllegalStateException();
        }
        game.stage = McLordClassic.GameStage.INITIALIZATION;

        System.out.println();
        System.out.println("Calling init() on plugins");
        for (Map.Entry<String, Plugin> entry : pluginMap.entrySet()) {
            try {
                entry.getValue().init();
            } catch (Throwable t) {
                System.err.println("An issue occurred while calling " +
                        "the init() method on plugin \"" + entry.getKey() + "\":");
                t.printStackTrace();
                System.err.println("The game will be terminated");

                System.exit(-1);
            }
        }

        EventManager.getInstance().fireEvent(PluginInitializationFinishedEvent.create());
    }

    /*
     * Unfortunately this method is almost a copy-paste from initPlugin().
     * Might be it is worth to rewrite these 2 methods into a single one in the future.
     */
    /* package-private */ void postInitPlugins() {
        McLordClassic game = McLordClassic.game();
        if (game.stage != McLordClassic.GameStage.INITIALIZATION) {
            throw new IllegalStateException();
        }
        game.stage = McLordClassic.GameStage.POST_INITIALIZATION;

        System.out.println();
        System.out.println("Calling postInit() on plugins");
        for (Map.Entry<String, Plugin> entry : pluginMap.entrySet()) {
            try {
                entry.getValue().postInit();
            } catch (Throwable t) {
                System.err.println("An issue occurred while calling " +
                        "the postInit() method on plugin \"" + entry.getKey() + "\":");
                t.printStackTrace();
                System.err.println("The game will be terminated");

                System.exit(-1);
            }
        }

        EventManager.getInstance().fireEvent(PluginPostInitializationFinishedEvent.create());
    }
}
