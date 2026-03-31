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

    public @NotNull String binding() {
        int binding=(info>>4)&0xf;
        return switch (binding) {
            case 0 -> "local";
            case 1 -> "global";
            case 2 -> "weak";
            case 10, 11, 12 -> "os specific, value: %d".formatted(binding);
            case 13, 14, 15 -> "processor specific, value: %d".formatted(binding);
            default -> "unknown, value: %d".formatted(binding);
        };
    }

    public void print(@NotNull String name) {
        System.out.printf("symbol table entry:%n");
        System.out.printf("  name:                 %s%n", name);
        System.out.printf("  name index:           0x%08x%n", name());
        System.out.printf("  value:                0x%016x%n", value);
        System.out.printf("  size:                 0x%016x%n", size);
        System.out.printf("  binding:              %s%n", binding());
        System.out.printf("  type:                 %s%n", type());
        System.out.printf("  visibility:           %s%n", visibility());
        System.out.printf("  other:                0x%02x%n", other&0xf8);
        System.out.printf("  section header index: 0x%04x%n", shndx);
    }

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

    public @NotNull String type() {
        int type=info&0xf;
        return switch (type) {
            case 0 -> "not specified";
            case 1 -> "object";
            case 2 -> "function";
            case 3 -> "section";
            case 4 -> "file";
            case 5 -> "common";
            case 6 -> "thread local storage";
            case 10, 11, 12 -> "os specific, value: %d".formatted(type);
            case 13, 14, 15 -> "processor specific, value: %d".formatted(type);
            default -> "unknown, value: %d".formatted(type);
        };
    }

    public @NotNull String visibility() {
        int visibility=other&3;
        return switch (visibility) {
            case 0 -> "default";
            case 1 -> "internal";
            case 2 -> "hidden";
            default -> "protected";
        };
    }
}
