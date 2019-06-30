package eb2501.fluor.core;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Set;

abstract class Node {
    final Context context;
    WeakReference<Node> reference;
    Set<WeakReference<Node>> parents;
    List<Node> children;

    protected Node() {
        context = Context.getInstance();
    }

    public abstract void invalidate();
}
