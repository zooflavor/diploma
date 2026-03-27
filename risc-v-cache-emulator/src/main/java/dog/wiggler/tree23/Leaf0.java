package dog.wiggler.tree23;

import org.jetbrains.annotations.NotNull;

public class Leaf0<A, L extends Leaf1<A, L>> extends AbstractTree<A, L> {
    @Override
    public long size() {
        return 0L;
    }

    @Override
    public <R> R visit(@NotNull Visitor<A, L, R> visitor) throws Throwable {
        return visitor.leaf0(this);
    }
}
