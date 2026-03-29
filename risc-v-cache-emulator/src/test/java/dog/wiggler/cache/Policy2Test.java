package dog.wiggler.cache;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.HashSet;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.MethodName.class)
public class Policy2Test {
    @Test
    public void testFIFOOrder() {
        var policy=new FIFOPolicy();
        testOrder(policy);

        policy=new FIFOPolicy();
        policy.addAndAccess(0, false);
        policy.addAndAccess(1, true);
        policy.addAndAccess(2, false);
        testOrder(policy, 0, 1, 2);

        policy=new FIFOPolicy();
        policy.addAndAccess(0, false);
        policy.addAndAccess(1, true);
        policy.addAndAccess(2, false);
        policy.access(1, false);
        testOrder(policy, 0, 1, 2);
    }
    
    @Test
    public void testLFUOrder() {
        var policy=new LFUPolicy();
        testOrder(policy);

        policy=new LFUPolicy();
        policy.addAndAccess(0, false);
        policy.addAndAccess(1, true);
        policy.addAndAccess(2, false);
        testOrder(policy, 2, 1, 0);

        policy=new LFUPolicy();
        policy.addAndAccess(0, false);
        policy.addAndAccess(1, true);
        policy.addAndAccess(2, false);
        policy.access(0, false);
        policy.access(0, false);
        policy.access(1, false);
        policy.access(2, false);
        policy.access(2, false);
        policy.access(2, false);
        testOrder(policy, 1, 0, 2);
    }

    @Test
    public void testLRUOrder() {
        var policy=new LRUPolicy();
        testOrder(policy);

        policy=new LRUPolicy();
        policy.addAndAccess(1, true);
        testOrder(policy, 1);

        policy=new LRUPolicy();
        policy.addAndAccess(1, true);
        policy.addAndAccess(2, true);
        testOrder(policy, 1, 2);

        policy=new LRUPolicy();
        policy.addAndAccess(1, false);
        policy.addAndAccess(2, true);
        policy.access(1, false);
        testOrder(policy, 2, 1);

        policy=new LRUPolicy();
        policy.addAndAccess(1, false);
        policy.addAndAccess(2, true);
        policy.addAndAccess(3, true);
        testOrder(policy, 1, 2, 3);

        policy=new LRUPolicy();
        policy.addAndAccess(1, false);
        policy.addAndAccess(2, true);
        policy.addAndAccess(3, true);
        policy.access(1, false);
        testOrder(policy, 2, 3, 1);

        policy=new LRUPolicy();
        policy.addAndAccess(1, false);
        policy.addAndAccess(2, true);
        policy.addAndAccess(3, true);
        policy.access(2, false);
        testOrder(policy, 1, 3, 2);

        policy=new LRUPolicy();
        policy.addAndAccess(1, false);
        policy.addAndAccess(2, true);
        policy.addAndAccess(3, true);
        policy.access(3, false);
        testOrder(policy, 1, 2, 3);
    }

    private void testOrder(
            @NotNull ReplacementPolicy policy,
            @NotNull Integer @NotNull ... evictOrder) {
        for (int ii=0; evictOrder.length>ii; ++ii) {
            assertEquals(evictOrder.length-ii, policy.size());
            for (int jj=0; evictOrder.length>jj; ++jj) {
                assertEquals(
                        ii<=jj,
                        policy.contains(evictOrder[jj]),
                        "evict value: %d".formatted(evictOrder[jj]));
            }
            var evicted=policy.evict();
            if (null!=evicted) {
                assertEquals((long)evictOrder[ii], evicted);
            }
        }
        assertEquals(0, policy.size());
        for (var value: evictOrder) {
            assertFalse(policy.contains(value));
        }
    }

    @Test
    public void testRandomEvict() {
        var policy=new RandomPolicy();
        var contained=new TreeSet<@NotNull Long>();
        var notContained=new TreeSet<@NotNull Long>();
        for (long ii=0L; 16L>ii; ++ii) {
            contained.add(ii);
            policy.addAndAccess(ii, 0!=(ii&1));
        }
        notContained.add(17L);
        assertEquals(contained.size(), policy.size());
        for (var value: contained) {
            assertTrue(policy.contains(value));
        }
        for (var value: notContained) {
            assertFalse(policy.contains(value));
        }
        while (!contained.isEmpty()) {
            var evicted1=policy.evict();
            var evicted2=new HashSet<@NotNull Long>();
            for (var value: contained) {
                if (!policy.contains(value)) {
                    evicted2.add(value);
                }
            }
            assertEquals(1, evicted2.size());
            var evicted3=evicted2.iterator().next();
            if (null!=evicted1) {
                assertEquals(evicted1, evicted3);
            }
            contained.remove(evicted3);
            notContained.add(evicted3);
            assertEquals(contained.size(), policy.size());
            for (var value: contained) {
                assertTrue(policy.contains(value));
            }
            for (var value: notContained) {
                assertFalse(policy.contains(value));
            }
        }
    }
}
