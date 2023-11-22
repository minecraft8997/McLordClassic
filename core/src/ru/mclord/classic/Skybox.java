package ru.mclord.classic;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.IntAttribute;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Disposable;

public class Skybox implements Disposable {
    private float size;
    private ModelInstance modelInstance;

    public Skybox() {
    }

    public void setSize(float size) {
        this.size = size;
    }

    public void initGraphics() {
        if (isReady()) return;

        if (size == 0.0f) throw new IllegalStateException("Size not set");

        TextureManager manager = TextureManager.getInstance();

        Model model = Helper.constructBlock(size,
                manager.getSkyboxTexture(3),
                manager.getSkyboxTexture(5),
                manager.getSkyboxTexture(1),
                manager.getSkyboxTexture(0),
                manager.getSkyboxTexture(4),
                manager.getSkyboxTexture(2)
        );
        IntAttribute disableCullFace = IntAttribute.createCullFace(0);
        for (Material material : model.materials) {
            material.set(disableCullFace);
        }

        modelInstance = new ModelInstance(model);
    }

    public boolean isReady() {
        return modelInstance != null;
    }

    public void render(ModelBatch batch, Camera camera) {
        modelInstance.transform.set((new Matrix4()).translate(camera.position));

        batch.render(modelInstance);
    }

    @Override
    public void dispose() {
        Helper.dispose(modelInstance.model);
        modelInstance = null;
    }
}
