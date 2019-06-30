package eb2501.fluor.core.utils;

@FunctionalInterface
public interface ThrowingRunnable<E extends Exception> {
    void run() throws E;
}
