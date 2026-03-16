package dog.wiggler.function;

/**
 * Functional interface for procedures with one argument.
 */
@FunctionalInterface
public interface Consumer<T> {
    void accept(T value) throws Throwable;
}
