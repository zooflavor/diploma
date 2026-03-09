package dog.wiggler.elf;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class SectionHeader {
    public static final int DYNAMIC_SYMBOL_TABLE=11;
    public static final int SIZE=64;
    public static final int STRING_TABLE=3;
    public static final int SYMBOL_TABLE=2;
    public static final int TYPE_PROGRAM_DATA=1;

    public final long address;
    public final long addressAlignment;
    public final long entrySize;
    public final long flags;
    public final int info;
    public final int link;
    public final int nameOffset;
    public final long offset;
    public final long size;
    public final int type;

    public SectionHeader(
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
        this.address=address;
        this.addressAlignment=addressAlignment;
        this.entrySize=entrySize;
        this.flags=flags;
        this.info=info;
        this.link=link;
        this.nameOffset=nameOffset;
        this.offset=offset;
        this.size=size;
        this.type=type;
    }

    public Set<String> flagNames() {
        Set<String> names=new HashSet<>(32);
        long ff=flags;
        for (int ii=63; 0<=ii; ff<<=1, --ii) {
            if (0!=(ff&0x8000000000000000L)) {
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
        return Collections.unmodifiableSet(names);
    }

    public static SectionHeader read(ByteBuffer buffer) {
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

    public String typeName() {
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
            case 8:
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
            return "os specific";
        }
        return "unknown";
    }
}
