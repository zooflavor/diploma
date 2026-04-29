package dog.wiggler.riscv64;

import dog.wiggler.emulator.EmulatorException;

import java.io.Serial;

/**
 * Thrown when an unimplemented instruction is encountered.
 */
public class IllegalInstructionException extends EmulatorException {
    @Serial
    private static final long serialVersionUID=0L;

    public IllegalInstructionException(String message) {
        super(message);
    }
}
