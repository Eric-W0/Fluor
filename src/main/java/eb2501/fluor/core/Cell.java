package eb2501.fluor.core;

import java.util.function.Consumer;
import java.util.function.Supplier;

public interface Cell<T> extends Supplier<T>, Consumer<T> {
    void set(T value);

    @Override
    default void accept(T value) {
        set(value);
    }
}
