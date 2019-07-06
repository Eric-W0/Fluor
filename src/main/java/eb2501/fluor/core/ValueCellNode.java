package eb2501.fluor.core;

class ValueCellNode<T> extends Node implements Cell<T> {
    private T value;

    ValueCellNode(Page owner, T value) {
        super(owner, true, false);
        this.value = value;
    }

    @Override
    public T get() {
        owner.context.registerCallee(this);
        return value;
    }

    @Override
    public void set(T value) {
        owner.context.ensureCanWrite();
        this.value = value;
        invalidateParents();
    }

    @Override
    protected void invalidate() {
        invalidateParents();
    }
}
