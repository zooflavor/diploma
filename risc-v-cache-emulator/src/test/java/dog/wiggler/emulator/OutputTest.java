package dog.wiggler.emulator;

import dog.wiggler.function.Runnable;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

@TestMethodOrder(MethodOrderer.MethodName.class)
public class OutputTest {
    @Test
    public void testAppendable() throws Throwable {
        var sb=new StringBuilder();
        var output=new AppendableOutput(sb);
        output.writeDouble(1.0);
        output.writeFloat(2.0f);
        output.writeInt16((short)3);
        output.writeInt32(4);
        output.writeInt64(5L);
        output.writeInt8((byte)6);
        output.writeUint16((short)7);
        output.writeUint32(8);
        output.writeUint64(9L);
        output.writeUint8((byte)10);
        assertEquals(
                "1.0"
                        +System.lineSeparator()
                        +"2.0"
                        +System.lineSeparator()
                        +"3"
                        +System.lineSeparator()
                        +"4"
                        +System.lineSeparator()
                        +"5"
                        +System.lineSeparator()
                        +"6"
                        +System.lineSeparator()
                        +"7"
                        +System.lineSeparator()
                        +"8"
                        +System.lineSeparator()
                        +"9"
                        +System.lineSeparator()
                        +"10"
                        +System.lineSeparator(),
                sb.toString());
    }

    @Test
    public void testConsumer() throws Throwable {
        var outputList=new ArrayList<@NotNull Number>();
        var output=Output.consumer(outputList::add);
        output.writeInt16((short)1);
        output.writeInt32(2);
        output.writeInt64(3L);
        output.writeInt8((byte)4);
        output.writeDouble(5.0);
        output.writeFloat(6.0f);
        output.writeUint16((short)7);
        output.writeUint32(8);
        output.writeUint64(9L);
        output.writeUint8((byte)10);
        assertEquals(
                List.of(
                        (short)1,
                        2,
                        3L,
                        (byte)4,
                        5.0,
                        6.0f,
                        (short)7,
                        8,
                        9L,
                        (byte)10),
                outputList);
    }

    @Test
    public void testRefuse() throws Throwable {
        var output=Output.refuse();
        for (var runnable: List.<@NotNull Runnable>of(
                ()->output.writeInt16((short)0),
                ()->output.writeInt32(0),
                ()->output.writeInt64(0L),
                ()->output.writeInt8((byte)0),
                ()->output.writeDouble(0.0),
                ()->output.writeFloat(0.0f),
                ()->output.writeUint16((short)0),
                ()->output.writeUint32(0),
                ()->output.writeUint64(0L),
                ()->output.writeUint8((byte)0))) {
            try {
                runnable.run();
                fail();
            }
            catch (RuntimeException ex) {
                assertEquals("no output is accepted", ex.getMessage());
            }
        }
    }
}
