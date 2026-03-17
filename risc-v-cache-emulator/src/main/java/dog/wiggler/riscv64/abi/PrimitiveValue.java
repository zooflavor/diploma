package dog.wiggler.riscv64.abi;

import dog.wiggler.memory.Memory;
import dog.wiggler.riscv64.Hart;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Bundles a value with its type.
 */
public record PrimitiveValue<J>(
        @NotNull PrimitiveType<J> type,
        J value) {
    public PrimitiveValue(@NotNull PrimitiveType<J> type, J value) {
        this.type=Objects.requireNonNull(type, "type");
        this.value=value;
    }

    public void storeArgument(
            @NotNull Hart hart,
            @NotNull HeapAndStack heapAndStack,
            int register) {
        type().storeArgument(hart, heapAndStack, register, value());
    }

    public void storeArgument(
            @NotNull Memory memory,
            long address)
            throws Throwable {
        type().storeArgument(memory, address, value());
    }
}
