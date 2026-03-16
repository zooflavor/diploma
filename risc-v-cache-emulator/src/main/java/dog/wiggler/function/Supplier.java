package dog.wiggler.function;

import org.jetbrains.annotations.NotNull;

/**
 * Functional interface for functions without arguments.
 */
@FunctionalInterface
public interface Supplier<T> {
    T get() throws Throwable;

    /**
     * Creates a factory safely wrapping a factory for {@link AutoCloseable} objects.
     */
    static <T extends AutoCloseable, U> @NotNull Supplier<U> factory(
            @NotNull Function<@NotNull T, U> function,
            @NotNull Supplier<@NotNull T> supplier) {
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

    /**
     * Creates a factory safely wrapping a factory for {@link AutoCloseable} objects.
     * This can be used to safely nest factories.
     */
    static <T extends AutoCloseable, U> @NotNull Supplier<U> factory2(
            @NotNull Function<@NotNull T, @NotNull Supplier<U>> function,
            @NotNull Supplier<@NotNull T> supplier) {
        return factory(
                (tt)->function.apply(tt).get(),
                supplier);
    }
}
