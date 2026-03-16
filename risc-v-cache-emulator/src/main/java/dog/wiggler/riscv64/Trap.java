package dog.wiggler.riscv64;

import dog.wiggler.HeapAndStack;
import dog.wiggler.function.Consumer;
import dog.wiggler.function.Runnable;
import dog.wiggler.function.Supplier;
import dog.wiggler.memory.MemoryLog;
import org.jetbrains.annotations.NotNull;

/**
 * Traps handle exceptional states.
 * Traps are also called interrupts, and exceptions.
 * Traps are used to implement system calls, like malloc(),
 * and debug calls, like logging user data.
 */
@FunctionalInterface
public interface Trap {
    /**
     * Subroutines are traps implementing a system call.
     * They behave like functions.
     * The implementation of the functions is not in object code,
     * but supplied by the emulator environment.
     * Subroutines are called when the risc-v hart tries to execute an instruction at specified addresses.
     * Subroutines follow the risc-v abi to manipulate parameters and return values.
     */
    @FunctionalInterface
    interface Subroutine extends Trap {
        void apply(
                @NotNull Hart hart,
                @NotNull HeapAndStack heapAndStack,
                @NotNull MemoryLog memoryLog)
                throws Throwable;

        @Override
        default void triggered(
                @NotNull Hart hart,
                @NotNull HeapAndStack heapAndStack,
                @NotNull MemoryLog memoryLog)
                throws Throwable {
            apply(hart, heapAndStack, memoryLog);
            hart.setPc(hart.getReturnAddress());
        }
    }

    /**
     * Creates a subroutine accepting a double value.
     */
    static @NotNull Trap.Subroutine doubleConsumer(
            @NotNull Consumer<@NotNull Double> consumer) {
        return (hart, heapAndStack, memoryLog)->
                consumer.accept(hart.fxRegisters.getDouble(ABI.REGISTER_FA0));
    }

    /**
     * Creates a subroutine returning a double value.
     */
    static @NotNull Trap.Subroutine doubleSupplier(
            @NotNull Supplier<@NotNull Double> supplier) {
        return (hart, heapAndStack, memoryLog)->
                hart.fxRegisters.setDouble(heapAndStack, ABI.REGISTER_FA0, supplier.get());
    }

    /**
     * Creates a subroutine accepting a float value.
     */
    static @NotNull Trap.Subroutine floatConsumer(
            @NotNull Consumer<@NotNull Float> consumer) {
        return (hart, heapAndStack, memoryLog)->
                consumer.accept(hart.fxRegisters.getFloat(ABI.REGISTER_FA0));
    }

    /**
     * Creates a subroutine returning a float value.
     */
    static @NotNull Trap.Subroutine floatSupplier(
            @NotNull Supplier<@NotNull Float> supplier) {
        return (hart, heapAndStack, memoryLog)->
                hart.fxRegisters.setFloat(heapAndStack, ABI.REGISTER_FA0, supplier.get());
    }

    /**
     * Creates a subroutine accepting a short value.
     */
    static @NotNull Trap.Subroutine int16Consumer(
            @NotNull Consumer<@NotNull Short> consumer) {
        return (hart, heapAndStack, memoryLog)->
                consumer.accept(hart.xRegisters.getInt16(ABI.REGISTER_A0));
    }

    /**
     * Creates a subroutine returning a short value.
     */
    static @NotNull Trap.Subroutine int16Supplier(
            @NotNull Supplier<@NotNull Short> supplier) {
        return (hart, heapAndStack, memoryLog)->
                hart.xRegisters.setInt16(heapAndStack, ABI.REGISTER_A0, supplier.get());
    }

    /**
     * Creates a subroutine accepting az int value.
     */
    static @NotNull Trap.Subroutine int32Consumer(
            @NotNull Consumer<@NotNull Integer> consumer) {
        return (hart, heapAndStack, memoryLog)->
                consumer.accept(hart.xRegisters.getInt32(ABI.REGISTER_A0));
    }

    /**
     * Creates a subroutine returning an int value.
     */
    static @NotNull Trap.Subroutine int32Supplier(
            @NotNull Supplier<@NotNull Integer> supplier) {
        return (hart, heapAndStack, memoryLog)->
                hart.xRegisters.setInt32(heapAndStack, ABI.REGISTER_A0, supplier.get());
    }

    /**
     * Creates a subroutine accepting a long value.
     */
    static @NotNull Trap.Subroutine int64Consumer(
            @NotNull Consumer<@NotNull Long> consumer) {
        return (hart, heapAndStack, memoryLog)->
                consumer.accept(hart.xRegisters.getInt64(ABI.REGISTER_A0));
    }

    /**
     * Creates a subroutine returning a long value.
     */
    static @NotNull Trap.Subroutine int64Supplier(
            @NotNull Supplier<@NotNull Long> supplier) {
        return (hart, heapAndStack, memoryLog)->
                hart.xRegisters.setInt64(heapAndStack, ABI.REGISTER_A0, supplier.get());
    }

    /**
     * Creates a subroutine accepting a byte value.
     */
    static @NotNull Trap.Subroutine int8Consumer(
            @NotNull Consumer<@NotNull Byte> consumer) {
        return (hart, heapAndStack, memoryLog)->
                consumer.accept(hart.xRegisters.getInt8(ABI.REGISTER_A0));
    }

    /**
     * Creates a subroutine returning a byte value.
     */
    static @NotNull Trap.Subroutine int8Supplier(
            @NotNull Supplier<@NotNull Byte> supplier) {
        return (hart, heapAndStack, memoryLog)->
                hart.xRegisters.setInt8(heapAndStack, ABI.REGISTER_A0, supplier.get());
    }

    /**
     * Creates a subroutine that neither accepts, nor return any value.
     */
    static @NotNull Trap.Subroutine runnable(
            @NotNull Runnable function) {
        return (hart, heapAndStack, memoryLog)->
                function.run();
    }

    void triggered(
            @NotNull Hart hart,
            @NotNull HeapAndStack heapAndStack,
            @NotNull MemoryLog memoryLog)
            throws Throwable;
}
