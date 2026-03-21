package dog.wiggler;

import dog.wiggler.memory.MemoryMappedMemory;
import dog.wiggler.memory.MisalignedMemoryAccessException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.fail;

public class HartTest {
    @Test
    public void testMisalignedInstructionAccess() throws Throwable {
        try (var emulator=Emulator.factory(
                        Input.empty(),
                        null,
                        MemoryMappedMemory.factory(false, 1L<<20),
                        Output.refuse())
                .get()) {
            emulator.reset();
            emulator.hart.setPc(1L);
            try {
                emulator.run();
                fail();
            }
            catch (MisalignedMemoryAccessException ignore) {
            }
        }
    }
}
