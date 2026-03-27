package dog.wiggler.memory;

import dog.wiggler.emulator.EmulatorException;

import java.io.Serial;

/**
 * Thrown on illegal memory accesses.
 * A memory access is illegal when it's try to access memory that doesn't exist.
 */
public class IllegalMemoryAccessException extends EmulatorException {
    @Serial
    private static final long serialVersionUID=0L;

    public IllegalMemoryAccessException(String message) {
        super(message);
    }
}
