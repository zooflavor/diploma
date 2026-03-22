package dog.wiggler;

import org.jetbrains.annotations.NotNull;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

public class InputStreamInput implements Input {
    private final @NotNull InputStream stream;

    public InputStreamInput(@NotNull InputStream stream) {
        this.stream=stream;
    }

    @Override
    public double readDouble() throws Throwable {
        var line=readLine();
        return Double.parseDouble(line);
    }

    @Override
    public float readFloat() throws Throwable {
        var line=readLine();
        return Float.parseFloat(line);
    }

    @Override
    public short readInt16() throws Throwable {
        var line=readLine();
        return Short.parseShort(line);
    }

    @Override
    public int readInt32() throws Throwable {
        var line=readLine();
        return Integer.parseInt(line);
    }

    @Override
    public long readInt64() throws Throwable {
        var line=readLine();
        return Long.parseLong(line);
    }

    @Override
    public byte readInt8() throws Throwable {
        var line=readLine();
        return Byte.parseByte(line);
    }

    private @NotNull String readLine() throws Throwable {
        var sb=new StringBuilder();
        while (true) {
            int cc=stream.read();
            if (0>cc) {
                if (sb.isEmpty()) {
                    throw new EOFException();
                }
                else {
                    return sb.toString();
                }
            }
            else if (('\n'==cc) || ('\r'==cc)) {
                if (!sb.isEmpty()) {
                    return sb.toString();
                }
            }
            else {
                sb.append((char)cc);
            }
        }
    }

    @Override
    public short readUint16() throws Throwable {
        var line=readLine();
        var value=Integer.parseUnsignedInt(line);
        if (0!=(value&0xffff0000)) {
            throw new IOException("invalid uint16: %s, 0x%x".formatted(line, value));
        }
        return (short)value;
    }

    @Override
    public int readUint32() throws Throwable {
        var line=readLine();
        return Integer.parseUnsignedInt(line);
    }

    @Override
    public long readUint64() throws Throwable {
        var line=readLine();
        return Long.parseUnsignedLong(line);
    }

    @Override
    public byte readUint8() throws Throwable {
        var line=readLine();
        var value=Integer.parseUnsignedInt(line);
        if (0!=(value&0xffffff00)) {
            throw new IOException("invalid uint8: %s, 0x%x".formatted(line, value));
        }
        return (byte)value;
    }
}
