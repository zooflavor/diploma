package dog.wiggler.cache;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Implements the least-frequently-used replacement policy.
 * <br>
 * There's {@link HashMap} in this containing all lines in the cache, and associated data.
 * The hashmap is used to check on a line.
 * There's also a {@link TreeMap} containing all frequencies having a line in cache with that access frequency.
 * The treemap values are sets of line, containing all the lines with frequency equaling the key.
 * Most operation has O(1) amortized time complexity and also O(ln(cacheLines)) time complexity.
 * {@link #contains(long)} is O(1).
 */
public class LFUPolicy implements ReplacementPolicy {
    private static class Line {
        public boolean dirty;
        public long frequency;

        public Line(boolean dirty) {
            this.dirty=dirty;
            frequency=1;
        }
    }

    private final @NotNull NavigableMap<@NotNull Long, @NotNull NavigableSet<@NotNull Long>> frequencies
            =new TreeMap<>();
    private final @NotNull Map<@NotNull Long, @NotNull Line> lines=new HashMap<>();

    @Override
    public void access(long address, boolean dirty) {
        var line=lines.get(address);
        if (null==line) {
            throw new IllegalArgumentException("address is not cached");
        }
        if (dirty) {
            line.dirty=true;
        }
        var set=frequencies.get(line.frequency);
        set.remove(address);
        if (set.isEmpty()) {
            frequencies.remove(line.frequency);
        }
        ++line.frequency;
        frequencies.computeIfAbsent(line.frequency, (key)->new TreeSet<>())
                .add(address);
    }

    @Override
    public void addAndAccess(long address, boolean dirty) {
        var line=new Line(dirty);
        var oldValue=lines.put(address, line);
        if (null!=oldValue) {
            throw new IllegalArgumentException("address is already cached");
        }
        frequencies.computeIfAbsent(line.frequency, (key)->new TreeSet<>())
                .add(address);
    }

    @Override
    public boolean contains(long address) {
        return lines.containsKey(address);
    }

    @Override
    public @Nullable Long evict() {
        if (frequencies.isEmpty()) {
            throw new IllegalStateException("cache is empty");
        }
        var entry=frequencies.firstEntry();
        var address=entry.getValue().removeLast();
        if (entry.getValue().isEmpty()) {
            frequencies.remove(entry.getKey());
        }
        var line=lines.remove(address);
        return line.dirty
                ?address
                :null;
    }

    @Override
    public int size() {
        return lines.size();
    }
}
