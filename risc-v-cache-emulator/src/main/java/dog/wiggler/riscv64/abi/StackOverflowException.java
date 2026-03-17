package dog.wiggler.riscv64.abi;

import dog.wiggler.EmulatorException;

import java.io.Serial;

/**
 * Thrown on stack overflows.
 */
public class StackOverflowException extends EmulatorException {
    @Serial
    private static final long serialVersionUID=0L;

    public StackOverflowException(String message) {
        super(message);
    }
}
