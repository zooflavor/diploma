package dog.wiggler.memory;

import org.jetbrains.annotations.NotNull;

/**
 * Visitor for memory access log entries.
 */
public interface LogVisitor<R> {
    /**
     * Memory access. The size is in bytes.
     */
    R access(long address, int size, @NotNull AccessType type) throws Throwable;

    /**
     * Access log disabled.
     */
    R accessLogDisabled() throws Throwable;

    /**
     * Access log enabled.
     */
    R accessLogEnabled() throws Throwable;

    /**
     * Total elapsed emulator cycles up to this point.
     */
    R elapsedCycles(long elapsedCycles) throws Throwable;

    /**
     * No more log entries.
     */
    R end() throws Throwable;

    /**
     * User data.
     */
    R userData(long userData) throws Throwable;
}
