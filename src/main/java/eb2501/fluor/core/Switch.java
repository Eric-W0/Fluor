package eb2501.fluor.core;

public interface Switch extends AutoCloseable {
    boolean isActive();
    void start();
    void stop();

    @Override
    default void close() {
        if (isActive()) {
            stop();
        }
    }
}
