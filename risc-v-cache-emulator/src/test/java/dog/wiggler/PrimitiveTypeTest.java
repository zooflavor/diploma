package dog.wiggler;

import dog.wiggler.memory.MemoryMappedMemory;
import dog.wiggler.riscv64.abi.PrimitiveType;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.fail;

public class PrimitiveTypeTest {
    @Test
    public void testVoid() throws Throwable {
        try (var emulator=Emulator.factory(
                        Input.empty(),
                        null,
                        MemoryMappedMemory.factory(false, 1L<<20),
                        Output.refuse())
                .get()) {
            for (var runnable: List.<@NotNull Runnable>of(
                    PrimitiveType.voidType()::checkStored,
                    PrimitiveType.voidType()::integral,
                    ()->PrimitiveType.voidType().load(emulator.memoryLog, 0L),
                    ()->PrimitiveType.voidType().loadArgument(emulator.hart, 0),
                    PrimitiveType.voidType()::size,
                    ()->PrimitiveType.voidType().store(emulator.memoryLog, 0L, null),
                    ()->PrimitiveType.voidType().storeArgument(emulator.hart, emulator.heapAndStack, 0, null),
                    ()->PrimitiveType.voidType().storeArgument(emulator.memoryLog, 0L, null))) {
                try {
                    runnable.run();
                    fail();
                }
                catch (UnsupportedOperationException ignore) {
                }
            }
        }
    }
}
