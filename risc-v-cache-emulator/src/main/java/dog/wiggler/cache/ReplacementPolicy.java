package dog.wiggler.cache;

import org.jetbrains.annotations.Nullable;

/**
 * Data structure to hold the addresses of cache lines, and the dirty flag.
 * Also implements the replacement policy.
 */
public interface ReplacementPolicy {
    /**
     * Signal that the cache line has been accessed.
     * Dirty = true sets the dirty flag on the line.
     * Dirty = false won't clear the dirty flag on the line.
     * <br>
     * The cache line must be in the cache.
     */
    void access(long address, boolean dirty);

    /**
     * Add a new cache line to this, and access it right away.
     * It must not be contained in this already.
     */
    void addAndAccess(long address, boolean dirty);

    /**
     * @return true iff the cache lines is contained in this.
     */
    boolean contains(long address);

    /**
     * Evicts a line. There must be at least one line contained in this.
     *
     * @return The address of the evicted line iff the line is dirty.
     */
    @Nullable Long evict();

    /**
     * The number of cache lines contained in this.
     */
    int size();
}
