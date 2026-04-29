package dog.wiggler.tree23;

import dog.wiggler.function.BiFunction;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * An internal node holding two children.
 */
public class Branch2<A, L extends Leaf1<A, L>>
        extends AbstractTree<A, L>
        implements Branch<A, L>, NormalizedTree<A, L> {
    private final A aggregate;
    public final @NotNull NormalizedTree<A, L> child0;
    public final @NotNull NormalizedTree<A, L> child1;
    private final long size;

    private Branch2(
            @NotNull BiFunction<A, A, A> aggregator,
            @NotNull NormalizedTree<A, L> child0,
            @NotNull NormalizedTree<A, L> child1)
            throws Throwable {
        this.child0=Objects.requireNonNull(child0, "child0");
        this.child1=Objects.requireNonNull(child1, "child1");
        aggregate=aggregator.apply(
                child0.aggregate(),
                child1.aggregate());
        size=child0.size()+child1.size();
    }

    @Override
    public A aggregate() {
        return aggregate;
    }

    public static <A, L extends Leaf1<A, L>> @NotNull Branch2<A, L> create(
            @NotNull BiFunction<A, A, A> aggregator,
            @NotNull NormalizedTree<A, L> child0,
            @NotNull NormalizedTree<A, L> child1)
            throws Throwable {
        var branch2=new Branch2<>(aggregator, child0, child1);
        child0.parent(branch2);
        child1.parent(branch2);
        return branch2;
    }

    @Override
    public long size() {
        return size;
    }

    @Override
    public <R> R visit(@NotNull Visitor<A, L, R> visitor) throws Throwable {
        return visitor.branch2(this);
    }
}
