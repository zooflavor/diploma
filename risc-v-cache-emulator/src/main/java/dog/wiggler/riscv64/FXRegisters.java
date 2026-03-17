package dog.wiggler.riscv64;

import dog.wiggler.riscv64.abi.HeapAndStack;
import org.jetbrains.annotations.NotNull;

/**
 * Floating point registers.
 */
public class FXRegisters implements Registers {
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
        registers[register]=value;
    }
}
