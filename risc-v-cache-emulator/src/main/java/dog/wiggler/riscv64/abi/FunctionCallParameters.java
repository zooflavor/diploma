package dog.wiggler.riscv64.abi;

import dog.wiggler.Emulator;
import dog.wiggler.memory.Memory;
import dog.wiggler.riscv64.Hart;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Prepares arguments for a function call.
 * There are 8 integer and 8 floating-point registers used to pass arguments to function.
 * When there are more arguments that need to be passed, the rest is passed through the stack.
 * All arguments must be sign extended to 64 bits, both in registers and the stack.
 * The single exception to this are single precision floating point values.
 * They must be extended to 64 bits, with the least significant 32 bits carrying the value,
 * and the most significant 32 bits undefined.
 * The stack pointer must be a multiple of 16 bytes.
 * When the stack pointer is not a multiple of 16 bytes, a dummy argument must be inserted.
 * The dummy value must be inserted at the top of the stack.
 */
public class FunctionCallParameters {
    public int integrals;
    private final @NotNull List<@NotNull PrimitiveValue<?>> parameters=new ArrayList<>();

    public @NotNull FunctionCallParameters add(
            @NotNull PrimitiveValue<?> value) {
        value.type().checkStored();
        if (value.type().integral()) {
            ++integrals;
        }
        parameters.add(value);
        return this;
    }

    public @NotNull FunctionCallParameters addAll(
            @NotNull Iterable<PrimitiveValue<?>> values) {
        FunctionCallParameters result=this;
        for (var value: values) {
            result=result.add(value);
        }
        return result;
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
        int stackSize=Math.max(0, integrals-ABI.ARGUMENT_REGISTERS)
                +Math.max(0, parameters.size()-integrals-ABI.FLOAT_ARGUMENT_REGISTERS);
        long newSp=(oldSp-8L*stackSize)&(~0xfL);
        int ai=0;
        int fai=0;
        int si=0;
        for (var value: parameters) {
            if (value.type().integral()) {
                if (ABI.ARGUMENT_REGISTERS>ai) {
                    value.storeArgument(hart, heapAndStack, ABI.REGISTER_A0+ai);
                    ++ai;
                    continue;
                }
            }
            else {
                if (ABI.FLOAT_ARGUMENT_REGISTERS>fai) {
                    value.storeArgument(hart, heapAndStack, ABI.REGISTER_FA0+fai);
                    ++fai;
                    continue;
                }
            }
            value.storeArgument(memory, newSp+8L*si);
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
