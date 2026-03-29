package dog.wiggler.cache;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

public class FIFOPolicy implements ReplacementPolicy {
    private final @NotNull Map<@NotNull Long, @NotNull Boolean> lines=new HashMap<>();
    private final @NotNull Deque<@NotNull Long> queue=new ArrayDeque<>();

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
        lines.put(address, dirty);
        queue.addLast(address);
    }

    @Override
    public boolean contains(long address) {
        return lines.containsKey(address);
    }

    @Override
    public @Nullable Long evict() {
        if (queue.isEmpty()) {
            throw new IllegalStateException("cache is empty");
        }
        long address=queue.removeFirst();
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
