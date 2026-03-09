package dog.wiggler;

import dog.wiggler.memory.Memory;
import dog.wiggler.riscv64.Hart;

public abstract class PrimitiveType<J, V extends PrimitiveValue<J>> {
    public static final PrimitiveType<Double, PrimitiveValue.DFloat> DFLOAT=new PrimitiveType<>() {
        @Override
        public PrimitiveValue.DFloat functionCallResult(Hart hart) {
            return PrimitiveValue.dfloat(hart.registersFxs.getDouble(Hart.REGISTER_FA0));
        }

        @Override
        public PrimitiveValue.DFloat load(Memory memory, long address) throws Throwable {
            return PrimitiveValue.dfloat(memory.loadDouble(address));
        }

        @Override
        public PrimitiveValue.DFloat java(Double value) {
            return PrimitiveValue.dfloat(value);
        }

        @Override
        public void store(Memory memory, long address, PrimitiveValue.DFloat value) throws Throwable {
            memory.storeDouble(address, value.value());
        }

        @Override
        public int size() {
            return 8;
        }
    };

    public static final PrimitiveType<Float, PrimitiveValue.SFloat> SFLOAT=new PrimitiveType<>() {
        @Override
        public PrimitiveValue.SFloat functionCallResult(Hart hart) {
            return PrimitiveValue.sfloat(hart.registersFxs.getFloat(Hart.REGISTER_FA0));
        }

        @Override
        public PrimitiveValue.SFloat load(Memory memory, long address) throws Throwable {
            return PrimitiveValue.sfloat(memory.loadFloat(address));
        }

        @Override
        public PrimitiveValue.SFloat java(Float value) {
            return PrimitiveValue.sfloat(value);
        }

        @Override
        public void store(Memory memory, long address, PrimitiveValue.SFloat value) throws Throwable {
            memory.storeFloat(address, value.value());
        }

        @Override
        public int size() {
            return 4;
        }
    };

    public static final PrimitiveType<Short, PrimitiveValue.SInt16> SINT16=new PrimitiveType<>() {
        @Override
        public PrimitiveValue.SInt16 functionCallResult(Hart hart) {
            return PrimitiveValue.sint16(hart.registersXs.getInt16(Hart.REGISTER_A0));
        }

        @Override
        public PrimitiveValue.SInt16 load(Memory memory, long address) throws Throwable {
            return PrimitiveValue.sint16(memory.loadInt16(address));
        }

        @Override
        public PrimitiveValue.SInt16 java(Short value) {
            return PrimitiveValue.sint16(value);
        }

        @Override
        public void store(Memory memory, long address, PrimitiveValue.SInt16 value) throws Throwable {
            memory.storeInt16(address, value.value());
        }

        @Override
        public int size() {
            return 2;
        }
    };

    public static final PrimitiveType<Integer, PrimitiveValue.SInt32> SINT32=new PrimitiveType<>() {
        @Override
        public PrimitiveValue.SInt32 functionCallResult(Hart hart) {
            return PrimitiveValue.sint32(hart.registersXs.getInt32(Hart.REGISTER_A0));
        }

        @Override
        public PrimitiveValue.SInt32 load(Memory memory, long address) throws Throwable {
            return PrimitiveValue.sint32(memory.loadInt32(address, false));
        }

        @Override
        public PrimitiveValue.SInt32 java(Integer value) {
            return PrimitiveValue.sint32(value);
        }

        @Override
        public void store(Memory memory, long address, PrimitiveValue.SInt32 value) throws Throwable {
            memory.storeInt32(address, value.value());
        }

        @Override
        public int size() {
            return 4;
        }
    };

    public static final PrimitiveType<Long, PrimitiveValue.SInt64> SINT64=new PrimitiveType<>() {
        @Override
        public PrimitiveValue.SInt64 functionCallResult(Hart hart) {
            return PrimitiveValue.sint64(hart.registersXs.getInt64(Hart.REGISTER_A0));
        }

        @Override
        public PrimitiveValue.SInt64 load(Memory memory, long address) throws Throwable {
            return PrimitiveValue.sint64(memory.loadInt64(address));
        }

        @Override
        public PrimitiveValue.SInt64 java(Long value) {
            return PrimitiveValue.sint64(value);
        }

        @Override
        public void store(Memory memory, long address, PrimitiveValue.SInt64 value) throws Throwable {
            memory.storeInt64(address, value.value());
        }

        @Override
        public int size() {
            return 8;
        }
    };

    public static final PrimitiveType<Byte, PrimitiveValue.SInt8> SINT8=new PrimitiveType<>() {
        @Override
        public PrimitiveValue.SInt8 functionCallResult(Hart hart) {
            return PrimitiveValue.sint8(hart.registersXs.getInt8(Hart.REGISTER_A0));
        }

        @Override
        public PrimitiveValue.SInt8 load(Memory memory, long address) throws Throwable {
            return PrimitiveValue.sint8(memory.loadInt8(address));
        }

        @Override
        public PrimitiveValue.SInt8 java(Byte value) {
            return PrimitiveValue.sint8(value);
        }

        @Override
        public void store(Memory memory, long address, PrimitiveValue.SInt8 value) throws Throwable {
            memory.storeInt8(address, value.value());
        }

        @Override
        public int size() {
            return 1;
        }
    };

    public static final PrimitiveType<Short, PrimitiveValue.UInt16> UINT16=new PrimitiveType<>() {
        @Override
        public PrimitiveValue.UInt16 functionCallResult(Hart hart) {
            return PrimitiveValue.uint16(hart.registersXs.getInt16(Hart.REGISTER_A0));
        }

        @Override
        public PrimitiveValue.UInt16 load(Memory memory, long address) throws Throwable {
            return PrimitiveValue.uint16(memory.loadInt16(address));
        }

        @Override
        public PrimitiveValue.UInt16 java(Short value) {
            return PrimitiveValue.uint16(value);
        }

        @Override
        public void store(Memory memory, long address, PrimitiveValue.UInt16 value) throws Throwable {
            memory.storeInt16(address, value.value());
        }

        @Override
        public int size() {
            return 2;
        }
    };

    public static final PrimitiveType<Integer, PrimitiveValue.UInt32> UINT32=new PrimitiveType<>() {
        @Override
        public PrimitiveValue.UInt32 functionCallResult(Hart hart) {
            return PrimitiveValue.uint32(hart.registersXs.getInt32(Hart.REGISTER_A0));
        }

        @Override
        public PrimitiveValue.UInt32 load(Memory memory, long address) throws Throwable {
            return PrimitiveValue.uint32(memory.loadInt32(address, false));
        }

        @Override
        public PrimitiveValue.UInt32 java(Integer value) {
            return PrimitiveValue.uint32(value);
        }

        @Override
        public void store(Memory memory, long address, PrimitiveValue.UInt32 value) throws Throwable {
            memory.storeInt32(address, value.value());
        }

        @Override
        public int size() {
            return 4;
        }
    };

    public static final PrimitiveType<Long, PrimitiveValue.UInt64> UINT64=new PrimitiveType<>() {
        @Override
        public PrimitiveValue.UInt64 functionCallResult(Hart hart) {
            return PrimitiveValue.uint64(hart.registersXs.getInt64(Hart.REGISTER_A0));
        }

        @Override
        public PrimitiveValue.UInt64 load(Memory memory, long address) throws Throwable {
            return PrimitiveValue.uint64(memory.loadInt64(address));
        }

        @Override
        public PrimitiveValue.UInt64 java(Long value) {
            return PrimitiveValue.uint64(value);
        }

        @Override
        public void store(Memory memory, long address, PrimitiveValue.UInt64 value) throws Throwable {
            memory.storeInt64(address, value.value());
        }

        @Override
        public int size() {
            return 8;
        }
    };

    public static final PrimitiveType<Byte, PrimitiveValue.UInt8> UINT8=new PrimitiveType<>() {
        @Override
        public PrimitiveValue.UInt8 functionCallResult(Hart hart) {
            return PrimitiveValue.uint8(hart.registersXs.getInt8(Hart.REGISTER_A0));
        }

        @Override
        public PrimitiveValue.UInt8 load(Memory memory, long address) throws Throwable {
            return PrimitiveValue.uint8(memory.loadInt8(address));
        }

        @Override
        public PrimitiveValue.UInt8 java(Byte value) {
            return PrimitiveValue.uint8(value);
        }

        @Override
        public void store(Memory memory, long address, PrimitiveValue.UInt8 value) throws Throwable {
            memory.storeInt8(address, value.value());
        }

        @Override
        public int size() {
            return 1;
        }
    };

    public static final PrimitiveType<Void, PrimitiveValue.VoidVoid> VOID=new PrimitiveType<>() {
        @Override
        public PrimitiveValue.VoidVoid functionCallResult(Hart hart) {
            return new PrimitiveValue.VoidVoid();
        }

        @Override
        public PrimitiveValue.VoidVoid load(Memory memory, long address) {
            throw new IllegalStateException("voids can not be store in memory");
        }

        @Override
        public PrimitiveValue.VoidVoid java(Void value) {
            return new PrimitiveValue.VoidVoid();
        }

        @Override
        public void store(Memory memory, long address, PrimitiveValue.VoidVoid value) {
            throw new IllegalStateException("voids can not be store in memory");
        }

        @Override
        public int size() {
            return 0;
        }
    };

    private PrimitiveType() {
    }

    public abstract V functionCallResult(Hart hart);

    public abstract V load(Memory memory, long address) throws Throwable;

    public abstract V java(J value);

    public abstract void store(Memory memory, long address, V value) throws Throwable;

    public abstract int size();
}
