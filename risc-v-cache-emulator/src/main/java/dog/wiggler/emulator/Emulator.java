package dog.wiggler.emulator;

import dog.wiggler.Exit;
import dog.wiggler.elf.ELF;
import dog.wiggler.elf.FileHeader;
import dog.wiggler.elf.SectionHeader;
import dog.wiggler.function.Supplier;
import dog.wiggler.memory.Log;
import dog.wiggler.memory.Memory;
import dog.wiggler.memory.MemoryLog;
import dog.wiggler.riscv64.Hart;
import dog.wiggler.riscv64.Trap;
import dog.wiggler.riscv64.abi.ABI;
import dog.wiggler.riscv64.abi.HeapAndStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

/**
 * All the things needed to run a command line program.
 * This has an execution unit, memory, stack, heap, system calls,
 * a way to check termination, an input, and an output.
 * To run a program the object code must be loaded from an ELF file.
 * Resetting the emulator resets the stack,
 * sets the program counter to the entry point of the program,
 * and sets the return address to the exit system call.
 */
public class Emulator implements AutoCloseable {
    /**
     * This is the same value as the address of the text section in the linker script.
     */
    public static final long DEFAULT_ENTRY_POINT=1L<<16;

    private @Nullable FileHeader elfHeader;
    public final @NotNull Exit exit=new Exit();
    public final @NotNull Hart hart;
    public final @NotNull HeapAndStack heapAndStack=new HeapAndStack();
    public long heapStart;
    public final @NotNull Input input;
    public final @NotNull MemoryLog memoryLog;
    public final @NotNull Output output;
    public final @NotNull Map<@NotNull Long, @NotNull Trap> traps;

    private Emulator(
            @NotNull Input input,
            @NotNull MemoryLog memoryLog,
            @NotNull Output output) {
        this.input=Objects.requireNonNull(input, "input");
        this.memoryLog=Objects.requireNonNull(memoryLog, "memoryAccessLog");
        this.output=Objects.requireNonNull(output, "output");
        hart=new Hart();
        Map<Long, Trap> traps2=new HashMap<>();
        traps2.put(IOMap.MALLOC, Emulator.mallocTrap());
        traps2.put(IOMap.FREE, Emulator.freeTrap());
        traps2.put(IOMap.EXIT, Trap.int32Consumer(exit::set));
        traps2.put(IOMap.EXIT_OK, Trap.runnable(exit::setOk));
        traps2.put(IOMap.MEMORY_ACCESS_LOG_DISABLE, Emulator.disableAccessLogTrap());
        traps2.put(IOMap.MEMORY_ACCESS_LOG_ENABLE, Emulator.enableAccessLogTrap());
        traps2.put(IOMap.MEMORY_ACCESS_LOG_USER_DATA, Emulator.logUserDataTrap());
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

    private static @NotNull Trap.Subroutine disableAccessLogTrap() {
        return (hart, heapAndStack, memoryLog)->
                memoryLog.accessLogDisabled();
    }

    public @NotNull FileHeader elfHeader() {
        return Objects.requireNonNull(elfHeader, "elfHeader");
    }

    private static @NotNull Trap.Subroutine enableAccessLogTrap() {
        return (hart, heapAndStack, memoryLog)->
                memoryLog.accessLogEnabled();
    }

    /**
     * Creates an emulator factory from a log and a memory factory.
     * The emulator will use input and output.
     */
    public static @NotNull Supplier<@NotNull Emulator> factory(
            @NotNull Input input,
            @NotNull Supplier<? extends @NotNull Log> logFactory,
            @NotNull Supplier<? extends @NotNull Memory> memoryFactory,
            @NotNull Output output) {
        return Supplier.factory(
                (memoryLog)->
                        new Emulator(input, memoryLog, output),
                MemoryLog.factory(
                        logFactory,
                        memoryFactory));
    }

    private static @NotNull Trap.Subroutine freeTrap() {
        return (hart, heapAndStack, memoryLog)->{
            long address=hart.xRegisters.getInt64(ABI.REGISTER_A0);
            heapAndStack.free(address);
        };
    }

    /**
     * Loads the object code from an ELF file, and resets the emulator.
     */
    public void loadELFAndReset(
            @NotNull SeekableByteChannel channel)
            throws Throwable {
        loadELFAndReset(channel, elfHeader=ELF.read(channel));
    }

    /**
     * Loads the object code from an ELF file, and resets the emulator.
     */
    public void loadELFAndReset(
            @NotNull SeekableByteChannel channel,
            @NotNull FileHeader elfHeader)
            throws Throwable {
        heapStart=0L;
        for (long trap: traps.keySet()) {
            heapStart=Math.max(heapStart, trap+4L);
        }
        ByteBuffer buffer=ByteBuffer.allocate(4096);
        for (SectionHeader sectionHeader: elfHeader.sectionHeaders()) {
            switch (sectionHeader.type()) {
                case SectionHeader.TYPE_NO_BITS -> {
                    long address=sectionHeader.address();
                    long size=sectionHeader.size();
                    heapStart=Math.max(heapStart, address+size);
                    while (0<size) {
                        memoryLog.storeInt8(address, (byte)0);
                        ++address;
                        --size;
                    }
                }
                case SectionHeader.TYPE_PROGRAM_DATA -> {
                    channel.position(sectionHeader.offset());
                    long address=sectionHeader.address();
                    long size=sectionHeader.size();
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

    /**
     * Loads the object code from an ELF file, and resets the emulator.
     */
    public void loadELFAndReset(
            @NotNull Path imageFile)
            throws Throwable {
        try (SeekableByteChannel channel=Files.newByteChannel(imageFile, StandardOpenOption.READ)) {
            loadELFAndReset(channel);
        }
    }

    private static @NotNull Trap.Subroutine logUserDataTrap() {
        return (hart, heapAndStack, memoryLog)->{
            long userData=hart.xRegisters.getInt64(ABI.REGISTER_A0);
            memoryLog.userData(userData);
        };
    }

    private static @NotNull Trap.Subroutine mallocTrap() {
        return (hart, heapAndStack, memoryLog)->{
            long size=hart.xRegisters.getInt64(ABI.REGISTER_A0);
            long result=heapAndStack.malloc(hart, size);
            hart.xRegisters.setInt64(heapAndStack, ABI.REGISTER_A0, result);
        };
    }

    /**
     * Clears the exit code,
     * clears the stack and the heap,
     * sets the program counter to the entry point of the program,
     * sets the return address to the exitOk system call,
     * and disables the memory log.
     */
    public void reset() throws Throwable {
        exit.clear();
        hart.reset(
                heapAndStack,
                IOMap.EXIT_OK,
                (null==elfHeader)
                        ?DEFAULT_ENTRY_POINT
                        :elfHeader.entryPoint());
        heapAndStack.reset(hart, heapStart, memoryLog.size());
        memoryLog.accessLogDisabled();
    }

    /**
     * Runs the program until the exit code is set or an exception is thrown.
     */
    public void run() throws Throwable {
        while (!exit.set()) {
            hart.step(heapAndStack, memoryLog, traps);
        }
    }
}
