package dog.wiggler.function;

@FunctionalInterface
public interface Supplier<T> {
    T get() throws Throwable;

    static <T extends AutoCloseable, U> Supplier<U> factory(
            Function<? super T, ? extends U> function, Supplier<? extends T> supplier) {
        return ()->{
            boolean error=true;
            T tt=supplier.get();
            try {
                U uu=function.apply(tt);
                error=false;
                return uu;
            }
            finally {
                if (error) {
                    tt.close();
                }
            }
        };
    }

    static <T extends AutoCloseable, U> Supplier<U> factory2(
            Function<? super T, ? extends Supplier<? extends U>> function, Supplier<? extends T> supplier) {
        return factory((tt)->function.apply(tt).get(), supplier);
    }
}
