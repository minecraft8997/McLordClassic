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
    private final Vector3 lastCameraPosition;

    public McLordFirstPersonCameraController(Camera camera) {
        super(camera);

        setDegreesPerPixel(0.08f);
        setVelocity(15.0f);
        lastCameraPosition = new Vector3(camera.position);
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        touchDragged(screenX, screenY, 0);

        return super.mouseMoved(screenX, screenY);
    }

    @Override
    public void update(float deltaTime) {
        if (!(camera.position.x == lastCameraPosition.x && camera.position.y == lastCameraPosition.y && camera.position.z == lastCameraPosition.z)) {
            int x = (int) camera.position.x;
            int y = (int) camera.position.y;
            int z = (int) camera.position.z;

            if (false) {
                //camera is in the block

                camera.position.x = lastCameraPosition.x;
                camera.position.y = lastCameraPosition.y;
                camera.position.z = lastCameraPosition.z;

                return;
            }

            lastCameraPosition.x = camera.position.x;
            lastCameraPosition.y = camera.position.y;
            lastCameraPosition.z = camera.position.z;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.SPACE) && !Gdx.input.isKeyPressed(Input.Keys.Q)) {
            tmp.set(camera.up).nor().scl(deltaTime * velocity);
            camera.position.add(tmp);
        }

        if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) && !Gdx.input.isKeyPressed(Input.Keys.E)) {
            tmp.set(camera.up).nor().scl(-deltaTime * velocity);
            camera.position.add(tmp);
        }

        super.update(deltaTime);
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
