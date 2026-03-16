package dog.wiggler;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CastsTest {
    @Test
    public void testDoubleToUint64() {
        assertEquals(0L, Casts.castDoubleToUint64(Double.NaN));
        assertEquals(0L, Casts.castDoubleToUint64(Double.NEGATIVE_INFINITY));
        assertEquals(-1L, Casts.castDoubleToUint64(Double.POSITIVE_INFINITY));
        for (long bits: List.of(0L, 1L, 3L, 5L, 13L)) {
            for (int shift=0; 64>shift; ++shift) {
                long value=bits<<shift;
                assertEquals(value, Casts.castDoubleToUint64(Casts.castUint64ToDouble(value)));
            }
        }
        for (long sign=0; 2>sign; ++sign) {
            for (long exponent=1021L; 1088L>exponent; ++exponent) {
                for (long bits: List.of(0L, 1L, 3L, 5L, 13L)) {
                    for (int shift=0; 52>shift; ++shift) {
                        double value=Double.longBitsToDouble(
                                (sign<<63)
                                |(exponent<<52)
                                |((bits<<shift)&0xfffffffffffffL));
                        if (0.0>=value) {
                            assertEquals(0L, Casts.castDoubleToUint64(value));
                        }
                        else {
                            // noinspection ExtractMethodRecommender
                            BigInteger value2=BigInteger.valueOf(bits)
                                    .shiftLeft(shift)
                                    .and(new BigInteger("fffffffffffff", 16))
                                    .or(new BigInteger("10000000000000", 16));
                            int exponent2=(int)exponent-1023-52;
                            if (0L<=exponent2) {
                                value2=value2.shiftLeft(exponent2);
                            }
                            else {
                                value2=value2.shiftRight(-exponent2);
                            }
                            if (64<value2.bitLength()) {
                                assertEquals(-1L, Casts.castDoubleToUint64(value));
                            }
                            else {
                                assertEquals(
                                        value2.toString(),
                                        Long.toUnsignedString(Casts.castDoubleToUint64(value)));
                            }
                        }
                    }
                }
            }
        }
    }

    @Test
    public void testUint64ToDouble() {
        for (long bits: List.of(0L, 1L, 3L, 5L, 13L)) {
            for (int shift=0; 64>shift; ++shift) {
                long value=bits<<shift;
                assertEquals(
                        Double.valueOf(new BigInteger(Long.toUnsignedString(value)).doubleValue()),
                        Double.valueOf(Casts.castUint64ToDouble(value)));
            }
        }
    }
}
