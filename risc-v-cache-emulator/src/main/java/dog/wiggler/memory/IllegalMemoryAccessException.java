package dog.wiggler.memory;

import dog.wiggler.EmulatorException;

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

    /**
     * Maximum memory size is capped at 64 terabytes.
     * This is a holdover from aarch64, and is no longer strictly necessary,
     * but it helps to catch overflows.
     */
    public static void checkAccess(long address) {
        if (0L!=(address>>>Memory.ADDRESS_BITS)) {
            throw new IllegalMemoryAccessException(
                    "address %016x is out of valid A64 range".formatted(address));
        }
    }
}
