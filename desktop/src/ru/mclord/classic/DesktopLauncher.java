package ru.mclord.classic;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.glutils.HdpiMode;

import java.io.*;
import java.util.Properties;

// Please note that on macOS the application
// needs to be started with the -XstartOnFirstThread JVM argument
public class DesktopLauncher {
	@SuppressWarnings("IOStreamConstructor")
	public static void main(String[] arg) throws IOException {
		Properties props = new Properties();
		props.setProperty("windowWidth", "1280");
		props.setProperty("windowHeight", "720");
		props.setProperty("fpsLimit", "0");
		props.setProperty("vsync", "true");
		props.setProperty("audio", "true");
		props.setProperty("hdpiMode", "Logical");
		props.setProperty("texturePack", TextureManager.DEFAULT_TEXTURE_PACK);
		props.setProperty("fov", "90.0");
		props.setProperty("xRay", "true");
		props.setProperty("searchForSkybox", "true");
		props.setProperty("renderDistance", "64.0");

		File propertiesFile = new File("game.properties");
		if (propertiesFile.exists()) {
			try (InputStream stream = new FileInputStream(propertiesFile)) {
				props.load(stream);
			}
		}
		try (OutputStream stream = new FileOutputStream(propertiesFile)) {
			props.store(stream, "hdpiMode can be either Logical " +
					"or Pixels, fpsLimit=0 means it's unlimited");
		}
		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();

		int width = Integer.parseInt(props.getProperty("windowWidth"));
		int height = Integer.parseInt(props.getProperty("windowHeight"));
		int fpsLimit = Integer.parseInt(props.getProperty("fpsLimit"));
		boolean vsync = Boolean.parseBoolean(props.getProperty("vsync"));
		boolean audio = Boolean.parseBoolean(props.getProperty("audio"));
		HdpiMode hdpiMode = HdpiMode.valueOf(props.getProperty("hdpiMode"));

		config.setWindowedMode(width, height);
		config.setForegroundFPS(fpsLimit);
		config.useVsync(vsync);
		config.disableAudio(!audio);
		config.setHdpiMode(hdpiMode);
		config.setTitle("McLordClassic");
		config.setWindowIcon();

		new Lwjgl3Application(McLordClassic.game().linkProperties(props), config);
	}
}
