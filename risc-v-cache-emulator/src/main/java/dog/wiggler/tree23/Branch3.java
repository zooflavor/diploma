package dog.wiggler.tree23;

import dog.wiggler.function.BiFunction;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * An internal node holding three children.
 */
public class Branch3<A, L extends Leaf1<A, L>>
        extends AbstractTree<A, L>
        implements Branch<A, L>, NormalizedTree<A, L> {
    private final A aggregate;
    public final @NotNull NormalizedTree<A, L> child0;
    public final @NotNull NormalizedTree<A, L> child1;
    public final @NotNull NormalizedTree<A, L> child2;
    private final long size;

    private Branch3(
            @NotNull BiFunction<A, A, A> aggregator,
            @NotNull NormalizedTree<A, L> child0,
            @NotNull NormalizedTree<A, L> child1,
            @NotNull NormalizedTree<A, L> child2)
            throws Throwable {
        this.child0=Objects.requireNonNull(child0, "child0");
        this.child1=Objects.requireNonNull(child1, "child1");
        this.child2=Objects.requireNonNull(child2, "child2");
        aggregate=aggregator.apply(
                aggregator.apply(
                        child0.aggregate(),
                        child1.aggregate()),
                child2.aggregate());
        size=child0.size()+child1.size()+child2.size();
    }

    @Override
    public A aggregate() {
        return aggregate;
    }

    public static <A, L extends Leaf1<A, L>> @NotNull Branch3<A, L> create(
            @NotNull BiFunction<A, A, A> aggregator,
            @NotNull NormalizedTree<A, L> child0,
            @NotNull NormalizedTree<A, L> child1,
            @NotNull NormalizedTree<A, L> child2)
            throws Throwable {
        var branch3=new Branch3<>(aggregator, child0, child1, child2);
        child0.parent(branch3);
        child1.parent(branch3);
        child2.parent(branch3);
        return branch3;
    }

    @Override
    public long size() {
        return size;
    }

    @Override
    public <R> R visit(@NotNull Visitor<A, L, R> visitor) throws Throwable {
        return visitor.branch3(this);
    }
}
