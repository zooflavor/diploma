package dog.wiggler.cache;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Implements the random replacement policy.
 * This policy is not a stack algorithm.
 * <br>
 * There's {@link HashMap} in this containing all lines in the cache, and their dirty flags.
 * There's also a {@link List} containing all lines, in some order.
 * The list is used to randomly choose a line.
 * The map is used to check on a line.
 * Every operation has O(1) amortized time complexity.
 */
public class RandomPolicy implements ReplacementPolicy {
    private final @NotNull List<@NotNull Long> addresses=new ArrayList<>();
    private final @NotNull Map<@NotNull Long, @NotNull Boolean> lines=new HashMap<>();
    private final @NotNull Random random;

    public RandomPolicy() {
        random=new Random();
    }

    public RandomPolicy(long seed) {
        random=new Random(seed);
    }

    @Override
    public void access(long address, boolean dirty) {
        if (!lines.containsKey(address)) {
            throw new IllegalArgumentException("address is not cached");
        }
        if (dirty) {
            lines.put(address, true);
        }
    }

    @Override
    public void addAndAccess(long address, boolean dirty) {
        if (lines.containsKey(address)) {
            throw new IllegalArgumentException("address is already cached");
        }
        addresses.add(address);
        lines.put(address, dirty);
    }

    @Override
    public boolean contains(long address) {
        return lines.containsKey(address);
    }

    @Override
    public @Nullable Long evict() {
        if (lines.isEmpty()) {
            throw new IllegalStateException("cache is empty");
        }
        int index=random.nextInt(addresses.size());
        long address=addresses.removeLast();
        if (index!=addresses.size()) {
            address=addresses.set(index, address);
        }
        boolean dirty=lines.remove(address);
        return dirty
                ?address
                :null;
    }

    @Override
    public int size() {
        return lines.size();
    }
}
