package dog.wiggler.riscv64;

import dog.wiggler.EmulatorException;

import java.io.Serial;

public class IllegalInstructionException extends EmulatorException {
    @Serial
    private static final long serialVersionUID=0L;

    public IllegalInstructionException(String format, Object... args) {
        super(String.format(format, args));
    }
}
