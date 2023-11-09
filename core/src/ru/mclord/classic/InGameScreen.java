package ru.mclord.classic;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;

public class InGameScreen implements Screen {
    private static final InGameScreen INSTANCE = new InGameScreen();

    private BitmapFont font;
    private SpriteBatch spriteBatch;
    private ModelBatch modelBatch;
    private McLordFirstPersonCameraController cameraController;
    private Level level;
    private Environment environment;
    private PerspectiveCamera camera;

    private InGameScreen() {
    }

    public static InGameScreen getInstance() {
        return INSTANCE;
    }

    public void setLevel(Level level) {
        if (font != null || spriteBatch != null ||
                modelBatch != null || cameraController != null
        ) {
            throw new IllegalStateException("InGameScreen is " +
                    "already initialized against another Level. Call dispose() first");
        }

        this.level = level;
    }

    @Override
    public void show() {
        if (level == null) {
            throw new IllegalStateException("level is not set");
        }
        font = new BitmapFont();
        spriteBatch = new SpriteBatch();
        modelBatch = new ModelBatch();

        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight,
                0.5f, 0.5f, 0.5f, 1.0f));
        environment.add(new DirectionalLight().set(
                0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.5f));
        environment.add(new DirectionalLight().set(
                0.2f, 0.2f, 0.2f, 1f, 0.8f, 0.5f));

        camera = new PerspectiveCamera(
                90.0f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.position.set(0, 0, 0);
        camera.near = 1f;
        camera.far = 300f;
        camera.update();

        level.initGraphics();

        cameraController = new McLordFirstPersonCameraController(camera);
        Gdx.input.setInputProcessor(cameraController);
        Gdx.input.setCursorCatched(true);
    }

    @Override
    public void render(float delta) {
        cameraController.update();

        Helper.clearDepthRGB(17, 137, 217);

        modelBatch.begin(camera);
        level.render(modelBatch, environment);
        modelBatch.end();
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
        Helper.dispose(font); font = null;
        Helper.dispose(spriteBatch); spriteBatch = null;
        Helper.dispose(modelBatch); modelBatch = null;
        Helper.dispose(level); level = null;
        cameraController = null;
    }
}
