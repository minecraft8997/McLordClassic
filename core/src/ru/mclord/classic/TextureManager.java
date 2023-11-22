package ru.mclord.classic;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Disposable;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class TextureManager implements Disposable {
    public interface Handler {
        void handle(Pixmap pixmap, int xOffset, int yOffset, int x, int y);

        default boolean shouldWalk(int i) {
            return true;
        }
    }

    public static final String DEFAULT_TEXTURE_PACK =
            "https://static.classicube.net/default.zip";

    private static final TextureManager INSTANCE = new TextureManager();
    private Texture[] textures;
    private final boolean searchForSkybox;
    private final Texture[] skyboxTextures = new Texture[6];
    /* package-private */ boolean skyboxPresented;
    /* package-private */ int textureSize;
    private final List<Pixmap> temporaryPixmaps = new ArrayList<>();
    private final List<Texture> temporaryTextures;
    private Texture emptyTexture;

    private TextureManager() {
        this.searchForSkybox = Boolean.parseBoolean(
                McLordClassic.getProperty("searchForSkybox"));
        this.temporaryTextures = new ArrayList<>();
    }

    public static TextureManager getInstance() {
        return INSTANCE;
    }

    @SuppressWarnings("unchecked")
    @ShouldBeCalledBy(thread = "main")
    public void load(String path, boolean allowNet, boolean allowFileSystem) {
        BufferedImage image;
        BufferedImage skybox = null;
        try {
            String[] query;
            if (searchForSkybox) {
                query = new String[]{"terrain.png", "skybox.png"};
            } else {
                query = new String[]{"terrain.png"};
            }
            Object result = createInputStreams(path, allowNet, allowFileSystem, query);
            if (result instanceof InputStream) { // no skybox for sure :(
                try (InputStream inputStream = (InputStream) result){
                    image = ImageIO.read(inputStream);
                }
            } else {
                Map<String, InputStream> streams = (Map<String, InputStream>) result;

                InputStream terrainStream = streams.get("terrain.png");
                if (terrainStream == null) {
                    throw new FileNotFoundException("Could not load terrain textures");
                }
                image = ImageIO.read(terrainStream);
                if (searchForSkybox) {
                    InputStream skyboxStream = streams.get("skybox.png");

                    if (skyboxStream != null) {
                        skybox = ImageIO.read(skyboxStream);
                    } else {
                        System.err.println("Could not load skybox textures");
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        int width = image.getWidth();
        int height = image.getHeight();

        int textureCount;
        if (width % 16 != 0) illegalTerrainDimensions();
        textureSize = width / 16;
        if (height % textureSize != 0) illegalTerrainDimensions();
        textureCount = (height / textureSize) * 16;

        textures = new Texture[textureCount];

        Pixmap emptyPixmap = new Pixmap(textureSize, textureSize, Pixmap.Format.RGBA8888);
        emptyTexture = new Texture(emptyPixmap);
        temporaryPixmaps.add(emptyPixmap);

        walk(textures, temporaryPixmaps, textures.length, textureSize,
                textureSize, 16, (pixmap, xOffset, yOffset, x, y) -> {

            int color;
            int realX = xOffset + x;
            int realY = yOffset + y;
            if (realX >= width || realY >= height) color = 0;
            else color = image.getRGB(realX, realY);

            pixmap.drawPixel(x, y, textureColorFrom(color));
        });

        if (skybox != null) {
            int skyboxWidth = skybox.getWidth();
            int skyboxHeight = skybox.getHeight();
            if (skyboxHeight % 2 != 0) {
                System.err.println("skybox.png height should be even");

                return;
            }
            int skyboxTextureSize = skyboxHeight / 2;
            if (skyboxTextureSize * 4 != skyboxWidth) {
                System.err.println("Unexpected width of skybox.png");

                return;
            }
            BufferedImage finalSkybox = skybox;
            walk(skyboxTextures, temporaryPixmaps, 8, skyboxTextureSize,
                    skyboxTextureSize, 4, new Handler() {

                @Override
                public void handle(Pixmap pixmap, int xOffset, int yOffset, int x, int y) {
                    int realX = xOffset + x;
                    int realY = yOffset + y;
                    int color = finalSkybox.getRGB(realX, realY);

                    pixmap.drawPixel(x, y, textureColorFrom(color));
                }

                @Override
                public boolean shouldWalk(int i) {
                    return (i != 0 && i != 3);
                }
            });

            skyboxPresented = true;
        }
    }

    private static void illegalTerrainDimensions() {
        throw new RuntimeException("Illegal terrain.png dimensions");
    }

    /*
     * Turns a BufferedImage pixel color into a Texture color by
     * moving the alpha value (the highest byte) to the lowest byte. For example
     * fixColor(0xFFFF0000) returns 0xFF0000FF (nontransparent red)
     * fixColor(0xFF888888) returns 0x888888FF (nontransparent grey)
     * fixColor(0x01888888) returns 0x88888801 (transparent grey)
     */
    public static int textureColorFrom(int bufferedImageColor) {
        byte alpha = (byte) ((bufferedImageColor >> 24) & 0xFF);
        bufferedImageColor <<= 8;
        bufferedImageColor += alpha;
        // might be it's possible to optimize this?

        return bufferedImageColor;
    }

    public void walk(
            Texture[] textures,
            List<Pixmap> temporaryPixmaps,
            int textureCountInImage,
            int textureWidth,
            int textureHeight,
            int textureCountInARow,
            Handler handler
    ) {
        int texturesI = 0;
        for (int i = 0; i < textureCountInImage; i++) {
            if (!handler.shouldWalk(i)) continue;

            Pixmap pixmap = new Pixmap(textureWidth, textureHeight, Pixmap.Format.RGBA8888);

            int xOffset = (i % textureCountInARow) * textureWidth;
            int yOffset = (i / textureCountInARow) * textureHeight;
            for (int x = 0; x < textureWidth; x++) {
                for (int y = 0; y < textureHeight; y++) {
                    handler.handle(pixmap, xOffset, yOffset, x, y);
                }
            }
            textures[texturesI++] = new Texture(pixmap);

            temporaryPixmaps.add(pixmap);
        }
    }

    @SuppressWarnings("IOStreamConstructor")
    public static Object createInputStreams(
            String path, boolean allowNet, boolean allowFileSystem, String... objects
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
            Map<String, InputStream> inputStreams = new HashMap<>();
            try (ZipInputStream zipStream = new ZipInputStream(inputStream)) {
                DataInputStream wrapper = new DataInputStream(zipStream);

                ZipEntry entry;
                while ((entry = zipStream.getNextEntry()) != null) {
                    if (entry.isDirectory()) continue;

                    String name = entry.getName();
                    if (Helper.containsIgnoreCase(name, objects)) {
                        byte[] bytes = new byte[(int) entry.getSize()];
                        wrapper.readFully(bytes);
                        inputStreams.put(name, new ByteArrayInputStream(bytes));
                        if (inputStreams.size() == objects.length) break;
                    }
                }
            }

            return inputStreams;
        }

        return inputStream;
    }

    @ShouldBeCalledBy(thread = "main")
    public Texture getTexture(int i) {
        if (i == -1) return emptyTexture;

        return textures[i];
    }

    public boolean isSkyboxPresented() {
        return skyboxPresented;
    }

    @ShouldBeCalledBy(thread = "main")
    public Texture getSkyboxTexture(int i) {
        return skyboxTextures[i];
    }

    public Texture getEmptyTexture() {
        return emptyTexture;
    }

    public int getTextureCount() {
        return textures.length;
    }

    public int getTextureSize() {
        return textureSize;
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
        if (textures != null) {
            for (Texture texture : textures) {
                Helper.dispose(texture);
            }

            textures = null;
        }
        for (Texture texture : skyboxTextures) {
            Helper.dispose(texture);
        }
        for (Pixmap pixmap : temporaryPixmaps) {
            pixmap.dispose();
        }
        for (Texture texture : temporaryTextures) {
            texture.dispose();
        }
        temporaryPixmaps.clear();
        temporaryTextures.clear();
    }
}
