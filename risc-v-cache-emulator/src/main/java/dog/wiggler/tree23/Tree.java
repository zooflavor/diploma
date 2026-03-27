package dog.wiggler.tree23;

import dog.wiggler.function.BiFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Nodes shouldn't be shared between trees, cause that will invalidate the parent pointers.
 */
public interface Tree<A, L extends Leaf1<A, L>> {
    @NotNull BiFunction<@NotNull Long, @NotNull Long, @NotNull Long> LONG_SUM_AGGREGATOR=Long::sum;
    @NotNull BiFunction<Void, Void, Void> VOID_AGGREGATOR=(left, right)->null;

    @Nullable Branch<A, L> parent();

    void parent(@Nullable Branch<A, L> parent);

    long size();

    static <A, L extends Leaf1<A, L>> long size(@Nullable Tree<A, L> tree) {
        return (null==tree)
                ?0L
                :tree.size();
    }

    <R> R visit(@NotNull Visitor<A, L, R> visitor) throws Throwable;
}
