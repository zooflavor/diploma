package dog.wiggler.elf;

import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;

/**
 * Header for program segments.
 */
public record ProgramHeader(
        long alignment,
        long fileSize,
        int flags,
        long memorySize,
        long offset,
        long physicalAddress,
        int type,
        long virtualAddress) {
    public static final int SIZE=56;

    public static @NotNull ProgramHeader read(
            @NotNull ByteBuffer buffer) {
        int type=buffer.getInt();
        int flags2=buffer.getInt();
        long offset=buffer.getLong();
        long virtualAddress=buffer.getLong();
        long physicalAddress=buffer.getLong();
        long fileSize=buffer.getLong();
        long memorySize=buffer.getLong();
        long alignment=buffer.getLong();
        return new ProgramHeader(
                alignment, fileSize, flags2, memorySize, offset, physicalAddress, type, virtualAddress);
    }

    public @NotNull String typeName() {
        switch (type) {
            case 0:
                return "null";
            case 1:
                return "loadable segment";
            case 2:
                return "dynamic linking information";
            case 3:
                return "interpreter information";
            case 4:
                return "auxiliary information";
            case 5:
                return "shlib";
            case 6:
                return "program header table";
            case 7:
                return "thread-local storage template";
        }
        if ((0x60000000<=type) && (0x6fffffff>=type)) {
            return "reserved, operating system specific";
        }
        if ((0x70000000<=type)/* && (0x7fffffff>=type)*/) {
            return "reserved, processor specific";
        }
        return "unknown";
    }
}
