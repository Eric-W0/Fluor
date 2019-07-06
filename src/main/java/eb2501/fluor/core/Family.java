package eb2501.fluor.core;

import java.util.function.BiConsumer;
import java.util.function.Function;

public interface Family<K, V> extends Function<K, V>, BiConsumer<K, V> {
    V get(final K key);
    void set(final K key, final V value);

    @Override
    default V apply(final K key) { return get(key); }

    @Override
    default void accept(final K key, final V value) {
        set(key, value);
    }
}
