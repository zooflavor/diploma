package dog.wiggler.memory;

import dog.wiggler.function.Supplier;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;

public class LogInputStream implements AutoCloseable {
    private final ByteBuffer buffer=ByteBuffer.allocate(4096).order(ByteOrder.LITTLE_ENDIAN).flip();
    private final ReadableByteChannel channel;

    public LogInputStream(ReadableByteChannel channel) {
        this.channel=Objects.requireNonNull(channel, "channel");
    }

    @Override
    public void close() throws IOException {
        channel.close();
    }

    public static Supplier<LogInputStream> factory(Path path) {
        return Supplier.factory(
                LogInputStream::new,
                ()->Files.newByteChannel(
                        path,
                        StandardOpenOption.READ));
    }

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

    public long readNext() throws IOException {
        if (hasNext()) {
            return buffer.getLong();
        }
        else {
            throw new EOFException();
        }
    }

    public <R> R readNext(LogVisitor<R> visitor) throws Throwable {
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
