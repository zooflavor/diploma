package dog.wiggler.memory;

public class Logs {
    public static final long ADDRESS_MASK=mask(48);
    public static final int ADDRESS_SHIFT=0;
    public static final long ELAPSED_CYCLES_MASK=mask(61);
    public static final int ELAPSED_CYCLES_SHIFT=0;
    public static final long SIZE_MASK=mask(5);
    public static final int SIZE_SHIFT=48;
    public static final long TYPE_MASK=mask(3);
    public static final int TYPE_SHIFT=61;
    public static final long USER_DATA_MASK=mask(61);
    public static final int USER_DATA_SHIFT=0;

    private Logs() {
    }

    private static long decode(long log, long mask, int shift) {
        return (log>>>shift)&mask;
    }

    public static long decodeAddress(long log) {
        return decode(log, ADDRESS_MASK, ADDRESS_SHIFT);
    }

    public static long decodeElapsedCycles(long log) {
        return decode(log, ELAPSED_CYCLES_MASK, ELAPSED_CYCLES_SHIFT);
    }

    public static int decodeSize(long log) {
        return 1<<(int)decode(log, SIZE_MASK, SIZE_SHIFT);
    }

    public static LogType decodeType(long log) {
        int type=(int)decode(log, TYPE_MASK, TYPE_SHIFT);
        if ((0>type) || (LogType.TYPES.size()<=type)) {
            throw new RuntimeException("unknown log type %x".formatted(type));
        }
        return LogType.TYPES.get(type);
    }

    public static long decodeUserData(long log) {
        return decode(log, USER_DATA_MASK, USER_DATA_SHIFT);
    }

    private static long encode(long mask, int shift, long value) {
        if (value!=(value&mask)) {
            throw new RuntimeException("invalid value %x".formatted(value));
        }
        return value<<shift;
    }

    public static long encodeAccess(long address, int size, AccessType type) {
        int shift=log2Checked(size);
        LogType type2=switch (type) {
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

    public static int log2(int value) {
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

    public static <R> R visit(long log, LogVisitor<R> visitor) throws Throwable {
        LogType type=Logs.decodeType(log);
        return switch (type) {
            case ACCESS_LOAD_DATA, ACCESS_LOAD_INSTRUCTION, ACCESS_STORE -> {
                long address=Logs.decodeAddress(log);
                int size=Logs.decodeSize(log);
                yield visitor.access(address, size, type.accessType);
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
