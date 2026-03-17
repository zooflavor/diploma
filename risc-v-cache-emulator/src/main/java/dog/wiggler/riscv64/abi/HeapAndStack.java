package dog.wiggler.riscv64.abi;

import dog.wiggler.riscv64.Hart;
import org.jetbrains.annotations.NotNull;

/**
 * Manages the heap and the stack.
 * The free heap memory is the memory between the {@code heapFreeStart} pointer and the stack pointer.
 * Malloc allocates at the bottom of the heap.
 * Free does nothing.
 * Stack grows downward, as usual.
 * Malloc and stack operations ensure that the heap and the stack cannot overlap.
 * Malloc fails when the free pointer catches up to the stack pointer.
 * Stack operations throw {@link StackOverflowException} when stack catches up with the free pointer.
 */
public class HeapAndStack {
    private static final long MIN_HEAP_STACK_DISTANCE=4096;

    /**
     * The start address of the free space on the heap.
     * It's aligned to 8 bytes.
     */
    private long heapFreeStart;

    /**
     * Checks the stack pointer for misalignment and stack overflow.
     */
    public void checkStackPointer(long stackPointer) {
        if (0!=(stackPointer&0xfL)) {
            throw new MisalignedStackPointerException(
                    "stack pointer: %016x"
                            .formatted(stackPointer));
        }
        if (MIN_HEAP_STACK_DISTANCE>stackPointer-heapFreeStart) {
            throw new StackOverflowException(
                    "heap free start: %016x, stack pointer: %016x"
                            .formatted(heapFreeStart, stackPointer));
        }
    }

    /**
     * Free does nothing.
     */
    public void free(long ignoreAddress) {
    }

    public long getStackPointer(
            @NotNull Hart hart) {
        return hart.xRegisters.getInt64(ABI.REGISTER_SP);
    }

    /**
     * Allocates memory at the start of the heap.
     * The size is always rounded up to be a multiple of 8,
     * and the result is always divisible by 8.
     * Malloc fails when there's no enough memory left between the start of the heap and the stack.
     */
    public long malloc(
            @NotNull Hart hart,
            long size) {
        if (0L>=size) {
            return 0L;
        }
        size=roundUp8(size);
        long result=heapFreeStart;
        long newHeapFreeStart=result+size;
        if (MIN_HEAP_STACK_DISTANCE>getStackPointer(hart)-newHeapFreeStart) {
            return 0L;
        }
        heapFreeStart=newHeapFreeStart;
        return result;
    }

    public void reset(
            @NotNull Hart hart,
            long heapStart,
            long heapEnd) {
        if ((0>=heapEnd)
                || (0L>heapStart)) {
            throw new IllegalArgumentException("heap too large");
        }
        heapFreeStart=roundUp8(heapStart);
        setStackPointer(hart, heapEnd&(~0xfL));
    }

    /**
     * Rounds down a value to be divisible by 16.
     */
    public static long roundDown16(long value) {
        return value&(~0xfL);
    }

    /**
     * Rounds up a value to be divisible by 8.
     */
    private static long roundUp8(long value) {
        long result=value&(~0x7L);
        if (result!=value) {
            result+=8L;
        }
        return result;
    }

    public void setStackPointer(
            @NotNull Hart hart,
            long stackPointer) {
        hart.xRegisters.setInt64(this, ABI.REGISTER_SP, roundDown16(stackPointer));
    }
}
