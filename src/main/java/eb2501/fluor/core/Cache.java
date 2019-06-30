package eb2501.fluor.core;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.function.Supplier;

public class Cache<T> extends Node implements Supplier<T> {
    private final Supplier<T> supplier;
    T value;
    boolean set;

    public Cache(final Supplier<T> supplier) {
        reference = new WeakReference<>(this);
        parents = new HashSet<>();
        children = new ArrayList<>();
        this.supplier = supplier;
    }

    public final boolean isCached() {
        return set;
    }

    @Override
    public final T get() {
        if (!set) {
            value = context.registerCaller(this, () -> context.readonly(supplier));
            set = true;
        }
        context.registerCallee(this);
        return value;
    }

    @Override
    public final void invalidate() {
        value = null;
        set = false;
        for (final var child : children) {
            child.parents.remove(this);
        }
        children.clear();
        context.transaction(() -> {
            for (final var reference : parents) {
                final var node = reference.get();
                if (node != null) {
                    node.invalidate();
                }
            }
            parents.clear();
        });
    }
}
