package eb2501.fluor.core;

import eb2501.fluor.core.utils.ThrowingRunnable;
import eb2501.fluor.core.utils.ThrowingSupplier;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

final class Context {
    static final ThreadLocal<Context> storage = ThreadLocal.withInitial(Context::new);

    static final Profile defaultProfile = new Profile();

    private Node caller;
    private boolean readonly;
    private List<LoopNode> loops;

    //
    // Snapshot
    //

    <E extends Exception> void snapshot(final ThrowingRunnable<E> runnable) throws E {
        final var backup = caller;
        caller = null;
        try {
            runnable.run();
        } finally {
            caller = backup;
        }
    }

    <T, E extends Exception> T snapshot(final ThrowingSupplier<T, E> supplier) throws E {
        final var backup = caller;
        caller = null;
        try {
            return supplier.get();
        } finally {
            caller = backup;
        }
    }

    //
    // Readonly
    //

    <E extends Exception> void readonly(final ThrowingRunnable<E> runnable) throws E {
        final var backup = readonly;
        readonly = true;
        try {
            runnable.run();
        } finally {
            readonly = backup;
        }
    }

    <T, E extends Exception> T readonly(final ThrowingSupplier<T, E> supplier) throws E {
        final var backup = readonly;
        readonly = true;
        try {
            return supplier.get();
        } finally {
            readonly = backup;
        }
    }

    boolean isReadonly() {
        return readonly;
    }

    void ensureCanWrite() {
        if (readonly) {
            throw new ReadonlyModeException();
        }
    }

    //
    // Transaction
    //

    private static final LoopNode[] EMPTY_LOOP_ARRAY = new LoopNode[0];

    <E extends Exception> void transaction(final ThrowingRunnable<E> runnable, boolean initialize) throws E {
        if (loops == null) {
            loops = new ArrayList<>();
            try {
                runnable.run();
                final var array = loops.toArray(EMPTY_LOOP_ARRAY);
                loops = null;
                if (!initialize) {
                    for (int i = 0; i < array.length; i++) {
                        array[i].active = false;
                    }
                }
                for (int i = 0; i < array.length; i++) {
                    try {
                        registerCaller(array[i], array[i].runnable);
                    } catch (final DependantLoopException exc) {
                        throw new DependantLoopException(array[i], exc);
                    }
                }
                for (int i = 0; i < array.length; i++) {
                    array[i].active = true;
                }
            } finally {
                loops = null;
            }
        } else {
            runnable.run();
        }
    }

    void registerLoop(final LoopNode loop) {
        loops.add(loop);
    }

    //
    // Caller/Callee
    //

    void registerCaller(final Node caller, final Runnable runnable) {
        final var backup = this.caller;
        try {
            this.caller = caller;
            runnable.run();
        } finally {
            this.caller = backup;
        }
    }

    <T> T registerCaller(final Node caller, final Supplier<T> supplier) {
        final var backup = this.caller;
        try {
            this.caller = caller;
            return supplier.get();
        } finally {
            this.caller = backup;
        }
    }

    void registerCallee(final Node callee) {
        if (caller != null) {
            if (callee.parents.add(caller.reference)) {
                callee.purge();
                caller.children.add(callee);
            }
        }
    }
}
