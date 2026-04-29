package dog.wiggler.emulator;

import dog.wiggler.function.Supplier;
import org.jetbrains.annotations.NotNull;

import java.util.NoSuchElementException;

/**
 * Input abstracts over the standard input.
 * The methods throw {@link NoSuchElementException} when there's no more to read.
 */
public interface Input {
    /**
     * Creates an empty input.
     */
    static @NotNull Input empty() {
        return supplier(
                ()->{
                    throw new NoSuchElementException("no inputs");
                });
    }

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

    /**
     * Creates an input getting all it's values from a {@link Supplier}.
     */
    static @NotNull Input supplier(
            @NotNull Supplier<? extends @NotNull Number> supplier) {
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
