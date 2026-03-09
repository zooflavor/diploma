package dog.wiggler.riscv64;

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

    default void setDouble(int register, double value) {
        setInt64(register, Double.doubleToRawLongBits(value));
    }

    default void setFloat(int register, float value) {
        setInt32(register, Float.floatToRawIntBits(value));
    }

    default void setInt16(int register, short value) {
        setInt64(register, value&0xffffL);
    }

    default void setInt32(int register, int value) {
        setInt64(register, value&0xffffffffL);
    }

    void setInt64(int register, long value);

    default void setInt8(int register, byte value) {
        setInt64(register, value&0xffL);
    }
}
