package dog.wiggler;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestMethodOrder(MethodOrderer.MethodName.class)
public class PowerTest {
    public static long power(long base, long exponent) {
        long result=1L;
        while (0L<exponent) {
            if (0L!=(exponent&1L)) {
                result*=base;
            }
            base*=base;
            exponent>>=1;
        }
        return result;
    }
    
    private static long powerSlow(long base, long exponent) {
        long result=1L;
        while (0<exponent) {
            result*=base;
            --exponent;
        }
        return result;
    }

    @Test
    public void test() {
        for (long bb=0; 16>bb; ++bb) {
            for (long ee=0; 16>ee; ++ee) {
                assertEquals(
                        powerSlow(bb, ee),
                        power(bb, ee));
            }
        }
    }
}
