package dog.wiggler.riscv64;

import dog.wiggler.emulator.EmulatorException;

import java.io.Serial;

public class IllegalInstructionException extends EmulatorException {
    @Serial
    private static final long serialVersionUID=0L;

    public IllegalInstructionException(String message) {
        super(message);
    }
}
