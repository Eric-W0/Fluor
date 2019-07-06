package eb2501.fluor.core;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class Chain<T> {

    public static <T> Chain<T> create(final T object) {
        Objects.requireNonNull(object);
        return new Chain<>(object);
    }

    public static <T> T let(final T object, final Consumer<T> consumer) {
        return create(object)
                .accept(consumer)
                .done();
    }

    private T object;

    private Chain(final T object) {
        this.object = object;
    }

    public T done() {
        final var result = object;
        object = null;
        return result;
    }

    private void checkObject() {
        if (object == null) {
            throw new IllegalStateException("Chain is no longer bound to an object");
        }
    }

    public Chain<T> accept(final Consumer<T> consumer) {
        checkObject();
        consumer.accept(object);
        return this;
    }

    public <U> Chain<T> accept(final BiConsumer<T, U> consumer, final U value) {
        checkObject();
        consumer.accept(object, value);
        return this;
    }

    public <U> Chain<T> set(final Function<T, Cell<U>> function, final U value) {
        checkObject();
        function.apply(object).set(value);
        return this;
    }
}
