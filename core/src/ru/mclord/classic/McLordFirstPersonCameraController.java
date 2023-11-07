package ru.mclord.classic;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.utils.FirstPersonCameraController;

public class McLordFirstPersonCameraController extends FirstPersonCameraController {
    public McLordFirstPersonCameraController(Camera camera) {
        super(camera);

        setDegreesPerPixel(0.08f);
        setVelocity(15.0f);
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        touchDragged(screenX, screenY, 0);

        return super.mouseMoved(screenX, screenY);
    }
}
