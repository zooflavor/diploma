package dog.wiggler.cache;

public enum WriteMiss {
    ALLOCATE, DON_T_ALLOCATE;

    public boolean allocate() {
        return ALLOCATE.equals(this);
    }
}
