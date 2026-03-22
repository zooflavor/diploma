package dog.wiggler.riscv64;

import dog.wiggler.memory.MemoryLog;
import dog.wiggler.memory.MisalignedMemoryAccessException;
import dog.wiggler.riscv64.abi.ABI;
import dog.wiggler.riscv64.abi.HeapAndStack;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * A hart is an execution unit of a risc-v processor.
 * It contains a full set of registers: a program counter, 32 integer registers, 32 floating point registers.
 */
public class Hart {
    private long elapsedCycles;
    public final @NotNull Registers fxRegisters=new FXRegisters();
    private final @NotNull Instructions instructions=RV64IMFD.create();
    private long registerPc;
    public final @NotNull Registers xRegisters=new XRegisters();

    public long getPc() {
        return registerPc;
    }

    public long getReturnAddress() {
        return xRegisters.getInt64(ABI.REGISTER_RA);
    }

    public void incPc() {
        setPc(nextPc());
    }

    public long nextPc() {
        return registerPc+4L;
    }

    /**
     * Sets the program counter to {@code startAddress},
     * the return address to {@code returnAddress},
     * and the stack pointer to {@code stackEndAddress}.
     */
    public void reset(
            @NotNull HeapAndStack heapAndStack,
            long returnAddress,
            long startAddress) {
        elapsedCycles=0;
        setPc(startAddress);
        setReturnAddress(heapAndStack, returnAddress);
    }

    public void setPc(long value) {
        registerPc=value;
    }

    public void setReturnAddress(
            @NotNull HeapAndStack heapAndStack,
            long value) {
        xRegisters.setInt64(heapAndStack, ABI.REGISTER_RA, value);
    }

    /**
     * Executes one instruction.
     * If the program counter points to a trap, it calls the trap.
     * The trap must manipulate all the registers and memory, including the program counter.
     * If there's no trap for the address of the program counter,
     * it reads an instruction from memory,
     * decodes it, and executes it.
     * In all cases, the elapsed cycles increase by one.
     */
    public void step(
            @NotNull HeapAndStack heapAndStack,
            @NotNull MemoryLog memoryLog,
            @NotNull Map<@NotNull Long, @NotNull Trap> traps)
            throws Throwable {
        if (0L!=(registerPc&3)) {
            throw new MisalignedMemoryAccessException(
                    "register pc %016x is not aligned to 4 bytes"
                            .formatted(registerPc));
        }
        ++elapsedCycles;
        Trap trap=traps.get(registerPc);
        if (null!=trap) {
            trap.triggered(this, heapAndStack, memoryLog);
            return;
        }
        int instruction=memoryLog.loadInt32(registerPc, true);
        instructions.execute(this, heapAndStack, instruction, memoryLog);
        memoryLog.elapsedCycles(elapsedCycles);
    }
}
