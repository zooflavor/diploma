package dog.wiggler.tree23;

import org.jetbrains.annotations.NotNull;

/**
 * A normalized leaf node, holding one data.
 */
public abstract class Leaf1<A, L extends Leaf1<A, L>> extends AbstractTree<A, L> implements NormalizedTree<A, L> {
    protected abstract @NotNull L self();

    @Override
    public long size() {
        return 1L;
    }

    @Override
    public <R> R visit(@NotNull Visitor<A, L, R> visitor) throws Throwable {
        return visitor.leaf1(self());
    }
}
