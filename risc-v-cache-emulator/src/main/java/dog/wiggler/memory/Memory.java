package dog.wiggler.memory;

import java.io.IOException;

/**
 * Interface for the emulator to access memory through.
 */
public interface Memory extends AutoCloseable {
    int ADDRESS_BITS=48;

    @Override
    void close() throws IOException;

    static boolean isAccessAligned(long address, int size) {
        return address==((address&(-size)));
    }

    default double loadDouble(long address) throws Throwable {
        return Double.longBitsToDouble(loadInt64(address));
    }

    default float loadFloat(long address) throws Throwable {
        return Float.intBitsToFloat(loadInt32(address, false));
    }

    short loadInt16(long address) throws Throwable;

    int loadInt32(long address, boolean instruction) throws Throwable;

    long loadInt64(long address) throws Throwable;

    byte loadInt8(long address) throws Throwable;

    /**
     * Returns the size of the memory.
     * Valid memory addresses are from 0 to size()-1, in bytes.
     */
    long size();

    default void storeDouble(long address, double value) throws Throwable {
        storeInt64(address, Double.doubleToRawLongBits(value));
    }

    default void storeFloat(long address, float value) throws Throwable {
        storeInt32(address, Float.floatToRawIntBits(value));
    }

    void storeInt16(long address, short value) throws Throwable;

    void storeInt32(long address, int value) throws Throwable;

    void storeInt64(long address, long value) throws Throwable;

    void storeInt8(long address, byte value) throws Throwable;
}
