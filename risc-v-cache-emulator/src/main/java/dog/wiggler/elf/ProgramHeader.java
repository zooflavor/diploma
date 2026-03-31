package dog.wiggler.elf;

import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

    public @NotNull List<@NotNull String> flagNames() {
        @NotNull List<@NotNull String> result=new ArrayList<>();
        for (int ii=0; 64>ii; ++ii) {
            if (0L!=(flags&(1L<<ii))) {
                result.add(switch (ii) {
                    case 0 -> "executable";
                    case 1 -> "writeable";
                    case 2 -> "readable";
                    default -> "unknown-bit-%d".formatted(ii);
                });
            }
        }
        return Collections.unmodifiableList(result);
    }

    public void print() {
        System.out.printf("program header:%n");
        System.out.printf("  type:             %s%n", typeName());
        System.out.printf("  flags:            %s%n", flagNames());
        System.out.printf("  offset:           0x%016x%n", offset);
        System.out.printf("  virtual address:  0x%016x%n", virtualAddress);
        System.out.printf("  physical address: 0x%016x%n", physicalAddress);
        System.out.printf("  file size:        0x%016x%n", fileSize);
        System.out.printf("  memory size:      0x%016x%n", memorySize);
        System.out.printf("  alignment:        0x%016x%n", alignment);
    }

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
            return "reserved, operating system specific, value: %d".formatted(type);
        }
        if ((0x70000000<=type)/* && (0x7fffffff>=type)*/) {
            return "reserved, processor specific, value: %d".formatted(type);
        }
        return "unknown, value: %d".formatted(type);
    }
}
