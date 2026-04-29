package dog.wiggler.cache;

import dog.wiggler.Progress;
import dog.wiggler.function.Supplier;
import dog.wiggler.memory.AccessType;
import dog.wiggler.memory.CollapseElapsedCyclesLog;
import dog.wiggler.memory.LogInputStream;
import dog.wiggler.memory.LogOutputStream;
import dog.wiggler.memory.LogVisitor;
import dog.wiggler.memory.Logs;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Runs a memory access log through an n-way associative cache.
 * This uses cacheLines/associativity number independent fully-associative caches.
 * The bits of a memory address is partitioned into subfields.
 * The log2(lineSize) least-significant bits specify the address of the byte in a cache line.
 * The next log2(cacheLines/associativity) bits select a cache
 * where the cache line of the memory address can be stored.
 */
public class NWayAssociativeCache {
    private NWayAssociativeCache() {
    }

    /**
     * Runs the entries of a log file through a cache.
     * Writes the new entries to a file.
     *
     * @param associativity the number of lines in a fully-associative subcache
     * @param cacheSizeInLines the total number of lines in the cache
     * @param cacheType filters the accesses
     * @param inputLogPath the file to process
     * @param lineSizeInBytes the size of a line, specified in bytes
     * @param outputLogPath the file to write the result to
     * @param progress interface to report progress to the use
     * @param replacementPolicyFactory factory to create replacement policies
     * @param writeMiss write miss policy to use
     * @param writePolicy write policy to use
     */
    public static void run(
            int associativity,
            int cacheSizeInLines,
            @NotNull CacheType cacheType,
            @NotNull Path inputLogPath,
            int lineSizeInBytes,
            @NotNull Path outputLogPath,
            @NotNull Progress progress,
            @NotNull Supplier<? extends @NotNull ReplacementPolicy> replacementPolicyFactory,
            @NotNull WriteMiss writeMiss,
            @NotNull WritePolicy writePolicy)
            throws Throwable {
        Logs.log2Checked("associativity", associativity);
        Logs.log2Checked("cache size", cacheSizeInLines);
        int lineSizeShift=Logs.log2Checked("line size", lineSizeInBytes);
        int caches=cacheSizeInLines/associativity;
        if (cacheSizeInLines!=associativity*caches) {
            throw new IllegalArgumentException(
                    "cache size is not a multiple of the associativity, cache size: %d, associativity: %d"
                            .formatted(cacheSizeInLines, associativity));
        }
        long entries=LogInputStream.entries(inputLogPath);
        try (var inputLog=LogInputStream.factory(inputLogPath)
                .get();
             var outputLog=CollapseElapsedCyclesLog.factory(
                             LogOutputStream.factory(outputLogPath))
                     .get()) {
            // subcaches
            @NotNull List<@NotNull FullyAssociativeCache> caches2=new ArrayList<>(caches);
            for (int ii=caches; 0<ii; --ii) {
                caches2.add(
                        new FullyAssociativeCache(
                                associativity,
                                lineSizeInBytes,
                                outputLog,
                                replacementPolicyFactory.get(),
                                writeMiss,
                                writePolicy));
            }
            // entry processor
            var visitor=new CachePreprocessorLogVisitor<>(
                    cacheType,
                    lineSizeInBytes,
                    false,
                    new LogVisitor<Void>() {
                        @Override
                        public Void access(long address, int size, @NotNull AccessType type) throws Throwable {
                            if (cacheType.notCached(type)) {
                                // delegate entries not interested in
                                return outputLog.access(address, size, type);
                            }
                            // mask out everything but the index of the cache
                            int cache=(int)((address>>lineSizeShift)&(caches-1));
                            caches2.get(cache)
                                    .access(address, size, type);
                            return null;
                        }

                        @Override
                        public Void accessLogDisabled() throws Throwable {
                            return outputLog.accessLogDisabled();
                        }

                        @Override
                        public Void accessLogEnabled() throws Throwable {
                            return outputLog.accessLogEnabled();
                        }

                        @Override
                        public Void elapsedCycles(long elapsedCycles) {
                            return outputLog.elapsedCycles(elapsedCycles);
                        }

                        @Override
                        public Void end() throws Throwable {
                            return outputLog.end();
                        }

                        @Override
                        public Void userData(long userData) throws Throwable {
                            return outputLog.userData(userData);
                        }
                    });
            // process all entries
            for (long entry=0; inputLog.hasNext(); ++entry) {
                progress.progress("forward", 100L*entry/entries);
                inputLog.readNext(visitor);
            }
            visitor.end();
            progress.progress("forward", 100L);
        }
    }
}
