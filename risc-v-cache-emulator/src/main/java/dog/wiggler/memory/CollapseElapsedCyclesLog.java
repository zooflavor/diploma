package dog.wiggler.memory;

import dog.wiggler.function.Supplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Objects;

/**
 * A log implementation to collapse consecutive elapsed cycles entries.
 * It tracks the latest elapsed cycles value since the last elapsed cycle write to the underlying log.
 * Received elapsed cycles entries are tracked in memory and not sent to the underlying log immediately.
 * When an entry is received that is not an elapsed cycles entry, and there's an unwritten elapsed cycles entry
 * in memory, it will send the elapsed cycle entry first, and clears it, then sends the entry received.
 */
public class CollapseElapsedCyclesLog implements Log {
    private @Nullable Long lastElapsedCycles;
    private final @NotNull Log log;

    public CollapseElapsedCyclesLog(@NotNull Log log) {
        this.log=Objects.requireNonNull(log, "visitor");
    }

    @Override
    public Void access(long address, int size, @NotNull AccessType type) throws Throwable {
        logElapsedCycles();
        return log.access(address, size, type);
    }

    @Override
    public Void accessLogDisabled() throws Throwable {
        logElapsedCycles();
        return log.accessLogDisabled();
    }

    @Override
    public Void accessLogEnabled() throws Throwable {
        logElapsedCycles();
        return log.accessLogEnabled();
    }

    @Override
    public void close() throws IOException {
        log.close();
    }

    @Override
    public Void elapsedCycles(long elapsedCycles) {
        lastElapsedCycles=elapsedCycles;
        return null;
    }

    @Override
    public Void end() throws Throwable {
        logElapsedCycles();
        return log.end();
    }

    public static @NotNull Supplier<@NotNull CollapseElapsedCyclesLog> factory(
            @NotNull Supplier<? extends @NotNull Log> logFactory)  {
        return Supplier.factory(
                CollapseElapsedCyclesLog::new,
                logFactory);
    }

    private void logElapsedCycles() throws Throwable {
        if (null!=lastElapsedCycles) {
            log.elapsedCycles(lastElapsedCycles);
            lastElapsedCycles=null;
        }
    }

    @Override
    public Void userData(long userData) throws Throwable {
        logElapsedCycles();
        return log.userData(userData);
    }
}
