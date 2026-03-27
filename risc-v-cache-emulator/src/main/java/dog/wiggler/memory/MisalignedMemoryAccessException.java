package dog.wiggler.memory;

import dog.wiggler.emulator.EmulatorException;

import java.io.Serial;

/**
 * Thrown on misaligned memory accesses.
 */
public class MisalignedMemoryAccessException extends EmulatorException {
    @Serial
    private static final long serialVersionUID=0L;

    public MisalignedMemoryAccessException(String message) {
        super(message);
    }
}
