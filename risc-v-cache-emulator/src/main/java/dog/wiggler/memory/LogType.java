package dog.wiggler.memory;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Memory access log entry types.
 * These contain all the memory access types, and elapsed emulator cycles, and user data.
 */
public enum LogType {
    ACCESS_LOAD_DATA(AccessType.LOAD_DATA),
    ACCESS_LOAD_INSTRUCTION(AccessType.LOAD_INSTRUCTION),
    ACCESS_STORE(AccessType.STORE),
    ELAPSED_CYCLES(null),
    USER_DATA(null);

    public final @Nullable AccessType accessType;

    LogType(@Nullable AccessType accessType) {
        this.accessType=accessType;
    }

    public static final @NotNull List<@NotNull LogType> TYPES
            =Collections.unmodifiableList(Arrays.asList(values()));
}
