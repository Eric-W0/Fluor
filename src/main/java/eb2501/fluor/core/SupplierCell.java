package eb2501.fluor.core;

import java.util.function.Supplier;

public final class SupplierCell<T> extends Cache<T> implements Cell<T> {

    public SupplierCell(final Supplier<T> supplier) {
        super(supplier);
    }

    @Override
    public void set(T value) {
        context.ensureCanWrite();
        this.value = value;
        set = true;
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
