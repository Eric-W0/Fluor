package eb2501.fluor.core;

import java.util.function.Supplier;

final class BindWrapper<T> implements Cell<T> {
    private final Supplier<Property<T>> supplier;
    private Property<T> property;

    BindWrapper(final Supplier<Property<T>> suopplier) {
        this.supplier = suopplier;
    }

    private void initialize() {
        if (property == null) {
            property = supplier.get();
        }
    }

    @Override
    public T get() {
        initialize();
        return property.get();
    }

    @Override
    public void set(final T value) {
        initialize();
        property.set(value);
    }

    @Override
    public void bind(final Property<T> property) {
        if (this.property != null) {
            throw new IllegalStateException("Cell is already bound");
        }
        this.property = property;
    }
}
