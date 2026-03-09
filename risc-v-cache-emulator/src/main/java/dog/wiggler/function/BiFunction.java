package dog.wiggler.function;

@FunctionalInterface
public interface BiFunction<T, U, V> {
    V apply(T value0, U value1) throws Throwable;
}
