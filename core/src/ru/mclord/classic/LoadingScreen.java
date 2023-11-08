package ru.mclord.classic;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;

public class LoadingScreen implements Screen {
    private BitmapFont font;
    private SpriteBatch batch;
    private int width;
    private int height;

    @Override
    public void show() {
        font = new BitmapFont();
        batch = new SpriteBatch();
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);

        batch.begin();
        for (int i = 0; i < TextureManager.TEXTURE_COUNT; i++) {
            Texture blockTexture = TextureManager.getInstance().getTexture(i);

            int x = i * TextureManager.TEXTURE_SIZE;
            if (x >= width) break;
            batch.draw(blockTexture, x, 0);
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
    }
}
