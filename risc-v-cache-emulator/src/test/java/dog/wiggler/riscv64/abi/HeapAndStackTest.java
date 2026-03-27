package dog.wiggler.riscv64.abi;

import dog.wiggler.emulator.Emulator;
import dog.wiggler.emulator.Input;
import dog.wiggler.emulator.Output;
import dog.wiggler.memory.Log;
import dog.wiggler.memory.MemoryMappedMemory;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static org.junit.jupiter.api.Assertions.fail;

@TestMethodOrder(MethodOrderer.MethodName.class)
public class HeapAndStackTest {
    @Test
    public void testHeapToLargeError() throws Throwable {
        try (var emulator=Emulator.factory(
                        Input.empty(),
                        Log::noOp,
                        MemoryMappedMemory.factory(false, 1L<<20),
                        Output.refuse())
                .get()) {
            try {
                emulator.heapAndStack.reset(
                        emulator.hart,
                        -1L,
                        1L);
                fail();
            }
            catch (HeapToLargeException ignore) {
            }
            try {
                emulator.heapAndStack.reset(
                        emulator.hart,
                        0L,
                        0L);
                fail();
            }
            catch (HeapToLargeException ignore) {
            }
        }
    }

    @Test
    public void testMisalignedStackPointer() throws Throwable {
        try (var emulator=Emulator.factory(
                        Input.empty(),
                        Log::noOp,
                        MemoryMappedMemory.factory(false, 1L<<20),
                        Output.refuse())
                .get()) {
            emulator.reset();
            try {
                emulator.heapAndStack.setStackPointer(
                        emulator.hart,
                        (1L<<20)-1L);
                fail();
            }
            catch (MisalignedStackPointerException ignore) {
            }
        }
    }
}
