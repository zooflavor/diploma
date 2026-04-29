package dog.wiggler.tree23;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Selects a leaf of a tree by navigating the branches from the root.
 * <br>
 * Selectors may have an internal state that changes on the course of a selection.
 */
public interface Selector<A, L extends Leaf1<A, L>> {
    /**
     * A selector to select the first leaf.
     */
    static <A, L extends Leaf1<A, L>> @NotNull Selector<A, L> firstSelector() {
        return new Selector<>() {
            @Override
            public @NotNull NormalizedTree<A, L> select2(@NotNull Branch2<A, L> branch2) {
                return branch2.child0;
            }

            @Override
            public @NotNull NormalizedTree<A, L> select3(@NotNull Branch3<A, L> branch3) {
                return branch3.child0;
            }
        };
    }

    /**
     * Leaf aggregate values are interpreted as lines that are as long as the value.
     * Trees containing these lines are interpreted as the lines put one after another, end on end,
     * in the order defined by the inorder traversal of the tree.
     * This selector selects a leaf that has the point index in it,
     * measured from the starting point of the leftmost leaf.
     * This should be user with Tree.LONG_SUM_AGGREGATOR.
     */
    static <L extends Leaf1<@NotNull Long, L>> @NotNull Selector<@NotNull Long, L> intervalSelector(
            long index) {
        return new Selector<@NotNull Long, L>() {
            private long index2=index;

            private void check(@NotNull NormalizedTree<@NotNull Long, L> tree) {
                if ((0>index2) || (tree.aggregate()<=index2)) {
                    throw new IllegalArgumentException(
                            "invalid selection index, index: %d, aggregate: %d"
                                    .formatted(index2, tree.aggregate()));
                }
            }

            @Override
            public @NotNull NormalizedTree<@NotNull Long, L> select2(@NotNull Branch2<@NotNull Long, L> branch2) {
                check(branch2);
                if (index2<branch2.child0.aggregate()) {
                    return branch2.child0;
                }
                else {
                    index2-=branch2.child0.aggregate();
                    return branch2.child1;
                }
            }

            @Override
            public @NotNull NormalizedTree<@NotNull Long, L> select3(@NotNull Branch3<@NotNull Long, L> branch3) {
                check(branch3);
                if (index2<branch3.child0.aggregate()) {
                    return branch3.child0;
                }
                else if (index2<branch3.child0.aggregate()+branch3.child1.aggregate()) {
                    index2-=branch3.child0.aggregate();
                    return branch3.child1;
                }
                else {
                    index2-=branch3.child0.aggregate()+branch3.child1.aggregate();
                    return branch3.child2;
                }
            }
        };
    }

    /**
     * A selector to select the last leaf.
     */
    static <A, L extends Leaf1<A, L>> @NotNull Selector<A, L> lastSelector() {
        return new Selector<>() {
            @Override
            public @NotNull NormalizedTree<A, L> select2(@NotNull Branch2<A, L> branch2) {
                return branch2.child1;
            }

            @Override
            public @NotNull NormalizedTree<A, L> select3(@NotNull Branch3<A, L> branch3) {
                return branch3.child2;
            }
        };
    }

    /**
     * Choose one of the branches of the tree.
     * @param branch2 the branch to choose on if its children
     * @return on of the child of the tree
     */
    @NotNull NormalizedTree<A, L> select2(@NotNull Branch2<A, L> branch2) throws Throwable;

    /**
     * Choose one of the branches of the tree.
     * @param branch3 the branch to choose on if its children
     * @return on of the child of the tree
     */
    @NotNull NormalizedTree<A, L> select3(@NotNull Branch3<A, L> branch3) throws Throwable;

    /**
     * Select a leaf of the tree by the selector.
     */
    static <A, L extends Leaf1<A, L>> @NotNull L selectLeaf(
            @NotNull Selector<A, L> selector,
            @NotNull NormalizedTree<A, L> tree)
            throws Throwable {
        class SelectLeafVisitor extends Visitor.FailVisitor<A, L, Void> {
            @Nullable L leaf;
            @NotNull NormalizedTree<A, L> tree2=tree;

            @Override
            public Void branch2(@NotNull Branch2<A, L> branch2) throws Throwable {
                tree2=selector.select2(branch2);
                return null;
            }

            @Override
            public Void branch3(@NotNull Branch3<A, L> branch3) throws Throwable {
                tree2=selector.select3(branch3);
                return null;
            }

            @Override
            public Void leaf1(@NotNull L leaf1) {
                leaf=leaf1;
                return null;
            }
        }
        var visitor=new SelectLeafVisitor();
        while (null==visitor.leaf) {
            visitor.tree2.visit(visitor);
        }
        return visitor.leaf;
    }
}
