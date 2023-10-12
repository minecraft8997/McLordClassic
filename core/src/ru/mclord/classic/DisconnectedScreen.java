package ru.mclord.classic;

import com.badlogic.gdx.Screen;

public class DisconnectedScreen implements Screen {
    private static final DisconnectedScreen INSTANCE = new DisconnectedScreen();

    private DisconnectedScreen() {
    }

    public static DisconnectedScreen getInstance() {
        return INSTANCE;
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {

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

    }
}
