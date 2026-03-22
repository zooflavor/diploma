package dog.wiggler;

import dog.wiggler.elf.ELF;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.io.EOFException;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@TestMethodOrder(MethodOrderer.MethodName.class)
public class ELFTest {
    @Test
    public void testInvalidABI() throws Throwable {
        byte[] array=new byte[64];
        array[0]=0x7f;
        array[1]=0x45;
        array[2]=0x4c;
        array[3]=0x46;
        array[4]=0x02;
        array[5]=0x01;
        array[6]=0x01;
        array[7]=0x01;
        try (var channel=new ReadableMemoryChannel(array)) {
            try {
                ELF.read(channel);
                fail();
            }
            catch (IOException ex) {
                assertTrue(
                        (null!=ex.getMessage())
                                && ex.getMessage().contains("ABI"),
                        ex.toString());
            }
        }
    }

    @Test
    public void testInvalidClass() throws Throwable {
        byte[] array=new byte[64];
        array[0]=0x7f;
        array[1]=0x45;
        array[2]=0x4c;
        array[3]=0x46;
        try (var channel=new ReadableMemoryChannel(array)) {
            try {
                ELF.read(channel);
                fail();
            }
            catch (IOException ex) {
                assertTrue(
                        (null!=ex.getMessage())
                                && ex.getMessage().contains("class"),
                        ex.toString());
            }
        }
    }

    @Test
    public void testInvalidEndianness() throws Throwable {
        byte[] array=new byte[64];
        array[0]=0x7f;
        array[1]=0x45;
        array[2]=0x4c;
        array[3]=0x46;
        array[4]=0x02;
        try (var channel=new ReadableMemoryChannel(array)) {
            try {
                ELF.read(channel);
                fail();
            }
            catch (IOException ex) {
                assertTrue(
                        (null!=ex.getMessage())
                                && ex.getMessage().contains("endianness"),
                        ex.toString());
            }
        }
    }

    @Test
    public void testInvalidInstructionSet() throws Throwable {
        byte[] array=new byte[64];
        array[0]=0x7f;
        array[1]=0x45;
        array[2]=0x4c;
        array[3]=0x46;
        array[4]=0x02;
        array[5]=0x01;
        array[6]=0x01;
        array[0x10]=0x02;
        try (var channel=new ReadableMemoryChannel(array)) {
            try {
                ELF.read(channel);
                fail();
            }
            catch (IOException ex) {
                assertTrue(
                        (null!=ex.getMessage())
                                && ex.getMessage().contains("instruction set"),
                        ex.toString());
            }
        }
    }

    @Test
    public void testInvalidFileHeaderSize() throws Throwable {
        byte[] array=new byte[64];
        array[0]=0x7f;
        array[1]=0x45;
        array[2]=0x4c;
        array[3]=0x46;
        array[4]=0x02;
        array[5]=0x01;
        array[6]=0x01;
        array[0x10]=0x02;
        array[0x12]=(byte)0xf3;
        array[0x14]=0x01;
        try (var channel=new ReadableMemoryChannel(array)) {
            try {
                ELF.read(channel);
                fail();
            }
            catch (IOException ex) {
                assertTrue(
                        (null!=ex.getMessage())
                                && ex.getMessage().contains("file header size"),
                        ex.toString());
            }
        }
    }

    @Test
    public void testInvalidMagic() throws Throwable {
        try (var channel=new ReadableMemoryChannel(new byte[64])) {
            try {
                ELF.read(channel);
                fail();
            }
            catch (IOException ex) {
                assertTrue(
                        (null!=ex.getMessage())
                                && ex.getMessage().contains("invalid magic"),
                        ex.toString());
            }
        }
    }

    @Test
    public void testInvalidProgramHeaderEntrySize() throws Throwable {
        byte[] array=new byte[64];
        array[0]=0x7f;
        array[1]=0x45;
        array[2]=0x4c;
        array[3]=0x46;
        array[4]=0x02;
        array[5]=0x01;
        array[6]=0x01;
        array[0x10]=0x02;
        array[0x12]=(byte)0xf3;
        array[0x14]=0x01;
        array[0x34]=0x40;
        try (var channel=new ReadableMemoryChannel(array)) {
            try {
                ELF.read(channel);
                fail();
            }
            catch (IOException ex) {
                assertTrue(
                        (null!=ex.getMessage())
                                && ex.getMessage().contains("program header entry size"),
                        ex.toString());
            }
        }
    }

    @Test
    public void testInvalidSectionHeaderEntrySize() throws Throwable {
        byte[] array=new byte[64];
        array[0]=0x7f;
        array[1]=0x45;
        array[2]=0x4c;
        array[3]=0x46;
        array[4]=0x02;
        array[5]=0x01;
        array[6]=0x01;
        array[0x10]=0x02;
        array[0x12]=(byte)0xf3;
        array[0x14]=0x01;
        array[0x34]=0x40;
        array[0x36]=0x38;
        try (var channel=new ReadableMemoryChannel(array)) {
            try {
                ELF.read(channel);
                fail();
            }
            catch (IOException ex) {
                assertTrue(
                        (null!=ex.getMessage())
                                && ex.getMessage().contains("section header entry size"),
                        ex.toString());
            }
        }
    }

    @Test
    public void testInvalidType() throws Throwable {
        byte[] array=new byte[64];
        array[0]=0x7f;
        array[1]=0x45;
        array[2]=0x4c;
        array[3]=0x46;
        array[4]=0x02;
        array[5]=0x01;
        array[6]=0x01;
        try (var channel=new ReadableMemoryChannel(array)) {
            try {
                ELF.read(channel);
                fail();
            }
            catch (IOException ex) {
                assertTrue(
                        (null!=ex.getMessage())
                                && ex.getMessage().contains("executable"),
                        ex.toString());
            }
        }
    }

    @Test
    public void testInvalidVersion1() throws Throwable {
        byte[] array=new byte[64];
        array[0]=0x7f;
        array[1]=0x45;
        array[2]=0x4c;
        array[3]=0x46;
        array[4]=0x02;
        array[5]=0x01;
        try (var channel=new ReadableMemoryChannel(array)) {
            try {
                ELF.read(channel);
                fail();
            }
            catch (IOException ex) {
                assertTrue(
                        (null!=ex.getMessage())
                                && ex.getMessage().contains("version"),
                        ex.toString());
            }
        }
    }

    @Test
    public void testInvalidVersion2() throws Throwable {
        byte[] array=new byte[64];
        array[0]=0x7f;
        array[1]=0x45;
        array[2]=0x4c;
        array[3]=0x46;
        array[4]=0x02;
        array[5]=0x01;
        array[6]=0x01;
        array[0x10]=0x03;
        array[0x12]=(byte)0xf3;
        try (var channel=new ReadableMemoryChannel(array)) {
            try {
                ELF.read(channel);
                fail();
            }
            catch (IOException ex) {
                assertTrue(
                        (null!=ex.getMessage())
                                && ex.getMessage().contains("version"),
                        ex.toString());
            }
        }
    }

    @Test
    public void testMissingProgramHeader() throws Throwable {
        byte[] array=new byte[64];
        array[0]=0x7f;
        array[1]=0x45;
        array[2]=0x4c;
        array[3]=0x46;
        array[4]=0x02;
        array[5]=0x01;
        array[6]=0x01;
        array[0x10]=0x02;
        array[0x12]=(byte)0xf3;
        array[0x14]=0x01;
        array[0x20]=0x40;
        array[0x34]=0x40;
        array[0x36]=0x38;
        array[0x38]=0x01;
        array[0x3a]=0x40;
        try (var channel=new ReadableMemoryChannel(array)) {
            try {
                ELF.read(channel);
                fail();
            }
            catch (EOFException ignore) {
            }
        }
    }

    @Test
    public void testNoProgramHeadersNoSectionHeaders() throws Throwable {
        byte[] array=new byte[64];
        array[0]=0x7f;
        array[1]=0x45;
        array[2]=0x4c;
        array[3]=0x46;
        array[4]=0x02;
        array[5]=0x01;
        array[6]=0x01;
        array[0x10]=0x02;
        array[0x12]=(byte)0xf3;
        array[0x14]=0x01;
        array[0x20]=0x40;
        array[0x34]=0x40;
        array[0x36]=0x38;
        array[0x3a]=0x40;
        try (var channel=new ReadableMemoryChannel(array)) {
            var elf=ELF.read(channel);
            assertEquals(0, elf.programHeaders().size());
            assertEquals(0, elf.sectionHeaders().size());
        }
    }

    @Test
    public void testShortFile() throws Throwable {
        try (var channel=new ReadableMemoryChannel(new byte[0])) {
            try {
                ELF.read(channel);
                fail();
            }
            catch (IOException ex) {
                assertTrue(
                        (null!=ex.getMessage())
                                && ex.getMessage().contains("file too short"),
                        ex.toString());
            }
        }
    }
}
