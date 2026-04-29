package dog.wiggler.cache;

import dog.wiggler.memory.AccessType;
import org.jetbrains.annotations.NotNull;

/**
 * The types of accesses a cache will process.
 * Access types not matching this will be passed through the cache without further processing.
 */
public enum CacheType {
    /**
     * Data and instruction accesses will be processed.
     */
    BOTH,
    /**
     * Data accesses will be processed, instruction accesses will be passed on.
     */
    DATA,
    /**
     * Instruction accesses will be processed, data accesses will be passed on.
     */
    INSTRUCTION;

    public boolean notCached(@NotNull AccessType type) {
        return switch (this) {
            case BOTH -> false;
            case DATA -> AccessType.LOAD_INSTRUCTION.equals(type);
            default -> !AccessType.LOAD_INSTRUCTION.equals(type);
        };
    }
}
