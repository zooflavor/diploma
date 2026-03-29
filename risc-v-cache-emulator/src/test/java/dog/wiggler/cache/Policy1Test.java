package dog.wiggler.cache;

import dog.wiggler.function.Supplier;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@ParameterizedClass
@MethodSource("parameters")
@TestMethodOrder(MethodOrderer.MethodName.class)
public class Policy1Test {
    private final @NotNull Supplier<? extends @NotNull ReplacementPolicy> policyFactory;

    public Policy1Test(
            @NotNull Supplier<? extends @NotNull ReplacementPolicy> policyFactory) {
        this.policyFactory=Objects.requireNonNull(policyFactory, "policyFactory");
    }

    public static @NotNull Stream<@NotNull Arguments> parameters() {
        @NotNull List<@NotNull Arguments> result=new ArrayList<>();
        result.add(Arguments.of(
                (@NotNull Supplier<? extends @NotNull ReplacementPolicy>)FIFOPolicy::new));
        result.add(Arguments.of(
                (@NotNull Supplier<? extends @NotNull ReplacementPolicy>)LFUPolicy::new));
        result.add(Arguments.of(
                (@NotNull Supplier<? extends @NotNull ReplacementPolicy>)LRUPolicy::new));
        result.add(Arguments.of(
                (@NotNull Supplier<? extends @NotNull ReplacementPolicy>)RandomPolicy::new));
        result.add(Arguments.of(
                (@NotNull Supplier<? extends @NotNull ReplacementPolicy>)()->new RandomPolicy(13L)));
        return result.stream();
    }
    
    @Test
    public void testAccessNotCached() throws Throwable {
        var policy=policyFactory.get();
        try {
            policy.access(1, false);
            fail();
        }
        catch (IllegalArgumentException ignore) {
        }
    }

    @Test
    public void testAddCached() throws Throwable {
        var policy=policyFactory.get();
        policy.addAndAccess(1, false);
        try {
            policy.addAndAccess(1, false);
            fail();
        }
        catch (IllegalArgumentException ignore) {
        }
    }

    @Test
    public void testContains() throws Throwable {
        var policy=policyFactory.get();
        policy.addAndAccess(1, true);
        policy.addAndAccess(2, false);
        assertFalse(policy.contains(0));
        assertTrue(policy.contains(1));
        assertTrue(policy.contains(2));
        if (null==policy.evict()) {
            assertFalse(policy.contains(0));
            assertTrue(policy.contains(1));
            assertFalse(policy.contains(2));
            assertEquals(Long.valueOf(1), policy.evict());
        }
        else {
            assertFalse(policy.contains(0));
            assertFalse(policy.contains(1));
            assertTrue(policy.contains(2));
            assertNull(policy.evict());
        }
        assertFalse(policy.contains(0));
        assertFalse(policy.contains(1));
        assertFalse(policy.contains(2));
    }

    @Test
    public void testDirtyAccess() throws Throwable {
        var policy=policyFactory.get();
        policy.addAndAccess(1, false);
        policy.access(1, true);
        assertEquals(Long.valueOf(1), policy.evict());
    }

    @Test
    public void testDirtyAccessDoesntClear() throws Throwable {
        var policy=policyFactory.get();
        policy.addAndAccess(1, true);
        policy.access(1, false);
        assertEquals(Long.valueOf(1), policy.evict());
    }

    @Test
    public void testEvictEmpty() throws Throwable {
        var policy=policyFactory.get();
        try {
            policy.evict();
            fail();
        }
        catch (IllegalStateException ignore) {
        }
    }
}
