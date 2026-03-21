package dog.wiggler;

import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.util.Objects;

public class MemoryByteChannel implements SeekableByteChannel {
    private final byte @NotNull [] array;
    private int position;

    public MemoryByteChannel(byte @NotNull [] array) {
        this.array=Objects.requireNonNull(array, "array");
    }

    @Override
    public void close() {
    }

    @Override
    public boolean isOpen() {
        return true;
    }

    @Override
    public long position() {
        return position;
    }

    @Override
    public SeekableByteChannel position(long newPosition) {
        if (0L>position) {
            position=0;
        }
        else if (array.length<newPosition) {
            position=array.length;
        }
        else {
            position=(int)newPosition;
        }
        return this;
    }

    @Override
    public int read(ByteBuffer dst) {
        if (!dst.hasRemaining()) {
            return 0;
        }
        if (array.length<=position) {
            return -1;
        }
        dst.put(array[position]);
        ++position;
        return 1;
    }

    @Override
    public long size() {
        return array.length;
    }

    @Override
    public SeekableByteChannel truncate(long size) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int write(ByteBuffer src) {
        throw new UnsupportedOperationException();
    }
}
