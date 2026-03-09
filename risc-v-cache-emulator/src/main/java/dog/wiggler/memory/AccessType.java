package dog.wiggler.memory;

public enum AccessType {
    LOAD_DATA, LOAD_INSTRUCTION, STORE;

    public boolean load() {
        return !STORE.equals(this);
    }

    public boolean store() {
        return STORE.equals(this);
    }
}
