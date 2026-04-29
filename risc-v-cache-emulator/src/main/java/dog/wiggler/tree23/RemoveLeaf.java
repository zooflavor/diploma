package dog.wiggler.tree23;

import dog.wiggler.function.BiFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class implementing the remove operation.
 * It removes a given leaf from a tree.
 * <br>
 * The implementation is very standard.
 * It starts from a leaf, removes it, and rebalances the tree as necessary ascending to the root.
 */
public class RemoveLeaf {
    private RemoveLeaf() {
    }

    /**
     * Removes leaf from the tree containing leaf.
     * The old tree is destroyed.
     *
     * @param aggregator used to calculate aggregates in internal nodes
     * @param leaf to be removed
     * @return the new tree
     */
    public static <A, L extends Leaf1<A, L>> @Nullable NormalizedTree<A, L> removeLeaf(
            @NotNull BiFunction<A, A, A> aggregator,
            @NotNull L leaf)
            throws Throwable {
        var parent=leaf.parent();
        if (null==parent) {
            return null;
        }
        class RemoveLeafVisitor extends Visitor.FailVisitor<A, L, @NotNull Tree<A, L>> {
            private class GrandChildrenVisitor extends FailVisitor<A, L, Void> {
                // least grandchildren: 1 branch1 and 1 branch2 => 3
                // most grandchildren: 1 branch1 and 2 branch3s => 7
                private final @NotNull List<@NotNull NormalizedTree<A, L>> grandChildren
                        =new ArrayList<>(7);

                @Override
                public Void branch1(@NotNull Branch1<A, L> branch1) {
                    grandChildren.add(branch1.child);
                    return null;
                }

                @Override
                public Void branch2(@NotNull Branch2<A, L> branch2) {
                    grandChildren.add(branch2.child0);
                    grandChildren.add(branch2.child1);
                    return null;
                }

                @Override
                public Void branch3(@NotNull Branch3<A, L> branch3) {
                    grandChildren.add(branch3.child0);
                    grandChildren.add(branch3.child1);
                    grandChildren.add(branch3.child2);
                    return null;
                }

                public @NotNull Tree<A, L> create() throws Throwable {
                    return switch (grandChildren.size()) {
                        case 3 -> Branch1.create(
                                Branch3.create(
                                        aggregator,
                                        grandChildren.get(0),
                                        grandChildren.get(1),
                                        grandChildren.get(2)));
                        case 4 -> Branch2.create(
                                aggregator,
                                Branch2.create(
                                        aggregator,
                                        grandChildren.get(0),
                                        grandChildren.get(1)),
                                Branch2.create(
                                        aggregator,
                                        grandChildren.get(2),
                                        grandChildren.get(3)));
                        case 5 -> Branch2.create(
                                aggregator,
                                Branch2.create(
                                        aggregator,
                                        grandChildren.get(0),
                                        grandChildren.get(1)),
                                Branch3.create(
                                        aggregator,
                                        grandChildren.get(2),
                                        grandChildren.get(3),
                                        grandChildren.get(4)));
                        case 6 -> Branch2.create(
                                aggregator,
                                Branch3.create(
                                        aggregator,
                                        grandChildren.get(0),
                                        grandChildren.get(1),
                                        grandChildren.get(2)),
                                Branch3.create(
                                        aggregator,
                                        grandChildren.get(3),
                                        grandChildren.get(4),
                                        grandChildren.get(5)));
                        default -> Branch3.create(
                                aggregator,
                                Branch2.create(
                                        aggregator,
                                        grandChildren.get(0),
                                        grandChildren.get(1)),
                                Branch2.create(
                                        aggregator,
                                        grandChildren.get(2),
                                        grandChildren.get(3)),
                                Branch3.create(
                                        aggregator,
                                        grandChildren.get(4),
                                        grandChildren.get(5),
                                        grandChildren.get(6)));
                    };
                }
            }

            public @NotNull Tree<A, L> newChild=new Leaf0<>();
            public @NotNull Tree<A, L> originalChild=leaf;

            @Override
            public @NotNull Tree<A, L> branch2(@NotNull Branch2<A, L> branch2Parent) throws Throwable {
                return newChild.visit(new FailVisitor<A, L, @NotNull Tree<A, L>>() {
                    @Override
                    public @NotNull Tree<A, L> branch1(@NotNull Branch1<A, L> branch1Child) throws Throwable {
                        var grandChildren=new GrandChildrenVisitor();
                        if (branch2Parent.child0==originalChild) {
                            branch1Child.visit(grandChildren);
                            branch2Parent.child1.visit(grandChildren);
                        }
                        else {
                            branch2Parent.child0.visit(grandChildren);
                            branch1Child.visit(grandChildren);
                        }
                        return grandChildren.create();
                    }

                    @Override
                    public @NotNull Tree<A, L> branch2(@NotNull Branch2<A, L> branch2Child) throws Throwable {
                        return normalized(branch2Child);
                    }

                    @Override
                    public @NotNull Tree<A, L> branch3(@NotNull Branch3<A, L> branch3Child) throws Throwable {
                        return normalized(branch3Child);
                    }

                    @Override
                    public @NotNull Tree<A, L> leaf0(@NotNull Leaf0<A, L> leaf0Child) {
                        if (branch2Parent.child0==originalChild) {
                            return Branch1.create(
                                    branch2Parent.child1);
                        }
                        else {
                            return Branch1.create(
                                    branch2Parent.child0);
                        }
                    }

                    private @NotNull Tree<A, L> normalized(
                            @NotNull NormalizedTree<A, L> normalizedChild)
                            throws Throwable {
                        if (branch2Parent.child0==originalChild) {
                            return Branch2.create(
                                    aggregator,
                                    normalizedChild,
                                    branch2Parent.child1);
                        }
                        else {
                            return Branch2.create(
                                    aggregator,
                                    branch2Parent.child0,
                                    normalizedChild);
                        }
                    }
                });
            }

            @Override
            public @NotNull Tree<A, L> branch3(@NotNull Branch3<A, L> branch3Parent) throws Throwable {
                return newChild.visit(new FailVisitor<A, L, @NotNull Tree<A, L>>() {
                    @Override
                    public @NotNull Tree<A, L> branch1(@NotNull Branch1<A, L> branch1Child) throws Throwable {
                        var grandChildren=new GrandChildrenVisitor();
                        if (branch3Parent.child0==originalChild) {
                            branch1Child.visit(grandChildren);
                            branch3Parent.child1.visit(grandChildren);
                            branch3Parent.child2.visit(grandChildren);
                        }
                        else if (branch3Parent.child1==originalChild) {
                            branch3Parent.child0.visit(grandChildren);
                            branch1Child.visit(grandChildren);
                            branch3Parent.child2.visit(grandChildren);
                        }
                        else {
                            branch3Parent.child0.visit(grandChildren);
                            branch3Parent.child1.visit(grandChildren);
                            branch1Child.visit(grandChildren);
                        }
                        return grandChildren.create();
                    }

                    @Override
                    public @NotNull Tree<A, L> branch2(@NotNull Branch2<A, L> branch2Child) throws Throwable {
                        return normalized(branch2Child);
                    }

                    @Override
                    public @NotNull Tree<A, L> branch3(@NotNull Branch3<A, L> branch3Child) throws Throwable {
                        return normalized(branch3Child);
                    }

                    @Override
                    public @NotNull Tree<A, L> leaf0(@NotNull Leaf0<A, L> leaf0Child) throws Throwable {
                        if (branch3Parent.child0==originalChild) {
                            return Branch2.create(
                                    aggregator,
                                    branch3Parent.child1,
                                    branch3Parent.child2);
                        }
                        else if (branch3Parent.child1==originalChild) {
                            return Branch2.create(
                                    aggregator,
                                    branch3Parent.child0,
                                    branch3Parent.child2);
                        }
                        else {
                            return Branch2.create(
                                    aggregator,
                                    branch3Parent.child0,
                                    branch3Parent.child1);
                        }
                    }

                    private @NotNull Tree<A, L> normalized(
                            @NotNull NormalizedTree<A, L> normalizedChild)
                            throws Throwable {
                        if (branch3Parent.child0==originalChild) {
                            return Branch3.create(
                                    aggregator,
                                    normalizedChild,
                                    branch3Parent.child1,
                                    branch3Parent.child2);
                        }
                        else if (branch3Parent.child1==originalChild) {
                            return Branch3.create(
                                    aggregator,
                                    branch3Parent.child0,
                                    normalizedChild,
                                    branch3Parent.child2);
                        }
                        else {
                            return Branch3.create(
                                    aggregator,
                                    branch3Parent.child0,
                                    branch3Parent.child1,
                                    normalizedChild);
                        }
                    }
                });
            }
        }
        var visitor=new RemoveLeafVisitor();
        do {
            visitor.newChild=parent.visit(visitor);
            visitor.originalChild=parent;
            parent=visitor.originalChild.parent();
        } while (null!=parent);
        return visitor.newChild.visit(new Visitor.FailVisitor<A, L, @NotNull NormalizedTree<A, L>>() {
            @Override
            public @NotNull NormalizedTree<A, L> branch1(@NotNull Branch1<A, L> branch1) {
                branch1.child.parent(null);
                return branch1.child;
            }

            @Override
            public @NotNull NormalizedTree<A, L> branch2(@NotNull Branch2<A, L> branch2) {
                return branch2;
            }

            @Override
            public @NotNull NormalizedTree<A, L> branch3(@NotNull Branch3<A, L> branch3) {
                return branch3;
            }
        });
    }
}
