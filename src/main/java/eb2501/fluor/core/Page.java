package eb2501.fluor.core;

import eb2501.fluor.core.utils.Rug;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class Page implements Switch {
    final Context context;
    private LoopNode[] loops;

    protected Page() {
        context = Context.storage.get();
    }

    protected final <T> Cell<T> cell() {
        return new ValueCellNode<>(this, null);
    }

    protected final <T> Cell<T> cell(final T value) {
        return new ValueCellNode<>(this, value);
    }

    protected final <T> Cell<T> cell(final Supplier<T> supplier) {
        return new SupplierCellNode<>(this, supplier);
    }

    protected final <K, V> Family<K, V> family(final Function<K, V> function) {
        return new FamilyNode<>(this, function);
    }

    protected final <T> Supplier<T> cached(final Supplier<T> supplier) {
        return new SupplierCacheNode<>(this, supplier);
    }

    protected final <K, V> Function<K, V> cached(final Function<K, V> function) {
        return new FunctionCacheNode<>(this, function);
    }

    @Override
    public boolean isActive() { return loops != null; }

    private void collectLoops(final Class<?> clazz, final List<LoopNode> accu) {
        final Class<?> parent = clazz.getSuperclass();
        if (parent != Object.class) {
            collectLoops(parent, accu);
        }
        for (final Method method : clazz.getDeclaredMethods()) {
            if (method.getAnnotation(Loop.class) == null) {
                continue;
            }
            if (method.getParameterCount() != 0) {
                throw new IllegalArgumentException(
                        String.format(
                                "Method '%s' set as @LoopNode cannot take parameters",
                                method
                        )
                );
            }
            final var loop = new LoopNode(this, Rug.under(() -> { method.invoke(this); }));
            accu.add(loop);
        }
    }

    private static final LoopNode[] EMPTY_LOOP_ARRAY = new LoopNode[0];

    @Override
    public void start() {
        if (loops != null) {
            throw new IllegalStateException("Page is already active");
        }
        final var accu = new ArrayList<LoopNode>();
        collectLoops(getClass(), accu);
        context.transaction(() -> accu.forEach(context::registerLoop), true);
        loops = accu.toArray(EMPTY_LOOP_ARRAY);
    }

    @Override
    public void stop() {
        if (loops == null) {
            throw new IllegalStateException("Page has already been deactivated");
        }
        for (int i = loops.length - 1; i >= 0; i--) {
            loops[i].stop();
        }
        loops = null;
    }
}
