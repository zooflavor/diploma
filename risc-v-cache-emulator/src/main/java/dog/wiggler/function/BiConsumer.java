package dog.wiggler.function;

/**
 * Functional interface for procedures with two arguments.
 */
@FunctionalInterface
public interface BiConsumer<T, U> {
    void accept(T value0, U value1) throws Throwable;
}
