package dog.wiggler;

import dog.wiggler.function.Supplier;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.io.Serial;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class SupplierTest {
    private static class Test1Exception extends Exception {
        @Serial
        private static final long serialVersionUID=0L;
    }

    private static class Test2Exception extends Exception {
        @Serial
        private static final long serialVersionUID=0L;
    }

    @Test
    public void testFactoryAndCloseError() throws Throwable {
        try {
            Supplier.<@NotNull AutoCloseable, Void>factory(
                            (value)->{
                                throw new Test1Exception();
                            },
                            ()->()->{
                                throw new Test2Exception();
                            })
                    .get();
            fail();
        }
        catch (Test2Exception ex) {
            assertEquals(1, ex.getSuppressed().length);
            assertInstanceOf(Test1Exception.class, ex.getSuppressed()[0]);
        }
    }
    
    @Test
    public void testFactoryErrorClosesResource() throws Throwable {
        boolean[] closed=new boolean[1];
        try {
            Supplier.<@NotNull AutoCloseable, Void>factory(
                            (value)->{
                                throw new Test1Exception();
                            },
                            ()->()->closed[0]=true)
                    .get();
            fail();
        }
        catch (Test1Exception ignore) {
        }
        assertTrue(closed[0]);
    }
}
