package dog.wiggler.cache;

import dog.wiggler.Progress;
import dog.wiggler.memory.AccessType;
import dog.wiggler.memory.LogInputStream;
import dog.wiggler.memory.LogOutputStream;
import dog.wiggler.memory.Logs;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;

@TestMethodOrder(MethodOrderer.MethodName.class)
public class NWayAssociativeCacheTest {
    private Path inputLogPath;
    private Path outputLogPath;

    @AfterEach
    public void afterEach() throws Throwable {
        for (var path: Arrays.asList(inputLogPath, outputLogPath)) {
            Files.deleteIfExists(path);
        }
    }

    @BeforeEach
    public void beforeEach() throws Throwable {
        var dir=Paths.get(".").toAbsolutePath();
        inputLogPath=Files.createTempFile(dir, "test-input-", ".log");
        outputLogPath=Files.createTempFile(dir, "test-output-", ".log");
    }

    @Test
    public void test2WayAssociative() throws Throwable {
        try (var log=LogOutputStream.factory(inputLogPath)
                .get()) {
            log.access(8, 8, AccessType.LOAD_DATA);
            log.access(16, 8, AccessType.LOAD_DATA);
            log.access(24, 8, AccessType.LOAD_DATA);
            log.access(32, 8, AccessType.LOAD_DATA);
            log.access(8, 8, AccessType.LOAD_DATA);
            log.access(16, 8, AccessType.LOAD_DATA);
            log.access(24, 8, AccessType.LOAD_DATA);
            log.access(32, 8, AccessType.LOAD_DATA);
            log.access(40, 8, AccessType.LOAD_DATA);
            log.access(72, 8, AccessType.LOAD_DATA);
            log.access(40, 8, AccessType.LOAD_DATA);
            log.access(72, 8, AccessType.LOAD_DATA);
            log.access(104, 8, AccessType.LOAD_DATA);
            log.access(40, 8, AccessType.LOAD_DATA);
            log.access(72, 8, AccessType.LOAD_DATA);
            log.access(104, 8, AccessType.LOAD_DATA);
        }
        NWayAssociativeCache.run(
                2,
                4,
                CacheType.BOTH,
                inputLogPath,
                8,
                outputLogPath,
                Progress.NO_OP,
                LRUPolicy::new,
                WriteMiss.ALLOCATE,
                WritePolicy.WRITE_BACK);
        try (var log=LogInputStream.factory(outputLogPath)
                .get()) {
            assertEquals(
                    Logs.encodeAccess(8, 8, AccessType.LOAD_DATA),
                    log.readNext());
            assertEquals(
                    Logs.encodeAccess(16, 8, AccessType.LOAD_DATA),
                    log.readNext());
            assertEquals(
                    Logs.encodeAccess(24, 8, AccessType.LOAD_DATA),
                    log.readNext());
            assertEquals(
                    Logs.encodeAccess(32, 8, AccessType.LOAD_DATA),
                    log.readNext());
            assertEquals(
                    Logs.encodeAccess(40, 8, AccessType.LOAD_DATA),
                    log.readNext());
            assertEquals(
                    Logs.encodeAccess(72, 8, AccessType.LOAD_DATA),
                    log.readNext());
            assertEquals(
                    Logs.encodeAccess(104, 8, AccessType.LOAD_DATA),
                    log.readNext());
            assertEquals(
                    Logs.encodeAccess(40, 8, AccessType.LOAD_DATA),
                    log.readNext());
            assertEquals(
                    Logs.encodeAccess(72, 8, AccessType.LOAD_DATA),
                    log.readNext());
            assertEquals(
                    Logs.encodeAccess(104, 8, AccessType.LOAD_DATA),
                    log.readNext());
            assertFalse(log.hasNext());
        }
    }

    @Test
    public void testCacheLineNotMultipleOfAssociativity() throws Throwable {
        try {
            NWayAssociativeCache.run(
                    4,
                    2,
                    CacheType.BOTH,
                    inputLogPath,
                    8,
                    outputLogPath,
                    Progress.NO_OP,
                    LRUPolicy::new,
                    WriteMiss.ALLOCATE,
                    WritePolicy.WRITE_BACK);
            fail();
        }
        catch (IllegalArgumentException ignore) {
        }
    }

    @Test
    public void testCacheTypes() throws Throwable {
        try (var log=LogOutputStream.factory(inputLogPath)
                .get()) {
            log.access(8, 8, AccessType.LOAD_DATA);
            log.access(8, 8, AccessType.LOAD_DATA);
            log.access(8, 8, AccessType.LOAD_INSTRUCTION);
            log.access(8, 8, AccessType.LOAD_INSTRUCTION);
            log.access(8, 8, AccessType.STORE);
        }
        NWayAssociativeCache.run(
                2,
                4,
                CacheType.BOTH,
                inputLogPath,
                8,
                outputLogPath,
                Progress.NO_OP,
                LRUPolicy::new,
                WriteMiss.ALLOCATE,
                WritePolicy.WRITE_BACK);
        try (var log=LogInputStream.factory(outputLogPath)
                .get()) {
            assertEquals(
                    Logs.encodeAccess(8, 8, AccessType.LOAD_DATA),
                    log.readNext());
            assertFalse(log.hasNext());
        }
        NWayAssociativeCache.run(
                2,
                4,
                CacheType.DATA,
                inputLogPath,
                8,
                outputLogPath,
                Progress.NO_OP,
                LRUPolicy::new,
                WriteMiss.ALLOCATE,
                WritePolicy.WRITE_BACK);
        try (var log=LogInputStream.factory(outputLogPath)
                .get()) {
            assertEquals(
                    Logs.encodeAccess(8, 8, AccessType.LOAD_DATA),
                    log.readNext());
            assertEquals(
                    Logs.encodeAccess(8, 8, AccessType.LOAD_INSTRUCTION),
                    log.readNext());
            assertEquals(
                    Logs.encodeAccess(8, 8, AccessType.LOAD_INSTRUCTION),
                    log.readNext());
            assertFalse(log.hasNext());
        }
        NWayAssociativeCache.run(
                2,
                4,
                CacheType.INSTRUCTION,
                inputLogPath,
                8,
                outputLogPath,
                Progress.NO_OP,
                LRUPolicy::new,
                WriteMiss.ALLOCATE,
                WritePolicy.WRITE_BACK);
        try (var log=LogInputStream.factory(outputLogPath)
                .get()) {
            assertEquals(
                    Logs.encodeAccess(8, 8, AccessType.LOAD_DATA),
                    log.readNext());
            assertEquals(
                    Logs.encodeAccess(8, 8, AccessType.LOAD_DATA),
                    log.readNext());
            assertEquals(
                    Logs.encodeAccess(8, 8, AccessType.LOAD_INSTRUCTION),
                    log.readNext());
            assertEquals(
                    Logs.encodeAccess(8, 8, AccessType.STORE),
                    log.readNext());
            assertFalse(log.hasNext());
        }
    }

    @Test
    public void testDirectMapping() throws Throwable {
        try (var log=LogOutputStream.factory(inputLogPath)
                .get()) {
            log.access(8, 8, AccessType.LOAD_DATA);
            log.access(16, 8, AccessType.LOAD_DATA);
            log.access(24, 8, AccessType.LOAD_DATA);
            log.access(32, 8, AccessType.LOAD_DATA);
            log.access(8, 8, AccessType.LOAD_DATA);
            log.access(16, 8, AccessType.LOAD_DATA);
            log.access(24, 8, AccessType.LOAD_DATA);
            log.access(32, 8, AccessType.LOAD_DATA);
            log.access(40, 8, AccessType.LOAD_DATA);
            log.access(8, 8, AccessType.LOAD_DATA);
            log.access(40, 8, AccessType.LOAD_DATA);
            log.access(8, 8, AccessType.LOAD_DATA);
        }
        NWayAssociativeCache.run(
                1,
                4,
                CacheType.BOTH,
                inputLogPath,
                8,
                outputLogPath,
                Progress.NO_OP,
                LRUPolicy::new,
                WriteMiss.ALLOCATE,
                WritePolicy.WRITE_BACK);
        try (var log=LogInputStream.factory(outputLogPath)
                .get()) {
            assertEquals(
                    Logs.encodeAccess(8, 8, AccessType.LOAD_DATA),
                    log.readNext());
            assertEquals(
                    Logs.encodeAccess(16, 8, AccessType.LOAD_DATA),
                    log.readNext());
            assertEquals(
                    Logs.encodeAccess(24, 8, AccessType.LOAD_DATA),
                    log.readNext());
            assertEquals(
                    Logs.encodeAccess(32, 8, AccessType.LOAD_DATA),
                    log.readNext());
            assertEquals(
                    Logs.encodeAccess(40, 8, AccessType.LOAD_DATA),
                    log.readNext());
            assertEquals(
                    Logs.encodeAccess(8, 8, AccessType.LOAD_DATA),
                    log.readNext());
            assertEquals(
                    Logs.encodeAccess(40, 8, AccessType.LOAD_DATA),
                    log.readNext());
            assertEquals(
                    Logs.encodeAccess(8, 8, AccessType.LOAD_DATA),
                    log.readNext());
            assertFalse(log.hasNext());
        }
    }

    @Test
    public void testFullyAssociative() throws Throwable {
        try (var log=LogOutputStream.factory(inputLogPath)
                .get()) {
            log.access(8, 8, AccessType.LOAD_DATA);
            log.access(16, 8, AccessType.LOAD_DATA);
            log.access(24, 8, AccessType.LOAD_DATA);
            log.access(32, 8, AccessType.LOAD_DATA);
            log.access(8, 8, AccessType.LOAD_DATA);
            log.access(16, 8, AccessType.LOAD_DATA);
            log.access(24, 8, AccessType.LOAD_DATA);
            log.access(32, 8, AccessType.LOAD_DATA);
            log.access(40, 8, AccessType.LOAD_DATA);
            log.access(72, 8, AccessType.LOAD_DATA);
            log.access(104, 8, AccessType.LOAD_DATA);
            log.access(136, 8, AccessType.LOAD_DATA);
            log.access(40, 8, AccessType.LOAD_DATA);
            log.access(72, 8, AccessType.LOAD_DATA);
            log.access(104, 8, AccessType.LOAD_DATA);
            log.access(136, 8, AccessType.LOAD_DATA);
        }
        NWayAssociativeCache.run(
                4,
                4,
                CacheType.BOTH,
                inputLogPath,
                8,
                outputLogPath,
                Progress.NO_OP,
                LRUPolicy::new,
                WriteMiss.ALLOCATE,
                WritePolicy.WRITE_BACK);
        try (var log=LogInputStream.factory(outputLogPath)
                .get()) {
            assertEquals(
                    Logs.encodeAccess(8, 8, AccessType.LOAD_DATA),
                    log.readNext());
            assertEquals(
                    Logs.encodeAccess(16, 8, AccessType.LOAD_DATA),
                    log.readNext());
            assertEquals(
                    Logs.encodeAccess(24, 8, AccessType.LOAD_DATA),
                    log.readNext());
            assertEquals(
                    Logs.encodeAccess(32, 8, AccessType.LOAD_DATA),
                    log.readNext());
            assertEquals(
                    Logs.encodeAccess(40, 8, AccessType.LOAD_DATA),
                    log.readNext());
            assertEquals(
                    Logs.encodeAccess(72, 8, AccessType.LOAD_DATA),
                    log.readNext());
            assertEquals(
                    Logs.encodeAccess(104, 8, AccessType.LOAD_DATA),
                    log.readNext());
            assertEquals(
                    Logs.encodeAccess(136, 8, AccessType.LOAD_DATA),
                    log.readNext());
            assertFalse(log.hasNext());
        }
    }

    @Test
    public void testNonAccess() throws Throwable {
        try (var log=LogOutputStream.factory(inputLogPath)
                .get()) {
            log.access(8, 8, AccessType.LOAD_DATA);
            log.accessLogDisabled();
            log.accessLogEnabled();
            log.elapsedCycles(13L);
            log.userData(24L);
        }
        NWayAssociativeCache.run(
                4,
                4,
                CacheType.BOTH,
                inputLogPath,
                8,
                outputLogPath,
                Progress.NO_OP,
                LRUPolicy::new,
                WriteMiss.ALLOCATE,
                WritePolicy.WRITE_BACK);
        try (var log=LogInputStream.factory(outputLogPath)
                .get()) {
            assertEquals(
                    Logs.encodeAccess(8, 8, AccessType.LOAD_DATA),
                    log.readNext());
            assertEquals(
                    Logs.encodeAccessLogDisabled(),
                    log.readNext());
            assertEquals(
                    Logs.encodeAccessLogEnabled(),
                    log.readNext());
            assertEquals(
                    Logs.encodeElapsedCycles(13L),
                    log.readNext());
            assertEquals(
                    Logs.encodeUserData(24L),
                    log.readNext());
            assertFalse(log.hasNext());
        }
    }
}
