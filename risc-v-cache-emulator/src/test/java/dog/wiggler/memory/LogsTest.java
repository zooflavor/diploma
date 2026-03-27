package dog.wiggler.memory;

import dog.wiggler.ReadableMemoryChannel;
import dog.wiggler.WritableMemoryChannel;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Test the encoding and decoding of memory access log entries.
 */
@TestMethodOrder(MethodOrderer.MethodName.class)
public class LogsTest {
    private static class FailVisitor<T> implements LogVisitor<T> {
        @Override
        public T access(long address, int size, @NotNull AccessType type) {
            throw new RuntimeException();
        }

        @Override
        public T accessLogDisabled() {
            throw new RuntimeException();
        }

        @Override
        public T accessLogEnabled() {
            throw new RuntimeException();
        }

        @Override
        public T elapsedCycles(long elapsedCycles) {
            throw new RuntimeException();
        }

        @Override
        public T end() {
            throw new RuntimeException();
        }

        @Override
        public T userData(long userData) {
            throw new RuntimeException();
        }
    }

    @Test
    public void testInvalidData() {
        try {
            Logs.encodeUserData(-1L);
            fail();
        }
        catch (IllegalArgumentException ignore) {
        }
    }

    @Test
    public void testInvalidType() {
        try {
            Logs.decodeType(-1L);
            fail();
        }
        catch (IllegalArgumentException ignore) {
        }
    }

    @Test
    public void testLog2() {
        assertEquals(0, Logs.log2(1));
        assertEquals(1, Logs.log2(2));
        assertEquals(2, Logs.log2(4));
        assertEquals(30, Logs.log2(1<<30));
        assertEquals(0, Logs.log2Checked(1));
        assertEquals(1, Logs.log2Checked(2));
        assertEquals(2, Logs.log2Checked(4));
        assertEquals(30, Logs.log2Checked(1<<30));
        for (int value: List.of(0, 31, 1<<31)) {
            assertEquals(-1, Logs.log2(value));
            try {
                Logs.log2Checked(value);
                fail();
            }
            catch (IllegalArgumentException ignore) {
            }
        }
    }

    @Test
    public void testEncodeAndDecode() throws Throwable {
        Logs.visit(
                Logs.encodeAccess(0x12345L, 2, AccessType.LOAD_DATA),
                new FailVisitor<Void>() {
                    @Override
                    public Void access(long address, int size, @NotNull AccessType type) {
                        assertEquals(0x12345L, address);
                        assertEquals(2, size);
                        assertEquals(AccessType.LOAD_DATA, type);
                        return null;
                    }
                });
        assertEquals(0x0001000000012345L, Logs.encodeAccess(0x12345, 2, AccessType.LOAD_DATA));

        Logs.visit(
                Logs.encodeAccess(0x12345, 4, AccessType.LOAD_INSTRUCTION),
                new FailVisitor<Void>() {
                    @Override
                    public Void access(long address, int size, @NotNull AccessType type) {
                        assertEquals(0x12345L, address);
                        assertEquals(4, size);
                        assertEquals(AccessType.LOAD_INSTRUCTION, type);
                        return null;
                    }
                });

        Logs.visit(
                Logs.encodeAccess(0x654321, 8, AccessType.STORE),
                new FailVisitor<Void>() {
                    @Override
                    public Void access(long address, int size, @NotNull AccessType type) {
                        assertEquals(0x654321L, address);
                        assertEquals(8, size);
                        assertEquals(AccessType.STORE, type);
                        return null;
                    }
                });

        Logs.visit(
                Logs.encodeAccessLogDisabled(),
                new FailVisitor<Void>() {
                    @Override
                    public Void accessLogDisabled() {
                        return null;
                    }
                });

        Logs.visit(
                Logs.encodeAccessLogEnabled(),
                new FailVisitor<Void>() {
                    @Override
                    public Void accessLogEnabled() {
                        return null;
                    }
                });

        Logs.visit(
                Logs.encodeElapsedCycles(0x12345L),
                new FailVisitor<Void>() {
                    @Override
                    public Void elapsedCycles(long elapsedCycles) {
                        assertEquals(0x12345L, elapsedCycles);
                        return null;
                    }
                });

        Logs.visit(
                Logs.encodeUserData(0x12345L),
                new FailVisitor<Void>() {
                    @Override
                    public Void userData(long userData) {
                        assertEquals(0x12345L, userData);
                        return null;
                    }
                });
    }

    @Test
    public void testLogStream() throws Throwable {
        byte[] data;
        try (var outputChannel=new WritableMemoryChannel()) {
            try (var outputStream=LogOutputStream.factory(()->outputChannel)
                    .get()) {
                outputStream.access(4, 2, AccessType.LOAD_DATA);
                outputStream.access(8, 4, AccessType.LOAD_INSTRUCTION);
                outputStream.access(16, 8, AccessType.STORE);
                outputStream.end();
                outputStream.elapsedCycles(5);
                outputStream.userData(7);
                outputStream.end();
            }
            data=outputChannel.toByteArray();
        }
        assertEquals(40, data.length);
        try (var inputStream=LogInputStream.factory(
                        ()->new ReadableMemoryChannel(data))
                .get()) {
            inputStream.readNext(
                    new FailVisitor<Void>() {
                        @Override
                        public Void access(long address, int size, @NotNull AccessType type) {
                            assertEquals(4, address);
                            assertEquals(2, size);
                            assertEquals(AccessType.LOAD_DATA, type);
                            return null;
                        }
                    });
            inputStream.readNext(
                    new FailVisitor<Void>() {
                        @Override
                        public Void access(long address, int size, @NotNull AccessType type) {
                            assertEquals(8, address);
                            assertEquals(4, size);
                            assertEquals(AccessType.LOAD_INSTRUCTION, type);
                            return null;
                        }
                    });
            inputStream.readNext(
                    new FailVisitor<Void>() {
                        @Override
                        public Void access(long address, int size, @NotNull AccessType type) {
                            assertEquals(16, address);
                            assertEquals(8, size);
                            assertEquals(AccessType.STORE, type);
                            return null;
                        }
                    });
            inputStream.readNext(
                    new FailVisitor<Void>() {
                        @Override
                        public Void elapsedCycles(long elapsedCycles) {
                            assertEquals(5, elapsedCycles);
                            return null;
                        }
                    });
            inputStream.readNext(
                    new FailVisitor<Void>() {
                        @Override
                        public Void userData(long userData) {
                            assertEquals(7, userData);
                            return null;
                        }
                    });
            int result=inputStream.readNext(
                    new FailVisitor<@NotNull Integer>() {
                        @Override
                        public @NotNull Integer end() {
                            return 9;
                        }
                    });
            assertEquals(9, result);
        }
    }

    @Test
    public void testNoOpLog() throws Throwable {
        var log=Log.noOp();
        log.close();
        log.access(1, 2, AccessType.LOAD_DATA);
        log.accessLogDisabled();
        log.accessLogEnabled();
        log.elapsedCycles(3);
        log.userData(4);
    }
}
