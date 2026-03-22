package dog.wiggler;

import dog.wiggler.function.Runnable;
import dog.wiggler.memory.IllegalMemoryAccessException;
import dog.wiggler.memory.MemoryMappedMemory;
import dog.wiggler.memory.MisalignedMemoryAccessException;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

@TestMethodOrder(MethodOrderer.MethodName.class)
public class MemoryMappedMemoryTest {
    @Test
    public void testAccessOutOfRange() throws Throwable {
        long size=1L<<12;
        try (var memory=MemoryMappedMemory.factory(false, size)
                .get()) {
            for (var runnable: List.<@NotNull Runnable>of(
                    ()->memory.loadInt16(-1L),
                    ()->memory.loadInt32(-1L, false),
                    ()->memory.loadInt64(-1L),
                    ()->memory.loadInt8(-1L),
                    ()->memory.storeInt16(-1L, (short)0),
                    ()->memory.storeInt32(-1L, 0),
                    ()->memory.storeInt64(-1L, 0L),
                    ()->memory.storeInt8(-1L, (byte)0),
                    ()->memory.loadInt16(size-1L),
                    ()->memory.loadInt32(size-3L, false),
                    ()->memory.loadInt64(size-7L),
                    ()->memory.loadInt8(size),
                    ()->memory.storeInt16(size-1L, (short)0),
                    ()->memory.storeInt32(size-3L, 0),
                    ()->memory.storeInt64(size-7L, 0L),
                    ()->memory.storeInt8(size, (byte)0))) {
                try {
                    runnable.run();
                    fail();
                }
                catch (IllegalMemoryAccessException ignore) {
                }
            }
        }
    }

    @Test
    public void testFile() throws Throwable {
        Path path=Files.createTempFile(
                Paths.get(".").toAbsolutePath(),
                "memory.",
                ".image");
        try {
            assertEquals(0L, Files.size(path));
            int size=1<<12;
            try (var memory=MemoryMappedMemory.factory(false, path, size)
                    .get()) {
                memory.storeInt8(0, (byte)1);
                memory.storeInt8(size-1, (byte)2);
                for (int ii=size-2; 0<ii; --ii) {
                    memory.storeInt8(ii, (byte)0);
                }
            }
            assertEquals(size, Files.size(path));
            byte[] buf=Files.readAllBytes(path);
            assertEquals((byte)1, buf[0]);
            assertEquals((byte)2, buf[size-1]);
            for (int ii=size-2; 0<ii; --ii) {
                assertEquals((byte)0, buf[ii]);
            }
            try (var memory=MemoryMappedMemory.factory(false, path, size)
                    .get()) {
                assertEquals((byte)1, memory.loadInt8(0));
                assertEquals((byte)2, memory.loadInt8(size-1));
                for (int ii=size-2; 0<ii; --ii) {
                    assertEquals((byte)0, memory.loadInt8(ii));
                }
            }
        }
        finally {
            Files.deleteIfExists(path);
        }
    }

    @Test
    public void testInvalidBufferBits() {
        for (var bufferBits: List.of(-1, 32)) {
            try {
                MemoryMappedMemory.factory(false, bufferBits, 1L<<12);
                fail();
            }
            catch (IllegalArgumentException ignore) {
            }
        }
    }

    @Test
    public void testLittleEndian() throws Throwable {
        for (var bufferBits: List.of(0, 20)) {
            try (var memory=MemoryMappedMemory.factory(false, bufferBits, 1L<<12)
                    .get()) {
                memory.storeInt64(0L, 0x0102030405060708L);
                assertEquals(0x0102030405060708L, memory.loadInt64(0L));
                assertEquals(0x05060708, memory.loadInt32(0L, false));
                assertEquals(0x01020304, memory.loadInt32(4L, false));
                assertEquals((short)0x0708, memory.loadInt16(0L));
                assertEquals((short)0x0506, memory.loadInt16(2L));
                assertEquals((short)0x0304, memory.loadInt16(4L));
                assertEquals((short)0x0102, memory.loadInt16(6L));
                assertEquals((byte)0x08, memory.loadInt8(0L));
                assertEquals((byte)0x07, memory.loadInt8(1L));
                assertEquals((byte)0x06, memory.loadInt8(2L));
                assertEquals((byte)0x05, memory.loadInt8(3L));
                assertEquals((byte)0x04, memory.loadInt8(4L));
                assertEquals((byte)0x03, memory.loadInt8(5L));
                assertEquals((byte)0x02, memory.loadInt8(6L));
                assertEquals((byte)0x01, memory.loadInt8(7L));

                memory.storeInt32(0L, 0x01020304);
                assertEquals(0x01020304, memory.loadInt32(0L, false));
                assertEquals((short)0x0304, memory.loadInt16(0L));
                assertEquals((short)0x0102, memory.loadInt16(2L));
                assertEquals((byte)0x04, memory.loadInt8(0L));
                assertEquals((byte)0x03, memory.loadInt8(1L));
                assertEquals((byte)0x02, memory.loadInt8(2L));
                assertEquals((byte)0x01, memory.loadInt8(3L));

                memory.storeInt16(0L, (short)0x0102);
                assertEquals((short)0x0102, memory.loadInt16(0L));
                assertEquals((byte)0x02, memory.loadInt8(0L));
                assertEquals((byte)0x01, memory.loadInt8(1L));
            }
        }
    }

    @Test
    public void testMisalignedAccess() throws Throwable {
        try (var memory=MemoryMappedMemory.factory(true, 1L<<12)
                .get()) {
            for (int ii=7; 0<=ii; --ii) {
                memory.storeInt64(ii, 17L*ii+ii);
                assertEquals(17L*ii+ii, memory.loadInt64(ii));
            }
            for (int ii=3; 0<=ii; --ii) {
                memory.storeInt32(ii, 17*ii+ii);
                assertEquals(17*ii+ii, memory.loadInt32(ii, false));
            }
            for (int ii=1; 0<=ii; --ii) {
                memory.storeInt16(ii, (short)(17*ii+ii));
                assertEquals((short)(17*ii+ii), memory.loadInt16(ii));
            }
        }
    }

    @Test
    public void testMisalignedAccessException() throws Throwable {
        try (var memory=MemoryMappedMemory.factory(false, 1L<<12)
                .get()) {
            @NotNull List<@NotNull Runnable> runnables=new ArrayList<>();
            for (var ii: IntStream.range(1, 8).toArray()) {
                runnables.add(()->memory.loadInt64(ii));
                runnables.add(()->memory.storeInt64(ii, 0L));
            }
            for (var ii: IntStream.range(1, 4).toArray()) {
                runnables.add(()->memory.loadInt32(ii, false));
                runnables.add(()->memory.storeInt32(ii, 0));
            }
            runnables.add(()->memory.loadInt16(1));
            runnables.add(()->memory.storeInt16(1, (short)0));
            for (var runnable: runnables) {
                try {
                    runnable.run();
                    fail();
                }
                catch (MisalignedMemoryAccessException ignore) {
                }
            }
        }
    }
}
