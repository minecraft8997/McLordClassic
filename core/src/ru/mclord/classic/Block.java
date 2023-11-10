package ru.mclord.classic;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.utils.Disposable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class Block implements Disposable {
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

    private static final BlendingAttribute ALPHA =
            new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

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

    protected Model model;

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
    
    public void initGraphics() {
        if (model != null) return;

        TextureManager manager = TextureManager.getInstance();
        ModelBuilder modelBuilder = new ModelBuilder();
        modelBuilder.begin();
        modelBuilder.node();

        modelBuilder.part(
                Helper.FRONT_SIDE_NAME,
                GL20.GL_TRIANGLES,
                Helper.ATTR,
                new Material(TextureAttribute.createDiffuse(manager
                        .rotate90Texture(manager.getTexture(frontTextureId), false)))
        ).rect(-0.5f, -0.5f, -0.5f,
                -0.5f, 0.5f, -0.5f,
                0.5f, 0.5f, -0.5f,
                0.5f, -0.5f, -0.5f,
                0f, 0f, -0.5f
        );

        modelBuilder.part(
                Helper.BACK_SIDE_NAME,
                GL20.GL_TRIANGLES,
                Helper.ATTR,
                new Material(TextureAttribute.createDiffuse(manager
                        .rotate90Texture(manager.getTexture(backTextureId), true)))
        ).rect(-0.5f, 0.5f, 0.5f,
                -0.5f, -0.5f, 0.5f,
                0.5f, -0.5f, 0.5f,
                0.5f, 0.5f, 0.5f,
                0f, 0f, 0.5f
        );

        modelBuilder.part(
                Helper.BOTTOM_SIDE_NAME,
                GL20.GL_TRIANGLES,
                Helper.ATTR,
                new Material(TextureAttribute
                        .createDiffuse(manager.getTexture(bottomTextureId)))
        ).rect(-0.5f, -0.5f, 0.5f,
                -0.5f, -0.5f, -0.5f,
                0.5f, -0.5f, -0.5f,
                0.5f, -0.5f, 0.5f,
                0f, -0.5f, 0f
        );

        modelBuilder.part(
                Helper.TOP_SIDE_NAME,
                GL20.GL_TRIANGLES,
                Helper.ATTR,
                new Material(TextureAttribute
                        .createDiffuse(manager.getTexture(topTextureId)))
        ).rect(-0.5f, 0.5f, -0.5f,
                -0.5f, 0.5f, 0.5f,
                0.5f, 0.5f, 0.5f,
                0.5f, 0.5f, -0.5f,
                0f, 0.5f, 0f
        );

        modelBuilder.part(
                Helper.LEFT_SIDE_NAME,
                GL20.GL_TRIANGLES,
                Helper.ATTR,
                new Material(TextureAttribute.createDiffuse(manager
                        .rotate90Texture(manager.getTexture(leftTextureId), false)))
        ).rect(-0.5f, -0.5f, 0.5f,
                -0.5f, 0.5f, 0.5f,
                -0.5f, 0.5f, -0.5f,
                -0.5f, -0.5f, -0.5f,
                -0.5f, 0f, 0f
        );

        modelBuilder.part(
                Helper.RIGHT_SIDE_NAME,
                GL20.GL_TRIANGLES,
                Helper.ATTR,
                new Material(TextureAttribute.createDiffuse(manager
                        .rotate90Texture(manager.getTexture(rightTextureId), false)))
        ).rect(0.5f, -0.5f, -0.5f,
                0.5f, 0.5f, -0.5f,
                0.5f, 0.5f, 0.5f,
                0.5f, -0.5f, 0.5f,
                0.5f, 0f, 0f
        );
        model = modelBuilder.end();

        for (Material material : model.materials) material.set(ALPHA);
    }

    public final Model getModel() {
        return model;
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
            if (neighbor.solidity != Solidity.SOLID || neighbor.slab) return true;
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
