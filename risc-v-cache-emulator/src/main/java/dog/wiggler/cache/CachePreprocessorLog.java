package dog.wiggler.cache;

import dog.wiggler.function.Supplier;
import dog.wiggler.memory.Log;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * Breaks up operations to line size sized chunks.
 * Enlarges loads to line size.
 * Breaks up non-power-of-2 sized stores to power of 2 sized stores.
 * Makes partial stores aligned to their sizes.
 * Optionally loads before partial stores, to facilitate write allocation.
 */
public class CachePreprocessorLog extends CachePreprocessorLogVisitor<Log> implements Log {
    public CachePreprocessorLog(
            @NotNull CacheType cacheType,
            int lineSizeInBytes,
            boolean loadBeforePartialStore,
            @NotNull Log log) {
        super(cacheType, lineSizeInBytes, loadBeforePartialStore, log);
    }

    @Override
    public void close() throws IOException {
        log.close();
    }

    public static @NotNull Supplier<@NotNull CachePreprocessorLog> factory(
            @NotNull CacheType cacheType,
            int lineSizeInBytes,
            boolean loadBeforePartialStore,
            @NotNull Supplier<? extends @NotNull Log> logFactory) {
        return Supplier.factory(
                (log)->
                        new CachePreprocessorLog(cacheType, lineSizeInBytes, loadBeforePartialStore, log),
                logFactory);
    }
}
