package dog.wiggler.elf;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileHeader {
    public static final short TYPE_EXECUTABLE=2;
    public static final short TYPE_SHARED_OBJECT=3;

    public final byte abiVersion;
    public final long entryPoint;
    public final int flags;
    public final List<ProgramHeader> programHeaders;
    public final int sectionNamesIndex;
    public final List<SectionHeader> sectionHeaders;
    public final Map<Integer, String> sectionNames;
    public final Map<String, SymbolTableEntry> symbolTable;
    public final short type;

    public FileHeader(
            byte abiVersion,
            long entryPoint,
            int flags,
            List<ProgramHeader> programHeaders,
            int sectionNamesIndex,
            List<SectionHeader> sectionHeaders,
            Map<Integer, String> sectionNames,
            Map<String, SymbolTableEntry> symbolTable,
            short type) {
        this.abiVersion=abiVersion;
        this.entryPoint=entryPoint;
        this.flags=flags;
        this.programHeaders=List.copyOf(programHeaders);
        this.sectionNamesIndex=sectionNamesIndex;
        this.sectionHeaders=List.copyOf(sectionHeaders);
        this.sectionNames=Collections.unmodifiableMap(new HashMap<>(sectionNames));
        this.symbolTable=Collections.unmodifiableMap(new HashMap<>(symbolTable));
        this.type=type;
    }

    public String sectionName(SectionHeader sectionHeader) {
        return sectionNames.get(sectionHeader.nameOffset);
    }
}
