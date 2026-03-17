package dog.wiggler.riscv64.abi;

import dog.wiggler.EmulatorException;

import java.io.Serial;

/**
 * Thrown on setting a misaligned stack pointer.
 */
public class MisalignedStackPointerException extends EmulatorException {
    @Serial
    private static final long serialVersionUID=0L;

    public MisalignedStackPointerException(String message) {
        super(message);
    }
}
