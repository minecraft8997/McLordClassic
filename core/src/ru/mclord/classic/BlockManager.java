package ru.mclord.classic;

import java.util.HashMap;
import java.util.Map;

public class BlockManager {
    private static final BlockManager INSTANCE = new BlockManager();

    private final Map<Short, Block> REGISTERED_BLOCKS = new HashMap<>();

    private BlockManager() {
    }

    public static BlockManager getInstance() {
        return INSTANCE;
    }

    public Block getBlock(short id) {
        return REGISTERED_BLOCKS.get(id);
    }

    public void registerBlock(Block block) {
        McLordClassic.GameStage stage = McLordClassic.game().stage;
        if (stage != McLordClassic.GameStage.PRE_INITIALIZATION &&
                stage != McLordClassic.GameStage.INITIALIZATION) {
            throw new IllegalStateException(
                    "Cannot register blocks during current game stage");
        }
        if (REGISTERED_BLOCKS.containsKey(block.id)) {
            throw new IllegalArgumentException(
                    "Specified block ID (" + block.id + ") is already registered");
        }

        REGISTERED_BLOCKS.put(block.id, block);
    }
}
