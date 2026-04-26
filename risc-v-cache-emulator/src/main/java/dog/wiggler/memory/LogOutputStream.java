package dog.wiggler.memory;

import dog.wiggler.function.Supplier;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;

/**
 * Writes memory access log to a file.
 */
public class LogOutputStream implements Log {
    private final @NotNull ByteBuffer buffer
            =ByteBuffer.allocate(Logs.PAGE_SIZE)
            .order(ByteOrder.LITTLE_ENDIAN);
    private final @NotNull WritableByteChannel channel;

    public LogOutputStream(
            @NotNull WritableByteChannel channel) {
        this.channel=Objects.requireNonNull(channel, "channel");
    }

    @Override
    public Void access(long address, int size, @NotNull AccessType type) throws Throwable {
        writeLog(Logs.encodeAccess(address, size, type));
        return null;
    }

    @Override
    public Void accessLogDisabled() throws Throwable {
        writeLog(Logs.encodeAccessLogDisabled());
        return null;
    }

    @Override
    public Void accessLogEnabled() throws Throwable {
        writeLog(Logs.encodeAccessLogEnabled());
        return null;
    }

    @Override
    public void close() throws IOException {
        try {
            writeBuffer();
        }
        finally {
            channel.close();
        }
    }

    @Override
    public Void elapsedCycles(long elapsedCycles) throws Throwable {
        writeLog(Logs.encodeElapsedCycles(elapsedCycles));
        return null;
    }

    @Override
    public Void end() throws Throwable {
        writeBuffer();
        return null;
    }

    public static @NotNull Supplier<@NotNull LogOutputStream> factory(
            @NotNull Supplier<@NotNull WritableByteChannel> channelFactory) {
        return Supplier.factory(
                LogOutputStream::new,
                channelFactory);
    }

    public static @NotNull Supplier<@NotNull LogOutputStream> factory(
            @NotNull Path path) {
        Objects.requireNonNull(path, "path");
        return factory(
                ()->Files.newByteChannel(
                        path,
                        StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING,
                        StandardOpenOption.WRITE));
    }

    @Override
    public Void userData(long userData) throws Throwable {
        writeLog(Logs.encodeUserData(userData));
        return null;
    }

    private void writeBuffer() throws IOException {
        try {
            buffer.flip();
            while (buffer.hasRemaining()) {
                channel.write(buffer);
            }
        }
        finally {
            buffer.clear();
        }
    }

    private void writeLog(long log) throws IOException {
        if (!buffer.hasRemaining()) {
            writeBuffer();
        }
        buffer.putLong(log);
    }
}
