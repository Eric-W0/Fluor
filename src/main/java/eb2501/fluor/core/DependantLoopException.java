package eb2501.fluor.core;

public final class DependantLoopException extends RuntimeException {
    private final LoopNode invalidater;
    private final LoopNode invalidated;

    public DependantLoopException(final LoopNode invalidated) {
        super(
                String.format(
                        "LoopNode [%s] has been invalidated a second time by another loop",
                        invalidated
                )
        );
        this.invalidated = invalidated;
        invalidater = null;
    }

    public DependantLoopException(final LoopNode invalidater, final DependantLoopException exception) {
        super(
                String.format(
                        "LoopNode [%s] has been invalidated a second time by loop [%s]",
                        exception.invalidated,
                        invalidater
                ),
                exception
        );
        this.invalidater = invalidater;
        invalidated = exception.invalidated;
    }
}
