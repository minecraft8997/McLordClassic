package ru.mclord.classic;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.utils.Disposable;

public class Block implements Disposable {
    public interface PermissionChecker {
        boolean doIHaveThisPermission();
    }

    public enum InteractPermission {
        EVERYONE(() -> true),
        OP_ONLY(() -> {
            McLordClassic game = McLordClassic.game();

            return (game.thePlayer != null && game.thePlayer.isOp());
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
    /* package-private */ final boolean liquid;
    /* package-private */ final boolean canWalkThrough;
    /* package-private */ final InteractPermission permissionBuild;
    /* package-private */ final InteractPermission permissionBreak;

    protected Model model;
    protected ModelInstance modelInstance;

    public Block(
            short id,
            String displayName,
            boolean liquid,
            boolean canWalkThrough,
            InteractPermission permissionBuild,
            InteractPermission permissionBreak
    ) {
        this.id = id;
        this.displayName = displayName;
        this.liquid = liquid;
        this.canWalkThrough = canWalkThrough;
        this.permissionBuild = permissionBuild;
        this.permissionBreak = permissionBreak;
    }
    
    public void initGraphics() {
        if (modelInstance != null) return;

        ModelBuilder modelBuilder = new ModelBuilder();
        modelBuilder.begin();
        modelBuilder.node();

        modelBuilder.part(
                Helper.FRONT_SIDE_NAME,
                GL20.GL_TRIANGLES,
                Helper.ATTR,
                new Material(TextureAttribute.createDiffuse(manager.doneFrontSideTexture))
        ).rect(-1.0f, -1.0f, -1.0f,
                -1.0f, 1.0f, -1.0f,
                1.0f, 1.0f, -1.0f,
                1.0f, -1.0f, -1.0f,
                0f, 0f, -1f
        );

        modelBuilder.part(
                Helper.BACK_SIDE_NAME,
                GL20.GL_TRIANGLES,
                Helper.ATTR,
                new Material(TextureAttribute.createDiffuse(manager.doneBackSideTexture))
        ).rect(-1.0f, 1.0f, 1.0f,
                -1.0f, -1.0f, 1.0f,
                1.0f, -1.0f, 1.0f,
                1.0f, 1.0f, 1.0f,
                0f, 0f, 1f
        );

        modelBuilder.part(
                Helper.BOTTOM_SIDE_NAME,
                GL20.GL_TRIANGLES,
                Helper.ATTR,
                new Material(TextureAttribute.createDiffuse(manager.doneBottomSideTexture))
        ).rect(-1.0f, -1.0f, 1.0f,
                -1.0f, -1.0f, -1.0f,
                1.0f, -1.0f, -1.0f,
                1.0f, -1.0f, 1.0f,
                0f, -1f, 0f
        );

        modelBuilder.part(
                Helper.TOP_SIDE_NAME,
                GL20.GL_TRIANGLES,
                Helper.ATTR,
                new Material(TextureAttribute.createDiffuse(manager.doneTopSideTexture))
        ).rect(-1.0f, 1.0f, -1.0f,
                -1.0f, 1.0f, 1.0f,
                1.0f, 1.0f, 1.0f,
                1.0f, 1.0f, -1.0f,
                0f, 1f, 0f
        );

        modelBuilder.part(
                Helper.LEFT_SIDE_NAME,
                GL20.GL_TRIANGLES,
                Helper.ATTR,
                new Material(TextureAttribute.createDiffuse(manager.doneLeftSideTexture))
        ).rect(-1.0f, -1.0f, 1.0f,
                -1.0f, 1.0f, 1.0f,
                -1.0f, 1.0f, -1.0f,
                -1.0f, -1.0f, -1.0f,
                -1f, 0f, 0f
        );

        modelBuilder.part(
                Helper.RIGHT_SIDE_NAME,
                GL20.GL_TRIANGLES,
                Helper.ATTR,
                new Material(TextureAttribute.createDiffuse(manager.doneRightSideTexture))
        ).rect(1.0f, -1.0f, -1.0f,
                1.0f, 1.0f, -1.0f,
                1.0f, 1.0f, 1.0f,
                1.0f, -1.0f, 1.0f,
                1f, 0f, 0f
        );

        model = modelBuilder.end();
        modelInstance = new ModelInstance(model);
    }

    public final ModelInstance getModelInstance() {
        return modelInstance;
    }

    public final short getId() {
        return id;
    }

    public final String getDisplayName() {
        return displayName;
    }

    public final boolean isLiquid() {
        return liquid;
    }

    public final boolean canWalkThrough() {
        return canWalkThrough;
    }

    public final InteractPermission getPermissionBuild() {
        return permissionBuild;
    }

    public final InteractPermission getPermissionBreak() {
        return permissionBreak;
    }

    public boolean shouldBeRenderedAt(int x, int y, int z) {
        return true;
    }

    public void onRightClick() {
    }

    @Override
    public void dispose() {
        Helper.dispose(model); model = null;
        modelInstance = null;
    }
}
