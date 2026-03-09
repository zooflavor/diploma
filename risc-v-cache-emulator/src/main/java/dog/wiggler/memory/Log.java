package dog.wiggler.memory;

import java.io.IOException;

public interface Log extends AutoCloseable, LogVisitor<Void> {
    @Override
    void close() throws IOException;

    static Log noOp() {
        return new Log() {
            @Override
            public void close() {
            }

            @Override
            public Void access(long address, int size, AccessType type) {
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
