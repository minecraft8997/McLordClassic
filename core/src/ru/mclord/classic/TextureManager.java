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
import java.util.ArrayList;
import java.util.List;
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

    public static final String DEFAULT_TEXTURE_PACK =
            "https://static.classicube.net/default.zip";

    private static final TextureManager INSTANCE = new TextureManager();
    private final Texture[] textures = new Texture[TEXTURE_COUNT];
    private final Pixmap[] temporaryPixmaps = new Pixmap[TEXTURE_COUNT + 1];
    private final List<Texture> temporaryTextures;
    private Texture emptyTexture;

    private TextureManager() {
        this.temporaryTextures = new ArrayList<>();
    }

    public static TextureManager getInstance() {
        return INSTANCE;
    }

    @ShouldBeCalledBy(thread = "main")
    public void load(String path, boolean allowNet, boolean allowFileSystem) {
        BufferedImage image;
        try (InputStream inputStream = createInputStream(path, allowNet, allowFileSystem)) {
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
        Pixmap emptyPixmap = new Pixmap(TEXTURE_SIZE, TEXTURE_SIZE, Pixmap.Format.RGBA8888);
        //for (int i = 0; i < TEXTURE_SIZE; i++) {
        //    for (int j = 0; j < TEXTURE_SIZE; j++) {
                //emptyPixmap.drawPixel(i, j, 0xFF000009);
        //    }
        //}
        emptyTexture = new Texture(emptyPixmap);
        temporaryPixmaps[temporaryPixmaps.length - 1] = emptyPixmap;
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

            temporaryPixmaps[i] = pixmap;
        }
    }

    @SuppressWarnings("IOStreamConstructor")
    private static InputStream createInputStream(
            String path, boolean allowNet, boolean allowFileSystem
    ) throws IOException {
        String lowercasePath = path.toLowerCase();
        boolean isZIP = lowercasePath.endsWith(".zip");
        InputStream inputStream;
        if (lowercasePath.startsWith("http://") || lowercasePath.startsWith("https://")) {
            if (!allowNet) {
                throw new SecurityException("Using networking is not allowed");
            }

            URL url = new URL(path);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("User-Agent",
                    McLordClassic.APP_NAME + "/" + McLordClassic.VERSION);

            inputStream = connection.getInputStream();
        } else {
            if (!allowFileSystem) {
                throw new SecurityException("Using file system is not allowed");
            }

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
        if (i == -1) return emptyTexture;

        return textures[i];
    }

    public Texture rotate90Texture(Texture texture, boolean clockwise) {
        if (texture == emptyTexture) return texture;

        if (!texture.getTextureData().isPrepared()) {
            texture.getTextureData().prepare();
        }
        Pixmap pixmap = texture.getTextureData().consumePixmap();

        int width = texture.getWidth();
        int height = texture.getHeight();
        Pixmap newPixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                newPixmap.drawPixel(x, y, pixmap.getPixel(x, y));
            }
        }
        rotate90Pixmap(newPixmap, clockwise);
        Texture newTexture = new Texture(newPixmap);
        temporaryTextures.add(newTexture);
        newPixmap.dispose();

        return newTexture;
    }

    @SuppressWarnings({"ReassignedVariable", "SuspiciousNameCombination"})
    public void rotate90Pixmap(Pixmap pixmap, boolean clockwise) {
        int width = pixmap.getWidth();
        int height = pixmap.getHeight();

        if (width != height || Math.floorMod(width, 2) != 0) {
            throw new IllegalArgumentException();
        }

        if (clockwise) {
            for (int x = 0; x < width / 2; x++) {
                for (int y = x; y < width - x - 1; y++) {
                    int tmp = pixmap.getPixel(x, y);
                    pixmap.drawPixel(x, y, pixmap.getPixel(width - 1 - y, x));
                    pixmap.drawPixel(width - 1 - y, x, pixmap
                            .getPixel(width - 1 - x, width - 1 - y));
                    pixmap.drawPixel(width - 1 - x, width - 1 - y, pixmap
                            .getPixel(y, width - 1 - x));
                    pixmap.drawPixel(y, width - 1 - x, tmp);
                }
            }
        } else {
            for (int x = 0; x < width / 2; x++) {
                for (int y = x; y < width - x - 1; y++) {
                    int tmp = pixmap.getPixel(x, y);
                    pixmap.drawPixel(x, y, pixmap.getPixel(y, width - 1 - x));
                    pixmap.drawPixel(y, width - 1 - x, pixmap
                            .getPixel(width - 1 - x, width - 1 - y));
                    pixmap.drawPixel(width - 1 - x, width - 1 - y, pixmap
                            .getPixel(width - 1 - y, x));
                    pixmap.drawPixel(width - 1 - y, x, tmp);
                }
            }
        }
    }

    @Override
    @ShouldBeCalledBy(thread = "main")
    public void dispose() {
        Helper.dispose(emptyTexture);
        for (Texture texture : textures) {
            Helper.dispose(texture);
        }
        for (Pixmap pixmap : temporaryPixmaps) {
            Helper.dispose(pixmap);
        }
        for (Texture texture : temporaryTextures) {
            texture.dispose();
        }
        temporaryTextures.clear();
    }
}
