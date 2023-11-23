package ru.mclord.classic;

import com.badlogic.gdx.utils.Disposable;

/*
 * A class implementing this interface is expected to be
 * able to initialize its graphics part multiple times as long as
 * the dispose() method is executed before the next initGraphics() call.
 */
public interface McLordRenderable extends Disposable {
    @ShouldBeCalledBy(thread = "main")
    void initGraphics();
}
