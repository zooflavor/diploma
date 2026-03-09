package dog.wiggler.memory;

import dog.wiggler.EmulatorException;

import java.io.Serial;

public class IllegalMemoryAccessException extends EmulatorException {
    @Serial
    private static final long serialVersionUID=0L;

    public IllegalMemoryAccessException(String format, Object... args) {
        super(String.format(format, args));
    }

    public static void checkAccess(long address) {
        if (0L!=(address>>48)) {
            throw new IllegalMemoryAccessException("address %016x is out of valid A64 range", address);
        }
    }

    public static IllegalMemoryAccessException illegalAccess(long address) {
        checkAccess(address);
        return new IllegalMemoryAccessException("illegal memory access %012s");
    }
}
