package ru.mclord.classic;

import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.math.Vector3;

import java.util.HashMap;
import java.util.Map;

public class Level implements McLordRenderable {
    public interface Handler {
        /*
         * Return <code>true</code> if we should continue iterating through
         * locations. <code>false<code> if you wish the loop to be terminated.
         */
        boolean handle(int x, int y, int z);
    }

    private final short[][][] blocks;
    private final Map<Pair<Integer, Integer>, Chunk> chunks;
    private final Map<Byte, Player> players;
    /* package-private */ final int sizeX;
    /* package-private */ final int sizeY;
    /* package-private */ final int sizeZ;

    private boolean graphicsInitialized;
    private boolean searchResult;

    public Level(int sizeX, int sizeY, int sizeZ) {
        this.blocks = new short[sizeX][sizeY][sizeZ];
        this.chunks = new HashMap<>();
        this.players = new HashMap<>();
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
    }

    @Override
    public void initGraphics() {
        if (graphicsInitialized) return;

        int endChunkX = (int) Math.ceil((double) sizeX / Chunk.CHUNK_SIZE);
        int endChunkZ = (int) Math.ceil((double) sizeZ / Chunk.CHUNK_SIZE);
        for (int chunkX = 0; chunkX < endChunkX; chunkX++) {
            for (int chunkZ = 0; chunkZ < endChunkZ; chunkZ++) {
                Chunk chunk = new Chunk(this, chunkX, chunkZ);
                chunk.initGraphics();

                chunks.put(Pair.of(chunkX, chunkZ), chunk);
            }
        }

        graphicsInitialized = true;
    }

    /*
     * Thanks to devquickie.
     * https://gitlab.com/devquickie/minecraft-clone
     *
     * src/com/devquickie/minecraftclone/Grid.java#L63
     */
    public Vector3 findAimedBlock(Vector3 startingPoint, Vector3 direction) {
        // int finish = Math.max(Math.max(sizeX, sizeY), sizeZ) * 2;
        for (int i = 1; i <= Player.MAX_CLICK_DISTANCE; i++) {
            Vector3 tmpStartingPoint = new Vector3(startingPoint);
            Vector3 tmpDirection = new Vector3(direction);
            Vector3 line = tmpStartingPoint.add(tmpDirection.nor().scl(i));
            int x = Math.round(line.x);
            int y = Math.round(line.y);
            int z = Math.round(line.z);

            if (x >= sizeX || y >= sizeY || z >= sizeZ || x < 0 || y < 0 || z < 0) {
                break;
            }
            Block block = getBlockDefAt(x, y, z);
            if (block.shouldBeRenderedAt(x, y, z)) {
                return new Vector3(x, y, z);
            }
        }

        return null;
    }

    public Chunk getChunk(int x, int z) {
        return getChunkByChunkCords(Chunk.getChunkX(x), Chunk.getChunkZ(z));
    }

    public Chunk getChunkByChunkCords(int chunkX, int chunkZ) {
        return chunks.get(Pair.of(chunkX, chunkZ));
    }

    @ShouldBeCalledBy(thread = "main")
    public boolean containsPlayer(byte id) {
        return getPlayer(id) != null;
    }

    @ShouldBeCalledBy(thread = "main")
    public void addPlayer(Player player) {
        if (player.id == -1) return;

        players.put(player.id, player);
    }

    @ShouldBeCalledBy(thread = "main")
    public Player getPlayer(byte id) {
        return players.get(id);
    }

    @ShouldBeCalledBy(thread = "main")
    public boolean removePlayer(Player player) {
        return removePlayer(player.id) != null;
    }

    @ShouldBeCalledBy(thread = "main")
    public Player removePlayer(byte id) {
        return players.remove(id);
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
        if (!graphicsInitialized) return;

        dispose();
        initGraphics();
    }

    @ShouldBeCalledBy(thread = "main")
    public void render(ModelBatch modelBatch, Environment environment) {
        if (!graphicsInitialized) return;

        for (Chunk chunk : chunks.values()) {
            chunk.render(modelBatch, environment);
        }
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
        for (Chunk chunk : chunks.values()) {
            chunk.dispose();
        }
        chunks.clear();

        graphicsInitialized = false;
    }
}
