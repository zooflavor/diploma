package dog.wiggler;

public sealed interface PrimitiveValue<J> {
    record DFloat(double value) implements PrimitiveValue<Double> {
        @Override
        public FunctionCallParameters addTo(FunctionCallParameters parameters) {
            return parameters.addDouble(value);
        }

        @Override
        public Double java() {
            return value;
        }
    }

    record SFloat(float value) implements PrimitiveValue<Float> {
        @Override
        public FunctionCallParameters addTo(FunctionCallParameters parameters) {
            return parameters.addFloat(value);
        }

        @Override
        public Float java() {
            return value;
        }
    }

    record SInt16(short value) implements PrimitiveValue<Short> {
        @Override
        public FunctionCallParameters addTo(FunctionCallParameters parameters) {
            return parameters.addInt16(true, value);
        }

        @Override
        public Short java() {
            return value;
        }
    }

    record SInt32(int value) implements PrimitiveValue<Integer> {
        @Override
        public FunctionCallParameters addTo(FunctionCallParameters parameters) {
            return parameters.addInt32(true, value);
        }

        @Override
        public Integer java() {
            return value;
        }
    }

    record SInt64(long value) implements PrimitiveValue<Long> {
        @Override
        public FunctionCallParameters addTo(FunctionCallParameters parameters) {
            return parameters.addInt64(value);
        }

        @Override
        public Long java() {
            return value;
        }
    }

    record SInt8(byte value) implements PrimitiveValue<Byte> {
        @Override
        public FunctionCallParameters addTo(FunctionCallParameters parameters) {
            return parameters.addInt8(true, value);
        }

        @Override
        public Byte java() {
            return value;
        }
    }

    record UInt16(short value) implements PrimitiveValue<Short> {
        @Override
        public FunctionCallParameters addTo(FunctionCallParameters parameters) {
            return parameters.addInt16(false, value);
        }

        @Override
        public Short java() {
            return value;
        }
    }

    record UInt32(int value) implements PrimitiveValue<Integer> {
        @Override
        public FunctionCallParameters addTo(FunctionCallParameters parameters) {
            return parameters.addInt32(false, value);
        }

        @Override
        public Integer java() {
            return value;
        }
    }

    record UInt64(long value) implements PrimitiveValue<Long> {
        @Override
        public FunctionCallParameters addTo(FunctionCallParameters parameters) {
            return parameters.addInt64(value);
        }

        @Override
        public Long java() {
            return value;
        }
    }

    record UInt8(byte value) implements PrimitiveValue<Byte> {
        @Override
        public FunctionCallParameters addTo(FunctionCallParameters parameters) {
            return parameters.addInt8(false, value);
        }

        @Override
        public Byte java() {
            return value;
        }
    }

    record VoidVoid() implements PrimitiveValue<Void> {
        @Override
        public FunctionCallParameters addTo(FunctionCallParameters parameters) {
            throw new IllegalStateException("voids can not be store neither in memory nor registers");
        }

        @Override
        public Void java() {
            return null;
        }
    }

    FunctionCallParameters addTo(FunctionCallParameters parameters);

    static DFloat dfloat(double value) {
        return new DFloat(value);
    }

    static SFloat sfloat(float value) {
        return new SFloat(value);
    }

    static SInt16 sint16(short value) {
        return new SInt16(value);
    }

    static SInt32 sint32(int value) {
        return new SInt32(value);
    }

    static SInt64 sint64(long value) {
        return new SInt64(value);
    }

    static SInt8 sint8(byte value) {
        return new SInt8(value);
    }

    static UInt16 uint16(short value) {
        return new UInt16(value);
    }

    static UInt32 uint32(int value) {
        return new UInt32(value);
    }

    static UInt64 uint64(long value) {
        return new UInt64(value);
    }

    static UInt8 uint8(byte value) {
        return new UInt8(value);
    }

    static VoidVoid voidVoid() {
        return new VoidVoid();
    }

    J java();
}
