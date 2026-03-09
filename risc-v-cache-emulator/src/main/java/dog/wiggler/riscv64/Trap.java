package dog.wiggler.riscv64;

import dog.wiggler.function.Consumer;
import dog.wiggler.function.Function;
import dog.wiggler.function.Runnable;
import dog.wiggler.function.Supplier;
import dog.wiggler.memory.MemoryLog;

public interface Trap {
    abstract class Subroutine implements Trap {
        protected abstract void apply(Hart hart, MemoryLog memoryLog) throws Throwable;

        @Override
        public void triggered(Hart hart, MemoryLog memoryLog) throws Throwable {
            apply(hart, memoryLog);
            hart.setPc(hart.getReturnAddress());
        }
    }

    static Trap doubleConsumer(Consumer<Double> consumer) {
        return new Subroutine() {
            @Override
            protected void apply(Hart hart, MemoryLog memoryLog) throws Throwable {
                consumer.accept(hart.registersFxs.getDouble(Hart.REGISTER_FA0));
            }
        };
    }

    static Trap doubleSupplier(Supplier<Double> supplier) {
        return new Subroutine() {
            @Override
            protected void apply(Hart hart, MemoryLog memoryLog) throws Throwable {
                hart.registersFxs.setDouble(Hart.REGISTER_FA0, supplier.get());
            }
        };
    }

    static Trap floatConsumer(Consumer<Float> consumer) {
        return new Subroutine() {
            @Override
            protected void apply(Hart hart, MemoryLog memoryLog) throws Throwable {
                consumer.accept(hart.registersFxs.getFloat(Hart.REGISTER_FA0));
            }
        };
    }

    static Trap floatSupplier(Supplier<Float> supplier) {
        return new Subroutine() {
            @Override
            protected void apply(Hart hart, MemoryLog memoryLog) throws Throwable {
                hart.registersFxs.setFloat(Hart.REGISTER_FA0, supplier.get());
            }
        };
    }

    static Trap int16Consumer(Consumer<Short> consumer) {
        return new Subroutine() {
            @Override
            protected void apply(Hart hart, MemoryLog memoryLog) throws Throwable {
                consumer.accept(hart.registersXs.getInt16(Hart.REGISTER_A0));
            }
        };
    }

    static Trap int16Supplier(Supplier<Short> supplier) {
        return new Subroutine() {
            @Override
            protected void apply(Hart hart, MemoryLog memoryLog) throws Throwable {
                hart.registersXs.setInt16(Hart.REGISTER_A0, supplier.get());
            }
        };
    }

    static Trap int32Consumer(Consumer<Integer> consumer) {
        return new Subroutine() {
            @Override
            protected void apply(Hart hart, MemoryLog memoryLog) throws Throwable {
                consumer.accept(hart.registersXs.getInt32(Hart.REGISTER_A0));
            }
        };
    }

    static Trap int32Supplier(Supplier<Integer> supplier) {
        return new Subroutine() {
            @Override
            protected void apply(Hart hart, MemoryLog memoryLog) throws Throwable {
                hart.registersXs.setInt32(Hart.REGISTER_A0, supplier.get());
            }
        };
    }

    static Trap int64Consumer(Consumer<Long> consumer) {
        return new Subroutine() {
            @Override
            protected void apply(Hart hart, MemoryLog memoryLog) throws Throwable {
                consumer.accept(hart.registersXs.getInt64(Hart.REGISTER_A0));
            }
        };
    }

    static Trap int64Supplier(Supplier<Long> supplier) {
        return new Subroutine() {
            @Override
            protected void apply(Hart hart, MemoryLog memoryLog) throws Throwable {
                hart.registersXs.setInt64(Hart.REGISTER_A0, supplier.get());
            }
        };
    }

    static Trap int8Consumer(Consumer<Byte> consumer) {
        return new Subroutine() {
            @Override
            protected void apply(Hart hart, MemoryLog memoryLog) throws Throwable {
                consumer.accept(hart.registersXs.getInt8(Hart.REGISTER_A0));
            }
        };
    }

    static Trap int8Supplier(Supplier<Byte> supplier) {
        return new Subroutine() {
            @Override
            protected void apply(Hart hart, MemoryLog memoryLog) throws Throwable {
                hart.registersXs.setInt8(Hart.REGISTER_A0, supplier.get());
            }
        };
    }

    static Trap int64Operator(Function<Long, Long> function) {
        return new Subroutine() {
            @Override
            protected void apply(Hart hart, MemoryLog memoryLog) throws Throwable {
                hart.registersXs.setInt64(
                        Hart.REGISTER_A0,
                        function.apply(hart.registersXs.getInt64(Hart.REGISTER_A0)));
            }
        };
    }

    static Trap runnable(Runnable function) {
        return new Subroutine() {
            @Override
            protected void apply(Hart hart, MemoryLog memoryLog) throws Throwable {
                function.run();
            }
        };
    }

    void triggered(Hart hart, MemoryLog memoryLog) throws Throwable;
}
