package dog.wiggler.elf;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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

    public @NotNull String sectionName(
            @NotNull SectionHeader sectionHeader) {
        return sectionNames.get(sectionHeader.nameOffset());
    }
}
