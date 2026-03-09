package dog.wiggler.function;

@FunctionalInterface
public interface BiConsumer<T, U> {
    void accept(T value0, U value1) throws Throwable;
}
