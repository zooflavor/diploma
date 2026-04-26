package dog.wiggler.cache;

import dog.wiggler.Progress;
import dog.wiggler.ReadableMemoryChannel;
import dog.wiggler.WritableMemoryChannel;
import dog.wiggler.memory.AccessType;
import dog.wiggler.memory.CollapseElapsedCyclesLog;
import dog.wiggler.memory.LogInputStream;
import dog.wiggler.memory.LogOutputStream;
import dog.wiggler.memory.LogVisitor;
import dog.wiggler.memory.Logs;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Random;
import java.util.TreeSet;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

@TestMethodOrder(MethodOrderer.MethodName.class)
public class OPTCacheTest {
    private Path inputLogPath;
    private Path outputLogPath1;
    private Path outputLogPath2;
    private Path tempPath;

    @AfterEach
    public void afterEach() throws Throwable {
        for (var path: Arrays.asList(inputLogPath, outputLogPath1, outputLogPath2, tempPath)) {
            Files.deleteIfExists(path);
        }
    }

    @BeforeEach
    public void beforeEach() throws Throwable {
        var dir=Paths.get(".").toAbsolutePath();
        inputLogPath=Files.createTempFile(dir, "test-input-", ".log");
        outputLogPath1=Files.createTempFile(dir, "test-output-1-", ".log");
        outputLogPath2=Files.createTempFile(dir, "test-output-2-", ".log");
        tempPath=Files.createTempFile(dir, "test-temp-", ".image");
    }

    private static <E extends Enum<E>> @NotNull E randomEnum(
            @NotNull Random random,
            @NotNull E @NotNull [] values) {
        return values[random.nextInt(values.length)];
    }

    @Test
    public void testData1() throws Throwable {
        try (var outputLog=LogOutputStream.factory(inputLogPath)
                .get()) {
            outputLog.accessLogDisabled();
            outputLog.accessLogEnabled();
            outputLog.elapsedCycles(13L);
            outputLog.userData(24L);
            outputLog.access(0x04L, 4, AccessType.LOAD_INSTRUCTION);
            outputLog.access(0xa0L, 1, AccessType.LOAD_DATA);
            outputLog.access(0xb0L, 2, AccessType.LOAD_DATA);
            outputLog.access(0xc0L, 4, AccessType.LOAD_DATA);
            outputLog.access(0xa0L, 8, AccessType.LOAD_DATA);
            outputLog.access(0xd0L, 4, AccessType.LOAD_DATA);
            outputLog.access(0xb0L, 2, AccessType.LOAD_DATA);
            outputLog.access(0xa0L, 1, AccessType.LOAD_DATA);
            outputLog.access(0xd0L, 2, AccessType.LOAD_DATA);
            outputLog.access(0xc0L, 4, AccessType.LOAD_DATA);
            outputLog.access(0xd0L, 8, AccessType.LOAD_DATA);
        }
        OPTCache.run(
                3,
                CacheType.DATA,
                inputLogPath,
                8,
                outputLogPath1,
                Progress.NO_OP,
                tempPath);
        testOPTCache(
                3,
                CacheType.DATA,
                inputLogPath,
                8,
                outputLogPath2);
        var log1=Files.readAllBytes(outputLogPath1);
        var log2=Files.readAllBytes(outputLogPath2);
        assertArrayEquals(log2, log1);
        try (var inputLog=LogInputStream.factory(outputLogPath1)
                .get()) {
            assertEquals(
                    Logs.encodeAccessLogDisabled(),
                    inputLog.readNext());
            assertEquals(
                    Logs.encodeAccessLogEnabled(),
                    inputLog.readNext());
            assertEquals(
                    Logs.encodeElapsedCycles(13L),
                    inputLog.readNext());
            assertEquals(
                    Logs.encodeUserData(24L),
                    inputLog.readNext());
            assertEquals(
                    Logs.encodeAccess(0x04L, 4, AccessType.LOAD_INSTRUCTION),
                    inputLog.readNext());
            assertEquals(
                    Logs.encodeAccess(0xa0L, 8, AccessType.LOAD_DATA),
                    inputLog.readNext());
            assertEquals(
                    Logs.encodeAccess(0xb0L, 8, AccessType.LOAD_DATA),
                    inputLog.readNext());
            assertEquals(
                    Logs.encodeAccess(0xc0L, 8, AccessType.LOAD_DATA),
                    inputLog.readNext());
            assertEquals(
                    Logs.encodeAccess(0xd0L, 8, AccessType.LOAD_DATA),
                    inputLog.readNext());
            assertEquals(
                    Logs.encodeAccess(0xc0L, 8, AccessType.LOAD_DATA),
                    inputLog.readNext());
            assertFalse(inputLog.hasNext());
        }
    }

    @Test
    public void testData2() throws Throwable {
        try (var outputLog=LogOutputStream.factory(inputLogPath)
                .get()) {
            outputLog.access(0xa0L, 1, AccessType.LOAD_DATA);
            outputLog.access(0xb0L, 2, AccessType.LOAD_DATA);
            outputLog.access(0xc0L, 4, AccessType.LOAD_DATA);
            outputLog.access(0xa0L, 8, AccessType.LOAD_DATA);
            outputLog.access(0xd0L, 4, AccessType.LOAD_DATA);
            outputLog.access(0xb0L, 2, AccessType.LOAD_DATA);
            outputLog.access(0xa0L, 1, AccessType.LOAD_DATA);
            outputLog.access(0xd0L, 2, AccessType.LOAD_DATA);
            outputLog.access(0xc0L, 4, AccessType.LOAD_DATA);
            outputLog.access(0xd0L, 8, AccessType.LOAD_DATA);
        }
        OPTCache.run(
                2,
                CacheType.DATA,
                inputLogPath,
                8,
                outputLogPath1,
                Progress.NO_OP,
                tempPath);
        testOPTCache(
                2,
                CacheType.DATA,
                inputLogPath,
                8,
                outputLogPath2);
        var log1=Files.readAllBytes(outputLogPath1);
        var log2=Files.readAllBytes(outputLogPath2);
        assertArrayEquals(log2, log1);
        try (var inputLog=LogInputStream.factory(outputLogPath1)
                .get()) {
            assertEquals(
                    Logs.encodeAccess(0xa0L, 8, AccessType.LOAD_DATA),
                    inputLog.readNext());
            assertEquals(
                    Logs.encodeAccess(0xb0L, 8, AccessType.LOAD_DATA),
                    inputLog.readNext());
            assertEquals(
                    Logs.encodeAccess(0xc0L, 8, AccessType.LOAD_DATA),
                    inputLog.readNext());
            assertEquals(
                    Logs.encodeAccess(0xd0L, 8, AccessType.LOAD_DATA),
                    inputLog.readNext());
            assertEquals(
                    Logs.encodeAccess(0xb0L, 8, AccessType.LOAD_DATA),
                    inputLog.readNext());
            assertEquals(
                    Logs.encodeAccess(0xd0L, 8, AccessType.LOAD_DATA),
                    inputLog.readNext());
            assertEquals(
                    Logs.encodeAccess(0xc0L, 8, AccessType.LOAD_DATA),
                    inputLog.readNext());
            assertFalse(inputLog.hasNext());
        }
    }

    @Test
    public void testData3() throws Throwable {
        try (var outputLog=LogOutputStream.factory(inputLogPath)
                .get()) {
            outputLog.access(0xa0L, 8, AccessType.LOAD_DATA);
            outputLog.access(0xb0L, 8, AccessType.LOAD_DATA);
            outputLog.access(0xc0L, 8, AccessType.STORE);
            outputLog.access(0xd0L, 8, AccessType.LOAD_DATA);
            outputLog.access(0xa0L, 8, AccessType.LOAD_DATA);
            outputLog.access(0xb0L, 8, AccessType.LOAD_DATA);
            outputLog.access(0xc0L, 8, AccessType.STORE);
        }
        OPTCache.run(
                2,
                CacheType.DATA,
                inputLogPath,
                8,
                outputLogPath1,
                Progress.NO_OP,
                tempPath);
        testOPTCache(
                2,
                CacheType.DATA,
                inputLogPath,
                8,
                outputLogPath2);
        var log1=Files.readAllBytes(outputLogPath1);
        var log2=Files.readAllBytes(outputLogPath2);
        assertArrayEquals(log2, log1);
        try (var inputLog=LogInputStream.factory(outputLogPath1)
                .get()) {
            assertEquals(
                    Logs.encodeAccess(0xa0L, 8, AccessType.LOAD_DATA),
                    inputLog.readNext());
            assertEquals(
                    Logs.encodeAccess(0xb0L, 8, AccessType.LOAD_DATA),
                    inputLog.readNext());
            assertEquals(
                    Logs.encodeAccess(0xc0L, 8, AccessType.STORE),
                    inputLog.readNext());
            assertEquals(
                    Logs.encodeAccess(0xd0L, 8, AccessType.LOAD_DATA),
                    inputLog.readNext());
            assertEquals(
                    Logs.encodeAccess(0xb0L, 8, AccessType.LOAD_DATA),
                    inputLog.readNext());
            assertFalse(inputLog.hasNext());
        }
    }

    @Test
    public void testData4() throws Throwable {
        try (var outputLog=LogOutputStream.factory(inputLogPath)
                .get()) {
            outputLog.access(0xa0L, 8, AccessType.LOAD_DATA);
            outputLog.access(0xb0L, 8, AccessType.LOAD_DATA);
            outputLog.access(0xa0L, 8, AccessType.LOAD_DATA);
        }
        OPTCache.run(
                1,
                CacheType.DATA,
                inputLogPath,
                8,
                outputLogPath1,
                Progress.NO_OP,
                tempPath);
        testOPTCache(
                1,
                CacheType.DATA,
                inputLogPath,
                8,
                outputLogPath2);
        var log1=Files.readAllBytes(outputLogPath1);
        var log2=Files.readAllBytes(outputLogPath2);
        assertArrayEquals(log2, log1);
        try (var inputLog=LogInputStream.factory(outputLogPath1)
                .get()) {
            assertEquals(
                    Logs.encodeAccess(0xa0L, 8, AccessType.LOAD_DATA),
                    inputLog.readNext());
            assertEquals(
                    Logs.encodeAccess(0xb0L, 8, AccessType.LOAD_DATA),
                    inputLog.readNext());
            assertEquals(
                    Logs.encodeAccess(0xa0L, 8, AccessType.LOAD_DATA),
                    inputLog.readNext());
            assertFalse(inputLog.hasNext());
        }
    }

    @Test
    public void testData5() throws Throwable {
        try (var outputLog=LogOutputStream.factory(inputLogPath)
                .get()) {
            outputLog.access(0xa0L, 8, AccessType.LOAD_DATA);
            outputLog.access(0xb0L, 8, AccessType.LOAD_DATA);
            outputLog.access(0xb0L, 8, AccessType.STORE);
            outputLog.access(0xc0L, 8, AccessType.LOAD_DATA);
        }
        OPTCache.run(
                2,
                CacheType.DATA,
                inputLogPath,
                8,
                outputLogPath1,
                Progress.NO_OP,
                tempPath);
        testOPTCache(
                2,
                CacheType.DATA,
                inputLogPath,
                8,
                outputLogPath2);
        var log1=Files.readAllBytes(outputLogPath1);
        var log2=Files.readAllBytes(outputLogPath2);
        assertArrayEquals(log2, log1);
        try (var inputLog=LogInputStream.factory(outputLogPath1)
                .get()) {
            assertEquals(
                    Logs.encodeAccess(0xa0L, 8, AccessType.LOAD_DATA),
                    inputLog.readNext());
            assertEquals(
                    Logs.encodeAccess(0xb0L, 8, AccessType.LOAD_DATA),
                    inputLog.readNext());
            assertEquals(
                    Logs.encodeAccess(0xb0L, 8, AccessType.STORE),
                    inputLog.readNext());
            assertEquals(
                    Logs.encodeAccess(0xc0L, 8, AccessType.LOAD_DATA),
                    inputLog.readNext());
            assertFalse(inputLog.hasNext());
        }
    }

    @Test
    public void testData6() throws Throwable {
        try (var outputLog=LogOutputStream.factory(inputLogPath)
                .get()) {
            outputLog.access(0xa0L, 8, AccessType.LOAD_DATA);
            outputLog.access(0xb0L, 8, AccessType.LOAD_DATA);
            outputLog.access(0xc0L, 8, AccessType.LOAD_DATA);
            outputLog.access(0xa0L, 8, AccessType.LOAD_DATA);
            outputLog.access(0xd0L, 8, AccessType.LOAD_DATA);
            outputLog.access(0xc0L, 8, AccessType.LOAD_DATA);
            outputLog.access(0xe0L, 8, AccessType.LOAD_DATA);
            outputLog.access(0xb0L, 8, AccessType.LOAD_DATA);
        }
        OPTCache.run(
                3,
                CacheType.DATA,
                inputLogPath,
                8,
                outputLogPath1,
                Progress.NO_OP,
                tempPath);
        testOPTCache(
                3,
                CacheType.DATA,
                inputLogPath,
                8,
                outputLogPath2);
        var log1=Files.readAllBytes(outputLogPath1);
        var log2=Files.readAllBytes(outputLogPath2);
        assertArrayEquals(log2, log1);
        try (var inputLog=LogInputStream.factory(outputLogPath1)
                .get()) {
            assertEquals(
                    Logs.encodeAccess(0xa0L, 8, AccessType.LOAD_DATA),
                    inputLog.readNext());
            assertEquals(
                    Logs.encodeAccess(0xb0L, 8, AccessType.LOAD_DATA),
                    inputLog.readNext());
            assertEquals(
                    Logs.encodeAccess(0xc0L, 8, AccessType.LOAD_DATA),
                    inputLog.readNext());
            assertEquals(
                    Logs.encodeAccess(0xd0L, 8, AccessType.LOAD_DATA),
                    inputLog.readNext());
            assertEquals(
                    Logs.encodeAccess(0xe0L, 8, AccessType.LOAD_DATA),
                    inputLog.readNext());
            assertFalse(inputLog.hasNext());
        }
    }

    @Test
    public void testData7() throws Throwable {
        try (var outputLog=LogOutputStream.factory(inputLogPath)
                .get()) {
            outputLog.access(0xa0L, 8, AccessType.LOAD_DATA);
            outputLog.access(0xb0L, 8, AccessType.LOAD_DATA);
            outputLog.access(0xc0L, 8, AccessType.LOAD_DATA);
            outputLog.access(0xa0L, 8, AccessType.LOAD_DATA);
            outputLog.access(0xc0L, 8, AccessType.LOAD_DATA);
            outputLog.access(0xd0L, 8, AccessType.LOAD_DATA);
            outputLog.access(0xb0L, 8, AccessType.LOAD_DATA);
        }
        OPTCache.run(
                3,
                CacheType.DATA,
                inputLogPath,
                8,
                outputLogPath1,
                Progress.NO_OP,
                tempPath);
        testOPTCache(
                3,
                CacheType.DATA,
                inputLogPath,
                8,
                outputLogPath2);
        var log1=Files.readAllBytes(outputLogPath1);
        var log2=Files.readAllBytes(outputLogPath2);
        assertArrayEquals(log2, log1);
        try (var inputLog=LogInputStream.factory(outputLogPath1)
                .get()) {
            assertEquals(
                    Logs.encodeAccess(0xa0L, 8, AccessType.LOAD_DATA),
                    inputLog.readNext());
            assertEquals(
                    Logs.encodeAccess(0xb0L, 8, AccessType.LOAD_DATA),
                    inputLog.readNext());
            assertEquals(
                    Logs.encodeAccess(0xc0L, 8, AccessType.LOAD_DATA),
                    inputLog.readNext());
            assertEquals(
                    Logs.encodeAccess(0xd0L, 8, AccessType.LOAD_DATA),
                    inputLog.readNext());
            assertFalse(inputLog.hasNext());
        }
    }

    @Test
    public void testError() throws Throwable {
        try {
            OPTCache.run(
                    0,
                    CacheType.BOTH,
                    inputLogPath,
                    8,
                    outputLogPath1,
                    Progress.NO_OP,
                    tempPath);
            fail();
        }
        catch (IllegalArgumentException ignore) {
        }
    }

    private static void testOPTCache(
            int cacheSizeInLine,
            @NotNull CacheType cacheType,
            @NotNull Path inputLogPath,
            int lineSizeInBytes,
            @NotNull Path outputLogPath)
            throws Throwable {
        byte[] preprocessedLog;
        try (var inputLog=LogInputStream.factory(inputLogPath)
                .get();
             var memoryChannel=new WritableMemoryChannel()) {
            try (var outputLog=CachePreprocessorLog.factory(
                            cacheType,
                            lineSizeInBytes,
                            true,
                            CollapseElapsedCyclesLog.factory(
                                    LogOutputStream.factory(
                                            ()->memoryChannel)))
                    .get()) {
                while (inputLog.hasNext()) {
                    inputLog.readNext(outputLog);
                }
                outputLog.end();
            }
            preprocessedLog=memoryChannel.toByteArray();
        }
        @NotNull List<@NotNull Long> log=new ArrayList<>();
        try (var inputLog=LogInputStream.factory(
                        ()->new ReadableMemoryChannel(preprocessedLog))
                .get()) {
            while (inputLog.hasNext()) {
                log.add(inputLog.readNext());
            }
        }
        @NotNull Map<@NotNull Long, @NotNull NavigableSet<@NotNull Integer>> accessTimes=new HashMap<>();
        for (int logIndex: IntStream.range(0, log.size()).toArray()) {
            long logData=log.get(logIndex);
            Logs.visit(
                    logData,
                    new LogVisitor<Void>() {
                        @Override
                        public Void access(long address, int size, @NotNull AccessType type) {
                            if (cacheType.notCached(type)) {
                                return null;
                            }
                            address&=(-lineSizeInBytes);
                            accessTimes.computeIfAbsent(address, (key)->new TreeSet<>())
                                    .add(logIndex);
                            return null;
                        }

                        @Override
                        public Void accessLogDisabled() {
                            return null;
                        }

                        @Override
                        public Void accessLogEnabled() {
                            return null;
                        }

                        @Override
                        public Void elapsedCycles(long elapsedCycles) {
                            return null;
                        }

                        @Override
                        public Void end() {
                            return null;
                        }

                        @Override
                        public Void userData(long userData) {
                            return null;
                        }
                    });
        }
        try (var outputLog=CollapseElapsedCyclesLog.factory(
                        LogOutputStream.factory(outputLogPath))
                .get()) {
            @NotNull Map<@NotNull Long, @NotNull Boolean> cache=new HashMap<>();
            for (int logIndex: IntStream.range(0, log.size()).toArray()) {
                long logData=log.get(logIndex);
                Logs.visit(
                        logData,
                        new LogVisitor<Void>() {
                            @Override
                            public Void access(long address, int size, @NotNull AccessType type) throws Throwable {
                                if (cacheType.notCached(type)) {
                                    return outputLog.access(address, size, type);
                                }
                                address&=(-lineSizeInBytes);
                                if (cache.containsKey(address)) {
                                    if (type.store()) {
                                        cache.put(address, true);
                                    }
                                }
                                else {
                                    if (cacheSizeInLine<=cache.size()) {
                                        @Nullable Long evictAddress=null;
                                        int evictNextAccessTime=-1;
                                        for (var cachedAddress: cache.keySet()) {
                                            var accessTimes2=accessTimes.get(cachedAddress);
                                            while ((!accessTimes2.isEmpty()) && (logIndex>=accessTimes2.first())) {
                                                accessTimes2.removeFirst();
                                            }
                                            int nextAccessTime=(accessTimes2.isEmpty())
                                                    ?Integer.MAX_VALUE
                                                    :accessTimes2.first();
                                            if ((nextAccessTime>evictNextAccessTime)
                                                    || ((nextAccessTime==evictNextAccessTime)
                                                    && (null!=evictAddress)
                                                    && (cachedAddress>evictAddress))) {
                                                evictAddress=cachedAddress;
                                                evictNextAccessTime=nextAccessTime;
                                            }
                                        }
                                        assertNotNull(evictAddress);
                                        if (cache.remove(evictAddress)) {
                                            outputLog.access(evictAddress, lineSizeInBytes, AccessType.STORE);
                                        }
                                    }
                                    if (type.load()) {
                                        cache.put(address, false);
                                        outputLog.access(address, lineSizeInBytes, type);
                                    }
                                    else {
                                        cache.put(address, true);
                                    }
                                }
                                return null;
                            }

                            @Override
                            public Void accessLogDisabled() throws Throwable {
                                outputLog.accessLogDisabled();
                                return null;
                            }

                            @Override
                            public Void accessLogEnabled() throws Throwable {
                                outputLog.accessLogEnabled();
                                return null;
                            }

                            @Override
                            public Void elapsedCycles(long elapsedCycles) {
                                outputLog.elapsedCycles(elapsedCycles);
                                return null;
                            }

                            @Override
                            public Void end() {
                                return null;
                            }

                            @Override
                            public Void userData(long userData) throws Throwable {
                                outputLog.userData(userData);
                                return null;
                            }
                        });
            }
            outputLog.end();
        }
    }

    @Test
    public void testRandom() throws Throwable {
        Random random=new Random(23534539383474L);
        for (int iterations=16; 0<iterations; --iterations) {
            for (var compressedAddresses: List.of(false, true)) {
                Files.deleteIfExists(inputLogPath);
                try (var inputLog=LogOutputStream.factory(inputLogPath)
                        .get()) {
                    long elapsedCycles=0L;
                    for (int logEntries=random.nextInt(1024, 2048); 0<logEntries; --logEntries) {
                        inputLog.elapsedCycles(elapsedCycles);
                        if (0==random.nextInt(16)) {
                            switch (random.nextInt(8)) {
                                case 0 -> inputLog.accessLogDisabled();
                                case 1 -> inputLog.accessLogEnabled();
                                default -> inputLog.userData(random.nextLong()&0x1fffffffffffffffL);
                            }
                        }
                        else {
                            int size=1<<random.nextInt(4);
                            long address=random.nextInt(512);
                            if (compressedAddresses) {
                                address&=(1L<<random.nextInt(9))-1L;
                            }
                            address&=-size;
                            inputLog.access(address, size, randomEnum(random, AccessType.values()));
                        }
                        ++elapsedCycles;
                    }
                }
                for (var cacheSize: List.of(1, 2, 4, 8, 16)) {
                    for (var cacheType: List.of(CacheType.BOTH, CacheType.DATA, CacheType.INSTRUCTION)) {
                        for (var lineSize: List.of(8, 16, 32)) {
                            Files.deleteIfExists(outputLogPath1);
                            Files.deleteIfExists(outputLogPath2);
                            Files.deleteIfExists(tempPath);
                            OPTCache.run(
                                    cacheSize,
                                    cacheType,
                                    inputLogPath,
                                    lineSize,
                                    outputLogPath1,
                                    Progress.NO_OP,
                                    tempPath);
                            testOPTCache(
                                    cacheSize,
                                    cacheType,
                                    inputLogPath,
                                    lineSize,
                                    outputLogPath2);
                            var log1=Files.readAllBytes(outputLogPath1);
                            var log2=Files.readAllBytes(outputLogPath2);
                            assertArrayEquals(log2, log1);
                        }
                    }
                }
            }
        }
    }
}
