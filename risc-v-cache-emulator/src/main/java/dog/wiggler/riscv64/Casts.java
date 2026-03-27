package dog.wiggler.riscv64;

/**
 * Casts between doubles and unsigned integers.
 * Signed integers are supported through java casts.
 */
public class Casts {
    private static final double UINT64_MAX=2.0*(1L<<62);

    private Casts() {
    }

    public static double castUint64ToDouble(long value) {
        double result=value&0x7fffffffffffffffL;
        if (0L!=(value&0x8000000000000000L)) {
            result+=UINT64_MAX;
        }
        return result;
    }

    public static int castDoubleToUint32(double value) {
        long result=castDoubleToUint64(value);
        if ((0L>result)
                || (0xffffffffL<result)) {
            return 0xffffffff;
        }
        return (int)result;
    }

    public static long castDoubleToUint64(double value) {
        if ((1.0>value)
                || Double.isNaN(value)) {
            return 0L;
        }
        if (Double.isInfinite(value)) {
            return -1L;
        }
        long bits=Double.doubleToRawLongBits(value);
        int exponent=(int)((bits>>52)-1023L);
        if (63<exponent) {
            return -1L;
        }
        long significand=(bits&0xfffffffffffffL)|0x10000000000000L;
        if (52==exponent) {
            return significand;
        }
        if (52>exponent) {
            return significand>>(52-exponent);
        }
        return significand<<(exponent-52);
    }
}
