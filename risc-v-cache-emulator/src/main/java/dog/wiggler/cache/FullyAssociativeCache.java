package dog.wiggler.cache;

import dog.wiggler.memory.AccessType;
import dog.wiggler.memory.Log;
import dog.wiggler.memory.Logs;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * A fully associative cache with pluggable replacement policy.
 * This doesn't handle cache types.
 * This is not supposed to be used on its own but through NWayAssociativeCache.
 * Handles all combination of {@link WriteMiss} and {@link WritePolicy} policies.
 * Delegates the generated memory accesses to an underlying log.
 */
public class FullyAssociativeCache {
    private final int cacheLines;
    private final int lineSize;
    private final @NotNull Log outputLog;
    private final @NotNull ReplacementPolicy replacementPolicy;
    private final @NotNull WriteMiss writeMiss;
    private final @NotNull WritePolicy writePolicy;

    public FullyAssociativeCache(
            int cacheLines,
            int lineSize,
            @NotNull Log outputLog,
            @NotNull ReplacementPolicy replacementPolicy,
            @NotNull WriteMiss writeMiss,
            @NotNull WritePolicy writePolicy) {
        this.cacheLines=cacheLines;
        this.lineSize=lineSize;
        this.outputLog=Objects.requireNonNull(outputLog, "outputLog");
        this.replacementPolicy=Objects.requireNonNull(replacementPolicy, "replacementPolicy");
        this.writeMiss=Objects.requireNonNull(writeMiss, "writeMiss");
        this.writePolicy=Objects.requireNonNull(writePolicy, "writePolicy");
        if (0>=cacheLines) {
            throw new IllegalArgumentException("invalid cache lines %d".formatted(cacheLines));
        }
        Logs.log2Checked("line size", lineSize);
    }

    public void access(long address, int size, @NotNull AccessType type) throws Throwable {
        // let's pretend we run the cache preprocessor
        // but with loadBeforePartialStore = false
        if (lineSize<size) {
            throw new IllegalArgumentException(
                    "size is larger than line size, size: %d, line size: %d"
                            .formatted(size, lineSize));
        }
        long alignedAddress=address&(-lineSize);
        if (type.load()) {
            if (address!=alignedAddress) {
                throw new IllegalArgumentException(
                        "misaligned load, address: 0x%x, line size: %d"
                                .formatted(address, lineSize));
            }
            if (lineSize!=size) {
                throw new IllegalArgumentException(
                        "partial load, size: %d, line size: %d"
                                .formatted(size, lineSize));
            }
            if (replacementPolicy.contains(alignedAddress)) {
                replacementPolicy.access(alignedAddress, false);
            }
            else {
                evict();
                replacementPolicy.addAndAccess(alignedAddress, false);
                outputLog.access(alignedAddress, lineSize, type);
            }
            return;
        }
        // store
        // stores may be not full lines
        if (replacementPolicy.contains(alignedAddress)) {
            switch (writePolicy) {
                case WRITE_BACK ->
                        replacementPolicy.access(alignedAddress, true);
                case WRITE_THROUGH -> {
                    replacementPolicy.access(alignedAddress, false);
                    outputLog.access(address, size, AccessType.STORE);
                }
            }
            return;
        }
        // store, miss
        switch (writeMiss) {
            case ALLOCATE -> {
                evict();
                switch (writePolicy) {
                    case WRITE_BACK -> {
                        replacementPolicy.addAndAccess(alignedAddress, true);
                        // this can't happen when loadBeforePartialStore = true
                        if (lineSize!=size) {
                            outputLog.access(alignedAddress, lineSize, AccessType.LOAD_DATA);
                        }
                    }
                    case WRITE_THROUGH -> {
                        replacementPolicy.addAndAccess(alignedAddress, false);
                        // this can't happen when loadBeforePartialStore = true
                        if (lineSize!=size) {
                            outputLog.access(alignedAddress, lineSize, AccessType.LOAD_DATA);
                        }
                        outputLog.access(address, size, AccessType.STORE);
                    }
                }
            }
            case DON_T_ALLOCATE ->
                    outputLog.access(address, size, AccessType.STORE);
        }
    }

    private void evict() throws Throwable {
        if (cacheLines<=replacementPolicy.size()) {
            var evictedDirtyAddress=replacementPolicy.evict();
            if (null!=evictedDirtyAddress) {
                outputLog.access(evictedDirtyAddress, lineSize, AccessType.STORE);
            }
        }
    }
}
