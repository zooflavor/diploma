package dog.wiggler.memory;

import dog.wiggler.function.Supplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Memory implementation using heap memory or a file mapped to memory.
 */
public class MemoryMappedMemory implements Memory {
    private static final int DEFAULT_BUFFER_BITS=30;

    private final boolean allowMisalignedAccess;
    private final int bufferBits;
    private final long bufferIndexMask;
    private final @NotNull List<@NotNull ByteBuffer> buffers;
    private final @Nullable FileChannel channel;
    private final long size;

    private MemoryMappedMemory(
            boolean allowMisalignedAccess,
            int bufferBits,
            @NotNull List<@NotNull ByteBuffer> buffers,
            @Nullable FileChannel channel,
            long size) {
        this.allowMisalignedAccess=allowMisalignedAccess;
        this.bufferBits=bufferBits;
        this.buffers=buffers;
        this.channel=channel;
        this.size=size;
        bufferIndexMask=(1L<<bufferBits)-1L;
    }

    private @NotNull ByteBuffer buffer(long address) {
        return buffers.get((int)(address >>> bufferBits));
    }

    private int bufferIndex(long address) {
        return (int)(address&bufferIndexMask);
    }

    private void check(long address, int size) {
        if ((0L>address)
                || (this.size<=address+size-1L)) {
            throw new IllegalMemoryAccessException(
                    "memory access is out of range, address: %016x, access size: %04x, memory size: %016x"
                            .formatted(address, size, this.size));
        }
        if ((!allowMisalignedAccess)
                && (!Memory.isAccessAligned(address, size))) {
            throw new MisalignedMemoryAccessException(
                    "misaligned memory access, address: %016x, access size: %04x"
                            .formatted(address, size));
        }
    }

    private static void checkBufferBits(int bufferBits) {
        if ((0>bufferBits) || (31<bufferBits)) {
            throw new IllegalArgumentException("buffer bits: %d".formatted(bufferBits));
        }
    }

    @Override
    public void close() throws IOException {
        buffers.clear();
        if (null!=channel) {
            channel.close();
        }
    }

    public static @NotNull Supplier<@NotNull Memory> factory(
            boolean allowMisalignedAccess,
            int bufferBits,
            @NotNull Path path,
            long size) {
        checkBufferBits(bufferBits);
        return Supplier.factory(
                (channel)->{
                    if (channel.size()<size) {
                        channel.truncate(size);
                    }
                    List<ByteBuffer> buffers=new ArrayList<>();
                    for (long position=0L; size>position; position+=1L<<bufferBits) {
                        buffers.add(
                                channel.map(
                                                FileChannel.MapMode.READ_WRITE,
                                                position,
                                                Math.min(size-position, 1L<<bufferBits))
                                        .order(ByteOrder.LITTLE_ENDIAN));
                    }
                    return new MemoryMappedMemory(
                            allowMisalignedAccess,
                            bufferBits,
                            buffers,
                            channel,
                            size);
                },
                ()->FileChannel.open(
                        path,
                        StandardOpenOption.CREATE,
                        StandardOpenOption.READ,
                        StandardOpenOption.WRITE));
    }

    public static @NotNull Supplier<@NotNull Memory> factory(
            boolean allowMisalignedAccess,
            int bufferBits,
            long size) {
        checkBufferBits(bufferBits);
        return ()->{
            List<ByteBuffer> buffers=new ArrayList<>();
            for (long position=0L; size>position; position+=1L<<bufferBits) {
                buffers.add(
                        ByteBuffer.allocate(
                                        (int)Math.min(size-position, 1L<<bufferBits))
                                .order(ByteOrder.LITTLE_ENDIAN));
            }
            return new MemoryMappedMemory(
                    allowMisalignedAccess,
                    bufferBits,
                    buffers,
                    null,
                    size);
        };
    }

    public static @NotNull Supplier<@NotNull Memory> factory(
            boolean allowMisalignedAccess,
            @NotNull Path path,
            long size) {
        return factory(allowMisalignedAccess, DEFAULT_BUFFER_BITS, path, size);
    }

    public static @NotNull Supplier<@NotNull Memory> factory(
            boolean allowMisalignedAccess,
            long size) {
        return factory(allowMisalignedAccess, DEFAULT_BUFFER_BITS, size);
    }

    private long load(long address) {
        return buffer(address).get(bufferIndex(address))&0xffL;
    }

    @Override
    public short loadInt16(long address) {
        check(address, 2);
        if ((address>>>bufferBits)==((address+1L)>>>bufferBits)) {
            return buffer(address).getShort(bufferIndex(address));
        }
        return (short)(load(address)
                |(load(address+1L)<<8));
    }

    @Override
    public int loadInt32(long address, boolean instruction) {
        check(address, 4);
        if ((address>>>bufferBits)==((address+3L)>>>bufferBits)) {
            return buffer(address).getInt(bufferIndex(address));
        }
        return (int)(load(address)
                |(load(address+1L)<<8)
                |(load(address+2L)<<16)
                |(load(address+3L)<<24));
    }

    @Override
    public long loadInt64(long address) {
        check(address, 8);
        if ((address>>>bufferBits)==((address+7L)>>>bufferBits)) {
            return buffer(address).getLong(bufferIndex(address));
        }
        return load(address)
                |(load(address+1L)<<8)
                |(load(address+2L)<<16)
                |(load(address+3L)<<24)
                |(load(address+4L)<<32)
                |(load(address+5L)<<40)
                |(load(address+6L)<<48)
                |(load(address+7L)<<56);
    }

    @Override
    public byte loadInt8(long address) {
        check(address, 1);
        return buffer(address).get(bufferIndex(address));
    }

    @Override
    public long size() {
        return size;
    }

    private void store(long address, long value) {
        buffer(address).put(bufferIndex(address), (byte)value);
    }

    @Override
    public void storeInt16(long address, short value) {
        check(address, 2);
        if ((address>>>bufferBits)==((address+1L)>>>bufferBits)) {
            buffer(address).putShort(bufferIndex(address), value);
            return;
        }
        store(address, value);
        store(address+1L, value>>>8);
    }

    @Override
    public void storeInt32(long address, int value) {
        check(address, 4);
        if ((address>>>bufferBits)==((address+3L)>>>bufferBits)) {
            buffer(address).putInt(bufferIndex(address), value);
            return;
        }
        store(address, value);
        store(address+1L, value>>>8);
        store(address+2L, value>>>16);
        store(address+3L, value>>>24);
    }

    @Override
    public void storeInt64(long address, long value) {
        check(address, 8);
        if ((address>>>bufferBits)==((address+7L)>>>bufferBits)) {
            buffer(address).putLong(bufferIndex(address), value);
            return;
        }
        store(address, value);
        store(address+1L, value>>>8);
        store(address+2L, value>>>16);
        store(address+3L, value>>>24);
        store(address+4L, value>>>32);
        store(address+5L, value>>>40);
        store(address+6L, value>>>48);
        store(address+7L, value>>>56);
    }

    @Override
    public void storeInt8(long address, byte value) {
        check(address, 1);
        buffer(address).put(bufferIndex(address), value);
    }
}
