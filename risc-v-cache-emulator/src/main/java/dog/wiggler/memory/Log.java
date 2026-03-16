package dog.wiggler.memory;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * Interface to write memory access logs.
 */
public interface Log extends AutoCloseable, LogVisitor<Void> {
    @Override
    void close() throws IOException;

    /**
     * Creates a memory access log doing nothing.
     */
    static @NotNull Log noOp() {
        return new Log() {
            @Override
            public void close() {
            }

            @Override
            public Void access(long address, int size, @NotNull AccessType type) {
                return null;
            }

            @Override
            public Void elapsedCycles(long elapsedCycles) {
                return null;
            }

            @Override
            public Void end() {
                return null;
            }

            @Override
            public Void userData(long userData) {
                return null;
            }
        };
    }
}
