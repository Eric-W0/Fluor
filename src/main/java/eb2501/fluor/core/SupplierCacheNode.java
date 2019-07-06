package eb2501.fluor.core;

import java.util.function.Supplier;

class SupplierCacheNode<T> extends Node implements Supplier<T> {
    private final Supplier<T> supplier;
    T value;
    boolean cached;

    SupplierCacheNode(final Page owner, final Supplier<T> supplier) {
        super(owner, true, true);
        this.supplier = supplier;
    }

    @Override
    public T get() {
        if (!cached) {
            value = owner.context.registerCaller(this, () -> owner.context.readonly(supplier::get));
            cached = true;
        }
        owner.context.registerCallee(this);
        return value;
    }

    @Override
    protected void invalidate() {
        value = null;
        cached = false;
        invalidateChildren();
        invalidateParents();
    }
}
