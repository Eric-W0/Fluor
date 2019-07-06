package eb2501.fluor.core;

import java.util.function.Function;

class FamilyNode<K, V> extends NodeMap<K, V> implements Family<K, V> {

    FamilyNode(final Page owner, final Function<K, V> function) {
        super(owner, function);
    }

    @Override
    public final V get(final K key) {
        return getCell(key).get();
    }

    @Override
    public final void set(K key, V value) {
        getCell(key).set(value);
    }
}
