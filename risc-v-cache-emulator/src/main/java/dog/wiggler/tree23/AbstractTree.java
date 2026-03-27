package dog.wiggler.tree23;

import org.jetbrains.annotations.Nullable;

public abstract class AbstractTree<A, L extends Leaf1<A, L>> implements Tree<A, L> {
    private @Nullable Branch<A, L> parent;

    @Override
    public @Nullable Branch<A, L> parent() {
        return parent;
    }

    @Override
    public void parent(@Nullable Branch<A, L> parent) {
        this.parent=parent;
    }
}
