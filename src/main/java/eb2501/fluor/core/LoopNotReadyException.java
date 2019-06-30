package eb2501.fluor.core;

public final class LoopNotReadyException extends RuntimeException {
    private final Loop loop;

    public LoopNotReadyException(final Loop loop) {
        super(String.format("loop [%s] is not ready", loop));
        this.loop = loop;
    }
}
