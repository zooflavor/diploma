package dog.wiggler.riscv64.abi;

import dog.wiggler.EmulatorException;

import java.io.Serial;

public class HeapToLargeException extends EmulatorException {
    @Serial
    private static final long serialVersionUID=0L;

    public HeapToLargeException(String message) {
        super(message);
    }
}
