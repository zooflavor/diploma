package dog.wiggler;

import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.util.Arrays;

public class WritableMemoryChannel implements WritableByteChannel {
    private byte @NotNull [] array=new byte[4096];
    private int position;

    @Override
    public void close() {
    }

    @Override
    public boolean isOpen() {
        return true;
    }

    public byte @NotNull [] toByteArray() {
        return Arrays.copyOf(array, position);
    }

    @Override
    public int write(ByteBuffer src) {
        if (!src.hasRemaining()) {
            return 0;
        }
        if (array.length==position) {
            array=Arrays.copyOf(array, 2*array.length);
        }
        array[position]=src.get();
        ++position;
        return 1;
    }
}
