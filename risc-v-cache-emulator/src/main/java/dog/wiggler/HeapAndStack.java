package dog.wiggler;

import dog.wiggler.riscv64.Hart;

public class HeapAndStack {
    private static final long MIN_HEAP_STACK_DISTANCE=4096;

    private long heapFreeStart;

    private static long alignmentBits(long size) {
        for (int ii=3; 0<ii; --ii) {
            if (0!=(size&((-1L)<<ii))) {
                return ii;
            }
        }
        return 0;
    }

    private static long alignDown(long address, long alignmentBits) {
        if (0>=alignmentBits) {
            return address;
        }
        return address&((-1L)<<alignmentBits);
    }

    private static long alignUp(long address, long alignmentBits) {
        long result=alignDown(address, alignmentBits);
        if (result<address) {
            result+=1L<<alignmentBits;
        }
        return result;
    }

    public void checkStackPointer(long stackPointer) {
        if (MIN_HEAP_STACK_DISTANCE>stackPointer-heapFreeStart) {
            throw new StackOverflowException();
        }
    }

    public void free(long ignoreAddress) {
    }

    public long getStackPointer(Hart hart) {
        return hart.registersXs.getInt64(Hart.REGISTER_SP);
    }

    public long malloc(Hart hart, long size) {
        if (0L>=size) {
            return 0L;
        }
        long result=alignUp(heapFreeStart, alignmentBits(size));
        long newHeapFreeStart=result+size;
        if (MIN_HEAP_STACK_DISTANCE>getStackPointer(hart)-newHeapFreeStart) {
            return 0L;
        }
        heapFreeStart=newHeapFreeStart;
        return result;
    }

    public void reset(Hart hart, long heapStart, long heapEnd) {
        if ((0>=heapEnd)
                || (0L>heapStart)) {
            throw new IllegalArgumentException("heap too large");
        }
        heapFreeStart=heapStart;
        long stackPointer=alignDown(heapEnd, 4);
        setStackPointer(hart, stackPointer);
        if (MIN_HEAP_STACK_DISTANCE>stackPointer-heapFreeStart) {
            throw new IllegalArgumentException("heap too small");
        }
    }

    public void setStackPointer(Hart hart, long stackPointer) {
        hart.registersXs.setInt64(Hart.REGISTER_SP, stackPointer);
        if (MIN_HEAP_STACK_DISTANCE>stackPointer-heapFreeStart) {
            throw new StackOverflowException();
        }
    }
}
