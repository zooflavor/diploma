package dog.wiggler.memory;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Objects;

/**
 * Wraps a memory and a log object.
 * All memory operations are first logged to the log object,
 * and then delegated to the memory object.
 * The logging of the memory access and user data can be turned on and off.
 * The elapsed cycles are always logged.
 */
public class MemoryLog implements Log, Memory {
    private boolean accessLogEnabled;
    private @Nullable Long lastElapsedCycle;
    private final @NotNull Log log;
    private final @NotNull Memory memory;

    public MemoryLog(
            @NotNull Log log,
            @NotNull Memory memory) {
        this.log=Objects.requireNonNull(log, "log");
        this.memory=Objects.requireNonNull(memory, "memory");
    }

    @Override
    public Void access(long address, int size, @NotNull AccessType type) throws Throwable {
        if (accessLogEnabled) {
            logElapsedCycles();
            return log.access(address, size, type);
        }
        return null;
    }

    @Override
    public Void accessLogDisabled() throws Throwable {
        accessLogEnabled=false;
        logElapsedCycles();
        return log.accessLogDisabled();
    }

    @Override
    public Void accessLogEnabled() throws Throwable {
        accessLogEnabled=true;
        logElapsedCycles();
        return log.accessLogEnabled();
    }

    @Override
    public void close() throws IOException {
        try {
            log.close();
        }
        finally {
            memory.close();
        }
    }

    @Override
    public Void elapsedCycles(long elapsedCycles) throws Throwable {
        lastElapsedCycle=elapsedCycles;
        return null;
    }

    @Override
    public Void end() throws Throwable {
        logElapsedCycles();
        return log.end();
    }

    @Override
    public double loadDouble(long address) throws Throwable {
        access(address, 8, AccessType.LOAD_DATA);
        return memory.loadDouble(address);
    }

    @Override
    public float loadFloat(long address) throws Throwable {
        access(address, 4, AccessType.LOAD_DATA);
        return memory.loadFloat(address);
    }

    @Override
    public short loadInt16(long address) throws Throwable {
        access(address, 2, AccessType.LOAD_DATA);
        return memory.loadInt16(address);
    }

    @Override
    public int loadInt32(long address, boolean instruction) throws Throwable {
        access(
                address,
                4,
                instruction
                        ?AccessType.LOAD_INSTRUCTION
                        :AccessType.LOAD_DATA);
        return memory.loadInt32(address, instruction);
    }

    @Override
    public long loadInt64(long address) throws Throwable {
        access(address, 8, AccessType.LOAD_DATA);
        return memory.loadInt64(address);
    }

    @Override
    public byte loadInt8(long address) throws Throwable {
        access(address, 1, AccessType.LOAD_DATA);
        return memory.loadInt8(address);
    }

    private void logElapsedCycles() throws Throwable {
        if (null!=lastElapsedCycle) {
            log.elapsedCycles(lastElapsedCycle);
            lastElapsedCycle=null;
        }
    }

    @Override
    public long size() {
        return memory.size();
    }

    @Override
    public void storeDouble(long address, double value) throws Throwable {
        access(address, 8, AccessType.STORE);
        memory.storeDouble(address, value);
    }

    @Override
    public void storeFloat(long address, float value) throws Throwable {
        access(address, 4, AccessType.STORE);
        memory.storeFloat(address, value);
    }

    @Override
    public void storeInt16(long address, short value) throws Throwable {
        access(address, 2, AccessType.STORE);
        memory.storeInt16(address, value);
    }

    @Override
    public void storeInt32(long address, int value) throws Throwable {
        access(address, 4, AccessType.STORE);
        memory.storeInt32(address, value);
    }

    @Override
    public void storeInt64(long address, long value) throws Throwable {
        access(address, 8, AccessType.STORE);
        memory.storeInt64(address, value);
    }

    @Override
    public void storeInt8(long address, byte value) throws Throwable {
        access(address, 1, AccessType.STORE);
        memory.storeInt8(address, value);
    }

    @Override
    public Void userData(long userData) throws Throwable {
        if (accessLogEnabled) {
            logElapsedCycles();
            return log.userData(userData);
        }
        return null;
    }
}
