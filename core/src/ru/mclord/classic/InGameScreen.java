package ru.mclord.classic;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import ru.mclord.classic.events.CustomizeEnvironmentEvent;

public class InGameScreen implements Screen {
    private static final InGameScreen INSTANCE = new InGameScreen();

    private final Plugin corePlugin = PluginManager.getInstance().getCorePlugin();
    private BitmapFont font;
    private SpriteBatch spriteBatch;
    private Texture crosshairTexture;
    private ModelInstance aimedBlockModelInstance;
    private ModelBatch modelBatch;
    private McLordFirstPersonCameraController cameraController;
    private Level level;
    private Skybox skybox;
    private Environment environment;
    private PerspectiveCamera camera;
    private final float fov;

    private InGameScreen() {
        fov = Float.parseFloat(McLordClassic.getProperty("fov"));
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

        Texture aimedBlockTexture = TextureManager.getInstance().aimedBlockTexture;
        aimedBlockModelInstance = new ModelInstance(Helper
                .constructBlock(0.5f,
                        aimedBlockTexture,
                        aimedBlockTexture,
                        aimedBlockTexture,
                        aimedBlockTexture,
                        aimedBlockTexture,
                        aimedBlockTexture
                ));

        skybox = new Skybox();

        environment = new Environment();
        if (!EventManager.getInstance()
                .fireEvent(CustomizeEnvironmentEvent.create(environment))) {
            environment = null;
        }

        camera = new PerspectiveCamera(
                fov, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        // camera.position.set(player.spawnLocation.x,
        // player.spawnLocation.y, player.spawnLocation.z);
        camera.position.set(-1.0f, -1.0f, -1.0f);
        camera.near = 0.35f;
        camera.far = 1000000000.0f;
        camera.update();

        level.initGraphics();
        if (TextureManager.getInstance().skyboxPresented) {
            float size = Math.max(Math.max(level.sizeX, level.sizeY), level.sizeZ);
            skybox.setSize(size);
            skybox.initGraphics();
        }
        cameraController = new McLordFirstPersonCameraController(camera);

        Gdx.input.setInputProcessor(cameraController);
        Gdx.input.setCursorCatched(true);
    }

    @Override
    public void render(float delta) {
        cameraController.update(delta);

        Helper.clearDepthRGB(17, 137, 217);

        modelBatch.begin(camera);
        if (skybox.isReady()) {
            skybox.render(modelBatch, camera);
        }

        corePlugin.levelRenderStart();
        level.render(modelBatch, environment);
        corePlugin.levelRenderFinish();

        Vector3 aimedBlock = level.findAimedBlock(camera.position, camera.direction);
        if (aimedBlock != null) {
            aimedBlockModelInstance.transform.set((new Matrix4()).translate(aimedBlock));
            modelBatch.render(aimedBlockModelInstance);
        }
        modelBatch.end();

        spriteBatch.begin();
        int size = crosshairTexture.getWidth();
        spriteBatch.draw(crosshairTexture, Gdx.graphics.getWidth() / 2.0f -
                size / 2.0f, Gdx.graphics.getHeight() / 2.0f - size / 2.0f);
        spriteBatch.end();
    }

    @Override
    public void resize(int width, int height) {
        Helper.dispose(crosshairTexture);
        crosshairTexture = Helper.generateCrosshairTexture(width, height);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {
        dispose();
    }

    @Override
    public void dispose() {
        Helper.dispose(font); font = null;
        Helper.dispose(spriteBatch); spriteBatch = null;
        Helper.dispose(crosshairTexture); crosshairTexture = null;
        if (aimedBlockModelInstance != null) {
            Helper.dispose(aimedBlockModelInstance.model); aimedBlockModelInstance = null;
        }
        Helper.dispose(modelBatch); modelBatch = null;
        Helper.dispose(level); level = null;
        Helper.dispose(skybox); skybox = null;
        cameraController = null;
        environment = null;
        camera = null;
    }
}
