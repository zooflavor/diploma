package dog.wiggler;

import dog.wiggler.function.Supplier;

public interface Input {
    double readDouble() throws Throwable;

    float readFloat() throws Throwable;

    short readInt16() throws Throwable;

    int readInt32() throws Throwable;

    long readInt64() throws Throwable;

    byte readInt8() throws Throwable;

    default short readUint16() throws Throwable {
        return readInt16();
    }

    default int readUint32() throws Throwable {
        return readInt32();
    }

    default long readUint64() throws Throwable {
        return readInt64();
    }

    default byte readUint8() throws Throwable {
        return readInt8();
    }

    static Input supplier(Supplier<? extends Number> supplier) {
        return new Input() {
            @Override
            public double readDouble() throws Throwable {
                return (Double)supplier.get();
            }

            @Override
            public float readFloat() throws Throwable {
                return (Float)supplier.get();
            }

            @Override
            public short readInt16() throws Throwable {
                return (Short)supplier.get();
            }

            @Override
            public int readInt32() throws Throwable {
                return (Integer)supplier.get();
            }

            @Override
            public long readInt64() throws Throwable {
                return (Long)supplier.get();
            }

            @Override
            public byte readInt8() throws Throwable {
                return (Byte)supplier.get();
            }
        };
    }
}
