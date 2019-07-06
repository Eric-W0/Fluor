package eb2501.fluor.core;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

class NodeMap<K, V> {

    protected class KeyNode extends SupplierCellNode<V> {
        private final K key;

        KeyNode(final K key) {
            super(NodeMap.this.owner, () -> function.apply(key));
            this.key = key;
        }

        @Override
        protected void invalidate() {
            super.invalidate();
            mapping.remove(key);
        }
    }

    private Page owner;
    private Function<K, V> function;
    private Map<K, KeyNode> mapping;

    NodeMap(final Page owner, final Function<K, V> function) {
        this.owner = owner;
        this.function = function;
        mapping = new HashMap<>();
    }

    protected Cell<V> getCell(final K key) {
        return mapping.computeIfAbsent(key, KeyNode::new);
    }
}
