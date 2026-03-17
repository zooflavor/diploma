package dog.wiggler.elf;

import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;

/**
 * One entry of the symbol table.
 */
public record SymbolTableEntry(
        // type and binding attributes
        byte info,
        // index of the name
        int name,
        // visibility
        byte other,
        // section header index
        short shndx,
        long size,
        long value) {
    public static final int SIZE=24;

    public static @NotNull SymbolTableEntry read(
            @NotNull ByteBuffer buffer) {
        int name=buffer.getInt();
        byte info=buffer.get();
        byte other=buffer.get();
        short shndx=buffer.getShort();
        long value=buffer.getLong();
        long size=buffer.getLong();
        return new SymbolTableEntry(info, name, other, shndx, size, value);
    }
}
