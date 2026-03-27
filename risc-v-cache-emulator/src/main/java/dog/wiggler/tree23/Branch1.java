package dog.wiggler.tree23;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class Branch1<A, L extends Leaf1<A, L>> extends AbstractTree<A, L> implements Branch<A, L> {
    public final @NotNull NormalizedTree<A, L> child;
    private final long size;

    private Branch1(
            @NotNull NormalizedTree<A, L> child) {
        this.child=Objects.requireNonNull(child, "child");
        size=child.size();
    }

    public static <A, L extends Leaf1<A, L>> @NotNull Branch1<A, L> create(
            @NotNull NormalizedTree<A, L> child) {
        var branch1=new Branch1<>(child);
        child.parent(branch1);
        return branch1;
    }

    @Override
    public long size() {
        return size;
    }

    @Override
    public <R> R visit(@NotNull Visitor<A, L, R> visitor) throws Throwable {
        return visitor.branch1(this);
    }
}
