package dog.wiggler.riscv64;

import dog.wiggler.riscv64.abi.HeapAndStack;
import dog.wiggler.riscv64.abi.ABI;
import org.jetbrains.annotations.NotNull;

/**
 * Integer registers.
 * Register 0 stays 0 forever.
 */
public class XRegisters implements Registers {
    private final long @NotNull [] registers=new long[32];

    @Override
    public long getInt64(int register) {
        return registers[register];
    }

    @Override
    public void setInt64(
            @NotNull HeapAndStack heapAndStack,
            int register,
            long value) {
        if (0!=register) {
            if (ABI.REGISTER_SP==register) {
                heapAndStack.checkStackPointer(value);
            }
            registers[register]=value;
        }
    }
}
