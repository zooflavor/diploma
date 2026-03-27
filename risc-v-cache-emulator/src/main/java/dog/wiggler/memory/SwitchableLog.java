package dog.wiggler.memory;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Objects;

public class SwitchableLog implements Log {
    private boolean accessLogEnabled;
    private final @NotNull Log log;

    public SwitchableLog(@NotNull Log log) {
        this.log=Objects.requireNonNull(log, "log");
    }

    @Override
    public Void access(long address, int size, @NotNull AccessType type) throws Throwable {
        if (accessLogEnabled) {
            return log.access(address, size, type);
        }
        return null;
    }

    @Override
    public Void accessLogDisabled() throws Throwable {
        accessLogEnabled=false;
        return log.accessLogDisabled();
    }

    @Override
    public Void accessLogEnabled() throws Throwable {
        accessLogEnabled=true;
        return log.accessLogEnabled();
    }

    @Override
    public void close() throws IOException {
        log.close();
    }

    @Override
    public Void elapsedCycles(long elapsedCycles) throws Throwable {
        return log.elapsedCycles(elapsedCycles);
    }

    @Override
    public Void end() throws Throwable {
        return log.end();
    }

    @Override
    public Void userData(long userData) throws Throwable {
        if (accessLogEnabled) {
            return log.userData(userData);
        }
        return null;
    }
}
