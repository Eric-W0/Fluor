package eb2501.fluor.core;

class LoopNode extends Node {
    final Runnable runnable;
    boolean active;

    LoopNode(final Page owner, final Runnable runnable) {
        super(owner, false, true);
        this.runnable = runnable;
    }

    void stop() {
        invalidateChildren();
        active = false;
    }

    @Override
    protected void invalidate() {
        if (!active) {
            throw new DependantLoopException(this);
        }
        invalidateChildren();
        owner.context.registerLoop(this);
    }
}
