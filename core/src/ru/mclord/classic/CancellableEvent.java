package ru.mclord.classic;

/*
 * A cancellable event does not have to (thought it is
 * recommended to) extend this class.
 *
 * <code>
 *     public class MyEvent extends Event {
 *         public MyEvent() {
 *             super(true);
 *         }
 *     }
 * </code>
 *
 * In this case MyEvent is considered cancellable in
 * spite of NOT extending this (CancellableEvent) class.
 * Though
 *
 * <code>
 *     public class MyEvent extends CancellableEvent {
 *     }
 * </code>
 *
 * should look nicer.
 */
public abstract class CancellableEvent extends Event {
    public CancellableEvent() {
        super(true);
    }
}
