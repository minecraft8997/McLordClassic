package ru.mclord.classic;

import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelCache;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.utils.Disposable;

public class Level implements Disposable {
    private final short[][][] blocks;
    /* package-private */ final int sizeX;
    /* package-private */ final int sizeY;
    /* package-private */ final int sizeZ;

    protected ModelCache modelCache;

    public Level(int sizeX, int sizeY, int sizeZ) {
        this.blocks = new short[sizeX][sizeY][sizeZ];
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
    }

    public void initGraphics() {
        if (modelCache != null) return;

        modelCache = new ModelCache();
        modelCache.begin();
        for (int x = 0; x < blocks.length; x++) {
            for (int y = 0; y < blocks[x].length; y++) {
                for (int z = 0; z < blocks[x][y].length; z++) {
                    Block block = getBlockDefAt(x, y, z);
                    if (!block.shouldBeRenderedAt(x, y, z)) continue;


                }
            }
        }
    }

    private ModelInstance createModelInstance(Block block) {

    }

    public void render(ModelBatch modelBatch, Environment environment) {
        modelBatch.render(modelCache, environment);
    }

    public short getBlockIdAt(int x, int y, int z) {
        return blocks[x][y][z];
    }

    public Block getBlockDefAt(int x, int y, int z) {
        return BlockManager.getInstance().getBlock(getBlockIdAt(x, y, z));
    }

    public int getSizeX() {
        return sizeX;
    }

    public int getSizeY() {
        return sizeY;
    }

    public int getSizeZ() {
        return sizeZ;
    }

    @Override
    public void dispose() {

    }
}
