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
import ru.mclord.classic.events.CustomizeEnvironmentEvent;

public class InGameScreen implements Screen {
    private static final InGameScreen INSTANCE = new InGameScreen();

    private BitmapFont font;
    private SpriteBatch spriteBatch;
    private ModelBatch modelBatch;
    private McLordFirstPersonCameraController cameraController;
    private Level level;
    private Environment environment;
    private PerspectiveCamera camera;
    private final float fov;
    private final float cameraFar;

    private InGameScreen() {
        fov = Float.parseFloat(McLordClassic.getProperty("fov"));
        cameraFar = Float.parseFloat(McLordClassic.getProperty("cameraFar"));
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
        EventManager.getInstance().fireEvent(CustomizeEnvironmentEvent.create(environment));

        Player player = McLordClassic.getPlayer();
        camera = new PerspectiveCamera(
                fov, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        // camera.position.set(player.spawnLocation.x, player.spawnLocation.y, player.spawnLocation.z);
        camera.position.set(0f, 0f, 0f);
        camera.near = 0.35f; // todo might be not the best value
        camera.far = cameraFar;
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
