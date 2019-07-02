package eb2501.fluor.core;

import java.util.HashSet;

final class StaticCell<T> extends Node implements Property<T> {
    private T value;

    StaticCell(T value) {
        parents = new HashSet<>();
        this.value = value;
    }

    @Override
    public T get() {
        fluor.registerCallee(this);
        return value;
    }

    @Override
    public void set(T value) {
        fluor.ensureCanWrite();
        this.value = value;
        invalidateParents();
    }

    @Override
    protected void invalidate() {
        invalidateParents();
    }
}
