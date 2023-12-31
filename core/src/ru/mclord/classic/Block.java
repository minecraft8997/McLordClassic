package ru.mclord.classic;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.utils.Disposable;

public class Block implements McLordRenderable, Disposable {
    public interface PermissionChecker {
        boolean doIHaveThisPermission();
    }

    public enum Solidity {
        SOLID, WATER, LAVA, WALKTHROUGH
    }

    public enum InteractPermission {
        EVERYONE(() -> true),
        OP_ONLY(() -> {
            Player me = McLordClassic.getPlayer();

            return (me != null && me.isOp());
        }),
        NO_ONE(() -> false);

        private final PermissionChecker checker;

        InteractPermission(PermissionChecker checker) {
            this.checker = checker;
        }

        public boolean doIHaveThisPermission() {
            return checker.doIHaveThisPermission();
        }
    }

    // All of these fields can be directly used by the
    // game while plugins will be required to use getters
    /* package-private */ final short id;
    /* package-private */ final String displayName;
    /* package-private */ final Solidity solidity;
    /* package-private */ final int movementSpeed;
    /* package-private */ final boolean slab;
    /* package-private */ final InteractPermission permissionBuild;
    /* package-private */ final InteractPermission permissionBreak;
    /* package-private */ final int topTextureId;
    /* package-private */ final int leftTextureId;
    /* package-private */ final int rightTextureId;
    /* package-private */ final int frontTextureId;
    /* package-private */ final int backTextureId;
    /* package-private */ final int bottomTextureId;

    private Model model;
    private boolean alphaTestEnabled = true;
    private float alphaTestValue = 0.5f;
    /* package-private */ boolean hasSomeTransparency;

    public Block(
            short id,
            String displayName,
            Solidity solidity,
            int movementSpeed,
            boolean slab,
            InteractPermission permissionBuild,
            InteractPermission permissionBreak,
            int topTextureId,
            int sideTextureId,
            int bottomTextureId
    ) {
        this(
                id,
                displayName,
                solidity,
                movementSpeed,
                slab,
                permissionBuild,
                permissionBreak,
                topTextureId,
                sideTextureId,
                sideTextureId,
                sideTextureId,
                sideTextureId,
                bottomTextureId
        );
    }

    public Block(
            short id,
            String displayName,
            Solidity solidity,
            int movementSpeed,
            boolean slab,
            InteractPermission permissionBuild,
            InteractPermission permissionBreak,
            int topTextureId,
            int leftTextureId,
            int rightTextureId,
            int frontTextureId,
            int backTextureId,
            int bottomTextureId
    ) {
        this.id = id;
        this.displayName = displayName;
        this.solidity = solidity;
        this.movementSpeed = movementSpeed;
        this.slab = slab;
        this.permissionBuild = permissionBuild;
        this.permissionBreak = permissionBreak;
        this.topTextureId = topTextureId;
        this.leftTextureId = leftTextureId;
        this.rightTextureId = rightTextureId;
        this.frontTextureId = frontTextureId;
        this.backTextureId = backTextureId;
        this.bottomTextureId = bottomTextureId;
    }

    public void onBlockRegister() {
    }

    @Override
    public void initGraphics() {
        if (model != null) return;

        TextureManager manager = TextureManager.getInstance();

        model = Helper.constructBlock(0.5f,
                checkTransparency(manager.getTexture(frontTextureId)),
                checkTransparency(manager.getTexture(backTextureId)),
                checkTransparency(manager.getTexture(bottomTextureId)),
                checkTransparency(manager.getTexture(topTextureId)),
                checkTransparency(manager.getTexture(leftTextureId)),
                checkTransparency(manager.getTexture(rightTextureId)),
                (alphaTestEnabled ? FloatAttribute.createAlphaTest(alphaTestValue) : null)
        );
    }

    private Texture checkTransparency(Texture texture) {
        if (hasSomeTransparency) return texture;

        boolean wasNotPrepared = false;
        if (!texture.getTextureData().isPrepared()) {
            texture.getTextureData().prepare();

            wasNotPrepared = true;
        }
        Pixmap pixmap = texture.getTextureData().consumePixmap();
        if (wasNotPrepared) {
            TextureManager.getInstance().addTemporaryPixmap(pixmap);
        }
        for (int i = 0; i < pixmap.getWidth(); i++) {
            for (int j = 0; j < pixmap.getHeight(); j++) {
                byte alpha = (byte) (pixmap.getPixel(i, j) & 0xFF);
                if (alpha != -1) {
                    hasSomeTransparency = true;

                    return texture;
                }
            }
        }

        return texture;
    }

    public final Model getModel() {
        return model;
    }

    protected final void setModel(Model model) {
        this.model = model;
    }

    protected final boolean isAlphaTestEnabled() {
        return alphaTestEnabled;
    }

    protected final void setAlphaTestEnabled(boolean alphaTestEnabled) {
        this.alphaTestEnabled = alphaTestEnabled;
    }

    protected final float getAlphaTestValue() {
        return alphaTestValue;
    }

    protected final void setAlphaTestValue(float alphaTestValue) {
        this.alphaTestValue = alphaTestValue;
    }

    public final short getId() {
        return id;
    }

    public final String getDisplayName() {
        return displayName;
    }

    public final Solidity getSolidity() {
        return solidity;
    }

    public int getMovementSpeed() {
        return movementSpeed;
    }

    public boolean isSlab() {
        return slab;
    }

    public final InteractPermission getPermissionBuild() {
        return permissionBuild;
    }

    public final InteractPermission getPermissionBreak() {
        return permissionBreak;
    }

    public boolean shouldBeRenderedAt(int x, int y, int z) {
        Level level = McLordClassic.game().level;
        Block[] neighbors = new Block[6];
        neighbors[0] = level.getBlockDefAt(x - 1, y, z);
        neighbors[1] = level.getBlockDefAt(x + 1, y, z);
        neighbors[2] = level.getBlockDefAt(x, y, z - 1);
        neighbors[3] = level.getBlockDefAt(x, y, z + 1);
        neighbors[4] = level.getBlockDefAt(x, y + 1, z);
        neighbors[5] = level.getBlockDefAt(x, y - 1, z);
        for (Block neighbor : neighbors) {
            if (neighbor.solidity != Solidity.SOLID ||
                    neighbor.slab || neighbor.hasSomeTransparency) return true;
        }

        return false;
    }

    public void onRightClick() {
    }

    @Override
    public void dispose() {
        Helper.dispose(model); model = null;
    }
}
