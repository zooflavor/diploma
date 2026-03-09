package dog.wiggler;

import dog.wiggler.memory.AccessType;
import dog.wiggler.memory.LogVisitor;
import dog.wiggler.memory.Logs;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class LogsTest {
    @Test
    public void test() throws Throwable {
        class FailVisitor implements LogVisitor<Void> {
            @Override
            public Void access(long address, int size, AccessType type) {
                fail();
                return null;
            }

            @Override
            public Void elapsedCycles(long elapsedCycles) {
                fail();
                return null;
            }

            @Override
            public Void end() {
                fail();
                return null;
            }

            @Override
            public Void userData(long userData) {
                fail();
                return null;
            }
        }

        Logs.visit(
                Logs.encodeAccess(0x12345L, 2, AccessType.LOAD_DATA),
                new FailVisitor() {
                    @Override
                    public Void access(long address, int size, AccessType type) {
                        assertEquals(0x12345L, address);
                        assertEquals(2, size);
                        assertEquals(AccessType.LOAD_DATA, type);
                        return null;
                    }
                });
                assertEquals(0x0001000000012345L, Logs.encodeAccess(0x12345, 2, AccessType.LOAD_DATA));

        Logs.visit(
                Logs.encodeAccess(0x12345, 4, AccessType.LOAD_INSTRUCTION),
                new FailVisitor() {
                    @Override
                    public Void access(long address, int size, AccessType type) {
                        assertEquals(0x12345L, address);
                        assertEquals(4, size);
                        assertEquals(AccessType.LOAD_INSTRUCTION, type);
                        return null;
                    }
                });

        Logs.visit(
                Logs.encodeAccess(0x654321, 8, AccessType.STORE),
                new FailVisitor() {
                    @Override
                    public Void access(long address, int size, AccessType type) {
                        assertEquals(0x654321L, address);
                        assertEquals(8, size);
                        assertEquals(AccessType.STORE, type);
                        return null;
                    }
                });

        Logs.visit(
                Logs.encodeElapsedCycles(0x12345L),
                new FailVisitor() {
                    @Override
                    public Void elapsedCycles(long elapsedCycles) {
                        assertEquals(0x12345L, elapsedCycles);
                        return null;
                    }
                });

        Logs.visit(
                Logs.encodeUserData(0x12345L),
                new FailVisitor() {
                    @Override
                    public Void userData(long userData) {
                        assertEquals(0x12345L, userData);
                        return null;
                    }
                });
    }
}
