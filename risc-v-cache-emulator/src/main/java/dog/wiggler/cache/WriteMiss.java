package dog.wiggler.cache;

public enum WriteMiss {
    /**
     * Allocate a cache line for missed writes.
     */
    ALLOCATE,
    /**
     * Don't allocate a cache line for missed writes, writes directly to memory.
     */
    DON_T_ALLOCATE
}
