package dog.wiggler.tree23;

import dog.wiggler.function.BiFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Utility class implementing the insert operation.
 * It inserts into a given index, where leaf indices
 * are counted among the leaves from left to right.
 * So index 0 will insert before the leftmost leaf,
 * and index {@code tree.size()} would insert after the rightmost tree.
 * <br>
 * The implementation is very standard.
 * It descends to the leaf level guided by the value of index and the sizes of the subtrees encountered.
 * After that, it inserts a new leaf, and rebalances the tree as necessary ascending to the root.
 */
public class InsertAtIndex {
    private InsertAtIndex() {
    }

    /**
     * Inserts newLeaf at index index into the tree.
     * The old tree is destroyed.
     *
     * @param aggregator used to calculate aggregates in internal nodes
     * @param index newLeaf will be inserted at this index
     * @param newLeaf will be inserted
     * @param tree to insert into
     * @return the new tree
     */
    public static <A, L extends Leaf1<A, L>> @NotNull NormalizedTree<A, L> insertAtIndex(
            @NotNull BiFunction<A, A, A> aggregator,
            long index,
            @NotNull L newLeaf,
            @Nullable NormalizedTree<A, L> tree)
            throws Throwable {
        if (null==tree) {
            if (0L!=index) {
                throw new IllegalArgumentException(
                        "invalid insert index, index: %d, size: 0"
                                .formatted(index));
            }
            return newLeaf;
        }
        if ((0L>index) || (tree.size()<index)) {
            throw new IllegalArgumentException(
                    "invalid insert index, index: %d, size: %d"
                            .formatted(index, tree.size()));
        }
        var result=insertAtIndex2(aggregator, index, newLeaf, tree);
        return result.visit(new Visitor.FailVisitor<A, L, @NotNull NormalizedTree<A, L>>() {
            @Override
            public @NotNull NormalizedTree<A, L> branch2(@NotNull Branch2<A, L> branch2) {
                return branch2;
            }

            @Override
            public @NotNull NormalizedTree<A, L> branch3(@NotNull Branch3<A, L> branch3) {
                return branch3;
            }

            @Override
            public @NotNull NormalizedTree<A, L> branch4(@NotNull Branch4<A, L> branch4) throws Throwable {
                return Branch2.create(
                        aggregator,
                        Branch2.create(
                                aggregator,
                                branch4.child0,
                                branch4.child1),
                        Branch2.create(
                                aggregator,
                                branch4.child2,
                                branch4.child3));
            }

            @Override
            public @NotNull NormalizedTree<A, L> leaf2(@NotNull Leaf2<A, L> leaf2) throws Throwable {
                return Branch2.create(
                        aggregator,
                        leaf2.leaf0,
                        leaf2.leaf1);
            }
        });
    }

    private static <A, L extends Leaf1<A, L>> @NotNull Tree<A, L> insertAtIndex2(
            @NotNull BiFunction<A, A, A> aggregator,
            long index,
            @NotNull L newLeaf,
            @NotNull Tree<A, L> oldTree) throws Throwable {
        return oldTree.visit(new Visitor.FailVisitor<A, L, @NotNull Tree<A, L>>() {
            @Override
            public @NotNull Tree<A, L> branch2(@NotNull Branch2<A, L> oldBranch2) throws Throwable {
                if (oldBranch2.child0.size()>=index) {
                    var newChild0=insertAtIndex2(
                            aggregator,
                            index,
                            newLeaf,
                            oldBranch2.child0);
                    return newChild0.visit(new FailVisitor<A, L, @NotNull Tree<A, L>>() {
                        public @NotNull Tree<A, L> branch(@NotNull NormalizedTree<A, L> newBranch) throws Throwable {
                            return Branch2.create(
                                    aggregator,
                                    newBranch,
                                    oldBranch2.child1);
                        }

                        public @NotNull Tree<A, L> branch2(@NotNull Branch2<A, L> newBranch2) throws Throwable {
                            return branch(newBranch2);
                        }

                        @Override
                        public @NotNull Tree<A, L> branch3(@NotNull Branch3<A, L> newBranch3) throws Throwable {
                            return branch(newBranch3);
                        }

                        @Override
                        public @NotNull Tree<A, L> branch4(@NotNull Branch4<A, L> newBranch4) throws Throwable {
                            return Branch3.create(
                                    aggregator,
                                    Branch2.create(
                                            aggregator,
                                            newBranch4.child0,
                                            newBranch4.child1),
                                    Branch2.create(
                                            aggregator,
                                            newBranch4.child2,
                                            newBranch4.child3),
                                    oldBranch2.child1);
                        }

                        @Override
                        public @NotNull Tree<A, L> leaf2(@NotNull Leaf2<A, L> leaf2) throws Throwable {
                            return Branch3.create(
                                    aggregator,
                                    leaf2.leaf0,
                                    leaf2.leaf1,
                                    oldBranch2.child1);
                        }
                    });
                }
                else {
                    var newChild1=insertAtIndex2(
                            aggregator,
                            index-oldBranch2.child0.size(),
                            newLeaf,
                            oldBranch2.child1);
                    return newChild1.visit(new FailVisitor<A, L, @NotNull Tree<A, L>>() {
                        public @NotNull Tree<A, L> branch(@NotNull NormalizedTree<A, L> newBranch) throws Throwable {
                            return Branch2.create(
                                    aggregator,
                                    oldBranch2.child0,
                                    newBranch);
                        }

                        @Override
                        public @NotNull Tree<A, L> branch2(@NotNull Branch2<A, L> newBranch2) throws Throwable {
                            return branch(newBranch2);
                        }

                        @Override
                        public @NotNull Tree<A, L> branch3(@NotNull Branch3<A, L> newBranch3) throws Throwable {
                            return branch(newBranch3);
                        }

                        @Override
                        public @NotNull Tree<A, L> branch4(@NotNull Branch4<A, L> newBranch4) throws Throwable {
                            return Branch3.create(
                                    aggregator,
                                    oldBranch2.child0,
                                    Branch2.create(
                                            aggregator,
                                            newBranch4.child0,
                                            newBranch4.child1),
                                    Branch2.create(
                                            aggregator,
                                            newBranch4.child2,
                                            newBranch4.child3));
                        }

                        @Override
                        public @NotNull Tree<A, L> leaf2(@NotNull Leaf2<A, L> leaf2) throws Throwable {
                            return Branch3.create(
                                    aggregator,
                                    oldBranch2.child0,
                                    leaf2.leaf0,
                                    leaf2.leaf1);
                        }
                    });
                }
            }

            @Override
            public @NotNull Tree<A, L> branch3(@NotNull Branch3<A, L> oldBranch3) throws Throwable {
                if (oldBranch3.child0.size()>=index) {
                    var newChild0=insertAtIndex2(
                            aggregator,
                            index,
                            newLeaf,
                            oldBranch3.child0);
                    return newChild0.visit(new FailVisitor<A, L, @NotNull Tree<A, L>>() {
                        public @NotNull Tree<A, L> branch(@NotNull NormalizedTree<A, L> newBranch) throws Throwable {
                            return Branch3.create(
                                    aggregator,
                                    newBranch,
                                    oldBranch3.child1,
                                    oldBranch3.child2);
                        }

                        public @NotNull Tree<A, L> branch2(@NotNull Branch2<A, L> newBranch2) throws Throwable {
                            return branch(newBranch2);
                        }

                        @Override
                        public @NotNull Tree<A, L> branch3(@NotNull Branch3<A, L> newBranch3) throws Throwable {
                            return branch(newBranch3);
                        }

                        @Override
                        public @NotNull Tree<A, L> branch4(@NotNull Branch4<A, L> newBranch4) throws Throwable {
                            return Branch4.create(
                                    Branch2.create(
                                            aggregator,
                                            newBranch4.child0,
                                            newBranch4.child1),
                                    Branch2.create(
                                            aggregator,
                                            newBranch4.child2,
                                            newBranch4.child3),
                                    oldBranch3.child1,
                                    oldBranch3.child2);
                        }

                        @Override
                        public @NotNull Tree<A, L> leaf2(@NotNull Leaf2<A, L> leaf2) {
                            return Branch4.create(
                                    leaf2.leaf0,
                                    leaf2.leaf1,
                                    oldBranch3.child1,
                                    oldBranch3.child2);
                        }
                    });
                }
                else if (oldBranch3.child0.size()+oldBranch3.child1.size()>=index) {
                    var newChild1=insertAtIndex2(
                            aggregator,
                            index-oldBranch3.child0.size(),
                            newLeaf,
                            oldBranch3.child1);
                    return newChild1.visit(new FailVisitor<A, L, @NotNull Tree<A, L>>() {
                        public @NotNull Tree<A, L> branch(@NotNull NormalizedTree<A, L> newBranch) throws Throwable {
                            return Branch3.create(
                                    aggregator,
                                    oldBranch3.child0,
                                    newBranch,
                                    oldBranch3.child2);
                        }

                        public @NotNull Tree<A, L> branch2(@NotNull Branch2<A, L> newBranch2) throws Throwable {
                            return branch(newBranch2);
                        }

                        @Override
                        public @NotNull Tree<A, L> branch3(@NotNull Branch3<A, L> newBranch3) throws Throwable {
                            return branch(newBranch3);
                        }

                        @Override
                        public @NotNull Tree<A, L> branch4(@NotNull Branch4<A, L> newBranch4) throws Throwable {
                            return Branch4.create(
                                    oldBranch3.child0,
                                    Branch2.create(
                                            aggregator,
                                            newBranch4.child0,
                                            newBranch4.child1),
                                    Branch2.create(
                                            aggregator,
                                            newBranch4.child2,
                                            newBranch4.child3),
                                    oldBranch3.child2);
                        }

                        @Override
                        public @NotNull Tree<A, L> leaf2(@NotNull Leaf2<A, L> leaf2) {
                            return Branch4.create(
                                    oldBranch3.child0,
                                    leaf2.leaf0,
                                    leaf2.leaf1,
                                    oldBranch3.child2);
                        }
                    });
                }
                else {
                    var newChild2=insertAtIndex2(
                            aggregator,
                            index-oldBranch3.child0.size()-oldBranch3.child1.size(),
                            newLeaf,
                            oldBranch3.child2);
                    return newChild2.visit(new FailVisitor<A, L, @NotNull Tree<A, L>>() {
                        public @NotNull Tree<A, L> branch(@NotNull NormalizedTree<A, L> newBranch) throws Throwable {
                            return Branch3.create(
                                    aggregator,
                                    oldBranch3.child0,
                                    oldBranch3.child1,
                                    newBranch);
                        }

                        public @NotNull Tree<A, L> branch2(@NotNull Branch2<A, L> newBranch2) throws Throwable {
                            return branch(newBranch2);
                        }

                        @Override
                        public @NotNull Tree<A, L> branch3(@NotNull Branch3<A, L> newBranch3) throws Throwable {
                            return branch(newBranch3);
                        }

                        @Override
                        public @NotNull Tree<A, L> branch4(@NotNull Branch4<A, L> newBranch4) throws Throwable {
                            return Branch4.create(
                                    oldBranch3.child0,
                                    oldBranch3.child1,
                                    Branch2.create(
                                            aggregator,
                                            newBranch4.child0,
                                            newBranch4.child1),
                                    Branch2.create(
                                            aggregator,
                                            newBranch4.child2,
                                            newBranch4.child3));
                        }

                        @Override
                        public @NotNull Tree<A, L> leaf2(@NotNull Leaf2<A, L> leaf2) {
                            return Branch4.create(
                                    oldBranch3.child0,
                                    oldBranch3.child1,
                                    leaf2.leaf0,
                                    leaf2.leaf1);
                        }
                    });
                }
            }

            @Override
            public @NotNull Tree<A, L> leaf1(@NotNull L oldLeaf1) {
                return (0==index)
                        ?new Leaf2<>(newLeaf, oldLeaf1)
                        :new Leaf2<>(oldLeaf1, newLeaf);
            }
        });
    }
}
