package eb2501.fluor.core;

import eb2501.fluor.core.utils.ThrowingRunnable;
import eb2501.fluor.core.utils.ThrowingSupplier;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

public final class Fluor {
    private static final ThreadLocal<Fluor> storage = ThreadLocal.withInitial(Fluor::new);

    public static Fluor getInstance() {
        return storage.get();
    }

    public static int AUTO_PURGE_LIMIT = 100;

    private Node caller;
    private boolean readonly;
    private Set<Loop> loops;

    //
    // Snapshot
    //

    public <E extends Exception> void snapshot(final ThrowingRunnable<E> runnable) throws E {
        final var backup = caller;
        try {
            caller = null;
            runnable.run();
        } finally {
            caller = backup;
        }
    }

    public <T, E extends Exception> T snapshot(final ThrowingSupplier<T, E> supplier) throws E {
        final var backup = caller;
        try {
            caller = null;
            return supplier.get();
        } finally {
            caller = backup;
        }
    }

    public <T> T snapshot(final Supplier<T> supplier) {
        final var backup = caller;
        try {
            caller = null;
            return supplier.get();
        } finally {
            caller = backup;
        }
    }

    //
    // Readonly
    //

    public <E extends Exception> void readonly(final ThrowingRunnable<E> runnable) throws E {
        final var backup = readonly;
        try {
            readonly = true;
            runnable.run();
        } finally {
            readonly = backup;
        }
    }

    public void readonly(final Runnable runnable) {
        final var backup = readonly;
        try {
            readonly = true;
            runnable.run();
        } finally {
            readonly = backup;
        }
    }

    public <T, E extends Exception> T readonly(final ThrowingSupplier<T, E> supplier) throws E {
        final var backup = readonly;
        try {
            readonly = true;
            return supplier.get();
        } finally {
            readonly = backup;
        }
    }

    public <T> T readonly(final Supplier<T> supplier) {
        final var backup = readonly;
        try {
            readonly = true;
            return supplier.get();
        } finally {
            readonly = backup;
        }
    }

    public boolean isReadonly() {
        return readonly;
    }

    public void ensureCanWrite() {
        if (readonly) {
            throw new ReadonlyModeException();
        }
    }

    //
    // Transaction
    //

    private static final Loop[] EMPTY_LOOP_ARRAY = new Loop[0];

    public <E extends Exception> void transaction(final ThrowingRunnable<E> runnable) throws E {
        final var backup = loops;
        if (loops == null) {
            loops = new HashSet<>();
        }
        try {
            runnable.run();
            if (backup == null) {
                final var loops = this.loops.toArray(EMPTY_LOOP_ARRAY);
                for (int i = 0; i < loops.length; i++) {
                    loops[i].activated = false;
                }
                for (int i = 0; i < loops.length; i++) {
                    final var loop = loops[i];
                    registerCaller(loop, loop.runnable);
                }
                for (int i = 0; i < loops.length; i++) {
                    loops[i].activated = true;
                }
            }
        } finally {
            loops = backup;
        }
    }

    void registerLoop(final Loop loop) {
        loops.add(loop);
    }

    //
    // Caller/Callee
    //

    void registerCaller(final Node caller, final Runnable runnable) {
        final var backup = this.caller;
        this.caller = caller;
        try {
            runnable.run();
        } finally {
            this.caller = backup;
        }
    }

    <T> T registerCaller(final Node caller, final Supplier<T> supplier) {
        final var backup = this.caller;
        this.caller = caller;
        try {
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
