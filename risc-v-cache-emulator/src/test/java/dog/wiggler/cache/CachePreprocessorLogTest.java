package dog.wiggler.cache;

import dog.wiggler.memory.AccessType;
import dog.wiggler.memory.Log;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@TestMethodOrder(MethodOrderer.MethodName.class)
public class CachePreprocessorLogTest {

    @Test
    public void testErrors() {
        for (int lineSize: List.of(0, 3, 1<<31)) {
            try {
                new CachePreprocessorLog(CacheType.BOTH, lineSize, false, Log.noOp());
                fail();
            }
            catch (IllegalArgumentException ignore) {
            }
        }
    }

    @Test
    public void testLoad() throws Throwable {
        testLoad("LD", AccessType.LOAD_DATA);
        testLoad("LI", AccessType.LOAD_INSTRUCTION);
    }

    private void testLoad(@NotNull String load, @NotNull AccessType type) throws Throwable {
        try (var log1=new TestLog();
             var log2=new CachePreprocessorLog(CacheType.BOTH, 128, false, log1)) {
            log1.assertLog();

            log2.access(0xffff, 1, type);
            log1.assertLog("A,0xff80,0x80,"+load);

            log2.access(0xff80, 1, type);
            log1.assertLog("A,0xff80,0x80,"+load);

            log2.access(0xff80, 128, type);
            log1.assertLog("A,0xff80,0x80,"+load);

            log2.access(0xff80, 129, type);
            log1.assertLog(
                    "A,0xff80,0x80,"+load,
                    "A,0x10000,0x80,"+load);

            log2.access(0xffff, 130, type);
            log1.assertLog(
                    //"A,0xff80,0x80,"+load,
                    "A,0xff80,0x80,"+load,
                    "A,0x10000,0x80,"+load,
                    "A,0x10080,0x80,"+load);
        }
    }

    @Test
    public void testNonAccess() throws Throwable {
        try (var log1=new TestLog();
             var log2=new CachePreprocessorLog(CacheType.BOTH, 128, false, log1)) {
            log1.assertLog();
            assertTrue(log1.log.isEmpty());

            log2.accessLogDisabled();
            log1.assertLog("ALD");

            log2.accessLogEnabled();
            log1.assertLog("ALE");

            log2.elapsedCycles(13);
            log1.assertLog("EC,13");

            log2.end();
            log1.assertLog("E");

            log2.userData(24);
            log1.assertLog("UD,24");
        }
    }

    @Test
    public void testStoreLoadBeforePartialStore() throws Throwable {
        try (var log1=new TestLog();
             var log2=new CachePreprocessorLog(CacheType.BOTH, 128, true, log1)) {
            log1.assertLog();

            log2.access(0x0, 256, AccessType.STORE);
            log1.assertLog(
                    "A,0x0,0x80,S",
                    "A,0x80,0x80,S");

            log2.access(0xffff, 2, AccessType.STORE);
            log1.assertLog(
                    "A,0xff80,0x80,LD",
                    "A,0xffff,0x1,S",
                    "A,0x10000,0x80,LD",
                    "A,0x10000,0x1,S");

            log2.access(0x0, 3, AccessType.STORE);
            log1.assertLog(
                    "A,0x0,0x80,LD",
                    "A,0x0,0x2,S",
                    "A,0x0,0x80,LD",
                    "A,0x2,0x1,S");

            log2.access(0xffff, 130, AccessType.STORE);
            log1.assertLog(
                    "A,0xff80,0x80,LD",
                    "A,0xffff,0x1,S",
                    "A,0x10000,0x80,S",
                    "A,0x10080,0x80,LD",
                    "A,0x10080,0x1,S");
        }
    }

    @Test
    public void testStoreNoLoadBeforePartialStore() throws Throwable {
        try (var log1=new TestLog();
             var log2=new CachePreprocessorLog(CacheType.BOTH, 128, false, log1)) {
            log1.assertLog();

            log2.access(0x0, 256, AccessType.STORE);
            log1.assertLog(
                    "A,0x0,0x80,S",
                    "A,0x80,0x80,S");

            log2.access(0xffff, 2, AccessType.STORE);
            log1.assertLog(
                    "A,0xffff,0x1,S",
                    "A,0x10000,0x1,S");

            log2.access(0x0, 3, AccessType.STORE);
            log1.assertLog(
                    "A,0x0,0x2,S",
                    "A,0x2,0x1,S");

            log2.access(0xffff, 130, AccessType.STORE);
            log1.assertLog(
                    "A,0xffff,0x1,S",
                    "A,0x10000,0x80,S",
                    "A,0x10080,0x1,S");
        }
    }
}
