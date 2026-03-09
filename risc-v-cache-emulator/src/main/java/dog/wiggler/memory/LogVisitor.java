package dog.wiggler.memory;

public interface LogVisitor<R> {
    R access(long address, int size, AccessType type) throws Throwable;
    
    R elapsedCycles(long elapsedCycles) throws Throwable;

    R end() throws Throwable;

    static LogVisitor<Void> mergeElapsedCycles(LogVisitor<Void> visitor) {
        return new LogVisitor<>() {
            private Long elapsedCycles;

            @Override
            public Void access(long address, int size, AccessType type) throws Throwable {
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

    R userData(long userData) throws Throwable;
}
