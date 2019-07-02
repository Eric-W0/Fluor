package eb2501.fluor.core;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

abstract class Node {
    protected final Fluor fluor;
    WeakReference<Node> reference;
    Set<WeakReference<Node>> parents;
    List<Node> children;
    int purgeCount;

    protected Node() {
        fluor = Fluor.getInstance();
    }

    protected final void invalidateChildren() {
        for (final var child : children) {
            child.parents.remove(this.reference);
        }
        children.clear();
    }

    protected final void invalidateParents() {
        final var copy = new ArrayList<>(parents);
        parents.clear();
        purgeCount = 0;
        fluor.transaction(() -> {
            for (final var reference : copy) {
                final var parent = reference.get();
                if (parent != null) {
                    parent.invalidate();
                }
            }
        });
    }

    protected abstract void invalidate();

    final void purge() {
        if (++purgeCount >= Fluor.AUTO_PURGE_LIMIT) {
            parents.removeIf(r -> r.get() == null);
            purgeCount = 0;
        }
    }
}
