package eb2501.fluor.core;

public class Loop extends Node {
    final Runnable runnable;
    boolean ready;

    public Loop(final Runnable runnable) {
        this.runnable = runnable;
        ready = true;
        context.registerCaller(this, runnable);
    }

    public final boolean isReady() {
        return ready;
    }

    @Override
    public final void invalidate() {
        if (!ready) {
            throw new LoopNotReadyException(this);
        }
        for (final var child : children) {
            child.parents.remove(this);
        }
        children.clear();
        context.registerLoop(this);
    }
}
