package eb2501.fluor.core;

import java.util.HashSet;
import java.util.Objects;

public final class ValueCell<T> extends Node implements Cell<T> {
    private T value;

    public ValueCell(T value) {
        parents = new HashSet<>();
        this.value = value;
    }

    @Override
    public T get() {
        context.registerCallee(this);
        return value;
    }

    @Override
    public void set(T value) {
        context.ensureCanWrite();
        this.value = value;
        invalidate();
    }

    @Override
    public void invalidate() {
        context.transaction(() -> {
            for (var reference : parents) {
                var node = reference.get();
                if (node != null) {
                    node.invalidate();
                }
            }
            parents.clear();
        });
    }
}
