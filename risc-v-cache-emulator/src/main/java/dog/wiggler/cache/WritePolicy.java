package dog.wiggler.cache;

public enum WritePolicy {
    /**
     * delay write
     */
    WRITE_BACK,
    /**
     * write immediately to backing store
     */
    WRITE_THROUGH
}
