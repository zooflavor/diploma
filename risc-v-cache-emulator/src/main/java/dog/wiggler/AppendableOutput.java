package dog.wiggler;

import dog.wiggler.emulator.Output;
import org.jetbrains.annotations.NotNull;

public class AppendableOutput implements Output {
    private final @NotNull Appendable appendable;

    public AppendableOutput(@NotNull Appendable appendable) {
        this.appendable=appendable;
    }

    @Override
    public void writeDouble(double value) throws Throwable {
        appendable.append(Double.toString(value));
        appendable.append(System.lineSeparator());
    }

    @Override
    public void writeFloat(float value) throws Throwable {
        appendable.append(Float.toString(value));
        appendable.append(System.lineSeparator());
    }

    @Override
    public void writeInt16(short value) throws Throwable {
        appendable.append(Short.toString(value));
        appendable.append(System.lineSeparator());
    }

    @Override
    public void writeInt32(int value) throws Throwable {
        appendable.append(Integer.toString(value));
        appendable.append(System.lineSeparator());
    }

    @Override
    public void writeInt64(long value) throws Throwable {
        appendable.append(Long.toString(value));
        appendable.append(System.lineSeparator());
    }

    @Override
    public void writeInt8(byte value) throws Throwable {
        appendable.append(Byte.toString(value));
        appendable.append(System.lineSeparator());
    }

    @Override
    public void writeUint16(short value) throws Throwable {
        appendable.append(Integer.toString(value&0xffff));
        appendable.append(System.lineSeparator());
    }

    @Override
    public void writeUint32(int value) throws Throwable {
        appendable.append(Integer.toUnsignedString(value));
        appendable.append(System.lineSeparator());
    }

    @Override
    public void writeUint64(long value) throws Throwable {
        appendable.append(Long.toUnsignedString(value));
        appendable.append(System.lineSeparator());
    }

    @Override
    public void writeUint8(byte value) throws Throwable {
        appendable.append(Integer.toString(value&0xff));
        appendable.append(System.lineSeparator());
    }
}
