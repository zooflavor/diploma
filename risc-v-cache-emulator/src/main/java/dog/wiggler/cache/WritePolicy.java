package dog.wiggler.cache;

/**
 * Possible values for write policies.
 */
public enum WritePolicy {
    /**
     * Write only to the cache. Mark the cache line dirty.
     * Dirty cache lines must be written before eviction.
     */
    WRITE_BACK,
    /**
     * Write immediately to backing store. Cache lines will never be dirty.
     */
    WRITE_THROUGH
}
