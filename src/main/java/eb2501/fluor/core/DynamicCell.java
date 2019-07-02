package eb2501.fluor.core;

import java.util.function.Supplier;

final class DynamicCell<T> extends Cache<T> implements Property<T> {

    DynamicCell(final Supplier<T> supplier) {
        super(supplier);
    }

    @Override
    public void set(T value) {
        fluor.ensureCanWrite();
        this.value = value;
        cached = true;
        invalidateChildren();
        invalidateParents();
    }
}
