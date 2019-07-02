package eb2501.fluor.core;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class Page {
    private final List<Loop> loops;
    private boolean activated;

    protected Page() {
        loops = new ArrayList<>();
    }

    protected final <T> Cell<T> cell() {
        return new BindWrapper<>(() -> new StaticCell<>(null));
    }

    protected final <T> Cell<T> cell(final T value) {
        return new BindWrapper<>(() -> new StaticCell<>(value));
    }

    protected final <T> Cell<T> cell(final Supplier<T> supplier) {
        return new BindWrapper<>(() -> new DynamicCell<>(supplier));
    }

    protected final <T> Cache<T> cache(final Supplier<T> supplier) {
        return new Cache<>(supplier);
    }

    protected final Loop loop(final Runnable runnable) {
        final var result = new Loop(runnable);
        loops.add(result);
        if (activated) {
            result.activate();
        }
        return result;
    }

    protected final boolean isActivated() { return activated; }

    protected final void activate() {
        if (activated) {
            throw new IllegalStateException("Page has already activated");
        }
        loops.forEach(l -> {
            if (!l.isActivated()) {
                l.activate();
            }
        });
        activated = true;
    }

    protected final void deactivate() {
        if (!activated) {
            throw new IllegalStateException("Page has already been deactivated");
        }
        for (int i = loops.size() - 1; i >= 0; i--) {
            final var loop = loops.get(i);
            if (loop.isActivated()) {
                loop.deactivate();
            }
        }
        activated = false;
    }

    protected final void deactivate(final Loop loop) {
        if (activated) {
            loop.deactivate();
        }
        loops.remove(loop);
    }
}
