package dog.wiggler.elf;

import dog.wiggler.function.Function;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
import java.util.TreeMap;

/**
 * Reads the header of an ELF (Executable and Linkable Format) file.
 * Only the 64 bit format is supported.
 */
public class ELF {
    private ELF() {
    }

    /**
     * Reads the file header from the channel.
     * The position of the channel will not be changed.
     * The channel will not bo closed.
     */
    public static @NotNull FileHeader read(
            @NotNull SeekableByteChannel channel)
            throws Throwable {
        if (64>channel.size()) {
            throw new IOException("file too short for a 64 bit ELF header");
        }
        long originalPosition=channel.position();
        try {
            @NotNull ByteBuffer buffer=ByteBuffer.allocate(4096)
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
                throw new IOException("file is not an executable, nor a shared object");
            }
            if (0xf3!=buffer.getShort()) {
                throw new IOException("instruction set is not RISC-V");
            }
            if (1!=buffer.getInt()) {
                throw new IOException("version is not 1");
            }
            long entryPoint=buffer.getLong();
            long programHeaderOffset=buffer.getLong();
            long sectionHeaderOffset=buffer.getLong();
            int flags=buffer.getInt();
            if (64!=buffer.getShort()) {
                throw new IOException("file header size is not 64");
            }
            if (56!=buffer.getShort()) {
                throw new IOException("program header entry size is not 56");
            }
            int programHeaderEntryNumber=buffer.getShort()&0xffff;
            if (64!=buffer.getShort()) {
                throw new IOException("section header entry size is not 64");
            }
            int sectionHeaderEntryNumber=buffer.getShort()&0xffff;
            int sectionNamesIndex=buffer.getShort()&0xffff;
            @NotNull List<@NotNull ProgramHeader> programHeaders=readBlocks(
                    programHeaderEntryNumber,
                    ProgramHeader.SIZE,
                    buffer,
                    channel,
                    ProgramHeader::read,
                    programHeaderOffset);
            @NotNull List<@NotNull SectionHeader> sectionHeaders=readBlocks(
                    sectionHeaderEntryNumber,
                    SectionHeader.SIZE,
                    buffer,
                    channel,
                    SectionHeader::read,
                    sectionHeaderOffset);
            @NotNull Map<@NotNull Integer, @NotNull String> sectionNames=new HashMap<>(sectionHeaders.size());
            if (sectionHeaders.size()>sectionNamesIndex) {
                SectionHeader stringTableHeader=sectionHeaders.get(sectionNamesIndex);
                for (SectionHeader sectionHeader: sectionHeaders) {
                    String name=readString(buffer, channel, sectionHeader.nameOffset(), stringTableHeader);
                    if (null!=name) {
                        sectionNames.put(sectionHeader.nameOffset(), name);
                    }
                }
            }
            @NotNull Map<@NotNull String, @NotNull SymbolTableEntry> symbolTable=new TreeMap<>();
            for (SectionHeader sectionHeader: sectionHeaders) {
                if ((SectionHeader.SYMBOL_TABLE!=sectionHeader.type())
                        || (0>sectionHeader.link())
                        || (sectionHeaders.size()<=sectionHeader.link())) {
                    continue;
                }
                @NotNull List<@NotNull SymbolTableEntry> symbolTableEntries=readBlocks(
                        (int)(sectionHeader.size()/SymbolTableEntry.SIZE), SymbolTableEntry.SIZE,
                        buffer, channel, SymbolTableEntry::read, sectionHeader.offset());
                for (@NotNull SymbolTableEntry symbolTableEntry: symbolTableEntries) {
                    String name=readString(
                            buffer, channel, symbolTableEntry.name(), sectionHeaders.get(sectionHeader.link()));
                    if ((null!=name)
                            && (!name.isEmpty())) {
                        symbolTable.put(name, symbolTableEntry);
                    }
                }
            }
            return new FileHeader(
                    abiVersion,
                    entryPoint,
                    flags,
                    programHeaders,
                    sectionNamesIndex,
                    sectionHeaders,
                    sectionNames,
                    symbolTable,
                    type);
        }
        finally {
            channel.position(originalPosition);
        }
    }

    /**
     * Reads the file header from the file specified by the path.
     */
    public static @NotNull FileHeader read(
            @NotNull Path path) throws Throwable {
        try (@NotNull SeekableByteChannel channel=Files.newByteChannel(path, StandardOpenOption.READ)) {
            return read(channel);
        }
    }

    /**
     * Reads a contiguous section of blocks of the channel.
     *
     * @param blocks      the number of blocks to read
     * @param blockSize   the size of a block
     * @param buffer      temporary buffer used for the operations
     * @param channel     is used to read the blocks
     * @param constructor to parse the blocks
     * @param position    is the starting position of the first block
     * @return the parsed blocks
     */
    private static <T> @NotNull List<@NotNull T> readBlocks(
            int blocks,
            int blockSize,
            @NotNull ByteBuffer buffer,
            @NotNull SeekableByteChannel channel,
            @NotNull Function<ByteBuffer, @NotNull T> constructor,
            long position)
            throws Throwable {
        @NotNull List<@NotNull T> result=new ArrayList<>(blocks);
        for (long ii=0; blocks>ii; ++ii) {
            readFully(buffer, channel, blockSize, position+ii*blockSize);
            @NotNull T value=constructor.apply(buffer);
            result.add(value);
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * Reads the buffer full.
     * Throws {@link java.io.EOFException} when there's not enough data to fill the buffer.
     */
    private static void readFully(
            @NotNull ByteBuffer buffer,
            @NotNull SeekableByteChannel channel)
            throws Throwable {
        while (buffer.hasRemaining()) {
            if (0>channel.read(buffer)) {
                throw new EOFException();
            }
        }
    }

    /**
     * Reads bytes to a buffer.
     * Clears the buffer first,
     * then reads fully the number of bytes requested,
     * then flips the buffer for immediate processing.
     *
     * @param buffer to read to
     * @param channel is used to read data
     * @param limit is the number of bytes to be read
     * @param position is the start of the section to be read in the channel
     */
    private static void readFully(
            @NotNull ByteBuffer buffer,
            @NotNull SeekableByteChannel channel,
            int limit,
            long position)
            throws Throwable {
        buffer.clear();
        buffer.limit(limit);
        channel.position(position);
        readFully(buffer, channel);
        buffer.flip();
    }

    /**
     * Reads a zero terminated string from a section.
     *
     * @param buffer temporary buffer for reading
     * @param channel is used to read data
     * @param nameOffset is the starting position of the string in the section. Its non-positive for nulls.
     * @param sectionHeader the header of the section containing the string.
     */
    private static @Nullable String readString(
            @NotNull ByteBuffer buffer,
            @NotNull SeekableByteChannel channel,
            int nameOffset,
            @NotNull SectionHeader sectionHeader)
            throws Throwable {
        if ((0>nameOffset)
                || (sectionHeader.size()<=nameOffset)) {
            return null;
        }
        readFully(
                buffer,
                channel,
                (int)Math.min(buffer.capacity(), sectionHeader.size()-nameOffset),
                sectionHeader.offset()+nameOffset);
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
