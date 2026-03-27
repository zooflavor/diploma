package dog.wiggler.emulator;

import java.io.Serial;

/**
 * Supertype of emulator exceptions.
 */
public class EmulatorException extends RuntimeException {
    @Serial
    private static final long serialVersionUID=0L;

    public EmulatorException(String message) {
        super(message);
    }
}
