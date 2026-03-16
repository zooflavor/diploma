package dog.wiggler.memory;

import org.jetbrains.annotations.NotNull;

/**
 * Visitor for memory access log entries.
 */
public interface LogVisitor<R> {
    /**
     * Memory access.
     */
    R access(long address, int size, @NotNull AccessType type) throws Throwable;
    
    /**
     * Total elapsed emulator cycles up to this point.
     */
    R elapsedCycles(long elapsedCycles) throws Throwable;

    /**
     * No more log entries.
     */
    R end() throws Throwable;

    /**
     * Creates a new visitor that delegates log entries to the specified visitor,
     * and collapses consecutive elapsed cycle entries.
     */
    static @NotNull LogVisitor<Void> mergeElapsedCycles(
            @NotNull LogVisitor<Void> visitor) {
        return new LogVisitor<>() {
            private Long elapsedCycles;

            @Override
            public Void access(long address, int size, @NotNull AccessType type) throws Throwable {
                flush();
                return visitor.access(address, size, type);
            }

            @Override
            public Void elapsedCycles(long elapsedCycles) {
                this.elapsedCycles=elapsedCycles;
                return null;
            }

            @Override
            public Void end() throws Throwable {
                flush();
                return visitor.end();
            }

            private void flush() throws Throwable {
                if (null!=elapsedCycles) {
                    visitor.elapsedCycles(elapsedCycles);
                    elapsedCycles=null;
                }
            }

            @Override
            public Void userData(long userData) throws Throwable {
                flush();
                return visitor.userData(userData);
            }
        };
    }

    /**
     * User data.
     */
    R userData(long userData) throws Throwable;
}
