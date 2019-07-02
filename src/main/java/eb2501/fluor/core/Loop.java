package eb2501.fluor.core;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public final class Loop extends Node {
    final Runnable runnable;
    boolean activated;

    public Loop(final Runnable runnable) {
        reference = new WeakReference<>(this);
        children = new ArrayList<>();
        this.runnable = runnable;
    }

    public final boolean isActivated() {
        return activated;
    }

    public void activate() {
        if (activated) {
            throw new IllegalStateException("Loop is already activated");
        }
        fluor.registerCaller(this, runnable);
        activated = true;
    }

    public void deactivate() {
        if (!activated) {
            throw new IllegalStateException("Loop is not activated");
        }
        invalidateChildren();
        activated = false;
    }

    @Override
    protected void invalidate() {
        if (!activated) {
            throw new LoopNotReadyException(this);
        }
        invalidateChildren();
        fluor.registerLoop(this);
    }
}
