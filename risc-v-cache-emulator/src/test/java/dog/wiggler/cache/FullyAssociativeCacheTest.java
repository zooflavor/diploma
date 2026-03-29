package dog.wiggler.cache;

import dog.wiggler.memory.AccessType;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static org.junit.jupiter.api.Assertions.fail;

@TestMethodOrder(MethodOrderer.MethodName.class)
public class FullyAssociativeCacheTest {
    @Test
    public void testAccessSizeTooLarge() throws Throwable {
        var cache=new FullyAssociativeCache(
                3,
                8,
                new TestLog(),
                new LRUPolicy(),
                WriteMiss.ALLOCATE,
                WritePolicy.WRITE_BACK);
        try {
            cache.access(0, 9, AccessType.LOAD_DATA);
            fail();
        }
        catch (IllegalArgumentException ignore) {
        }
    }

    @Test
    public void testEvict() throws Throwable {
        var log=new TestLog();
        var cache=new FullyAssociativeCache(
                2,
                8,
                log,
                new LRUPolicy(),
                WriteMiss.ALLOCATE,
                WritePolicy.WRITE_BACK);
        log.assertLog();

        cache.access(0, 8, AccessType.LOAD_DATA);
        log.assertLog(
                "A,0x0,0x8,LD");
        cache.access(8, 8, AccessType.LOAD_DATA);
        log.assertLog(
                "A,0x8,0x8,LD");
        cache.access(16, 8, AccessType.LOAD_DATA);
        log.assertLog(
                "A,0x10,0x8,LD");

        cache.access(8, 8, AccessType.LOAD_DATA);
        log.assertLog();
        cache.access(16, 8, AccessType.LOAD_DATA);
        log.assertLog();

        cache.access(0, 8, AccessType.LOAD_DATA);
        log.assertLog(
                "A,0x0,0x8,LD");
    }

    @Test
    public void testEvictDirty() throws Throwable {
        var log=new TestLog();
        var cache=new FullyAssociativeCache(
                2,
                8,
                log,
                new LRUPolicy(),
                WriteMiss.ALLOCATE,
                WritePolicy.WRITE_BACK);
        log.assertLog();

        cache.access(0, 8, AccessType.STORE);
        log.assertLog();
        
        cache.access(8, 8, AccessType.LOAD_DATA);
        log.assertLog(
                "A,0x8,0x8,LD");

        cache.access(16, 8, AccessType.LOAD_DATA);
        log.assertLog(
                "A,0x0,0x8,S",
                "A,0x10,0x8,LD");
    }

    @Test
    public void testInvalidCacheLines() {
        try {
            new FullyAssociativeCache(
                    0,
                    8,
                    new TestLog(),
                    new LRUPolicy(),
                    WriteMiss.ALLOCATE,
                    WritePolicy.WRITE_BACK);
            fail();
        }
        catch (IllegalArgumentException ignore) {
        }
    }

    @Test
    public void testInvalidLineSize() {
        try {
            new FullyAssociativeCache(
                    1,
                    7,
                    new TestLog(),
                    new LRUPolicy(),
                    WriteMiss.ALLOCATE,
                    WritePolicy.WRITE_BACK);
            fail();
        }
        catch (IllegalArgumentException ignore) {
        }
    }

    @Test
    public void testLoadHit() throws Throwable {
        var log=new TestLog();
        var cache=new FullyAssociativeCache(
                3,
                8,
                log,
                new LRUPolicy(),
                WriteMiss.ALLOCATE,
                WritePolicy.WRITE_BACK);
        log.assertLog();

        cache.access(0, 8, AccessType.LOAD_DATA);
        log.assertLog(
                "A,0x0,0x8,LD");

        cache.access(8, 8, AccessType.LOAD_DATA);
        log.assertLog(
                "A,0x8,0x8,LD");

        cache.access(0, 8, AccessType.LOAD_INSTRUCTION);
        log.assertLog();
    }

    @Test
    public void testMisalignedLoad() throws Throwable {
        var cache=new FullyAssociativeCache(
                3,
                8,
                new TestLog(),
                new LRUPolicy(),
                WriteMiss.ALLOCATE,
                WritePolicy.WRITE_BACK);
        try {
            cache.access(1, 8, AccessType.LOAD_DATA);
            fail();
        }
        catch (IllegalArgumentException ignore) {
        }
    }

    @Test
    public void testPartialLoad() throws Throwable {
        var cache=new FullyAssociativeCache(
                3,
                8,
                new TestLog(),
                new LRUPolicy(),
                WriteMiss.ALLOCATE,
                WritePolicy.WRITE_BACK);
        try {
            cache.access(0, 1, AccessType.LOAD_DATA);
            fail();
        }
        catch (IllegalArgumentException ignore) {
        }
    }

    @Test
    public void testPartialStoreLoads() throws Throwable {
        var log=new TestLog();
        var cache=new FullyAssociativeCache(
                2,
                8,
                log,
                new LRUPolicy(),
                WriteMiss.ALLOCATE,
                WritePolicy.WRITE_BACK);
        log.assertLog();

        cache.access(1, 1, AccessType.STORE);
        log.assertLog(
                "A,0x0,0x8,LD");

        cache.access(8, 8, AccessType.LOAD_DATA);
        log.assertLog(
                "A,0x8,0x8,LD");

        cache.access(16, 8, AccessType.LOAD_DATA);
        log.assertLog(
                "A,0x0,0x8,S",
                "A,0x10,0x8,LD");
    }

    @Test
    public void testStoreHitWriteBack() throws Throwable {
        var log=new TestLog();
        var cache=new FullyAssociativeCache(
                2,
                8,
                log,
                new LRUPolicy(),
                WriteMiss.ALLOCATE,
                WritePolicy.WRITE_BACK);
        log.assertLog();

        cache.access(0, 8, AccessType.LOAD_DATA);
        log.assertLog(
                "A,0x0,0x8,LD");

        cache.access(0, 8, AccessType.STORE);
        log.assertLog();
        
        cache.access(0, 8, AccessType.LOAD_DATA);
        log.assertLog();

        cache.access(8, 8, AccessType.LOAD_DATA);
        log.assertLog(
                "A,0x8,0x8,LD");

        cache.access(16, 8, AccessType.LOAD_DATA);
        log.assertLog(
                "A,0x0,0x8,S",
                "A,0x10,0x8,LD");
    }

    @Test
    public void testStoreHitWriteThrough() throws Throwable {
        var log=new TestLog();
        var cache=new FullyAssociativeCache(
                2,
                8,
                log,
                new LRUPolicy(),
                WriteMiss.ALLOCATE,
                WritePolicy.WRITE_THROUGH);
        log.assertLog();

        cache.access(0, 8, AccessType.LOAD_DATA);
        log.assertLog(
                "A,0x0,0x8,LD");

        cache.access(0, 8, AccessType.STORE);
        log.assertLog(
                "A,0x0,0x8,S");

        cache.access(0, 8, AccessType.LOAD_DATA);
        log.assertLog();

        cache.access(8, 8, AccessType.LOAD_DATA);
        log.assertLog(
                "A,0x8,0x8,LD");

        cache.access(16, 8, AccessType.LOAD_DATA);
        log.assertLog(
                "A,0x10,0x8,LD");
    }

    @Test
    public void testStoreMissAllocateWriteBack() throws Throwable {
        var log=new TestLog();
        var cache=new FullyAssociativeCache(
                2,
                8,
                log,
                new LRUPolicy(),
                WriteMiss.ALLOCATE,
                WritePolicy.WRITE_BACK);
        log.assertLog();

        cache.access(0, 8, AccessType.LOAD_DATA);
        log.assertLog(
                "A,0x0,0x8,LD");

        cache.access(8, 8, AccessType.LOAD_DATA);
        log.assertLog(
                "A,0x8,0x8,LD");

        cache.access(16, 8, AccessType.STORE);
        log.assertLog();

        cache.access(16, 8, AccessType.LOAD_DATA);
        log.assertLog();

        cache.access(8, 8, AccessType.LOAD_DATA);
        log.assertLog();

        cache.access(0, 8, AccessType.LOAD_DATA);
        log.assertLog(
                "A,0x10,0x8,S",
                "A,0x0,0x8,LD");
    }

    @Test
    public void testStoreMissAllocateWriteThrough() throws Throwable {
        var log=new TestLog();
        var cache=new FullyAssociativeCache(
                2,
                8,
                log,
                new LRUPolicy(),
                WriteMiss.ALLOCATE,
                WritePolicy.WRITE_THROUGH);
        log.assertLog();

        cache.access(0, 8, AccessType.LOAD_DATA);
        log.assertLog(
                "A,0x0,0x8,LD");

        cache.access(8, 8, AccessType.LOAD_DATA);
        log.assertLog(
                "A,0x8,0x8,LD");

        cache.access(16, 8, AccessType.STORE);
        log.assertLog(
                "A,0x10,0x8,S");

        cache.access(16, 8, AccessType.LOAD_DATA);
        log.assertLog();

        cache.access(8, 8, AccessType.LOAD_DATA);
        log.assertLog();

        cache.access(0, 8, AccessType.LOAD_DATA);
        log.assertLog(
                "A,0x0,0x8,LD");
    }

    @Test
    public void testStoreMissAllocateWriteThroughPartialStore() throws Throwable {
        var log=new TestLog();
        var cache=new FullyAssociativeCache(
                2,
                8,
                log,
                new LRUPolicy(),
                WriteMiss.ALLOCATE,
                WritePolicy.WRITE_THROUGH);
        log.assertLog();

        cache.access(0, 8, AccessType.LOAD_DATA);
        log.assertLog(
                "A,0x0,0x8,LD");

        cache.access(8, 8, AccessType.LOAD_DATA);
        log.assertLog(
                "A,0x8,0x8,LD");

        cache.access(16, 1, AccessType.STORE);
        log.assertLog(
                "A,0x10,0x8,LD",
                "A,0x10,0x1,S");

        cache.access(16, 8, AccessType.LOAD_DATA);
        log.assertLog();

        cache.access(8, 8, AccessType.LOAD_DATA);
        log.assertLog();

        cache.access(0, 8, AccessType.LOAD_DATA);
        log.assertLog(
                "A,0x0,0x8,LD");
    }

    @Test
    public void testStoreMissDontAllocate() throws Throwable {
        var log=new TestLog();
        var cache=new FullyAssociativeCache(
                2,
                8,
                log,
                new LRUPolicy(),
                WriteMiss.DON_T_ALLOCATE,
                WritePolicy.WRITE_THROUGH);
        log.assertLog();

        cache.access(0, 8, AccessType.LOAD_DATA);
        log.assertLog(
                "A,0x0,0x8,LD");

        cache.access(8, 8, AccessType.LOAD_DATA);
        log.assertLog(
                "A,0x8,0x8,LD");

        cache.access(16, 8, AccessType.STORE);
        log.assertLog(
                "A,0x10,0x8,S");

        cache.access(0, 8, AccessType.LOAD_DATA);
        log.assertLog();

        cache.access(8, 8, AccessType.LOAD_DATA);
        log.assertLog();

        cache.access(16, 8, AccessType.LOAD_DATA);
        log.assertLog(
                "A,0x10,0x8,LD");
    }
}
