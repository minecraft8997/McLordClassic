package ru.mclord.classic;

public class Block {
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

    public void onRightClick() {
    }
}
