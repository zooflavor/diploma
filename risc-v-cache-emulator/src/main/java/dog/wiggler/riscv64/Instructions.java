package dog.wiggler.riscv64;

import dog.wiggler.HeapAndStack;
import dog.wiggler.memory.MemoryLog;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

/**
 * Instruction decoding for risc-v.
 * The 7 least significant bits are the opcode.
 * @see <a href="https://docs.riscv.org/reference/isa/index.html">https://docs.riscv.org/reference/isa/index.html</a>
 */
public class Instructions {
    public static class Builder {
        private final @Nullable Instruction @NotNull [] instructions=new Instruction[128];

        private Builder() {
        }

        public @NotNull Builder add(
                int opcode,
                @NotNull Instruction.Type instruction) {
            if (null!=instructions[opcode]) {
                throw new IllegalStateException(String.format("opcode %02x is already defined", opcode));
            }
            instructions[opcode]=instruction.instruction();
            return this;
        }

        public @NotNull Instructions build() {
            return new Instructions(Arrays.copyOf(instructions, instructions.length));
        }
    }

    private final @Nullable Instruction @NotNull [] instructions;

    private Instructions(@Nullable Instruction @NotNull [] instructions) {
        this.instructions=instructions;
    }

    public static @NotNull Builder builder() {
        return new Builder();
    }

    public void execute(
            @NotNull Hart hart,
            @NotNull HeapAndStack heapAndStack,
            int instruction,
            @NotNull MemoryLog memoryLog)
            throws Throwable {
        int opcode=instruction&0x7f;
        var instruction2=instructions[opcode];
        if (null==instruction2) {
            throw new IllegalInstructionException(
                    "illegal instruction 0x%08x at 0x%012x, opcode: 0x%02x"
                            .formatted(instruction, hart.getPc(), opcode));
        }
        instruction2.execute(hart, heapAndStack, instruction, memoryLog, opcode);
    }
}
