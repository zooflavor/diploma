package dog.wiggler.function;

public interface Consumer<T> {
    void accept(T value) throws Throwable;

    static <T> Consumer<T> noOp() {
        return value->{
        };
    }
}
