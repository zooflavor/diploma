package dog.wiggler;

import java.io.Serial;

/**
 * Thrown on setting a misaligned the stack pointer.
 */
public class MisalignedStackPointerException extends EmulatorException {
    @Serial
    private static final long serialVersionUID=0L;

    public MisalignedStackPointerException(String message) {
        super(message);
    }
}
