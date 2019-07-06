package eb2501.fluor.core.utils;

import java.util.function.Supplier;

public class Rug {

    public static class Exception extends RuntimeException {

        private Exception(final java.lang.Exception exception) {
            super(exception);
        }
    }

    public static Runnable under(final ThrowingRunnable<?> runnable) {
        return () -> {
            try {
                runnable.run();
            } catch (final java.lang.Exception exc) {
                if (exc instanceof RuntimeException) {
                    throw (RuntimeException)exc;
                } else {
                    throw new Exception(exc);
                }
            }
        };
    }

    public static <T> Supplier<T> under(final ThrowingSupplier<T, ?> supplier) {
        return () -> {
            try {
                return supplier.get();
            } catch (final java.lang.Exception exc) {
                if (exc instanceof RuntimeException) {
                    throw (RuntimeException)exc;
                } else {
                    throw new Exception(exc);
                }
            }
        };
    }
}
