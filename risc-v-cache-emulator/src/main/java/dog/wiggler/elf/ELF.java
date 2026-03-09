package dog.wiggler.elf;

import dog.wiggler.function.Function;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ELF {
    private ELF() {
    }

    public static FileHeader read(SeekableByteChannel channel) throws Throwable {
        if (64>channel.size()) {
            throw new IOException("file too short for a 64 bit ELF header");
        }
        long originalPosition=channel.position();
        try {
            ByteBuffer buffer=ByteBuffer.allocate(4096)
                    .order(ByteOrder.LITTLE_ENDIAN);
            readFully(buffer, channel, 64, 0L);
            if (0x464c457f!=buffer.getInt()) {
                throw new IOException("invalid magic");
            }
            if (2!=buffer.get()) {
                throw new IOException("class is not 64 bit");
            }
            if (1!=buffer.get()) {
                throw new IOException("endianness is not little");
            }
            if (1!=buffer.get()) {
                throw new IOException("version is not 1");
            }
            if (0!=buffer.get()) {
                throw new IOException("ABI is not System V");
            }
            byte abiVersion=buffer.get();
            buffer.position(buffer.position()+7); // reserved padding
            short type=buffer.getShort();
            if ((FileHeader.TYPE_EXECUTABLE!=type)
                    && (FileHeader.TYPE_SHARED_OBJECT!=type)) {
                throw new IOException("file is not an shared object");
            }
            if (0xf3!=buffer.getShort()) {
                throw new IOException("instruction set is not Aarch64");
            }
            if (1!=buffer.getInt()) {
                throw new IOException("version is not 1");
            }
            long entryPoint=buffer.getLong();
            long programHeaderOffset=buffer.getLong();
            long sectionHeaderOffset=buffer.getLong();
            int flags=buffer.getInt();
            if (64!=buffer.getShort()) {
                throw new IOException("size is not 64");
            }
            if (56!=buffer.getShort()) {
                throw new IOException("program header entry size is not 64");
            }
            int programHeaderEntryNumber=buffer.getShort()&0xffff;
            if (64!=buffer.getShort()) {
                throw new IOException("section header entry size is not 64");
            }
            int sectionHeaderEntryNumber=buffer.getShort()&0xffff;
            int sectionNamesIndex=buffer.getShort()&0xffff;
            List<ProgramHeader> programHeaders=readBlocks(
                    programHeaderEntryNumber,
                    ProgramHeader.SIZE,
                    buffer,
                    channel,
                    ProgramHeader::read,
                    programHeaderOffset);
            List<SectionHeader> sectionHeaders=readBlocks(
                    sectionHeaderEntryNumber,
                    SectionHeader.SIZE,
                    buffer,
                    channel,
                    SectionHeader::read,
                    sectionHeaderOffset);
            Map<Integer, String> sectionNames=new HashMap<>(sectionHeaders.size());
            if (sectionHeaders.size()>sectionNamesIndex) {
                SectionHeader stringTableHeader=sectionHeaders.get(sectionNamesIndex);
                for (SectionHeader sectionHeader: sectionHeaders) {
                    String name=readString(buffer, channel, sectionHeader.nameOffset, stringTableHeader);
                    if (null!=name) {
                        sectionNames.put(sectionHeader.nameOffset, name);
                    }
                }
            }
            Map<String, SymbolTableEntry> symbolTable=new HashMap<>();
            for (SectionHeader sectionHeader: sectionHeaders) {
                if ((SectionHeader.SYMBOL_TABLE!=sectionHeader.type)
                        || (0>sectionHeader.link)
                        || (sectionHeaders.size()<=sectionHeader.link)) {
                    continue;
                }
                List<SymbolTableEntry> symbolTableEntries=readBlocks(
                        (int)(sectionHeader.size/SymbolTableEntry.SIZE), SymbolTableEntry.SIZE,
                        buffer, channel, SymbolTableEntry::read, sectionHeader.offset);
                for (SymbolTableEntry symbolTableEntry: symbolTableEntries) {
                    String name=readString(
                            buffer, channel, symbolTableEntry.name, sectionHeaders.get(sectionHeader.link));
                    if ((null!=name)
                            && (!name.isEmpty())) {
                        symbolTable.put(name, symbolTableEntry);
                    }
                }
            }
            return new FileHeader(
                    abiVersion, entryPoint, flags, programHeaders, sectionNamesIndex,
                    sectionHeaders, sectionNames, symbolTable, type);
        }
        finally {
            channel.position(originalPosition);
        }
    }

    public static FileHeader read(Path path) throws Throwable {
        try (SeekableByteChannel channel=Files.newByteChannel(path, StandardOpenOption.READ)) {
            return read(channel);
        }
    }

    public static <T> List<T> readBlocks(
            int blocks, int blockSize, ByteBuffer buffer, SeekableByteChannel channel,
            Function<? super ByteBuffer, ? extends T> constructor, long position) throws Throwable {
        List<T> result=new ArrayList<>(blocks);
        for (long ii=0; blocks>ii; ++ii) {
            readFully(buffer, channel, blockSize, position+ii*blockSize);
            T value=constructor.apply(buffer);
            result.add(value);
        }
        return Collections.unmodifiableList(result);
    }

    public static void readFully(ByteBuffer buffer, SeekableByteChannel channel) throws IOException {
        while (buffer.hasRemaining()) {
            if (0>channel.read(buffer)) {
                throw new EOFException();
            }
        }
    }

    public static void readFully(
            ByteBuffer buffer, SeekableByteChannel channel, int limit, long position) throws IOException {
        buffer.clear();
        buffer.limit(limit);
        channel.position(position);
        readFully(buffer, channel);
        buffer.flip();
    }

    public static String readString(
            ByteBuffer buffer, SeekableByteChannel channel, int nameOffset, SectionHeader sectionHeader)
            throws Throwable {
        if ((0>nameOffset)
                || (sectionHeader.size<=nameOffset)) {
            return null;
        }
        readFully(
                buffer,
                channel,
                (int)Math.min(buffer.capacity(), sectionHeader.size-nameOffset),
                sectionHeader.offset+nameOffset);
        ByteArrayOutputStream baos=new ByteArrayOutputStream();
        while (buffer.hasRemaining()) {
            byte bb=buffer.get();
            if (0==bb) {
                break;
            }
            baos.write(bb);
        }
        return baos.toString(StandardCharsets.US_ASCII);
    }
}
