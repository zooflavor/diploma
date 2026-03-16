package dog.wiggler;

import org.jetbrains.annotations.NotNull;

public sealed interface PrimitiveValue<J> {
    record DFloat(
            double value)
            implements PrimitiveValue<@NotNull Double> {
        @Override
        public @NotNull FunctionCallParameters addTo(
                @NotNull FunctionCallParameters parameters) {
            return parameters.addDouble(value);
        }

        @Override
        public @NotNull Double java() {
            return value;
        }
    }

    record SFloat(
            float value)
            implements PrimitiveValue<@NotNull Float> {
        @Override
        public @NotNull FunctionCallParameters addTo(
                @NotNull FunctionCallParameters parameters) {
            return parameters.addFloat(value);
        }

        @Override
        public @NotNull Float java() {
            return value;
        }
    }

    record SInt16(
            short value)
            implements PrimitiveValue<@NotNull Short> {
        @Override
        public @NotNull FunctionCallParameters addTo(
                @NotNull FunctionCallParameters parameters) {
            return parameters.addInt16(true, value);
        }

        @Override
        public @NotNull Short java() {
            return value;
        }
    }

    record SInt32(
            int value)
            implements PrimitiveValue<@NotNull Integer> {
        @Override
        public @NotNull FunctionCallParameters addTo(
                @NotNull FunctionCallParameters parameters) {
            return parameters.addInt32(true, value);
        }

        @Override
        public @NotNull Integer java() {
            return value;
        }
    }

    record SInt64(
            long value)
            implements PrimitiveValue<@NotNull Long> {
        @Override
        public @NotNull FunctionCallParameters addTo(
                @NotNull FunctionCallParameters parameters) {
            return parameters.addInt64(value);
        }

        @Override
        public @NotNull Long java() {
            return value;
        }
    }

    record SInt8(
            byte value)
            implements PrimitiveValue<@NotNull Byte> {
        @Override
        public @NotNull FunctionCallParameters addTo(
                @NotNull FunctionCallParameters parameters) {
            return parameters.addInt8(true, value);
        }

        @Override
        public @NotNull Byte java() {
            return value;
        }
    }

    record UInt16(
            short value)
            implements PrimitiveValue<@NotNull Short> {
        @Override
        public @NotNull FunctionCallParameters addTo(
                @NotNull FunctionCallParameters parameters) {
            return parameters.addInt16(false, value);
        }

        @Override
        public @NotNull Short java() {
            return value;
        }
    }

    record UInt32(
            int value)
            implements PrimitiveValue<@NotNull Integer> {
        @Override
        public @NotNull FunctionCallParameters addTo(
                @NotNull FunctionCallParameters parameters) {
            return parameters.addInt32(false, value);
        }

        @Override
        public @NotNull Integer java() {
            return value;
        }
    }

    record UInt64(
            long value)
            implements PrimitiveValue<@NotNull Long> {
        @Override
        public @NotNull FunctionCallParameters addTo(
                @NotNull FunctionCallParameters parameters) {
            return parameters.addInt64(value);
        }

        @Override
        public @NotNull Long java() {
            return value;
        }
    }

    record UInt8(
            byte value)
            implements PrimitiveValue<@NotNull Byte> {
        @Override
        public @NotNull FunctionCallParameters addTo(
                @NotNull FunctionCallParameters parameters) {
            return parameters.addInt8(false, value);
        }

        @Override
        public @NotNull Byte java() {
            return value;
        }
    }

    record VoidVoid()
            implements PrimitiveValue<Void> {
        @Override
        public @NotNull FunctionCallParameters addTo(
                @NotNull FunctionCallParameters parameters) {
            throw new IllegalStateException("voids can not be store neither in memory nor registers");
        }

        @Override
        public Void java() {
            return null;
        }
    }

    @NotNull FunctionCallParameters addTo(
            @NotNull FunctionCallParameters parameters);

    static @NotNull DFloat dfloat(double value) {
        return new DFloat(value);
    }

    static @NotNull SFloat sfloat(float value) {
        return new SFloat(value);
    }

    static @NotNull SInt16 sint16(short value) {
        return new SInt16(value);
    }

    static @NotNull SInt32 sint32(int value) {
        return new SInt32(value);
    }

    static @NotNull SInt64 sint64(long value) {
        return new SInt64(value);
    }

    static @NotNull SInt8 sint8(byte value) {
        return new SInt8(value);
    }

    static @NotNull UInt16 uint16(short value) {
        return new UInt16(value);
    }

    static @NotNull UInt32 uint32(int value) {
        return new UInt32(value);
    }

    static @NotNull UInt64 uint64(long value) {
        return new UInt64(value);
    }

    static @NotNull UInt8 uint8(byte value) {
        return new UInt8(value);
    }

    static @NotNull VoidVoid voidVoid() {
        return new VoidVoid();
    }

    J java();
}
