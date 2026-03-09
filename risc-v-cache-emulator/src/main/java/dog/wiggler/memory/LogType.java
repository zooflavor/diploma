package dog.wiggler.memory;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum LogType {
    ACCESS_LOAD_DATA(AccessType.LOAD_DATA),
    ACCESS_LOAD_INSTRUCTION(AccessType.LOAD_INSTRUCTION),
    ACCESS_STORE(AccessType.STORE),
    ELAPSED_CYCLES(null),
    USER_DATA(null);

    public final AccessType accessType;

    LogType(AccessType accessType) {
        this.accessType=accessType;
    }

    public static final List<LogType> TYPES=Collections.unmodifiableList(Arrays.asList(values()));
}
