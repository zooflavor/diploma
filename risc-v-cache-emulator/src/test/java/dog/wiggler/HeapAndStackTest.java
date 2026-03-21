package dog.wiggler;

import dog.wiggler.memory.MemoryMappedMemory;
import dog.wiggler.riscv64.abi.HeapToLargeException;
import dog.wiggler.riscv64.abi.MisalignedStackPointerException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.fail;

public class HeapAndStackTest {
    @Test
    public void testHeapToLargeError() throws Throwable {
        try (var emulator=Emulator.factory(
                        Input.empty(),
                        null,
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
                        null,
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
