package dog.wiggler.tree23;

import org.jetbrains.annotations.NotNull;

public interface LeafPath<A, L extends Leaf1<A, L>> {
    class Index<A, L extends Leaf1<A, L>> implements LeafPath<A, L> {
        public long index;

        @Override
        public void branch2(@NotNull Branch2<A, L> branch2Parent, @NotNull NormalizedTree<A, L> child) {
            if (branch2Parent.child1==child) {
                index+=branch2Parent.child0.size();
            }
        }

        @Override
        public void branch3(@NotNull Branch3<A, L> branch3Parent, @NotNull NormalizedTree<A, L> child) {
            if (branch3Parent.child1==child) {
                index+=branch3Parent.child0.size();
            }
            else if (branch3Parent.child2==child) {
                index+=branch3Parent.child0.size()+branch3Parent.child1.size();
            }
        }

        @Override
        public void leaf1(@NotNull L leaf1) {
            index=0L;
        }
    }

    /**
     * The aggregate values are interpreted as intervals, as in Selector.intervalSelector.
     */
    class Interval<L extends Leaf1<@NotNull Long, L>> implements LeafPath<@NotNull Long, L> {
        public long aggregate;

        @Override
        public void branch2(
                @NotNull Branch2<@NotNull Long, L> branch2Parent,
                @NotNull NormalizedTree<@NotNull Long, L> child) {
            if (branch2Parent.child1==child) {
                aggregate+=branch2Parent.child0.aggregate();
            }
        }

        @Override
        public void branch3(
                @NotNull Branch3<@NotNull Long, L> branch3Parent,
                @NotNull NormalizedTree<@NotNull Long, L> child) {
            if (branch3Parent.child1==child) {
                aggregate+=branch3Parent.child0.aggregate();
            }
            else if (branch3Parent.child2==child) {
                aggregate+=branch3Parent.child0.aggregate()+branch3Parent.child1.aggregate();
            }
        }

        @Override
        public void leaf1(@NotNull L leaf1) {
            aggregate=0L;
        }
    }

    void branch2(@NotNull Branch2<A, L> branch2Parent, @NotNull NormalizedTree<A, L> child) throws Throwable;

    void branch3(@NotNull Branch3<A, L> branch3Parent, @NotNull NormalizedTree<A, L> child) throws Throwable;

    void leaf1(@NotNull L leaf1) throws Throwable;

    static <A, L extends Leaf1<A, L>, P extends LeafPath<A, L>> @NotNull P leafPath(
            @NotNull L leaf,
            @NotNull P path)
            throws Throwable {
        class IndexVisitor extends Visitor.FailVisitor<A, L, @NotNull NormalizedTree<A, L>> {
            public @NotNull NormalizedTree<A, L> child=leaf;

            @Override
            public @NotNull NormalizedTree<A, L> branch2(@NotNull Branch2<A, L> branch2) throws Throwable {
                path.branch2(branch2, child);
                return branch2;
            }

            @Override
            public @NotNull NormalizedTree<A, L> branch3(@NotNull Branch3<A, L> branch3) throws Throwable {
                path.branch3(branch3, child);
                return branch3;
            }
        }
        var visitor=new IndexVisitor();
        path.leaf1(leaf);
        while (true) {
            var parent=visitor.child.parent();
            if (null==parent) {
                break;
            }
            visitor.child=parent.visit(visitor);
        }
        return path;
    }
}
