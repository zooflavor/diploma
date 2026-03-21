package dog.wiggler;

import dog.wiggler.elf.ELF;
import dog.wiggler.elf.FileHeader;
import dog.wiggler.memory.AccessType;
import dog.wiggler.memory.Log;
import dog.wiggler.memory.MemoryMappedMemory;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.io.EOFException;
import java.io.IOException;
import java.io.Serial;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.fail;

public class EmulatorTest {
    @Test
    public void testCloseException() throws Throwable {
        class TestException extends Exception {
            @Serial
            private static final long serialVersionUID=0L;
        }
        var emulator=Emulator.factory(
                        Input.empty(),
                        ()->new Log() {
                            @Override
                            public void close() {
                            }

                            @Override
                            public Void access(
                                    long address,
                                    int size,@NotNull AccessType type) {
                                return null;
                            }

                            @Override
                            public Void elapsedCycles(
                                    long elapsedCycles) {
                                return null;
                            }

                            @Override
                            public Void end() throws Throwable {
                                throw new TestException();
                            }

                            @Override
                            public Void userData(
                                    long userData) {
                                return null;
                            }
                        },
                        MemoryMappedMemory.factory(false, 1L<<20),
                        Output.refuse())
                .get();
        try {
            emulator.close();
            fail();
        }
        catch (IOException ex) {
            assertInstanceOf(TestException.class, ex.getCause());
        }
    }

    @Test
    public void testLoadElfEof() throws Throwable {
        try (var emulator=Emulator.factory(
                        Input.empty(),
                        null,
                        MemoryMappedMemory.factory(false, 1L<<20),
                        Output.refuse())
                .get()) {
            @NotNull FileHeader elfHeader=ELF.read(
                    EmulatorTests.imagePath(
                            EmulatorTests.EXECUTABLE_IMAGE_OPTIONS.getFirst(),
                            "emulator-tests"));
            try {
                emulator.loadELFAndReset(
                        new MemoryByteChannel(new byte[0]),
                        elfHeader);
                fail();
            }
            catch (EOFException ignore) {
            }
        }
    }
}
