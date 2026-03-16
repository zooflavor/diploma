package dog.wiggler.function;

/**
 * Functional interface for procedures without arguments.
 */
@FunctionalInterface
public interface Runnable {
    void run() throws Throwable;
}
