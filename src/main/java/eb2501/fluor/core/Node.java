package eb2501.fluor.core;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

abstract class Node {
    protected final Page owner;
    final WeakReference<Node> reference;
    final Set<WeakReference<Node>> parents;
    final List<Node> children;
    int purgeCount;

    protected Node(final Page owner, final boolean hasParents, final boolean hasChildren) {
        this.owner = owner;
        if (hasParents) {
            parents = new HashSet<>();
        } else {
            parents = null;
        }
        if (hasChildren) {
            reference = new WeakReference<>(this);
            children = new ArrayList<>();
        } else {
            reference = null;
            children = null;
        }
    }

    protected final void invalidateChildren() {
        assert children != null;
        for (final var child : children) {
            assert child.parents != null;
            child.parents.remove(this.reference);
        }
        children.clear();
    }

    protected final void invalidateParents() {
        final var copy = new ArrayList<>(parents);
        parents.clear();
        purgeCount = 0;
        owner.context.transaction(
                () -> {
                    for (final var reference : copy) {
                        final var parent = reference.get();
                        if (parent != null) {
                            parent.invalidate();
                        }
                    }
                },
                false
        );
    }

    protected abstract void invalidate();

    final void purge() {
        if (++purgeCount >= Context.defaultProfile.autoPurgeLimit) {
            assert parents != null;
            parents.removeIf(r -> r.get() == null);
            purgeCount = 0;
        }
    }
}
