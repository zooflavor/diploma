package dog.wiggler.tree23;

import dog.wiggler.function.Runnable;
import dog.wiggler.function.Supplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.fail;

@TestMethodOrder(MethodOrderer.MethodName.class)
public class TreeTest {
    private static abstract class RemoveLeafTest {
        private final @NotNull List<@NotNull TestLeaf> leaves;
        private int nextLeaf;
        private final int size;

        public RemoveLeafTest(int size) {
            this.size=size;
            leaves=new ArrayList<>(size);
        }

        public abstract void create() throws Throwable;

        protected @NotNull TestLeaf nextLeaf() {
            var result=leaves.get(nextLeaf);
            ++nextLeaf;
            return result;
        }

        public void reset() throws Throwable {
            leaves.clear();
            for (int ii=0; size>ii; ++ii) {
                leaves.add(leaf(ii));
            }
            nextLeaf=0;
            create();
        }

        public void test() throws Throwable {
            for (int ii=0; size>ii; ++ii) {
                reset();
                var leaf=leaves.get(ii);
                @NotNull Tree<@NotNull Long, TestLeaf> tree=leaf;
                while (true) {
                    var parent=tree.parent();
                    if (null==parent) {
                        break;
                    }
                    tree=parent;
                }
                assertTreeInvariants(null, (NormalizedTree<@NotNull Long, TestLeaf>)tree);
                var expected=toValues(tree);
                // noinspection SuspiciousListRemoveInLoop
                expected.remove(ii);
                var tree2=RemoveLeaf.removeLeaf(Tree.LONG_SUM_AGGREGATOR, leaf);
                assertTreeInvariants(null, tree2);
                var actual=toValues(tree2);
                assertEquals(expected, actual);
            }
        }
    }

    private static class TestLeaf extends Leaf1<@NotNull Long, TestLeaf> {
        public final long value;

        public TestLeaf(long value) {
            this.value=value;
        }

        @Override
        public @NotNull Long aggregate() {
            return value;
        }

        @Override
        protected @NonNull TestLeaf self() {
            return this;
        }
    }

    private static int assertTreeInvariants(
            @Nullable Branch<@NotNull Long, TestLeaf> parent,
            @Nullable NormalizedTree<@NotNull Long, TestLeaf> tree)
            throws Throwable {
        if (null==tree) {
            assertNull(parent);
            return 0;
        }
        assertSame(parent, tree.parent());
        return tree.visit(new Visitor.FailVisitor<@NotNull Long, TestLeaf, @NotNull Integer>() {
            @Override
            public @NotNull Integer branch2(@NotNull Branch2<@NotNull Long, TestLeaf> branch2) throws Throwable {
                int height0=assertTreeInvariants(branch2, branch2.child0);
                int height1=assertTreeInvariants(branch2, branch2.child1);
                assertEquals(height0, height1);
                return height0+1;
            }

            @Override
            public @NotNull Integer branch3(@NotNull Branch3<@NotNull Long, TestLeaf> branch3) throws Throwable {
                int height0=assertTreeInvariants(branch3, branch3.child0);
                int height1=assertTreeInvariants(branch3, branch3.child1);
                int height2=assertTreeInvariants(branch3, branch3.child2);
                assertEquals(height0, height1);
                assertEquals(height0, height2);
                return height0+1;
            }

            @Override
            public @NotNull Integer leaf1(TreeTest.@NonNull TestLeaf leaf1) {
                return 1;
            }
        });
    }

    private static @NotNull Branch2<@NotNull Long, TestLeaf> branch2(
            NormalizedTree<@NotNull Long, TestLeaf> child0,
            NormalizedTree<@NotNull Long, TestLeaf> child1)
            throws Throwable {
        return Branch2.create(Tree.LONG_SUM_AGGREGATOR, child0, child1);
    }

    private static @NotNull Branch3<@NotNull Long, TestLeaf> branch3(
            NormalizedTree<@NotNull Long, TestLeaf> child0,
            NormalizedTree<@NotNull Long, TestLeaf> child1,
            NormalizedTree<@NotNull Long, TestLeaf> child2)
            throws Throwable {
        return Branch3.create(Tree.LONG_SUM_AGGREGATOR, child0, child1, child2);
    }

    private static @NotNull TestLeaf leaf(int value) {
        return new TestLeaf(value);
    }

    @Test
    public void testFailVisitor() throws Throwable {
        var visitor=new Visitor.FailVisitor<@NotNull Long, TestLeaf, Void>() {
        };
        for (var runnable: List.<@NotNull Runnable>of(
                ()->visitor.branch1(Branch1.create(leaf(0))),
                ()->visitor.branch2(branch2(leaf(0), leaf(1))),
                ()->visitor.branch3(branch3(leaf(0), leaf(1), leaf(2))),
                ()->visitor.branch4(Branch4.create(leaf(0), leaf(1), leaf(2), leaf(3))),
                ()->visitor.leaf0(new Leaf0<>()),
                ()->visitor.leaf1(leaf(0)),
                ()->visitor.leaf2(new Leaf2<>(leaf(0), leaf(1))))) {
            try {
                runnable.run();
                fail();
            }
            catch (IllegalArgumentException ignore) {
            }
        }
    }

    @Test
    public void testInsert() throws Throwable {
        testInsert(()->
                null);
        testInsert(()->
                leaf(0));
        testInsert(()->
                branch2(
                        leaf(0),
                        leaf(1)));
        testInsert(()->
                branch3(
                        leaf(0),
                        leaf(1),
                        leaf(2)));
        testInsert(()->
                branch2(
                        branch2(
                                leaf(0),
                                leaf(1)),
                        branch2(
                                leaf(2),
                                leaf(3))));
        testInsert(()->
                branch2(
                        branch3(
                                leaf(0),
                                leaf(1),
                                leaf(2)),
                        branch3(
                                leaf(3),
                                leaf(4),
                                leaf(5))));
        testInsert(()->
                branch3(
                        branch2(
                                leaf(0),
                                leaf(1)),
                        branch2(
                                leaf(2),
                                leaf(3)),
                        branch2(
                                leaf(4),
                                leaf(5))));
        testInsert(()->
                branch3(
                        branch3(
                                leaf(0),
                                leaf(1),
                                leaf(2)),
                        branch3(
                                leaf(3),
                                leaf(4),
                                leaf(5)),
                        branch3(
                                leaf(6),
                                leaf(7),
                                leaf(8))));
        testInsert(()->
                branch2(
                        branch2(
                                branch2(
                                        leaf(0),
                                        leaf(1)),
                                branch2(
                                        leaf(2),
                                        leaf(3))),
                        branch2(
                                branch2(
                                        leaf(4),
                                        leaf(5)),
                                branch2(
                                        leaf(6),
                                        leaf(7)))));
        testInsert(()->
                branch3(
                        branch2(
                                branch2(
                                        leaf(0),
                                        leaf(1)),
                                branch2(
                                        leaf(2),
                                        leaf(3))),
                        branch2(
                                branch2(
                                        leaf(4),
                                        leaf(5)),
                                branch2(
                                        leaf(6),
                                        leaf(7))),
                        branch2(
                                branch2(
                                        leaf(8),
                                        leaf(9)),
                                branch2(
                                        leaf(10),
                                        leaf(11)))));
    }

    private void testInsert(
            @NotNull Supplier<@Nullable NormalizedTree<@NotNull Long, TestLeaf>> treeFactory)
            throws Throwable {
        int size=(int)Tree.size(treeFactory.get());
        for (int ii=0; size>=ii; ++ii) {
            var tree=treeFactory.get();
            assertTreeInvariants(null, tree);
            var expectedValues=toValues(tree);
            expectedValues.add(ii, -1L);
            tree=InsertAtIndex.insertAtIndex(Tree.LONG_SUM_AGGREGATOR, ii, leaf(-1), tree);
            assertTreeInvariants(null, tree);
            var actualValues=toValues(tree);
            assertEquals(expectedValues, actualValues);
        }
    }

    @Test
    public void testInsertPositionError() throws Throwable {
        try {
            InsertAtIndex.insertAtIndex(
                    Tree.LONG_SUM_AGGREGATOR,
                    1,
                    leaf(0),
                    null);
            fail();
        }
        catch (IllegalArgumentException ignore) {
        }
        try {
            InsertAtIndex.insertAtIndex(
                    Tree.LONG_SUM_AGGREGATOR,
                    -1,
                    leaf(0),
                    branch2(leaf(1),
                            leaf(2)));
            fail();
        }
        catch (IllegalArgumentException ignore) {
        }
        try {
            InsertAtIndex.insertAtIndex(
                    Tree.LONG_SUM_AGGREGATOR,
                    3,
                    leaf(0),
                    branch2(leaf(1),
                            leaf(2)));
            fail();
        }
        catch (IllegalArgumentException ignore) {
        }
    }

    @Test
    public void testLeafPathIndex() throws Throwable {
        testLeafPathIndex(leaf(0));
        var leaf0=leaf(0);
        var leaf1=leaf(1);
        var leaf2=leaf(2);
        var leaf3=leaf(3);
        var leaf4=leaf(4);
        var leaf5=leaf(5);
        var leaf6=leaf(6);
        var leaf7=leaf(7);
        var leaf8=leaf(8);
        var leaf9=leaf(9);
        var leaf10=leaf(10);
        var leaf11=leaf(11);
        branch2(leaf0, leaf1);
        testLeafPathIndex(leaf0, leaf1);
        branch3(leaf0, leaf1, leaf2);
        testLeafPathIndex(leaf0, leaf1, leaf2);
        branch2(
                branch3(
                        branch2(
                                leaf0,
                                leaf1),
                        branch2(
                                leaf2,
                                leaf3),
                        branch2(
                                leaf4,
                                leaf5)),
                branch3(
                        branch2(
                                leaf6,
                                leaf7),
                        branch2(
                                leaf8,
                                leaf9),
                        branch2(
                                leaf10,
                                leaf11)));
        testLeafPathIndex(leaf0, leaf1, leaf2, leaf3, leaf4, leaf5, leaf6, leaf7, leaf8, leaf9, leaf10, leaf11);
    }

    private void testLeafPathIndex(@NotNull TestLeaf @NotNull ... leaves) throws Throwable {
        for (int ii=0; leaves.length>ii; ++ii) {
            assertEquals(
                    ii,
                    LeafPath.leafPath(
                            leaves[ii],
                            new LeafPath.Index<>())
                            .index);
        }
    }

    @Test
    public void testLeafPathInterval() throws Throwable {
        var leaves=List.of(
                leaf(2),
                leaf(1),
                leaf(1),
                leaf(0),
                leaf(2),
                leaf(3));
        branch3(
                branch2(
                        leaves.get(0),
                        leaves.get(1)),
                branch2(
                        leaves.get(2),
                        leaves.get(3)),
                branch2(
                        leaves.get(4),
                        leaves.get(5)));
        assertEquals(
                0L,
                LeafPath.leafPath(
                        leaves.get(0),
                        new LeafPath.Interval<>())
                        .aggregate);
        assertEquals(
                2L,
                LeafPath.leafPath(
                        leaves.get(1),
                        new LeafPath.Interval<>())
                        .aggregate);
        assertEquals(
                3L,
                LeafPath.leafPath(
                        leaves.get(2),
                        new LeafPath.Interval<>())
                        .aggregate);
        assertEquals(
                4L,
                LeafPath.leafPath(
                        leaves.get(3),
                        new LeafPath.Interval<>())
                        .aggregate);
        assertEquals(
                4L,
                LeafPath.leafPath(
                        leaves.get(4),
                        new LeafPath.Interval<>())
                        .aggregate);
        assertEquals(
                6L,
                LeafPath.leafPath(
                        leaves.get(5),
                        new LeafPath.Interval<>())
                        .aggregate);
    }

    @Test
    public void testNotNormalizedSizes() {
        assertEquals(1, Branch1.create(leaf(0)).size());
        assertEquals(4, Branch4.create(leaf(0), leaf(1), leaf(2), leaf(3)).size());
        assertEquals(0, new Leaf0<@NotNull Long, TestLeaf>().size());
        assertEquals(2, new Leaf2<>(leaf(0), leaf(1)).size());
    }

    @Test
    public void testRemoveLeaf() throws Throwable {
        new RemoveLeafTest(1) {
            @Override
            public void create() {
            }
        }.test();
        new RemoveLeafTest(2) {
            @Override
            public void create() throws Throwable {
                branch2(
                        nextLeaf(),
                        nextLeaf());
            }
        }.test();
        new RemoveLeafTest(3) {
            @Override
            public void create() throws Throwable {
                branch3(
                        nextLeaf(),
                        nextLeaf(),
                        nextLeaf());
            }
        }.test();
        new RemoveLeafTest(4) {
            @Override
            public void create() throws Throwable {
                branch2(
                        branch2(
                                nextLeaf(),
                                nextLeaf()),
                        branch2(
                                nextLeaf(),
                                nextLeaf()));
            }
        }.test();
        new RemoveLeafTest(9) {
            @Override
            public void create() throws Throwable {
                branch3(
                        branch3(
                                nextLeaf(),
                                nextLeaf(),
                                nextLeaf()),
                        branch3(
                                nextLeaf(),
                                nextLeaf(),
                                nextLeaf()),
                        branch3(
                                nextLeaf(),
                                nextLeaf(),
                                nextLeaf()));
            }
        }.test();
        new RemoveLeafTest(8) {
            @Override
            public void create() throws Throwable {
                branch2(
                        branch2(
                                branch2(
                                        nextLeaf(),
                                        nextLeaf()),
                                branch2(
                                        nextLeaf(),
                                        nextLeaf())),
                        branch2(
                                branch2(
                                        nextLeaf(),
                                        nextLeaf()),
                                branch2(
                                        nextLeaf(),
                                        nextLeaf())));
            }
        }.test();
        new RemoveLeafTest(5) {
            @Override
            public void create() throws Throwable {
                branch2(
                        branch3(
                                nextLeaf(),
                                nextLeaf(),
                                nextLeaf()),
                        branch2(
                                nextLeaf(),
                                nextLeaf()));
            }
        }.test();
        new RemoveLeafTest(6) {
            @Override
            public void create() throws Throwable {
                branch3(
                        branch2(
                                nextLeaf(),
                                nextLeaf()),
                        branch2(
                                nextLeaf(),
                                nextLeaf()),
                        branch2(
                                nextLeaf(),
                                nextLeaf()));
            }
        }.test();
        new RemoveLeafTest(7) {
            @Override
            public void create() throws Throwable {
                branch3(
                        branch3(
                                nextLeaf(),
                                nextLeaf(),
                                nextLeaf()),
                        branch2(
                                nextLeaf(),
                                nextLeaf()),
                        branch2(
                                nextLeaf(),
                                nextLeaf()));
            }
        }.test();
        new RemoveLeafTest(8) {
            @Override
            public void create() throws Throwable {
                branch3(
                        branch3(
                                nextLeaf(),
                                nextLeaf(),
                                nextLeaf()),
                        branch3(
                                nextLeaf(),
                                nextLeaf(),
                                nextLeaf()),
                        branch2(
                                nextLeaf(),
                                nextLeaf()));
            }
        }.test();
        new RemoveLeafTest(11) {
            @Override
            public void create() throws Throwable {
                branch2(
                        branch2(
                                branch2(
                                        nextLeaf(),
                                        nextLeaf()),
                                branch2(
                                        nextLeaf(),
                                        nextLeaf())),
                        branch3(
                                branch2(
                                        nextLeaf(),
                                        nextLeaf()),
                                branch3(
                                        nextLeaf(),
                                        nextLeaf(),
                                        nextLeaf()),
                                branch2(
                                        nextLeaf(),
                                        nextLeaf())));
            }
        }.test();
        new RemoveLeafTest(15) {
            @Override
            public void create() throws Throwable {
                branch3(
                        branch2(
                                branch2(
                                        nextLeaf(),
                                        nextLeaf()),
                                branch2(
                                        nextLeaf(),
                                        nextLeaf())),
                        branch2(
                                branch2(
                                        nextLeaf(),
                                        nextLeaf()),
                                branch2(
                                        nextLeaf(),
                                        nextLeaf())),
                        branch3(
                                branch2(
                                        nextLeaf(),
                                        nextLeaf()),
                                branch3(
                                        nextLeaf(),
                                        nextLeaf(),
                                        nextLeaf()),
                                branch2(
                                        nextLeaf(),
                                        nextLeaf())));
            }
        }.test();
    }

    @Test
    public void testSelectLeaf() throws Throwable {
        var leaves=List.of(
                leaf(2),
                leaf(1),
                leaf(1),
                leaf(0),
                leaf(2),
                leaf(3));
        var tree=branch3(
                branch2(
                        leaves.get(0),
                        leaves.get(1)),
                branch2(
                        leaves.get(2),
                        leaves.get(3)),
                branch2(
                        leaves.get(4),
                        leaves.get(5)));
        assertSame(
                leaves.get(0),
                Selector.selectLeaf(
                        Selector.firstSelector(),
                        tree));
        assertSame(
                leaves.get(5),
                Selector.selectLeaf(
                        Selector.lastSelector(),
                        tree));
        for (var pair: List.of(
                List.of(0, 0),
                List.of(0, 1),
                List.of(1, 2),
                List.of(2, 3),
                List.of(4, 4),
                List.of(4, 5),
                List.of(5, 6),
                List.of(5, 7),
                List.of(5, 8))) {
            var leaf=Selector.selectLeaf(
                    Selector.intervalSelector(pair.get(1)),
                    tree);
            assertSame(
                    leaves.get(pair.get(0)),
                    leaf);
        }
        for (var index: List.of(-1, 9)) {
            try {
                Selector.selectLeaf(
                        Selector.intervalSelector(index),
                        tree);
                fail();
            }
            catch (IllegalArgumentException ignore) {
            }
        }
    }

    private static @NotNull List<@NotNull Long> toValues(@Nullable Tree<@NotNull Long, TestLeaf> tree) throws Throwable {
        @NotNull List<@NotNull Long> values=new ArrayList<>((int)Tree.size(tree));
        if (null!=tree) {
            toValues(tree, values);
        }
        return values;
    }

    private static void toValues(
            @NotNull Tree<@NotNull Long, TestLeaf> tree,
            @NotNull List<@NotNull Long> values)
            throws Throwable {
        tree.visit(new Visitor.FailVisitor<@NotNull Long, TestLeaf, Void>() {
            @Override
            public Void branch2(@NotNull Branch2<@NotNull Long, TestLeaf> branch2) throws Throwable {
                toValues(branch2.child0, values);
                toValues(branch2.child1, values);
                return null;
            }

            @Override
            public Void branch3(@NotNull Branch3<@NotNull Long, TestLeaf> branch3) throws Throwable {
                toValues(branch3.child0, values);
                toValues(branch3.child1, values);
                toValues(branch3.child2, values);
                return null;
            }

            @Override
            public Void leaf1(TreeTest.@NonNull TestLeaf leaf1) {
                values.add(leaf1.value);
                return null;
            }
        });
    }
}
