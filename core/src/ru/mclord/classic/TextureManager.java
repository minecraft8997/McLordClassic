package ru.mclord.classic;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Disposable;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class TextureManager implements Disposable {
    public static final int TEXTURE_SIZE = 16;
    public static final int IMAGE_WIDTH = 256;
    public static final int IMAGE_HEIGHT = 512;
    public static final int TEXTURE_COUNT;

    static {
        //noinspection ConstantValue
        if (IMAGE_WIDTH % TEXTURE_SIZE != 0 || IMAGE_HEIGHT % TEXTURE_SIZE != 0) {
            throw new IllegalStateException("Invalid " +
                    "texture size, image width or image height");
        }

        TEXTURE_COUNT = IMAGE_WIDTH * IMAGE_HEIGHT / (TEXTURE_SIZE * TEXTURE_SIZE);
    }

    private static final TextureManager INSTANCE = new TextureManager();
    private final Texture[] textures = new Texture[TEXTURE_COUNT];

    private TextureManager() {
    }

    public static TextureManager getInstance() {
        return INSTANCE;
    }

    @ShouldBeCalledBy(thread = "main")
    public void load(String path) {
        BufferedImage image;
        try (InputStream inputStream = createInputStream(path)) {
            image = ImageIO.read(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        int width = image.getWidth();
        int height = image.getHeight();
        /*
        if (image.getWidth() != IMAGE_WIDTH || image.getHeight() != IMAGE_HEIGHT) {
            throw new IllegalArgumentException("The " +
                    "image must be " + IMAGE_WIDTH + "x" + IMAGE_HEIGHT);
        }
         */
        for (int i = 0; i < TEXTURE_COUNT; i++) {
            Pixmap pixmap = new Pixmap(TEXTURE_SIZE, TEXTURE_SIZE, Pixmap.Format.RGBA8888);

            int xOffset = (i % 16) * TEXTURE_SIZE;
            int yOffset = (i / 16) * TEXTURE_SIZE;
            for (int x = 0; x < TEXTURE_SIZE; x++) {
                for (int y = 0; y < TEXTURE_SIZE; y++) {
                    int color;
                    int realX = xOffset + x;
                    int realY = yOffset + y;
                    if (realX >= width || realY >= height) color = 0;
                    else color = image.getRGB(realX, realY);

                    // The highest byte here represents the alpha
                    // value. We need to move it to the lowest byte
                    byte alpha = (byte) ((color >> 24) & 0xFF);
                    color <<= 8;
                    color += alpha;
                    pixmap.drawPixel(x, y, color);
                }
            }
            textures[i] = new Texture(pixmap);

            pixmap.dispose();
        }
    }

    private static InputStream createInputStream(String path) throws IOException {
        String lowercasePath = path.toLowerCase();
        boolean isZIP = lowercasePath.endsWith(".zip");
        InputStream inputStream;
        if (lowercasePath.startsWith("http://") || lowercasePath.startsWith("https://")) {
            URL url = new URL(path);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("User-Agent",
                    McLordClassic.APP_NAME + "/" + McLordClassic.VERSION);

            inputStream = connection.getInputStream();
        } else {
            inputStream = new FileInputStream(path);
        }
        if (isZIP) {
            inputStream = new ZipInputStream(inputStream);
            ZipEntry entry;
            while ((entry = ((ZipInputStream) inputStream).getNextEntry()) != null) {
                if (entry.isDirectory()) continue;
                if (entry.getName().equalsIgnoreCase("terrain.png")) {
                    return inputStream;
                }
            }
            inputStream.close();

            throw new FileNotFoundException("Could not find terrain.png file in the ZIP");
        }

        return inputStream;
    }

    @ShouldBeCalledBy(thread = "main")
    public Texture getTexture(int i) {
        return textures[i];
    }

    @Override
    @ShouldBeCalledBy(thread = "main")
    public void dispose() {
        for (Texture texture : textures) {
            if (texture != null) texture.dispose();
        }
    }
}
