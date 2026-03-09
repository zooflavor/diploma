package dog.wiggler;

import java.io.Serial;

public class EmulatorException extends RuntimeException {
    @Serial
    private static final long serialVersionUID=0L;

    public EmulatorException() {
    }

    public EmulatorException(String message) {
        super(message);
    }

    public EmulatorException(String message, Throwable cause) {
        super(message, cause);
    }

    public EmulatorException(Throwable cause) {
        super(cause);
    }

    public EmulatorException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
