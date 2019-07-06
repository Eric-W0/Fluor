package eb2501.fluor.core;

import java.util.function.Function;

class FunctionCacheNode<K, V> extends NodeMap<K, V> implements Function<K, V> {

    FunctionCacheNode(final Page owner, final Function<K, V> function) {
        super(owner, function);
    }

    @Override
    public final V apply(final K key) {
        return getCell(key).get();
    }
}
