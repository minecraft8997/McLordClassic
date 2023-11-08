package ru.mclord.classic;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class LoadingScreen implements Screen {
    public static final int PROGRESS_HEIGHT = 32;

    private static final LoadingScreen INSTANCE = new LoadingScreen();

    private BitmapFont font;
    private SpriteBatch batch;
    private Texture progressTexture;
    private String status;
    private byte progress;
    private int width;
    private int height;

    private LoadingScreen() {
        font = new BitmapFont();
        batch = new SpriteBatch();

        Pixmap progressPixmap =
                new Pixmap(1, PROGRESS_HEIGHT, Pixmap.Format.RGB888);
        for (int i = 0; i < PROGRESS_HEIGHT; i++) {
            progressPixmap.drawPixel(0, i, 0xFFFFFF);
        }
        progressTexture = new Texture(progressPixmap);

        progressPixmap.dispose();
    }

    public static LoadingScreen getInstance() {
        return INSTANCE;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setProgress(byte progress) {
        this.progress = progress;
    }

    @Override
    public void show() {
    }

    @Override
    public void render(float delta) {
        Helper.clearScreen();

        batch.begin();
        font.draw(batch, (status != null ? status : "Loading"), 32, height - 32);
        for (int i = 0; i < TextureManager.TEXTURE_COUNT; i++) {
            Texture blockTexture = TextureManager.getInstance().getTexture(i);

            int x = i * TextureManager.TEXTURE_SIZE;
            if (x >= width) break;
            batch.draw(blockTexture, x, PROGRESS_HEIGHT);
        }
        for (int i = 0; i < progress; i++) {
            batch.draw(progressTexture, i, 0);
        }
        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        Helper.dispose(font); font = null;
        Helper.dispose(batch); batch = null;
        Helper.dispose(progressTexture); progressTexture = null;
    }
}
