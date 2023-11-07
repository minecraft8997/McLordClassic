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
            boolean liquid,
            boolean canWalkThrough,
            InteractPermission permissionBuild,
            InteractPermission permissionBreak,
            int topTextureId,
            int sideTextureId,
            int bottomTextureId
    ) {
        this(
                id,
                displayName,
                liquid,
                canWalkThrough,
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
            boolean liquid,
            boolean canWalkThrough,
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
        this.liquid = liquid;
        this.canWalkThrough = canWalkThrough;
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
                new Material(TextureAttribute
                        .createDiffuse(manager.getTexture(frontTextureId)))
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
                new Material(TextureAttribute
                        .createDiffuse(manager.getTexture(backTextureId)))
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
                new Material(TextureAttribute
                        .createDiffuse(manager.getTexture(bottomTextureId)))
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
                new Material(TextureAttribute
                        .createDiffuse(manager.getTexture(topTextureId)))
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
                new Material(TextureAttribute
                        .createDiffuse(manager.getTexture(leftTextureId)))
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
                new Material(TextureAttribute
                        .createDiffuse(manager.getTexture(rightTextureId)))
        ).rect(1.0f, -1.0f, -1.0f,
                1.0f, 1.0f, -1.0f,
                1.0f, 1.0f, 1.0f,
                1.0f, -1.0f, 1.0f,
                1f, 0f, 0f
        );

        model = modelBuilder.end();
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
    }
}
