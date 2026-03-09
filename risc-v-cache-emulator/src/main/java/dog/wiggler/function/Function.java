package dog.wiggler.function;

public interface Function<T, U> {
    U apply(T value) throws Throwable;
}
