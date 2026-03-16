package dog.wiggler.riscv64;

import dog.wiggler.HeapAndStack;
import org.jetbrains.annotations.NotNull;

/**
 * Helper methods to get and set register values.
 * The HeapAndStack object is used to ensure that the heap and the stack never overlap.
 */
public interface Registers {
    default double getDouble(int register) {
        return Double.longBitsToDouble(getInt64(register));
    }

    default float getFloat(int register) {
        return Float.intBitsToFloat(getInt32(register));
    }

    default short getInt16(int register) {
        return (short)getInt64(register);
    }

    default int getInt32(int register) {
        return (int)getInt64(register);
    }

    long getInt64(int register);

    default byte getInt8(int register) {
        return (byte)getInt64(register);
    }

    default void setDouble(
            @NotNull HeapAndStack heapAndStack,
            int register,
            double value) {
        setInt64(heapAndStack, register, Double.doubleToRawLongBits(value));
    }

    default void setFloat(
            @NotNull HeapAndStack heapAndStack,
            int register,
            float value) {
        setInt32(heapAndStack, register, Float.floatToRawIntBits(value));
    }

    default void setInt16(
            @NotNull HeapAndStack heapAndStack,
            int register,
            short value) {
        setInt64(heapAndStack, register, value&0xffffL);
    }

    default void setInt32(
            @NotNull HeapAndStack heapAndStack,
            int register,
            int value) {
        setInt64(heapAndStack, register, value&0xffffffffL);
    }

    void setInt64(
            @NotNull HeapAndStack heapAndStack,
            int register,
            long value);

    default void setInt8(
            @NotNull HeapAndStack heapAndStack,
            int register,
            byte value) {
        setInt64(heapAndStack, register, value&0xffL);
    }
}
