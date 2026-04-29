package dog.wiggler.tree23;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * A branch node holding four children. This is not a {@link NormalizedTree}.
 */
public class Branch4<A, L extends Leaf1<A, L>> extends AbstractTree<A, L> implements Branch<A, L> {
    public final @NotNull NormalizedTree<A, L> child0;
    public final @NotNull NormalizedTree<A, L> child1;
    public final @NotNull NormalizedTree<A, L> child2;
    public final @NotNull NormalizedTree<A, L> child3;
    private final long size;

    private Branch4(
            @NotNull NormalizedTree<A, L> child0,
            @NotNull NormalizedTree<A, L> child1,
            @NotNull NormalizedTree<A, L> child2,
            @NotNull NormalizedTree<A, L> child3) {
        this.child0=Objects.requireNonNull(child0, "child0");
        this.child1=Objects.requireNonNull(child1, "child1");
        this.child2=Objects.requireNonNull(child2, "child2");
        this.child3=Objects.requireNonNull(child3, "child3");
        size=child0.size()+child1.size()+child2.size()+child3.size();
    }

    public static <A, L extends Leaf1<A, L>> @NotNull Branch4<A, L> create(
            @NotNull NormalizedTree<A, L> child0,
            @NotNull NormalizedTree<A, L> child1,
            @NotNull NormalizedTree<A, L> child2,
            @NotNull NormalizedTree<A, L> child3) {
        var branch4=new Branch4<>(child0, child1, child2, child3);
        child0.parent(branch4);
        child1.parent(branch4);
        child2.parent(branch4);
        child3.parent(branch4);
        return branch4;
    }

    @Override
    public long size() {
        return size;
    }

    @Override
    public <R> R visit(@NotNull Visitor<A, L, R> visitor) throws Throwable {
        return visitor.branch4(this);
    }
}
