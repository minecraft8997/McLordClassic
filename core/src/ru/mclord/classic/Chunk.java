package ru.mclord.classic;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelCache;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Matrix4;

public class Chunk implements McLordRenderable {
    /*
     * Chunk size on Y axis is unlimited.
     * This might be inefficient on some levels though.
     */
    public static final int CHUNK_SIZE = 16;
    private static final float RENDER_DISTANCE_SQUARED;

    private ModelCache modelCache;
    /* package-private */ final Level level;
    /* package-private */ final int chunkX;
    /* package-private */ final int chunkZ;

    static {
        RENDER_DISTANCE_SQUARED = (float) Math.pow(
                Float.parseFloat(McLordClassic.getProperty("renderDistance")), 2.0D);
    }

    public Chunk(Level level, int chunkX, int chunkZ) {
        this.level = level;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
    }

    public static int getX(Chunk chunk) {
        return chunk.chunkX * CHUNK_SIZE;
    }

    public static int getChunkX(int x) {
        return x / CHUNK_SIZE;
    }

    public static int getZ(Chunk chunk) {
        return chunk.chunkZ * CHUNK_SIZE;
    }

    public static int getChunkZ(int z) {
        return z / CHUNK_SIZE;
    }

    @Override
    public void initGraphics() {
        if (modelCache != null) return;

        int realX = getX(this);
        int realZ = getZ(this);
        modelCache = new ModelCache(new ModelCache.Sorter(), new ModelCache.TightMeshPool());
        modelCache.begin();
        for (int x = realX; x < realX + CHUNK_SIZE; x++) {
            for (int y = 0; y < level.sizeY; y++) {
                for (int z = realZ; z < realZ + CHUNK_SIZE; z++) {
                    Block block = level.getBlockDefAt(x, y, z);
                    if (!block.shouldBeRenderedAt(x, y, z)) continue;

                    block.initGraphics();
                    ModelInstance modelInstance = new ModelInstance(block.getModel(),
                            new Matrix4().translate(x, y, z), (String[]) null);
                    modelCache.add(modelInstance);
                }
            }
        }
        modelCache.end();
    }

    @ShouldBeCalledBy(thread = "main")
    public void render(ModelBatch modelBatch, Environment environment) {
        if (modelCache == null) return;

        Camera camera = modelBatch.getCamera();
        float distanceSquared =
                Helper.distanceSquared(this, camera.position.x, camera.position.z);
        if (distanceSquared > RENDER_DISTANCE_SQUARED) return;

        modelBatch.render(modelCache, environment);
    }

    public Level getLevel() {
        return level;
    }

    public int getChunkX() {
        return chunkX;
    }

    public int getChunkZ() {
        return chunkZ;
    }

    @Override
    public void dispose() {
        Helper.dispose(modelCache); modelCache = null;
    }
}
