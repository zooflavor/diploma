package dog.wiggler.riscv64;

import dog.wiggler.HeapAndStack;
import dog.wiggler.memory.IllegalMemoryAccessException;
import dog.wiggler.memory.MemoryLog;

import java.util.Map;
import java.util.Objects;

public class Hart {
    public static final int REGISTER_A0=10;
    public static final int REGISTER_FA0=10;
    public static final int REGISTER_RA=1;
    public static final int REGISTER_SP=2;

    private final class RegistersFxs implements Registers {
        @Override
        public long getInt64(int register) {
            return registerFxs[register];
        }

        @Override
        public void setInt64(int register, long value) {
            registerFxs[register]=value;
        }
    }

    private final class RegistersXs implements Registers {
        @Override
        public long getInt64(int register) {
            return (0==register)
                    ?0
                    :registerXs[register-1];
        }

        @Override
        public void setInt64(int register, long value) {
            if (0!=register) {
                if (REGISTER_SP==register) {
                    stack.checkStackPointer(value);
                }
                registerXs[register-1]=value;
            }
        }
    }

    public long elapsedCycles;
    private final Instructions instructions=new RV64IMFD();
    private long registerPc;
    private final long[] registerFxs=new long[32];
    public final Registers registersFxs=new RegistersFxs();
    public final Registers registersXs=new RegistersXs();
    private final long[] registerXs=new long[31];
    public final HeapAndStack stack;

    public Hart(HeapAndStack stack) {
        this.stack=Objects.requireNonNull(stack, "stack");
    }

    public long getPc() {
        return registerPc;
    }

    public long getReturnAddress() {
        return registersXs.getInt64(REGISTER_RA);
    }

    public void incPc() {
        setPc(nextPc());
    }

    public long nextPc() {
        return registerPc+4L;
    }

    public void reset(long returnAddress, long stackEndAddress, long startAddress) {
        IllegalMemoryAccessException.checkAccess(startAddress);
        IllegalMemoryAccessException.checkAccess(stackEndAddress-8L);
        if (0L!=(stackEndAddress&0xfL)) {
            throw new IllegalMemoryAccessException(
                    "end address of the stack %013x is not aligned to 16 bytes", stackEndAddress);
        }
        elapsedCycles=0;
        setPc(startAddress);
        setReturnAddress(returnAddress);
        stack.setStackPointer(this, stackEndAddress);
    }

    public void setPc(long value) {
        registerPc=value;
    }

    public void setReturnAddress(long value) {
        registersXs.setInt64(REGISTER_RA, value);
    }

    public void step(MemoryLog memoryLog, Map<Long, Trap> traps) throws Throwable {
        IllegalMemoryAccessException.checkAccess(registerPc);
        if (0L!=(registerPc&3)) {
            throw new IllegalMemoryAccessException("register pc %012x is not aligned to 4 bytes", registerPc);
        }
        ++elapsedCycles;
        Trap trap=traps.get(registerPc);
        if (null!=trap) {
            trap.triggered(this, memoryLog);
            return;
        }
        int instruction=memoryLog.loadInt32(registerPc, true);
        int opcode=instruction&0x7f;
        Instruction instruction2=instructions.getInstruction(opcode);
        if (null==instruction2) {
            throw new IllegalInstructionException(
                    "illegal instruction 0x%08x at 0x%012x, opcode: 0x%02x", instruction, registerPc, opcode);
        }
        instruction2.execute(this, instruction, memoryLog, opcode);
        memoryLog.elapsedCycles(elapsedCycles);
    }
}
