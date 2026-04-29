package dog.wiggler.memory;

import dog.wiggler.function.Supplier;
import org.jetbrains.annotations.NotNull;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;

/**
 * Reads a memory access log from a file.
 */
public class LogInputStream implements AutoCloseable {
    private final @NotNull ByteBuffer buffer
            =ByteBuffer.allocate(Logs.PAGE_SIZE)
            .order(ByteOrder.LITTLE_ENDIAN)
            .flip();
    private final @NotNull ReadableByteChannel channel;

    public LogInputStream(
            @NotNull ReadableByteChannel channel) {
        this.channel=Objects.requireNonNull(channel, "channel");
    }

    @Override
    public void close() throws IOException {
        channel.close();
    }

    /**
     * Returns the number of entries in a file.
     */
    public static long entries(@NotNull Path path) throws Throwable {
        return Files.size(path)/8L;
    }

    /**
     * Creates a factory from a channel factory.
     */
    public static @NotNull Supplier<@NotNull LogInputStream> factory(
            @NotNull Supplier<? extends @NotNull ReadableByteChannel> channelFactory) {
        return Supplier.factory(
                LogInputStream::new,
                channelFactory);
    }

    /**
     * Creates a factory for a file.
     */
    public static @NotNull Supplier<@NotNull LogInputStream> factory(
            @NotNull Path path) {
        return factory(
                ()->Files.newByteChannel(
                        path,
                        StandardOpenOption.READ));
    }

    /**
     * Returns true when there's more entries in the log.
     */
    public boolean hasNext() throws IOException {
        if (!buffer.hasRemaining()) {
            buffer.clear();
            while (buffer.hasRemaining()) {
                if (0>channel.read(buffer)) {
                    break;
                }
            }
            buffer.flip();
        }
        return buffer.hasRemaining();
    }

    /**
     * Reads one entry from the log.
     * Throws {@link EOFException} when there's no more entries.
     */
    public long readNext() throws IOException {
        if (hasNext()) {
            return buffer.getLong();
        }
        else {
            throw new EOFException();
        }
    }

    /**
     * Reads one entry from the log, and pattern matches it on the visitor.
     * Calls {@link LogVisitor#end()} when there's no more entries.
     */
    public <R> R readNext(
            @NotNull LogVisitor<R> visitor)
            throws Throwable {
        Objects.requireNonNull(visitor, "visitor");
        if (hasNext()) {
            long log=buffer.getLong();
            return Logs.visit(log, visitor);
        }
        else {
            return visitor.end();
        }
    }
}
