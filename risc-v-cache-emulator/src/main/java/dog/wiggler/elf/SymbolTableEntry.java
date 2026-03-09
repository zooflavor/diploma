package dog.wiggler.elf;

import java.nio.ByteBuffer;

public class SymbolTableEntry {
    public static final int SIZE=24;

    public final byte info;
    public final int name;
    public final byte other;
    public final short shndx;
    public final long size;
    public final long value;

    public SymbolTableEntry(byte info, int name, byte other, short shndx, long size, long value) {
        this.info=info;
        this.name=name;
        this.other=other;
        this.shndx=shndx;
        this.size=size;
        this.value=value;
    }

    public static SymbolTableEntry read(ByteBuffer buffer) {
        int name=buffer.getInt();
        byte info=buffer.get();
        byte other=buffer.get();
        short shndx=buffer.getShort();
        long value=buffer.getLong();
        long size=buffer.getLong();
        return new SymbolTableEntry(info, name, other, shndx, size, value);
    }
}
