package dog.wiggler.memory;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Helper methods to encode and decode memory access log entries.
 * All type of entries are encoded to 64 bits.
 * For all entries the three most significant bits specify the type.
 * This leaves 61 bits for data.
 * Elapsed cycles and user data uses all 61 bits, and limited to 61 bits.
 * Memory access addresses are limited to 48 bits.
 * Memory access sizes must be powers of two.
 */
public class Logs {
    private static final long ADDRESS_MASK=mask(Memory.ADDRESS_BITS);
    private static final int ADDRESS_SHIFT=0;
    private static final long ELAPSED_CYCLES_MASK=mask(61);
    private static final int ELAPSED_CYCLES_SHIFT=0;
    public static final int PAGE_SIZE=1<<12;
    private static final long SIZE_MASK=mask(5);
    private static final int SIZE_SHIFT=Memory.ADDRESS_BITS;
    private static final long TYPE_MASK=mask(3);
    private static final int TYPE_SHIFT=61;
    private static final long USER_DATA_MASK=mask(61);
    private static final int USER_DATA_SHIFT=0;

    private Logs() {
    }

    private static long decode(long log, long mask, int shift) {
        return (log>>>shift)&mask;
    }

    public static long decodeAddress(long log) {
        return decode(log, ADDRESS_MASK, ADDRESS_SHIFT);
    }

    private static long decodeElapsedCycles(long log) {
        return decode(log, ELAPSED_CYCLES_MASK, ELAPSED_CYCLES_SHIFT);
    }

    private static int decodeSize(long log) {
        return 1<<(int)decode(log, SIZE_MASK, SIZE_SHIFT);
    }

    public static @NotNull LogType decodeType(long log) {
        int type=(int)decode(log, TYPE_MASK, TYPE_SHIFT);
        if ((0>type) || (LogType.TYPES.size()<=type)) {
            throw new RuntimeException("unknown log type %x".formatted(type));
        }
        return LogType.TYPES.get(type);
    }

    private static long decodeUserData(long log) {
        return decode(log, USER_DATA_MASK, USER_DATA_SHIFT);
    }

    private static long encode(long mask, int shift, long value) {
        if (value!=(value&mask)) {
            throw new RuntimeException("invalid value %x".formatted(value));
        }
        return value<<shift;
    }

    public static long encodeAccess(long address, int size, @NotNull AccessType type) {
        int shift=log2Checked(size);
        @NotNull LogType type2=switch (type) {
            case LOAD_DATA -> LogType.ACCESS_LOAD_DATA;
            case LOAD_INSTRUCTION -> LogType.ACCESS_LOAD_INSTRUCTION;
            case STORE -> LogType.ACCESS_STORE;
        };
        return encode(ADDRESS_MASK, ADDRESS_SHIFT, address)
                |encode(SIZE_MASK, SIZE_SHIFT, shift)
                |encode(TYPE_MASK, TYPE_SHIFT, type2.ordinal());
    }

    public static long encodeElapsedCycles(long elapsedCycles) {
        return encode(ELAPSED_CYCLES_MASK, ELAPSED_CYCLES_SHIFT, elapsedCycles)
                |encode(TYPE_MASK, TYPE_SHIFT, LogType.ELAPSED_CYCLES.ordinal());
    }

    public static long encodeUserData(long userData) {
        return encode(USER_DATA_MASK, USER_DATA_SHIFT, userData)
                |encode(TYPE_MASK, TYPE_SHIFT, LogType.USER_DATA.ordinal());
    }

    private static int log2(int value) {
        if (0==value) {
            return -1;
        }
        int shift=Integer.numberOfTrailingZeros(value);
        if ((1<<shift)!=value) {
            return -1;
        }
        return shift;
    }

    public static int log2Checked(int value) {
        int log2=log2(value);
        if (0>log2) {
            throw new RuntimeException("%x is not a power of 2".formatted(value));
        }
        return log2;
    }

    private static long mask(int bits) {
        return (1L<<bits)-1L;
    }

    public static <R> R visit(
            long log,
            @NotNull LogVisitor<R> visitor)
            throws Throwable {
        LogType type=Logs.decodeType(log);
        return switch (type) {
            case ACCESS_LOAD_DATA, ACCESS_LOAD_INSTRUCTION, ACCESS_STORE -> {
                long address=Logs.decodeAddress(log);
                int size=Logs.decodeSize(log);
                yield visitor.access(
                        address,
                        size,
                        Objects.requireNonNull(type.accessType, "type.accessType"));
            }
            case ELAPSED_CYCLES -> {
                long elapsedCycles=Logs.decodeElapsedCycles(log);
                yield visitor.elapsedCycles(elapsedCycles);
            }
            case USER_DATA -> {
                long userData=Logs.decodeUserData(log);
                yield visitor.userData(userData);
            }
        };
    }
}
