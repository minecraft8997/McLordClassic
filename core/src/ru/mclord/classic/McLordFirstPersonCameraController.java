package ru.mclord.classic;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.utils.FirstPersonCameraController;
import com.badlogic.gdx.math.Vector3;

public class McLordFirstPersonCameraController extends FirstPersonCameraController {
    private final Vector3 tmp = new Vector3();
    private final Vector3 tmp2 = new Vector3();
    private final Vector3 tmp3 = new Vector3();
    private final boolean xRay;
    private final Block[] blocks = new Block[8];

    public McLordFirstPersonCameraController(Camera camera) {
        super(camera);

        xRay = Boolean.parseBoolean(McLordClassic.getProperty("xRay"));

        setDegreesPerPixel(0.08f);
        setVelocity(15.0f);
        autoUpdate = false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        touchDragged(screenX, screenY, 0);

        return super.mouseMoved(screenX, screenY);
    }

    @Override
    public void update(float deltaTime) {
        Vector3 initialCameraPosition = new Vector3(camera.position);

        if (Gdx.input.isKeyPressed(Input.Keys.SPACE) && !Gdx.input.isKeyPressed(Input.Keys.Q)) {
            tmp.set(camera.up).nor().scl(deltaTime * velocity);
            camera.position.add(tmp);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) && !Gdx.input.isKeyPressed(Input.Keys.E)) {
            tmp.set(camera.up).nor().scl(-deltaTime * velocity);
            camera.position.add(tmp);
        }
        super.update(deltaTime);

        fixCords(initialCameraPosition);

        camera.update(true);
    }

    private void fixCords(Vector3 initialCameraPosition) {
        if (xRay) return;

        // too bad, fixme at some point
        int x = (int) (camera.position.x);
        int y = (int) (camera.position.y);
        int z = (int) (camera.position.z);
        int x1 = (int) Math.ceil(camera.position.x);
        int y1 = (int) Math.ceil(camera.position.y);
        int z1 = (int) Math.ceil(camera.position.z);

        Level level = McLordClassic.game().level;
        blocks[0] = level.getBlockDefAt(x, y, z);
        blocks[1] = level.getBlockDefAt(x1, y1, z1);
        blocks[2] = level.getBlockDefAt(x1, y, z);
        blocks[3] = level.getBlockDefAt(x1, y1, z);
        blocks[4] = level.getBlockDefAt(x, y1, z);
        blocks[5] = level.getBlockDefAt(x, y1, z1);
        blocks[6] = level.getBlockDefAt(x, y, z1);
        blocks[7] = level.getBlockDefAt(x1, y, z1);

        if (checkInvalid()) {
            camera.position.set(initialCameraPosition);
        }
    }

    private boolean checkInvalid() {
        for (Block block : blocks) {
            if (block.solidity == Block.Solidity.SOLID) return true;
        }

        return false;
    }

    // thanks to https://github.com/libgdx/libgdx/issues/4023#issuecomment-211675619
    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        float deltaX = -Gdx.input.getDeltaX() * degreesPerPixel;
        float deltaY = -Gdx.input.getDeltaY() * degreesPerPixel;
        camera.direction.rotate(camera.up, deltaX);
        Vector3 oldPitchAxis = tmp.set(camera.direction).crs(camera.up).nor();
        Vector3 newDirection = tmp2.set(camera.direction).rotate(tmp, deltaY);
        Vector3 newPitchAxis = tmp3.set(tmp2).crs(camera.up);
        if (!newPitchAxis.hasOppositeDirection(oldPitchAxis))
            camera.direction.set(newDirection);

        return true;
    }
}
