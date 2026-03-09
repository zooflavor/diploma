package dog.wiggler.cache;

public enum WritePolicy {
    /**
     * delay write
     */
    WRITE_BACK,
    /**
     * write immediately to backing store
     */
    WRITE_THROUGH;

    public boolean back() {
        return WRITE_BACK.equals(this);
    }

    public boolean through() {
        return WRITE_THROUGH.equals(this);
    }
}
