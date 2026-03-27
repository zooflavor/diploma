package dog.wiggler.cache;

import dog.wiggler.memory.AccessType;
import org.jetbrains.annotations.NotNull;

public enum CacheType {
    BOTH,
    DATA,
    INSTRUCTION;

    public boolean notCached(@NotNull AccessType type) {
        return switch (this) {
            case BOTH -> false;
            case DATA -> AccessType.LOAD_INSTRUCTION.equals(type);
            default -> !AccessType.LOAD_INSTRUCTION.equals(type);
        };
    }
}
