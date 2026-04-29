package dog.wiggler.tree23;

import dog.wiggler.function.BiFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Base interface for the nodes of a 2-3 tree.
 * <br>
 * Empty trees are represented by null.
 * <br>
 * Every tree has aggregated data. A leaf is free to define its own aggregate.
 * A branch node uses a binary function, the aggregator, to combine the aggregates of its children
 * to compute its own aggregate.
 * <br>
 * Nodes shouldn't be shared between trees, cause that will invalidate the parent pointers.
 *
 * @param <A> the type of the aggregated data
 * @param <L> the type of the leaf nodes
 */
public interface Tree<A, L extends Leaf1<A, L>> {
    /**
     * Tree aggregator summing long values.
     */
    @NotNull BiFunction<@NotNull Long, @NotNull Long, @NotNull Long> LONG_SUM_AGGREGATOR=Long::sum;
    /**
     * Tree aggregator doing nothing.
     */
    @NotNull BiFunction<Void, Void, Void> VOID_AGGREGATOR=(left, right)->null;

    /**
     * The parent of this node.
     */
    @Nullable Branch<A, L> parent();

    /**
     * Sets the parent of this node.
     */
    void parent(@Nullable Branch<A, L> parent);

    /**
     * The number of leaf nodes in this subtree.
     */
    long size();

    /**
     * The number of leaf nodes in the tree.
     */
    static <A, L extends Leaf1<A, L>> long size(@Nullable Tree<A, L> tree) {
        return (null==tree)
                ?0L
                :tree.size();
    }

    /**
     * Implements the visitor pattern on 2-3 tree nodes.
     * Can be you used to pattern match on the type of this node.
     */
    <R> R visit(@NotNull Visitor<A, L, R> visitor) throws Throwable;
}
