package dog.wiggler.memory;

import java.io.IOException;
import java.util.Objects;

/**
 * Wraps a memory object. Logs every access to the standard output,
 * and then delegates the access to the memory object.
 */
@Deprecated
public class DebugMemory implements Memory {
    private final Memory memory;

    public DebugMemory(Memory memory) {
        this.memory=Objects.requireNonNull(memory, "memory");
    }

    @Override
    public void close() throws IOException {
        memory.close();
    }

    @Override
    public double loadDouble(long address) throws Throwable {
        System.out.printf("loadDouble(address: %1$012x)%n", address);
        double value=memory.loadDouble(address);
        System.out.printf("loadDouble(address: %1$012x, value: %2$f)%n", address, value);
        return value;
    }

    @Override
    public float loadFloat(long address) throws Throwable {
        System.out.printf("loadFloat(address: %1$012x)%n", address);
        float value=memory.loadFloat(address);
        System.out.printf("loadFloat(address: %1$012x, value: %2$f)%n", address, value);
        return value;
    }

    @Override
    public short loadInt16(long address) throws Throwable {
        System.out.printf("loadUint16(address: %1$012x)%n", address);
        short value=memory.loadInt16(address);
        System.out.printf("loadUint16(address: %1$012x, value: %2$04x)%n", address, value);
        return value;
    }

    @Override
    public int loadInt32(long address, boolean instruction) throws Throwable {
        System.out.printf("loadUint32(address: %1$012x)%n", address);
        int value=memory.loadInt32(address, instruction);
        System.out.printf("loadUint32(address: %1$012x, value: %2$08x)%n", address, value);
        return value;
    }

    @Override
    public long loadInt64(long address) throws Throwable {
        System.out.printf("loadUint64(address: %1$012x)%n", address);
        long value=memory.loadInt64(address);
        System.out.printf("loadUint64(address: %1$012x, value: %2$016x)%n", address, value);
        return value;
    }

    @Override
    public byte loadInt8(long address) throws Throwable {
        System.out.printf("loadUint8(address: %1$012x)%n", address);
        byte value=memory.loadInt8(address);
        System.out.printf("loadUint8(address: %1$012x, value: %2$02x)%n", address, value);
        return value;
    }

    @Override
    public long size() {
        return memory.size();
    }

    @Override
    public void storeDouble(long address, double value) throws Throwable {
        System.out.printf("storeDouble(address: %1$012x, value: %2$f)%n", address, value);
        memory.storeDouble(address, value);
    }

    @Override
    public void storeFloat(long address, float value) throws Throwable {
        System.out.printf("storeFloat(address: %1$012x, value: %2$f)%n", address, value);
        memory.storeFloat(address, value);
    }

    @Override
    public void storeInt16(long address, short value) throws Throwable {
        System.out.printf("storeUint16(address: %1$012x, value: %2$04x)%n", address, value);
        memory.storeInt16(address, value);
    }

    @Override
    public void storeInt32(long address, int value) throws Throwable {
        System.out.printf("storeUint32(address: %1$012x, value: %2$08x)%n", address, value);
        memory.storeInt32(address, value);
    }

    @Override
    public void storeInt64(long address, long value) throws Throwable {
        System.out.printf("storeUint64(address: %1$012x, value: %2$016x)%n", address, value);
        memory.storeInt64(address, value);
    }

    @Override
    public void storeInt8(long address, byte value) throws Throwable {
        System.out.printf("storeUint8(address: %1$012x, value: %2$02x)%n", address, value);
        memory.storeInt8(address, value);
    }
}
