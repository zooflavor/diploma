package dog.wiggler.emulator;

import dog.wiggler.memory.Log;
import dog.wiggler.memory.MemoryMappedMemory;
import dog.wiggler.riscv64.abi.PrimitiveType;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.fail;

@TestMethodOrder(MethodOrderer.MethodName.class)
public class PrimitiveTypeTest {
    @Test
    public void testVoid() throws Throwable {
        try (var emulator=Emulator.factory(
                        Input.empty(),
                        Log::noOp,
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
