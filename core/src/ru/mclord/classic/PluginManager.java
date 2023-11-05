package ru.mclord.classic;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class PluginManager {
    private static final PluginManager INSTANCE = new PluginManager();

    private final Map<String, Plugin> pluginMap = new HashMap<>();

    private PluginManager() {
    }

    public static PluginManager getInstance() {
        return INSTANCE;
    }

    /**
     * Loads plugins and calls the preInit() method.
     */
    /* package-private */ void loadPlugins() {
        McLordClassic game = McLordClassic.game();
        if (game.stage != McLordClassic.GameStage.INTERNAL_INITIALIZATION) {
            throw new IllegalStateException();
        }
        game.stage = McLordClassic.GameStage.PRE_INITIALIZATION;

        System.out.println("Calling preInit() on plugins");

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
            for (File file : files) {
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

                    if (name == null || version == null ||
                            author == null || mainClass == null) {
                        System.err.println("\"/plugin.properties\" file in " + file
                                .getName() + " is missing one or multiple required " +
                                "fields. The jarfile will just be stored in classpath");

                        continue;
                    }
                    System.out.printf("Loading %s v%s (from %s) by %s%s",
                            name, version, file.getName(), author, System.lineSeparator());
                    if (pluginMap.containsKey(name)) {
                        throw new RuntimeException("Detected " +
                                "two plugins having exactly the same identifiers");
                    }

                    Class<?> clazz = Class.forName(mainClass, true, child);
                    Object pluginInstanceObj = clazz.newInstance();
                    if (!(pluginInstanceObj instanceof Plugin)) {
                        throw new RuntimeException(mainClass + " from " + file
                                .getName() + " does not implement ru.mclord." +
                                "classic.Plugin interface");
                    }
                    Plugin plugin = (Plugin) pluginInstanceObj;
                    try {
                        plugin.preInit();
                    } catch (Throwable t) {
                        throw new RuntimeException(String.format("Calling %s" +
                                "#preInit from %s", mainClass, file.getName()), t);
                    }

                    pluginMap.put(name, plugin);
                }
            }
        } catch (Exception e) {
            System.err.println("A serious issue occurred while loading plugins:");
            e.printStackTrace();
            System.err.println("The game will be terminated");

            System.exit(-1);
        }
    }

    @ShouldBeCalledBy(thread = "main")
    public Plugin getPlugin(String name) {
        return pluginMap.get(name);
    }

    // is called by a plugin
    /* package-private */ void initPlugins() {
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
                System.err.println("An issue " +
                        "occurred while initializing plugin \"" + entry.getKey() + "\":");
                t.printStackTrace();
                System.err.println("The game will be terminated");

                System.exit(-1);
            }
        }
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
                        "the postInit() method in plugin \"" + entry.getKey() + "\":");
                t.printStackTrace();
                System.err.println("The game will be terminated");

                System.exit(-1);
            }
        }
    }
}
