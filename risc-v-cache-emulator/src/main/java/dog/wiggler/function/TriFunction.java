package dog.wiggler.function;

@FunctionalInterface
public interface TriFunction<T, U, V, W> {
    W apply(T value0, U value1, V value2) throws Throwable;
}
