package ru.mclord.classic;

import java.util.*;

public class BlockManager implements Manager {
    private static final BlockManager INSTANCE = new BlockManager();

    private final Map<Short, Block> REGISTERED_BLOCKS = new HashMap<>();

    private BlockManager() {
    }

    public static BlockManager getInstance() {
        return INSTANCE;
    }

    @ShouldBeCalledBy(thread = "main")
    public Block getBlock(short id) {
        return REGISTERED_BLOCKS.get(id);
    }

    @ShouldBeCalledBy(thread = "main")
    public Set<Block> enumerateBlocks() {
        return new HashSet<>(enumerateBlocksFast());
    }

    @ShouldBeCalledBy(thread = "main")
    /* package-private */ Collection<Block> enumerateBlocksFast() {
        return REGISTERED_BLOCKS.values();
    }

    @ShouldBeCalledBy(thread = "main")
    public void registerBlock(Block block) {
        if (!checkStage()) {
            throw new IllegalStateException(
                    "Cannot register blocks during current game stage");
        }
        if (REGISTERED_BLOCKS.containsKey(block.id)) {
            throw new IllegalArgumentException(
                    "Specified block ID (" + block.id + ") is already registered");
        }

        REGISTERED_BLOCKS.put(block.id, block);
    }

    @ShouldBeCalledBy(thread = "main")
    public void unregisterBlock(Block block) {
        unregisterBlock(block.id);
    }

    @ShouldBeCalledBy(thread = "main")
    public void unregisterBlock(short id) {
        if (!checkStage()) {
            throw new IllegalStateException(
                    "Cannot unregister blocks during current game stage");
        }
        Block removed = REGISTERED_BLOCKS.remove(id);

        if (removed != null) removed.dispose();
    }

    @Override
    public boolean checkStage() {
        return true;
    }
}
