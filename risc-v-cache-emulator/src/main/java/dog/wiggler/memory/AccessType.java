package dog.wiggler.memory;

/**
 * Memory access types.
 * Data and instruction loads are differentiated to support separate instruction caches.
 */
public enum AccessType {
    LOAD_DATA,
    LOAD_INSTRUCTION,
    STORE;

    public boolean load() {
        return !STORE.equals(this);
    }

    public boolean store() {
        return STORE.equals(this);
    }
}
