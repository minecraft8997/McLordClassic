package ru.mclord.classic;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;

public class LoadingScreen implements Screen {
    private SpriteBatch batch = new SpriteBatch();

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);
        batch.begin();
        int c = 0;
        for (int i = 31; i >= 0; i--) {
            for (int j = 15; j >= 0; j--) {
                //System.out.println("Rendering " + (c - 1) + " at " + i * 16 + ", " + j * 16);
                batch.draw(TextureManager.getInstance().getTexture(c++), i * 16, j * 16);
            }
        }
        batch.draw(TextureManager.getInstance().getTexture(2), 500, 500);


        batch.end();
        //throw new RuntimeException();
    }

    @Override
    public void resize(int width, int height) {

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
        batch.dispose();
    }
}
