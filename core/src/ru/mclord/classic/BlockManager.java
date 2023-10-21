package ru.mclord.classic;

import java.util.HashMap;
import java.util.Map;

public class BlockManager implements Manager {
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

    @Override
    public boolean checkStage() {
        McLordClassic.GameStage stage = McLordClassic.game().stage;

        return stage == McLordClassic.GameStage.PRE_INITIALIZATION ||
                stage == McLordClassic.GameStage.ENABLING_PROTOCOL_EXTENSIONS ||
                stage == McLordClassic.GameStage.INITIALIZATION;
    }
}
