package eb2501.fluor.core;

import eb2501.fluor.core.utils.ThrowingRunnable;
import eb2501.fluor.core.utils.ThrowingSupplier;

public class Fluor {

    public static <E extends Exception> void snapshot(final ThrowingRunnable<E> runnable) throws E {
        Context.storage.get().snapshot(runnable);
    }

    public static <T, E extends Exception> T snapshot(final ThrowingSupplier<T, E> supplier) throws E {
        return Context.storage.get().snapshot(supplier);
    }

    public static <E extends Exception> void readonly(final ThrowingRunnable<E> runnable) throws E {
        Context.storage.get().readonly(runnable);
    }

    public static <T, E extends Exception> T readonly(final ThrowingSupplier<T, E> supplier) throws E {
        return Context.storage.get().readonly(supplier);
    }

    public boolean isReadonly() {
        return Context.storage.get().isReadonly();
    }

    public static <E extends Exception> void transaction(final ThrowingRunnable<E> runnable) throws E {
        Context.storage.get().transaction(runnable, false);
    }
}
