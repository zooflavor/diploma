package dog.wiggler.elf;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Header for sections. Sections are parts of the ELF binary.
 */
public record SectionHeader(
        long address,
        long addressAlignment,
        long entrySize,
        long flags,
        int info,
        int link,
        int nameOffset,
        long offset,
        long size,
        int type) {
    public static final int DYNAMIC_SYMBOL_TABLE=11;
    public static final int SIZE=64;
    public static final int STRING_TABLE=3;
    public static final int SYMBOL_TABLE=2;
    public static final int TYPE_NO_BITS=8;
    public static final int TYPE_PROGRAM_DATA=1;

    public @NotNull List<@NotNull String> flagNames() {
        @NotNull List<@NotNull String> names=new ArrayList<>();
        for (int ii=0; 64>ii; ++ii) {
            if (0!=(flags&(1L<<ii))) {
                switch (ii) {
                    case 0 -> names.add("writeable");
                    case 1 -> names.add("allocate");
                    case 2 -> names.add("executable");
                    case 4 -> names.add("merge");
                    case 5 -> names.add("strings");
                    case 6 -> names.add("info-link");
                    case 7 -> names.add("link-order");
                    case 8 -> names.add("os-nonconforming");
                    case 9 -> names.add("group");
                    case 10 -> names.add("thread-local storage");
                    case 20, 21, 22, 23, 24, 25 -> names.add("os-specific-"+ii);
                    case 26 -> names.add("os-specific-"+ii+"-(solaris:ordered)");
                    case 27 -> names.add("os-specific-"+ii+"-(solaris:exclude)");
                    case 28, 29, 30, 31 -> names.add("processor-specific-"+ii);
                    default -> names.add("unknown-"+ii);
                }
            }
        }
        return Collections.unmodifiableList(names);
    }

    public void print(@Nullable String name) {
        System.out.printf("section header:%n");
        if (null!=name) {
            System.out.printf("  name:              %s%n", name);
        }
        System.out.printf("  name offset:       0x%08x%n", nameOffset);
        System.out.printf("  type:              %s%n", typeName());
        System.out.printf("  flags:             %s%n", flagNames());
        System.out.printf("  address:           0x%016x%n", address);
        System.out.printf("  offset:            0x%016x%n", offset);
        System.out.printf("  size:              0x%016x%n", size);
        System.out.printf("  link:              0x%08x%n", link);
        System.out.printf("  info:              0x%08x%n", info);
        System.out.printf("  address alignment: 0x%016x%n", addressAlignment);
        System.out.printf("  entry size:        0x%016x%n", entrySize);
    }

    public static @NotNull SectionHeader read(
            @NotNull ByteBuffer buffer) {
        int nameOffset=buffer.getInt();
        int type=buffer.getInt();
        long flags2=buffer.getLong();
        long address=buffer.getLong();
        long offset=buffer.getLong();
        long size=buffer.getLong();
        int link=buffer.getInt();
        int info=buffer.getInt();
        long addressAlignment=buffer.getLong();
        long entrySize=buffer.getLong();
        return new SectionHeader(
                address, addressAlignment, entrySize, flags2, info, link, nameOffset, offset, size, type);
    }

    public @NotNull String typeName() {
        switch (type) {
            case 0:
                return "null";
            case TYPE_PROGRAM_DATA:
                return "program data";
            case SYMBOL_TABLE:
                return "symbol table";
            case STRING_TABLE:
                return "string table";
            case 4:
                return "relocation entries with addends";
            case 5:
                return "symbol hash table";
            case 6:
                return "dynamic linking information";
            case 7:
                return "notes";
            case TYPE_NO_BITS:
                return "program space with no data (bss)";
            case 9:
                return "relocation entries, no addends";
            case 10:
                return "shlib";
            case DYNAMIC_SYMBOL_TABLE:
                return "dynamic linker symbol table";
            case 14:
                return "array of constructors";
            case 15:
                return "array of destructors";
            case 16:
                return "array of pre-constructors";
            case 17:
                return "section group";
            case 18:
                return "extended section indices";
            case 19:
                return "number of defined types";
        }
        if (0x60000000<=type) {
            return "os specific, value: %d".formatted(type);
        }
        return "unknown, value: %d".formatted(type);
    }
}
