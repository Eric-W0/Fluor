package eb2501.fluor.core;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.function.Supplier;

class Cache<T> extends Node implements Supplier<T> {
    private final Supplier<T> supplier;
    T value;
    boolean cached;

    Cache(final Supplier<T> supplier) {
        reference = new WeakReference<>(this);
        parents = new HashSet<>();
        children = new ArrayList<>();
        this.supplier = supplier;
    }

    @Override
    public final T get() {
        if (!cached) {
            value = fluor.registerCaller(this, () -> fluor.readonly(supplier));
            cached = true;
        }
        fluor.registerCallee(this);
        return value;
    }

    @Override
    protected final void invalidate() {
        value = null;
        cached = false;
        invalidateChildren();
        invalidateParents();
    }
}
