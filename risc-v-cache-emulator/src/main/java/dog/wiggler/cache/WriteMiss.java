package dog.wiggler.cache;

/**
 * Possible write miss policies.
 */
public enum WriteMiss {
    /**
     * Allocate a cache line for a missed write.
     */
    ALLOCATE,
    /**
     * Don't allocate a cache line for a missed write, writes it directly to memory.
     */
    DON_T_ALLOCATE
}
