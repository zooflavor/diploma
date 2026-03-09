package dog.wiggler;

import dog.wiggler.memory.Memory;
import dog.wiggler.riscv64.Hart;

import java.util.ArrayList;
import java.util.List;

public class FunctionCallParameters {
    private record Value(boolean integral, long value) {
    }

    public int integrals;
    public final List<Value> parameters=new ArrayList<>();

    public FunctionCallParameters add(boolean integral, long value) {
        if (integral) {
            ++integrals;
        }
        parameters.add(new Value(integral, value));
        return this;
    }

    public FunctionCallParameters add(PrimitiveValue<?> value) {
        return value.addTo(this);
    }

    public FunctionCallParameters addAll(List<PrimitiveValue<?>> values) {
        for (PrimitiveValue<?> value: values) {
            add(value);
        }
        return this;
    }

    public FunctionCallParameters addDouble(double value) {
        return add(false, Double.doubleToRawLongBits(value));
    }

    public FunctionCallParameters addFloat(float value) {
        return add(false, Float.floatToRawIntBits(value));
    }

    public FunctionCallParameters addInt16(boolean signed, short value) {
        return add(true, signed?value:(value&0xffffL));
    }

    public FunctionCallParameters addInt32(boolean signed, int value) {
        return add(true, signed?value:(value&0xffffffffL));
    }

    public FunctionCallParameters addInt64(long value) {
        return add(true, value);
    }

    public FunctionCallParameters addInt8(boolean signed, byte value) {
        return add(true, signed?value:(value&0xffL));
    }

    public static FunctionCallParameters create() {
        return new FunctionCallParameters();
    }

    public long setParameters(Hart hart, Memory memory) throws Throwable {
        long oldSp=hart.stack.getStackPointer(hart);
        int stackSize=Math.max(0, integrals-8)+Math.max(0, parameters.size()-integrals-8);
        long newSp=(oldSp-8L*stackSize)&(~0xfL);
        int ai=0;
        int fai=0;
        int si=0;
        for (Value value: parameters) {
            if (value.integral) {
                if (8>ai) {
                    hart.registersXs.setInt64(Hart.REGISTER_A0+ai, value.value);
                    ++ai;
                    continue;
                }
            }
            else {
                if (8>fai) {
                    hart.registersFxs.setInt64(Hart.REGISTER_FA0+fai, value.value);
                    ++fai;
                    continue;
                }
            }
            memory.storeInt64(newSp+8L*si, value.value);
            ++si;
        }
        hart.stack.setStackPointer(hart, newSp);
        return oldSp;
    }

    public long setParameters(Emulator emulator) throws Throwable {
        return setParameters(emulator.hart, emulator.memoryLog);
    }
}
