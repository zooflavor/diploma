package dog.wiggler;

import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Holder for the exit code.
 * The emulator stops running when the exit code gets set.
 * The exit code can be set by calling exit(), exitOk(), and returning from the main() function.
 */
public class Exit {
    private @Nullable Integer code;

    public void clear() {
        code=null;
    }

    public int code() {
        return Objects.requireNonNull(code, "code");
    }

    public boolean set() {
        return null!=code;
    }

    public void set(int code) {
        this.code=code;
    }

    public void setOk() {
        set(0);
    }
}
