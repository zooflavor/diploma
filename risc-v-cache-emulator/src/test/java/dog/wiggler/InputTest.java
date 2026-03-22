package dog.wiggler;

import dog.wiggler.function.Supplier;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.ArrayDeque;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

@TestMethodOrder(MethodOrderer.MethodName.class)
public class InputTest {
    @Test
    public void testEmpty() throws Throwable {
        var input=Input.empty();
        for (var supplier: List.<@NotNull Supplier<?>>of(
                input::readInt16,
                input::readInt32,
                input::readInt64,
                input::readInt8,
                input::readDouble,
                input::readFloat,
                input::readUint16,
                input::readUint32,
                input::readUint64,
                input::readUint8)) {
            try {
                supplier.get();
                fail();
            }
            catch (NoSuchElementException ex) {
                assertEquals("no inputs", ex.getMessage());
            }
        }
    }

    @Test
    public void testSupplier() throws Throwable {
        var inputDeque=new ArrayDeque<@NotNull Number>();
        inputDeque.add((short)1);
        inputDeque.add(2);
        inputDeque.add(3L);
        inputDeque.add((byte)4);
        inputDeque.add(5.0);
        inputDeque.add(6.0f);
        inputDeque.add((short)7);
        inputDeque.add(8);
        inputDeque.add(9L);
        inputDeque.add((byte)10);
        var input=Input.supplier(inputDeque::removeFirst);
        assertEquals((short)1, input.readInt16());
        assertEquals(2, input.readInt32());
        assertEquals(3L, input.readInt64());
        assertEquals((byte)4, input.readInt8());
        assertEquals(5.0, input.readDouble());
        assertEquals(6.0f, input.readFloat());
        assertEquals((short)7, input.readInt16());
        assertEquals(8, input.readInt32());
        assertEquals(9L, input.readInt64());
        assertEquals((byte)10, input.readInt8());
        for (var supplier: List.<@NotNull Supplier<?>>of(
                input::readInt16,
                input::readInt32,
                input::readInt64,
                input::readInt8,
                input::readDouble,
                input::readFloat,
                input::readUint16,
                input::readUint32,
                input::readUint64,
                input::readUint8)) {
            try {
                supplier.get();
                fail();
            }
            catch (NoSuchElementException ignore) {
            }
        }
    }
}
