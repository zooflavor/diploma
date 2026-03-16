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
    private static final int BUFFER_BITS=30;
    private static final long BUFFER_MASK=-1L<<BUFFER_BITS;

    private final boolean allowMisalignedAccess;
    private final @NotNull List<@NotNull ByteBuffer> buffers;
    private final @Nullable FileChannel channel;
    private final long size;

    private MemoryMappedMemory(
            boolean allowMisalignedAccess,
            @NotNull List<@NotNull ByteBuffer> buffers,
            @Nullable FileChannel channel,
            long size) {
        this.allowMisalignedAccess=allowMisalignedAccess;
        this.buffers=buffers;
        this.channel=channel;
        this.size=size;
    }

    private @NotNull ByteBuffer buffer(long address) {
        return buffers.get((int)(address >>> BUFFER_BITS));
    }

    private int bufferIndex(long address) {
        return (int)(address&((1L<<BUFFER_BITS)-1L));
    }

    private void check(long address, int size) {
        IllegalMemoryAccessException.checkAccess(address);
        if (address+size-1>=this.size) {
            throw new IllegalMemoryAccessException(
                    "memory access is out of range, address: %016x, access size: %04x, memory size: %016x".formatted(
                            address, size, this.size));
        }
        if ((!allowMisalignedAccess)
                && (!Memory.isAccessAligned(address, size))) {
            throw new MisalignedMemoryAccessException(
                    "misaligned memory access, address: %016x, access size: %04x".formatted(
                            address, size));
        }
    }

    @Override
    public void close() throws IOException {
        try {
            buffers.clear();
        }
        finally {
            if (null!=channel) {
                channel.close();
            }
        }
    }

    public static @NotNull Supplier<@NotNull Memory> factory(
            boolean allowMisalignedAccess,
            @NotNull Path path,
            long size) {
        return Supplier.factory(
                (channel)->{
                    if (channel.size()<size) {
                        channel.truncate(size);
                    }
                    List<ByteBuffer> buffers=new ArrayList<>();
                    for (long position=0L; size>position; position+=1L<<BUFFER_BITS) {
                        buffers.add(
                                channel.map(
                                                FileChannel.MapMode.READ_WRITE,
                                                position,
                                                Math.min(size-position, 1L<<BUFFER_BITS))
                                        .order(ByteOrder.LITTLE_ENDIAN));
                    }
                    return new MemoryMappedMemory(
                            allowMisalignedAccess,
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
            long size) {
        return ()->{
            List<ByteBuffer> buffers=new ArrayList<>();
            for (long position=0L; size>position; position+=1L<<BUFFER_BITS) {
                buffers.add(
                        ByteBuffer.allocate(
                                        (int)Math.min(size-position, 1L<<BUFFER_BITS))
                                .order(ByteOrder.LITTLE_ENDIAN));
            }
            return new MemoryMappedMemory(
                    allowMisalignedAccess,
                    buffers,
                    null,
                    size);
        };
    }

    private long load(long address) {
        return buffer(address).get(bufferIndex(address))&0xffL;
    }

    @Override
    public short loadInt16(long address) {
        check(address, 2);
        if ((address&BUFFER_MASK)==((address+1)&BUFFER_MASK)) {
            return buffer(address).getShort(bufferIndex(address));
        }
        return (short)(load(address)
                |(load(address+1)<<8));
    }

    @Override
    public int loadInt32(long address, boolean instruction) {
        check(address, 4);
        if ((address&BUFFER_MASK)==((address+3)&BUFFER_MASK)) {
            return buffer(address).getInt(bufferIndex(address));
        }
        return (int)(load(address)
                |(load(address+1)<<8)
                |(load(address+2)<<16)
                |(load(address+3)<<24));
    }

    @Override
    public long loadInt64(long address) {
        check(address, 8);
        if ((address&BUFFER_MASK)==((address+7)&BUFFER_MASK)) {
            return buffer(address).getLong(bufferIndex(address));
        }
        return load(address)
                |(load(address+1)<<8)
                |(load(address+2)<<16)
                |(load(address+3)<<24)
                |(load(address+4)<<32)
                |(load(address+5)<<40)
                |(load(address+6)<<48)
                |(load(address+7)<<56);
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
        if ((address&BUFFER_MASK)==((address+1)&BUFFER_MASK)) {
            buffer(address).putShort(bufferIndex(address), value);
            return;
        }
        store(address, value);
        store(address+1, value>>>8);
    }

    @Override
    public void storeInt32(long address, int value) {
        check(address, 4);
        if ((address&BUFFER_MASK)==((address+3)&BUFFER_MASK)) {
            buffer(address).putInt(bufferIndex(address), value);
            return;
        }
        store(address, value);
        store(address+1, value>>>8);
        store(address+2, value>>>16);
        store(address+3, value>>>24);
    }

    @Override
    public void storeInt64(long address, long value) {
        check(address, 8);
        if ((address&BUFFER_MASK)==((address+7)&BUFFER_MASK)) {
            buffer(address).putLong(bufferIndex(address), value);
            return;
        }
        store(address, value);
        store(address+1, value>>>8);
        store(address+2, value>>>16);
        store(address+3, value>>>24);
        store(address+4, value>>>32);
        store(address+5, value>>>40);
        store(address+6, value>>>48);
        store(address+7, value>>>56);
    }

    @Override
    public void storeInt8(long address, byte value) {
        check(address, 1);
        buffer(address).put(bufferIndex(address), value);
    }
}
