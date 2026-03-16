package dog.wiggler;

import dog.wiggler.memory.Memory;
import dog.wiggler.riscv64.ABI;
import dog.wiggler.riscv64.Hart;
import org.jetbrains.annotations.NotNull;

public abstract class PrimitiveType<J, V extends PrimitiveValue<J>> {
    public static final PrimitiveType<@NotNull Double, PrimitiveValue.DFloat> DFLOAT=new PrimitiveType<>() {
        @Override
        public @NotNull PrimitiveValue.DFloat functionCallResult(
                @NotNull Hart hart) {
            return PrimitiveValue.dfloat(hart.fxRegisters.getDouble(ABI.REGISTER_FA0));
        }

        @Override
        public @NotNull PrimitiveValue.DFloat load(
                @NotNull Memory memory,
                long address)
                throws Throwable {
            return PrimitiveValue.dfloat(memory.loadDouble(address));
        }

        @Override
        public @NotNull PrimitiveValue.DFloat java(
                @NotNull Double value) {
            return PrimitiveValue.dfloat(value);
        }

        @Override
        public void store(
                @NotNull Memory memory,
                long address,
                @NotNull PrimitiveValue.DFloat value)
                throws Throwable {
            memory.storeDouble(address, value.value());
        }

        @Override
        public int size() {
            return 8;
        }
    };

    public static final PrimitiveType<@NotNull Float, PrimitiveValue.SFloat> SFLOAT=new PrimitiveType<>() {
        @Override
        public @NotNull PrimitiveValue.SFloat functionCallResult(
                @NotNull Hart hart) {
            return PrimitiveValue.sfloat(hart.fxRegisters.getFloat(ABI.REGISTER_FA0));
        }

        @Override
        public @NotNull PrimitiveValue.SFloat load(
                @NotNull Memory memory,
                long address)
                throws Throwable {
            return PrimitiveValue.sfloat(memory.loadFloat(address));
        }

        @Override
        public @NotNull PrimitiveValue.SFloat java(
                @NotNull Float value) {
            return PrimitiveValue.sfloat(value);
        }

        @Override
        public void store(
                @NotNull Memory memory,
                long address,
                @NotNull PrimitiveValue.SFloat value)
                throws Throwable {
            memory.storeFloat(address, value.value());
        }

        @Override
        public int size() {
            return 4;
        }
    };

    public static final PrimitiveType<@NotNull Short, PrimitiveValue.SInt16> SINT16=new PrimitiveType<>() {
        @Override
        public @NotNull PrimitiveValue.SInt16 functionCallResult(
                @NotNull Hart hart) {
            return PrimitiveValue.sint16(hart.xRegisters.getInt16(ABI.REGISTER_A0));
        }

        @Override
        public @NotNull PrimitiveValue.SInt16 load(
                @NotNull Memory memory,
                long address)
                throws Throwable {
            return PrimitiveValue.sint16(memory.loadInt16(address));
        }

        @Override
        public @NotNull PrimitiveValue.SInt16 java(
                @NotNull Short value) {
            return PrimitiveValue.sint16(value);
        }

        @Override
        public void store(
                @NotNull Memory memory,
                long address,
                @NotNull PrimitiveValue.SInt16 value)
                throws Throwable {
            memory.storeInt16(address, value.value());
        }

        @Override
        public int size() {
            return 2;
        }
    };

    public static final PrimitiveType<@NotNull Integer, PrimitiveValue.SInt32> SINT32=new PrimitiveType<>() {
        @Override
        public @NotNull PrimitiveValue.SInt32 functionCallResult(
                @NotNull Hart hart) {
            return PrimitiveValue.sint32(hart.xRegisters.getInt32(ABI.REGISTER_A0));
        }

        @Override
        public @NotNull PrimitiveValue.SInt32 load(
                @NotNull Memory memory,
                long address)
                throws Throwable {
            return PrimitiveValue.sint32(memory.loadInt32(address, false));
        }

        @Override
        public @NotNull PrimitiveValue.SInt32 java(
                @NotNull Integer value) {
            return PrimitiveValue.sint32(value);
        }

        @Override
        public void store(
                @NotNull Memory memory,
                long address,
                @NotNull PrimitiveValue.SInt32 value)
                throws Throwable {
            memory.storeInt32(address, value.value());
        }

        @Override
        public int size() {
            return 4;
        }
    };

    public static final PrimitiveType<@NotNull Long, PrimitiveValue.SInt64> SINT64=new PrimitiveType<>() {
        @Override
        public @NotNull PrimitiveValue.SInt64 functionCallResult(
                @NotNull Hart hart) {
            return PrimitiveValue.sint64(hart.xRegisters.getInt64(ABI.REGISTER_A0));
        }

        @Override
        public @NotNull PrimitiveValue.SInt64 load(
                @NotNull Memory memory,
                long address)
                throws Throwable {
            return PrimitiveValue.sint64(memory.loadInt64(address));
        }

        @Override
        public @NotNull PrimitiveValue.SInt64 java(
                @NotNull Long value) {
            return PrimitiveValue.sint64(value);
        }

        @Override
        public void store(
                @NotNull Memory memory,
                long address,
                @NotNull PrimitiveValue.SInt64 value)
                throws Throwable {
            memory.storeInt64(address, value.value());
        }

        @Override
        public int size() {
            return 8;
        }
    };

    public static final PrimitiveType<@NotNull Byte, PrimitiveValue.SInt8> SINT8=new PrimitiveType<>() {
        @Override
        public @NotNull PrimitiveValue.SInt8 functionCallResult(
                @NotNull Hart hart) {
            return PrimitiveValue.sint8(hart.xRegisters.getInt8(ABI.REGISTER_A0));
        }

        @Override
        public @NotNull PrimitiveValue.SInt8 load(
                @NotNull Memory memory,
                long address)
                throws Throwable {
            return PrimitiveValue.sint8(memory.loadInt8(address));
        }

        @Override
        public @NotNull PrimitiveValue.SInt8 java(
                @NotNull Byte value) {
            return PrimitiveValue.sint8(value);
        }

        @Override
        public void store(
                @NotNull Memory memory,
                long address,
                @NotNull PrimitiveValue.SInt8 value)
                throws Throwable {
            memory.storeInt8(address, value.value());
        }

        @Override
        public int size() {
            return 1;
        }
    };

    public static final PrimitiveType<@NotNull Short, PrimitiveValue.UInt16> UINT16=new PrimitiveType<>() {
        @Override
        public @NotNull PrimitiveValue.UInt16 functionCallResult(
                @NotNull Hart hart) {
            return PrimitiveValue.uint16(hart.xRegisters.getInt16(ABI.REGISTER_A0));
        }

        @Override
        public @NotNull PrimitiveValue.UInt16 load(
                @NotNull Memory memory,
                long address)
                throws Throwable {
            return PrimitiveValue.uint16(memory.loadInt16(address));
        }

        @Override
        public @NotNull PrimitiveValue.UInt16 java(
                @NotNull Short value) {
            return PrimitiveValue.uint16(value);
        }

        @Override
        public void store(
                @NotNull Memory memory,
                long address,
                @NotNull PrimitiveValue.UInt16 value)
                throws Throwable {
            memory.storeInt16(address, value.value());
        }

        @Override
        public int size() {
            return 2;
        }
    };

    public static final PrimitiveType<@NotNull Integer, PrimitiveValue.UInt32> UINT32=new PrimitiveType<>() {
        @Override
        public @NotNull PrimitiveValue.UInt32 functionCallResult(
                @NotNull Hart hart) {
            return PrimitiveValue.uint32(hart.xRegisters.getInt32(ABI.REGISTER_A0));
        }

        @Override
        public @NotNull PrimitiveValue.UInt32 load(
                @NotNull Memory memory,
                long address)
                throws Throwable {
            return PrimitiveValue.uint32(memory.loadInt32(address, false));
        }

        @Override
        public @NotNull PrimitiveValue.UInt32 java(
                @NotNull Integer value) {
            return PrimitiveValue.uint32(value);
        }

        @Override
        public void store(
                @NotNull Memory memory,
                long address,
                @NotNull PrimitiveValue.UInt32 value)
                throws Throwable {
            memory.storeInt32(address, value.value());
        }

        @Override
        public int size() {
            return 4;
        }
    };

    public static final PrimitiveType<@NotNull Long, PrimitiveValue.UInt64> UINT64=new PrimitiveType<>() {
        @Override
        public @NotNull PrimitiveValue.UInt64 functionCallResult(
                @NotNull Hart hart) {
            return PrimitiveValue.uint64(hart.xRegisters.getInt64(ABI.REGISTER_A0));
        }

        @Override
        public @NotNull PrimitiveValue.UInt64 load(
                @NotNull Memory memory,
                long address)
                throws Throwable {
            return PrimitiveValue.uint64(memory.loadInt64(address));
        }

        @Override
        public @NotNull PrimitiveValue.UInt64 java(
                @NotNull Long value) {
            return PrimitiveValue.uint64(value);
        }

        @Override
        public void store(
                @NotNull Memory memory,
                long address,
                @NotNull PrimitiveValue.UInt64 value) throws Throwable {
            memory.storeInt64(address, value.value());
        }

        @Override
        public int size() {
            return 8;
        }
    };

    public static final PrimitiveType<@NotNull Byte, PrimitiveValue.UInt8> UINT8=new PrimitiveType<>() {
        @Override
        public @NotNull PrimitiveValue.UInt8 functionCallResult(
                @NotNull Hart hart) {
            return PrimitiveValue.uint8(hart.xRegisters.getInt8(ABI.REGISTER_A0));
        }

        @Override
        public @NotNull PrimitiveValue.UInt8 load(
                @NotNull Memory memory,
                long address)
                throws Throwable {
            return PrimitiveValue.uint8(memory.loadInt8(address));
        }

        @Override
        public @NotNull PrimitiveValue.UInt8 java(
                @NotNull Byte value) {
            return PrimitiveValue.uint8(value);
        }

        @Override
        public void store(
                @NotNull Memory memory,
                long address,
                @NotNull PrimitiveValue.UInt8 value)
                throws Throwable {
            memory.storeInt8(address, value.value());
        }

        @Override
        public int size() {
            return 1;
        }
    };

    public static final PrimitiveType<Void, PrimitiveValue.VoidVoid> VOID=new PrimitiveType<>() {
        @Override
        public @NotNull PrimitiveValue.VoidVoid functionCallResult(
                @NotNull Hart hart) {
            return new PrimitiveValue.VoidVoid();
        }

        @Override
        public @NotNull PrimitiveValue.VoidVoid load(
                @NotNull Memory memory,
                long address) {
            throw new IllegalStateException("voids can not be store in memory");
        }

        @Override
        public @NotNull PrimitiveValue.VoidVoid java(
                Void value) {
            return new PrimitiveValue.VoidVoid();
        }

        @Override
        public void store(
                @NotNull Memory memory,
                long address,
                @NotNull PrimitiveValue.VoidVoid value) {
            throw new IllegalStateException("voids can not be store in memory");
        }

        @Override
        public int size() {
            return 0;
        }
    };

    private PrimitiveType() {
    }

    public abstract @NotNull V functionCallResult(
            @NotNull Hart hart);

    public abstract @NotNull V load(
            @NotNull Memory memory,
            long address)
            throws Throwable;

    public abstract @NotNull V java(
            J value);

    public abstract void store(
            @NotNull Memory memory,
            long address,
            @NotNull V value)
            throws Throwable;

    public abstract int size();
}
