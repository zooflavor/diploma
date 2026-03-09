package dog.wiggler.cache;

public interface CacheCallback {
    void end() throws Throwable;

    static CacheCallback noOp() {
        return new CacheCallback() {
            @Override
            public void end() {
            }

            @Override
            public void statistics(long cacheHits, long cacheMisses, long elapsedCycles, long loadStores) {
            }

            @Override
            public void userData(long userData) {
            }
        };
    }

    void statistics(long cacheHits, long cacheMisses, long elapsedCycles, long loadStores) throws Throwable;

    void userData(long userData) throws Throwable;
}
