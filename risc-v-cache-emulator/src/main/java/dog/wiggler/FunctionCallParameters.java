package dog.wiggler;

import dog.wiggler.memory.Memory;
import dog.wiggler.riscv64.ABI;
import dog.wiggler.riscv64.Hart;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class FunctionCallParameters {
    private record Value(boolean integral, long value) {
    }

    public int integrals;
    private final @NotNull List<@NotNull Value> parameters=new ArrayList<>();

    public @NotNull FunctionCallParameters add(
            boolean integral,
            long value) {
        if (integral) {
            ++integrals;
        }
        parameters.add(new Value(integral, value));
        return this;
    }

    public @NotNull FunctionCallParameters add(
            @NotNull PrimitiveValue<?> value) {
        return value.addTo(this);
    }

    public @NotNull FunctionCallParameters addAll(
            @NotNull List<@NotNull PrimitiveValue<?>> values) {
        FunctionCallParameters result=this;
        for (PrimitiveValue<?> value: values) {
            result=result.add(value);
        }
        return result;
    }

    public @NotNull FunctionCallParameters addDouble(
            double value) {
        return add(false, Double.doubleToRawLongBits(value));
    }

    public @NotNull FunctionCallParameters addFloat(
            float value) {
        return add(false, Float.floatToRawIntBits(value));
    }

    public @NotNull FunctionCallParameters addInt16(
            boolean signed,
            short value) {
        return add(true, signed?value:(value&0xffffL));
    }

    public @NotNull FunctionCallParameters addInt32(
            boolean signed,
            int value) {
        return add(true, signed?value:(value&0xffffffffL));
    }

    public @NotNull FunctionCallParameters addInt64(
            long value) {
        return add(true, value);
    }

    public @NotNull FunctionCallParameters addInt8(
            boolean signed,
            byte value) {
        return add(true, signed?value:(value&0xffL));
    }

    public static @NotNull FunctionCallParameters create() {
        return new FunctionCallParameters();
    }

    public void setParameters(
            @NotNull Hart hart,
            @NotNull HeapAndStack heapAndStack,
            @NotNull Memory memory)
            throws Throwable {
        long oldSp=heapAndStack.getStackPointer(hart);
        int stackSize=Math.max(0, integrals-8)+Math.max(0, parameters.size()-integrals-8);
        long newSp=(oldSp-8L*stackSize)&(~0xfL);
        int ai=0;
        int fai=0;
        int si=0;
        for (Value value: parameters) {
            if (value.integral) {
                if (8>ai) {
                    hart.xRegisters.setInt64(heapAndStack, ABI.REGISTER_A0+ai, value.value);
                    ++ai;
                    continue;
                }
            }
            else {
                if (8>fai) {
                    hart.fxRegisters.setInt64(heapAndStack, ABI.REGISTER_FA0+fai, value.value);
                    ++fai;
                    continue;
                }
            }
            memory.storeInt64(newSp+8L*si, value.value);
            ++si;
        }
        heapAndStack.setStackPointer(hart, newSp);
    }

    public void setParameters(
            @NotNull Emulator emulator)
            throws Throwable {
        setParameters(emulator.hart, emulator.heapAndStack, emulator.memoryLog);
    }
}
