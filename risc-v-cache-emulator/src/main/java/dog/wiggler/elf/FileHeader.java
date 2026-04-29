package dog.wiggler.elf;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * The first header of an ELF file.
 */
public record FileHeader(
        byte abiVersion,
        long entryPoint,
        int flags,
        @NotNull List<@NotNull ProgramHeader> programHeaders,
        int sectionNamesIndex,
        @NotNull List<@NotNull SectionHeader> sectionHeaders,
        @NotNull Map<@NotNull Integer, @NotNull String> sectionNames,
        @NotNull Map<@NotNull String, @NotNull SymbolTableEntry> symbolTable,
        short type) {
    public static final short TYPE_EXECUTABLE=2;
    public static final short TYPE_SHARED_OBJECT=3;


    public FileHeader(
            byte abiVersion,
            long entryPoint,
            int flags,
            @NotNull List<@NotNull ProgramHeader> programHeaders,
            int sectionNamesIndex,
            @NotNull List<@NotNull SectionHeader> sectionHeaders,
            @NotNull Map<@NotNull Integer, @NotNull String> sectionNames,
            @NotNull Map<@NotNull String, @NotNull SymbolTableEntry> symbolTable,
            short type) {
        this.abiVersion=abiVersion;
        this.entryPoint=entryPoint;
        this.flags=flags;
        this.programHeaders=List.copyOf(programHeaders);
        this.sectionNamesIndex=sectionNamesIndex;
        this.sectionHeaders=List.copyOf(sectionHeaders);
        this.sectionNames=Collections.unmodifiableMap(new TreeMap<>(sectionNames));
        this.symbolTable=Collections.unmodifiableMap(new TreeMap<>(symbolTable));
        this.type=type;
    }

    public void print() {
        System.out.printf("ABI version: %d%n", abiVersion);
        System.out.printf("type:        %s%n", typeName());
        System.out.printf("flags:       0x%08x%n", flags);
        System.out.printf("entry point: 0x%016x%n", entryPoint);
        for (var programHeader: programHeaders) {
            programHeader.print();
        }
        for (var sectionHeader: sectionHeaders) {
            sectionHeader.print(sectionName(sectionHeader));
        }
        for (var entry: symbolTable.entrySet()) {
            entry.getValue().print(entry.getKey());
        }
    }

    public @Nullable String sectionName(
            @NotNull SectionHeader sectionHeader) {
        return sectionNames.get(sectionHeader.nameOffset());
    }

    public @NotNull String typeName() {
        return switch (type) {
            case 1 -> "relocatable file";
            case 2 -> "executable file";
            case 3 -> "shared object";
            case 4 -> "core file";
            default -> "unknown, value: %d".formatted(type);
        };
    }
}
