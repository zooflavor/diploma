package dog.wiggler.tree23;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * A leaf node holding two data. This is not a {@link NormalizedTree}.
 */
public class Leaf2<A, L extends Leaf1<A, L>> extends AbstractTree<A, L> {
    public final @NotNull L leaf0;
    public final @NotNull L leaf1;

    public Leaf2(
            @NotNull L leaf0,
            @NotNull L leaf1) {
        this.leaf0=Objects.requireNonNull(leaf0, "leaf0");
        this.leaf1=Objects.requireNonNull(leaf1, "leaf1");
    }

    @Override
    public long size() {
        return 2L;
    }

    @Override
    public <R> R visit(@NotNull Visitor<A, L, R> visitor) throws Throwable {
        return visitor.leaf2(this);
    }
}
