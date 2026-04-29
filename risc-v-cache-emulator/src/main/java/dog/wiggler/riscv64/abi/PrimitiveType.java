package dog.wiggler.riscv64.abi;

import dog.wiggler.memory.Memory;
import dog.wiggler.riscv64.Hart;
import org.jetbrains.annotations.NotNull;

/**
 * Describes how primitive types are stored in memory and registers.
 * @param <J> the type corresponding to the primitive type.
 */
public sealed abstract class PrimitiveType<J> {
    public static final class DFloatType
            extends StoredType<@NotNull Double> {
        public static final @NotNull DFloatType INSTANCE=new DFloatType();

        private DFloatType() {
        }

        @Override
        public boolean integral() {
            return false;
        }

        @Override
        public @NotNull Double loadArgument(
                @NotNull Hart hart,
                int register) {
            return hart.fxRegisters.getDouble(register);
        }

        @Override
        public @NotNull Double load(
                @NotNull Memory memory,
                long address)
                throws Throwable {
            return memory.loadDouble(address);
        }

        @Override
        public int size() {
            return 8;
        }

        @Override
        public void store(
                @NotNull Memory memory,
                long address,
                @NotNull Double value)
                throws Throwable {
            memory.storeDouble(address, value);
        }

        @Override
        public void storeArgument(
                @NotNull Hart hart,
                @NotNull HeapAndStack heapAndStack,
                int register,
                @NotNull Double value) {
            hart.fxRegisters.setDouble(heapAndStack, register, value);
        }

        @Override
        public void storeArgument(
                @NotNull Memory memory,
                long address,
                @NotNull Double value)
                throws Throwable {
            memory.storeDouble(address, value);
        }
    }

    public static final class SFloatType
            extends StoredType<@NotNull Float> {
        public static final @NotNull SFloatType INSTANCE=new SFloatType();

        private SFloatType() {
        }

        @Override
        public boolean integral() {
            return false;
        }

        @Override
        public @NotNull Float loadArgument(
                @NotNull Hart hart,
                int register) {
            return hart.fxRegisters.getFloat(register);
        }

        @Override
        public @NotNull Float load(
                @NotNull Memory memory,
                long address)
                throws Throwable {
            return memory.loadFloat(address);
        }

        @Override
        public int size() {
            return 4;
        }

        @Override
        public void store(
                @NotNull Memory memory,
                long address,
                @NotNull Float value)
                throws Throwable {
            memory.storeFloat(address, value);
        }

        @Override
        public void storeArgument(
                @NotNull Hart hart,
                @NotNull HeapAndStack heapAndStack,
                int register,
                @NotNull Float value) {
            hart.fxRegisters.setFloat(heapAndStack, register, value);
        }

        @Override
        public void storeArgument(
                @NotNull Memory memory,
                long address,
                @NotNull Float value)
                throws Throwable {
            memory.storeInt64(address, Float.floatToRawIntBits(value)&0xffffffffL);
        }
    }

    public static final class SInt16Type
            extends StoredType<@NotNull Short> {
        public static final @NotNull SInt16Type INSTANCE=new SInt16Type();

        private SInt16Type() {
        }

        @Override
        public boolean integral() {
            return true;
        }

        @Override
        public @NotNull Short loadArgument(
                @NotNull Hart hart,
                int register) {
            return hart.xRegisters.getInt16(register);
        }

        @Override
        public @NotNull Short load(
                @NotNull Memory memory,
                long address)
                throws Throwable {
            return memory.loadInt16(address);
        }

        @Override
        public int size() {
            return 2;
        }

        @Override
        public void store(
                @NotNull Memory memory,
                long address,
                @NotNull Short value)
                throws Throwable {
            memory.storeInt16(address, value);
        }

        @Override
        public void storeArgument(
                @NotNull Hart hart,
                @NotNull HeapAndStack heapAndStack,
                int register,
                @NotNull Short value) {
            hart.xRegisters.setInt64(heapAndStack, register, value);
        }

        @Override
        public void storeArgument(
                @NotNull Memory memory,
                long address,
                @NotNull Short value)
                throws Throwable {
            memory.storeInt64(address, value);
        }
    }

    public static final class SInt32Type
            extends StoredType<@NotNull Integer> {
        public static final @NotNull SInt32Type INSTANCE=new SInt32Type();

        private SInt32Type() {
        }

        @Override
        public boolean integral() {
            return true;
        }

        @Override
        public @NotNull Integer load(
                @NotNull Memory memory,
                long address)
                throws Throwable {
            return memory.loadInt32(address, false);
        }

        @Override
        public @NotNull Integer loadArgument(@NotNull Hart hart, int register) {
            return hart.xRegisters.getInt32(register);
        }

        @Override
        public int size() {
            return 4;
        }

        @Override
        public void store(
                @NotNull Memory memory,
                long address,
                @NotNull Integer value)
                throws Throwable {
            memory.storeInt32(address, value);
        }

        @Override
        public void storeArgument(
                @NotNull Hart hart,
                @NotNull HeapAndStack heapAndStack,
                int register,
                @NotNull Integer value) {
            hart.xRegisters.setInt64(heapAndStack, register, value);
        }

        @Override
        public void storeArgument(
                @NotNull Memory memory,
                long address,
                @NotNull Integer value)
                throws Throwable {
            memory.storeInt64(address, value);
        }
    }

    public static final class SInt64Type
            extends StoredType<@NotNull Long> {
        public static final @NotNull SInt64Type INSTANCE=new SInt64Type();

        private SInt64Type() {
        }

        @Override
        public boolean integral() {
            return true;
        }

        @Override
        public @NotNull Long load(
                @NotNull Memory memory,
                long address)
                throws Throwable {
            return memory.loadInt64(address);
        }

        @Override
        public @NotNull Long loadArgument(
                @NotNull Hart hart,
                int register) {
            return hart.xRegisters.getInt64(register);
        }

        @Override
        public int size() {
            return 8;
        }

        @Override
        public void store(
                @NotNull Memory memory,
                long address,
                @NotNull Long value)
                throws Throwable {
            memory.storeInt64(address, value);
        }

        @Override
        public void storeArgument(
                @NotNull Hart hart,
                @NotNull HeapAndStack heapAndStack,
                int register,
                @NotNull Long value) {
            hart.xRegisters.setInt64(heapAndStack, register, value);
        }

        @Override
        public void storeArgument(
                @NotNull Memory memory,
                long address,
                @NotNull Long value)
                throws Throwable {
            memory.storeInt64(address, value);
        }
    }

    public static final class SInt8Type
            extends StoredType<@NotNull Byte> {
        public static final @NotNull SInt8Type INSTANCE=new SInt8Type();

        private SInt8Type() {
        }

        @Override
        public boolean integral() {
            return true;
        }

        @Override
        public @NotNull Byte load(
                @NotNull Memory memory,
                long address)
                throws Throwable {
            return memory.loadInt8(address);
        }

        @Override
        public @NotNull Byte loadArgument(
                @NotNull Hart hart,
                int register) {
            return hart.xRegisters.getInt8(register);
        }

        @Override
        public int size() {
            return 1;
        }

        @Override
        public void store(
                @NotNull Memory memory,
                long address,
                @NotNull Byte value)
                throws Throwable {
            memory.storeInt8(address, value);
        }

        @Override
        public void storeArgument(
                @NotNull Hart hart,
                @NotNull HeapAndStack heapAndStack,
                int register,
                @NotNull Byte value) {
            hart.xRegisters.setInt64(heapAndStack, register, value);
        }

        @Override
        public void storeArgument(
                @NotNull Memory memory,
                long address,
                @NotNull Byte value)
                throws Throwable {
            memory.storeInt64(address, value);
        }
    }

    public static sealed abstract class StoredType<J>
            extends PrimitiveType<J> {
        @Override
        public void checkStored() {
        }
    }

    public static final class UInt16Type
            extends StoredType<@NotNull Short> {
        public static final @NotNull UInt16Type INSTANCE=new UInt16Type();

        private UInt16Type() {
        }

        @Override
        public boolean integral() {
            return true;
        }

        @Override
        public @NotNull Short load(
                @NotNull Memory memory,
                long address)
                throws Throwable {
            return memory.loadInt16(address);
        }

        @Override
        public @NotNull Short loadArgument(
                @NotNull Hart hart,
                int register) {
            return hart.xRegisters.getInt16(register);
        }

        @Override
        public int size() {
            return 2;
        }

        @Override
        public void store(
                @NotNull Memory memory,
                long address,
                @NotNull Short value)
                throws Throwable {
            memory.storeInt16(address, value);
        }

        @Override
        public void storeArgument(
                @NotNull Hart hart,
                @NotNull HeapAndStack heapAndStack,
                int register,
                @NotNull Short value) {
            hart.xRegisters.setInt64(heapAndStack, register, value&0xffffL);
        }

        @Override
        public void storeArgument(
                @NotNull Memory memory,
                long address,
                @NotNull Short value)
                throws Throwable {
            memory.storeInt64(address, value&0xffffL);
        }
    }

    public static final class UInt32Type
            extends StoredType<@NotNull Integer> {
        public static final @NotNull UInt32Type INSTANCE=new UInt32Type();

        private UInt32Type() {
        }

        @Override
        public boolean integral() {
            return true;
        }

        @Override
        public @NotNull Integer load(
                @NotNull Memory memory,
                long address)
                throws Throwable {
            return memory.loadInt32(address, false);
        }

        @Override
        public @NotNull Integer loadArgument(
                @NotNull Hart hart,
                int register) {
            return hart.xRegisters.getInt32(register);
        }

        @Override
        public int size() {
            return 4;
        }

        @Override
        public void store(
                @NotNull Memory memory,
                long address,
                @NotNull Integer value)
                throws Throwable {
            memory.storeInt32(address, value);
        }

        @Override
        public void storeArgument(
                @NotNull Hart hart,
                @NotNull HeapAndStack heapAndStack,
                int register,
                @NotNull Integer value) {
            hart.xRegisters.setInt64(heapAndStack, register, value&0xffffffffL);
        }

        @Override
        public void storeArgument(
                @NotNull Memory memory,
                long address,
                @NotNull Integer value)
                throws Throwable {
            memory.storeInt64(address, value&0xffffffffL);
        }
    }

    public static final class UInt64Type
            extends StoredType<@NotNull Long> {
        public static final @NotNull UInt64Type INSTANCE=new UInt64Type();

        private UInt64Type() {
        }

        @Override
        public boolean integral() {
            return true;
        }

        @Override
        public @NotNull Long load(
                @NotNull Memory memory,
                long address)
                throws Throwable {
            return memory.loadInt64(address);
        }

        @Override
        public @NotNull Long loadArgument(
                @NotNull Hart hart,
                int register) {
            return hart.xRegisters.getInt64(register);
        }

        @Override
        public int size() {
            return 8;
        }

        @Override
        public void store(
                @NotNull Memory memory,
                long address,
                @NotNull Long value) throws Throwable {
            memory.storeInt64(address, value);
        }

        @Override
        public void storeArgument(
                @NotNull Hart hart,
                @NotNull HeapAndStack heapAndStack,
                int register,
                @NotNull Long value) {
            hart.xRegisters.setInt64(heapAndStack, register, value);
        }

        @Override
        public void storeArgument(
                @NotNull Memory memory,
                long address,
                @NotNull Long value)
                throws Throwable {
            memory.storeInt64(address, value);
        }
    }

    public static final class UInt8Type
            extends StoredType<@NotNull Byte> {
        public static final @NotNull UInt8Type INSTANCE=new UInt8Type();

        private UInt8Type() {
        }

        @Override
        public boolean integral() {
            return true;
        }

        @Override
        public @NotNull Byte load(
                @NotNull Memory memory,
                long address)
                throws Throwable {
            return memory.loadInt8(address);
        }

        @Override
        public @NotNull Byte loadArgument(
                @NotNull Hart hart,
                int register) {
            return hart.xRegisters.getInt8(register);
        }

        @Override
        public int size() {
            return 1;
        }

        @Override
        public void store(
                @NotNull Memory memory,
                long address,
                @NotNull Byte value)
                throws Throwable {
            memory.storeInt8(address, value);
        }

        @Override
        public void storeArgument(
                @NotNull Hart hart,
                @NotNull HeapAndStack heapAndStack,
                int register,
                @NotNull Byte value) {
            hart.xRegisters.setInt64(heapAndStack, register, value&0xffL);
        }

        @Override
        public void storeArgument(
                @NotNull Memory memory,
                long address,
                @NotNull Byte value)
                throws Throwable {
            memory.storeInt64(address, value&0xffL);
        }
    }

    public static final class VoidType
            extends PrimitiveType<Void> {
        public static final @NotNull VoidType INSTANCE=new VoidType();

        private VoidType() {
        }

        @Override
        public void checkStored() {
            fail();
        }

        private static <T> T fail() {
            throw new UnsupportedOperationException("voids can't be stored");
        }

        @Override
        public Void functionCallResult(@NotNull Hart hart) {
            return null;
        }

        @Override
        public boolean integral() {
            return fail();
        }

        @Override
        public Void load(
                @NotNull Memory memory,
                long address) {
            return fail();
        }

        @Override
        public Void loadArgument(
                @NotNull Hart hart,
                int register) {
            return fail();
        }

        @Override
        public int size() {
            return fail();
        }

        @Override
        public void store(
                @NotNull Memory memory,
                long address,
                Void value) {
            fail();
        }

        @Override
        public void storeArgument(
                @NotNull Hart hart,
                @NotNull HeapAndStack heapAndStack,
                int register,
                Void value) {
            fail();
        }

        @Override
        public void storeArgument(
                @NotNull Memory memory,
                long address,
                Void value) {
            fail();
        }
    }

    /**
     * Throws  an exception when a value of the primitive type cannot be stored.
     */
    public abstract void checkStored();

    public static @NotNull DFloatType dfloat() {
        return DFloatType.INSTANCE;
    }

    /**
     * Returns the result of a function call.
     */
    public J functionCallResult(
            @NotNull Hart hart) {
        return integral()
                ?loadArgument(hart, ABI.REGISTER_A0)
                :loadArgument(hart, ABI.REGISTER_FA0);
    }

    public abstract boolean integral();

    /**
     * Loads a value from memory.
     */
    public abstract J load(
            @NotNull Memory memory,
            long address)
            throws Throwable;

    /**
     * Loads a function call argument from a register.
     */
    public abstract J loadArgument(
            @NotNull Hart hart,
            int register);

    public static @NotNull SFloatType sfloat() {
        return SFloatType.INSTANCE;
    }

    public static @NotNull SInt16Type sint16() {
        return SInt16Type.INSTANCE;
    }

    public static @NotNull SInt32Type sint32() {
        return SInt32Type.INSTANCE;
    }

    public static @NotNull SInt64Type sint64() {
        return SInt64Type.INSTANCE;
    }

    public static @NotNull SInt8Type sint8() {
        return SInt8Type.INSTANCE;
    }

    /**
     * The size of a value of the primitive type in bytes.
     */
    public abstract int size();

    /**
     * Stores a value in memory.
     */
    public abstract void store(
            @NotNull Memory memory,
            long address,
            J value)
            throws Throwable;

    /**
     * Stores a function call argument in a register.
     */
    public abstract void storeArgument(
            @NotNull Hart hart,
            @NotNull HeapAndStack heapAndStack,
            int register,
            J value);

    /**
     * Stores a function call argument in memory.
     */
    public abstract void storeArgument(
            @NotNull Memory memory,
            long address,
            J value)
            throws Throwable;

    public @NotNull PrimitiveValue<J> value(
            J value) {
        return new PrimitiveValue<>(this, value);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName()+"()";
    }
    
    public static @NotNull UInt16Type uint16() {
        return UInt16Type.INSTANCE;
    }

    public static @NotNull UInt32Type uint32() {
        return UInt32Type.INSTANCE;
    }

    public static @NotNull UInt64Type uint64() {
        return UInt64Type.INSTANCE;
    }

    public static @NotNull UInt8Type uint8() {
        return UInt8Type.INSTANCE;
    }

    public static @NotNull VoidType voidType() {
        return VoidType.INSTANCE;
    }
}
