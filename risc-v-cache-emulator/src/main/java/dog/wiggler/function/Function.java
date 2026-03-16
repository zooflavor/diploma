package dog.wiggler.function;

/**
 * Functional interface for functions with one argument.
 */
@FunctionalInterface
public interface Function<T, U> {
    U apply(T value) throws Throwable;
}
