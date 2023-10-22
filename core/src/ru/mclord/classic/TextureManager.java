package ru.mclord.classic;

public class TextureManager {
    public enum Format {
        ZIP(".zip"), PNG(".png");

        /* package-private */ final String endsWith;

        Format(String endsWith) {
            this.endsWith = endsWith;
        }

        public String getEndsWith() {
            return endsWith;
        }
    }

    private static final TextureManager INSTANCE = new TextureManager();

    private TextureManager() {
    }

    public static TextureManager getInstance() {
        return INSTANCE;
    }

    public void loadFromFile(String filePath) {
        Format format = determineFormat(filePath);
        if (format == Format.ZIP) {

        }
    }

    public void loadFromURL(String url) {

    }

    public Format determineFormat(String path) {
        path = path.toLowerCase();
        for (Format format : Format.values()) {
            if (path.endsWith(format.endsWith)) {
                return format;
            }
        }

        throw new IllegalStateException("Unknown format");
    }
}
