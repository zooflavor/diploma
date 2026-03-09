package dog.wiggler;

import dog.wiggler.elf.ELF;
import dog.wiggler.elf.FileHeader;
import dog.wiggler.elf.SectionHeader;
import dog.wiggler.function.Supplier;
import dog.wiggler.memory.Log;
import dog.wiggler.memory.Memory;
import dog.wiggler.memory.MemoryLog;
import dog.wiggler.riscv64.Hart;
import dog.wiggler.riscv64.Trap;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Emulator implements AutoCloseable {
    public FileHeader elfHeader;
    public final Exit exit=new Exit();
    public final Hart hart;
    public final HeapAndStack heapAndStack=new HeapAndStack();
    public long heapStart;
    public final Input input;
    public final MemoryLog memoryLog;
    public final Output output;
    public final Map<Long, Trap> traps;

    private Emulator(Input input, MemoryLog memoryLog, Output output) {
        this.input=Objects.requireNonNull(input, "input");
        this.memoryLog=Objects.requireNonNull(memoryLog, "memoryAccessLog");
        this.output=Objects.requireNonNull(output, "output");
        hart=new Hart(heapAndStack);
        Map<Long, Trap> traps2=new HashMap<>();
        traps2.put(IOMap.MALLOC, Trap.int64Operator((size)->heapAndStack.malloc(hart, size)));
        traps2.put(IOMap.FREE, Trap.int64Consumer(heapAndStack::free));
        traps2.put(IOMap.EXIT, Trap.int32Consumer(exit::set));
        traps2.put(IOMap.EXIT_OK, Trap.runnable(exit::setOk));
        traps2.put(IOMap.MEMORY_ACCESS_LOG_DISABLE, Trap.runnable(this.memoryLog::disableAccessLog));
        traps2.put(IOMap.MEMORY_ACCESS_LOG_ENABLE, Trap.runnable(this.memoryLog::enableAccessLog));
        traps2.put(IOMap.MEMORY_ACCESS_LOG_USER_DATA, Trap.int64Consumer(this.memoryLog::userData));
        traps2.put(IOMap.READ_DOUBLE, Trap.doubleSupplier(input::readDouble));
        traps2.put(IOMap.READ_FLOAT, Trap.floatSupplier(input::readFloat));
        traps2.put(IOMap.READ_INT16, Trap.int16Supplier(input::readInt16));
        traps2.put(IOMap.READ_INT32, Trap.int32Supplier(input::readInt32));
        traps2.put(IOMap.READ_INT64, Trap.int64Supplier(input::readInt64));
        traps2.put(IOMap.READ_INT8, Trap.int8Supplier(input::readInt8));
        traps2.put(IOMap.READ_UINT16, Trap.int16Supplier(input::readUint16));
        traps2.put(IOMap.READ_UINT32, Trap.int32Supplier(input::readUint32));
        traps2.put(IOMap.READ_UINT64, Trap.int64Supplier(input::readUint64));
        traps2.put(IOMap.READ_UINT8, Trap.int8Supplier(input::readUint8));
        traps2.put(IOMap.WRITE_DOUBLE, Trap.doubleConsumer(output::writeDouble));
        traps2.put(IOMap.WRITE_FLOAT, Trap.floatConsumer(output::writeFloat));
        traps2.put(IOMap.WRITE_INT16, Trap.int16Consumer(output::writeInt16));
        traps2.put(IOMap.WRITE_INT32, Trap.int32Consumer(output::writeInt32));
        traps2.put(IOMap.WRITE_INT64, Trap.int64Consumer(output::writeInt64));
        traps2.put(IOMap.WRITE_INT8, Trap.int8Consumer(output::writeInt8));
        traps2.put(IOMap.WRITE_UINT16, Trap.int16Consumer(output::writeUint16));
        traps2.put(IOMap.WRITE_UINT32, Trap.int32Consumer(output::writeUint32));
        traps2.put(IOMap.WRITE_UINT64, Trap.int64Consumer(output::writeUint64));
        traps2.put(IOMap.WRITE_UINT8, Trap.int8Consumer(output::writeUint8));
        traps=Collections.unmodifiableMap(new HashMap<>(traps2));
    }

    @Override
    public void close() throws IOException {
        try {
            memoryLog.end();
        }
        catch (Error|IOException|RuntimeException ex) {
            throw ex;
        }
        catch (Throwable throwable) {
            throw new IOException(throwable);
        }
        finally {
            memoryLog.close();
        }
    }

    public static Supplier<Emulator> factory(
            Input input, Supplier<? extends Log> logFactory, Supplier<? extends Memory> memoryFactory, Output output) {
        return Supplier.factory2(
                (log)->Supplier.factory(
                        (memory)->new Emulator(input, new MemoryLog(log, memory), output),
                        memoryFactory),
                (null==logFactory)
                        ?Log::noOp
                        :logFactory);
    }

    public void loadELFAndReset(Path imageFile) throws Throwable {
        heapStart=0L;
        for (long trap: traps.keySet()) {
            heapStart=Math.max(heapStart, trap+4L);
        }
        try (SeekableByteChannel channel=Files.newByteChannel(imageFile, StandardOpenOption.READ)) {
            elfHeader=ELF.read(channel);
            ByteBuffer buffer=ByteBuffer.allocate(4096);
            for (SectionHeader sectionHeader: elfHeader.sectionHeaders) {
                if (SectionHeader.TYPE_PROGRAM_DATA==sectionHeader.type) {
                    channel.position(sectionHeader.offset);
                    long address=sectionHeader.address;
                    long size=sectionHeader.size;
                    heapStart=Math.max(heapStart, address+size);
                    while (0<size) {
                        buffer.clear();
                        if (0>channel.read(buffer)) {
                            throw new EOFException();
                        }
                        buffer.flip();
                        while (buffer.hasRemaining()) {
                            memoryLog.storeInt8(address, buffer.get());
                            ++address;
                            --size;
                        }
                    }
                }
            }
        }
        reset();
    }

    public void reset() {
        hart.reset(IOMap.EXIT, memoryLog.size(), elfHeader.entryPoint);
        exit.clear();
        heapAndStack.reset(hart, heapStart, memoryLog.size());
        memoryLog.disableAccessLog();
    }

    public void run() throws Throwable {
        while (!exit.set()) {
            hart.step(memoryLog, traps);
        }
    }
}
