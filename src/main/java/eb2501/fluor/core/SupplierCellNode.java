package eb2501.fluor.core;

import java.util.function.Supplier;

class SupplierCellNode<T> extends SupplierCacheNode<T> implements Cell<T> {

    SupplierCellNode(final Page owner, final Supplier<T> supplier) {
        super(owner, supplier);
    }

    @Override
    public void set(T value) {
        owner.context.ensureCanWrite();
        this.value = value;
        cached = true;
        invalidateChildren();
        invalidateParents();
    }
}
