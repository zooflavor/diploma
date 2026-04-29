package dog.wiggler.emulator;

import dog.wiggler.function.Consumer;
import org.jetbrains.annotations.NotNull;

/**
 * Output abstracts over the standard output.
 */
public interface Output {
    /**
     * Creates an output delegating all values to a consumer.
     */
    static @NotNull Output consumer(
            @NotNull Consumer<? super @NotNull Number> consumer) {
        return new Output() {
            @Override
            public void writeDouble(double value) throws Throwable {
                consumer.accept(value);
            }

            @Override
            public void writeFloat(float value) throws Throwable {
                consumer.accept(value);
            }

            @Override
            public void writeInt16(short value) throws Throwable {
                consumer.accept(value);
            }

            @Override
            public void writeInt32(int value) throws Throwable {
                consumer.accept(value);
            }

            @Override
            public void writeInt64(long value) throws Throwable {
                consumer.accept(value);
            }

            @Override
            public void writeInt8(byte value) throws Throwable {
                consumer.accept(value);
            }
        };
    }

    /**
     * Creates an output throwing an exception on all outputs.
     */
    static @NotNull Output refuse() {
        return consumer(
                (value)->{
                    throw new RuntimeException("no output is accepted");
                });
    }

    void writeDouble(double value) throws Throwable;

    void writeFloat(float value) throws Throwable;

    void writeInt16(short value) throws Throwable;

    void writeInt32(int value) throws Throwable;

    void writeInt64(long value) throws Throwable;

    void writeInt8(byte value) throws Throwable;

    default void writeUint16(short value) throws Throwable {
        writeInt16(value);
    }

    default void writeUint32(int value) throws Throwable {
        writeInt32(value);
    }

    default void writeUint64(long value) throws Throwable {
        writeInt64(value);
    }

    default void writeUint8(byte value) throws Throwable {
        writeInt8(value);
    }
}
