package eb2501.fluor.core;

public interface Cell<T> extends Property<T> {
    void bind(final Property<T> property);
}
