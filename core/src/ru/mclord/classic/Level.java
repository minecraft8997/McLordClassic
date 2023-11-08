package ru.mclord.classic;

import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelCache;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Disposable;

public class Level implements Disposable {
    public interface Handler {
        /*
         * Return <code>true</code> if we should continue iterating through
         * locations. <code>false<code> if you wish the loop to be terminated.
         */
        boolean handle(int x, int y, int z);
    }

    private final short[][][] blocks;
    /* package-private */ final int sizeX;
    /* package-private */ final int sizeY;
    /* package-private */ final int sizeZ;

    protected ModelCache modelCache;
    private boolean searchResult;

    public Level(int sizeX, int sizeY, int sizeZ) {
        this.blocks = new short[sizeX][sizeY][sizeZ];
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
    }

    @ShouldBeCalledBy(thread = "main")
    public void initGraphics() {
        if (modelCache != null) return;

        modelCache = new ModelCache();
        modelCache.begin();
        iterateThroughLocations((x, y, z) -> {
            Block block = getBlockDefAt(x, y, z);
            if (!block.shouldBeRenderedAt(x, y, z)) return true;

            block.initGraphics();
            ModelInstance modelInstance = new ModelInstance(block.getModel());
            modelInstance.transform = new Matrix4().translate(x, y, z);
            modelCache.add(modelInstance);

            return true;
        });
        modelCache.end();
    }

    public final void iterateThroughLocations(Handler handler) {
        for (int x = 0; x < blocks.length; x++) {
            for (int y = 0; y < blocks[x].length; y++) {
                for (int z = 0; z < blocks[x][y].length; z++) {
                    if (!handler.handle(x, y, z)) return;
                }
            }
        }
    }

    @ShouldBeCalledBy(thread = "main")
    public final boolean searchForBlockId(short id) {
        searchResult = false;
        iterateThroughLocations((x, y, z) -> {
            searchResult = (getBlockIdAt(x, y, z) == id);

            return !searchResult; // if it's false, continue iterating
        });

        return searchResult;
    }

    public final boolean checkOutOfBounds(int x, int y, int z) {
        return (x < 0 || y < 0 || z < 0 ||
                x >= sizeX || y >= sizeY || z >= sizeZ);
    }

    @ShouldBeCalledBy(thread = "main")
    public void updateGraphics() {
        if (modelCache == null) return;

        dispose();
        initGraphics();
    }

    @ShouldBeCalledBy(thread = "main")
    public void render(ModelBatch modelBatch, Environment environment) {
        modelBatch.render(modelCache, environment);
    }

    @ShouldBeCalledBy(thread = "main")
    public final Block getBlockDefAt(int x, int y, int z) {
        return BlockManager.getInstance().getBlock(getBlockIdAt(x, y, z));
    }

    @ShouldBeCalledBy(thread = "main")
    public short getBlockIdAt(int x, int y, int z) {
        if (checkOutOfBounds(x, y, z)) return 0;

        return blocks[x][y][z];
    }

    @ShouldBeCalledBy(thread = "main")
    public final void setBlockAt(int x, int y, int z, Block block) {
        setBlockAt(x, y, z, block.id);
    }

    @ShouldBeCalledBy(thread = "main")
    public void setBlockAt(int x, int y, int z, short id) {
        if (checkOutOfBounds(x, y, z)) return;

        short oldId = blocks[x][y][z];
        blocks[x][y][z] = id;
        if (oldId != id) updateGraphics();
    }

    public final int getSizeX() {
        return sizeX;
    }

    public final int getSizeY() {
        return sizeY;
    }

    public final int getSizeZ() {
        return sizeZ;
    }

    @Override
    public void dispose() {
        Helper.dispose(modelCache); modelCache = null;
    }
}
