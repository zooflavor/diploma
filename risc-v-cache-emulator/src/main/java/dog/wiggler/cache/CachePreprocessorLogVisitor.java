package dog.wiggler.cache;

import dog.wiggler.memory.AccessType;
import dog.wiggler.memory.LogVisitor;
import dog.wiggler.memory.Logs;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Breaks up operations to line size sized chunks.
 * Enlarges loads to line size.
 * Breaks up non-power-of-2 sized stores to power of 2 sized stores.
 * Makes partial stores aligned to their sizes.
 * Optionally loads before partial stores, to facilitate write allocation.
 */
public class CachePreprocessorLogVisitor<L extends LogVisitor<Void>> implements LogVisitor<Void> {
    private final @NotNull CacheType cacheType;
    private final int lineSize;
    private final int lineSizeMask;
    private final int lineSizeShift;
    private final boolean loadBeforePartialStore;
    protected final @NotNull L log;

    public CachePreprocessorLogVisitor(
            @NotNull CacheType cacheType,
            int lineSizeInBytes,
            boolean loadBeforePartialStore,
            @NotNull L log) {
        this.cacheType=Objects.requireNonNull(cacheType, "cacheType");
        this.lineSize=lineSizeInBytes;
        this.loadBeforePartialStore=loadBeforePartialStore;
        this.log=Objects.requireNonNull(log, "log");
        lineSizeShift=Logs.log2Checked("line size", lineSizeInBytes);
        lineSizeMask=lineSizeInBytes-1;
    }

    @Override
    public Void access(long address, int size, @NotNull AccessType type) throws Throwable {
        if (cacheType.notCached(type)) {
            return log.access(address, size, type);
        }
        if (type.load()) {
            int misalignment=(int)(address&lineSizeMask);
            address-=misalignment;
            size+=misalignment;
            int sizeRemainder=size&lineSizeMask;
            if (0!=sizeRemainder) {
                size+=lineSize-sizeRemainder;
            }
            while (0<size) {
                log.access(address, lineSize, type);
                address+=lineSize;
                size-=lineSize;
            }
        }
        else {
            while (0<size) {
                int addressAlignmentShift=Long.numberOfTrailingZeros(address);
                int sizeShift=31-Integer.numberOfLeadingZeros(size);
                int size2=1<<Math.min(
                        addressAlignmentShift,
                        Math.min(lineSizeShift, sizeShift));
                if (loadBeforePartialStore && (lineSize!=size2)) {
                    log.access(address&(~lineSizeMask), lineSize, AccessType.LOAD_DATA);
                }
                log.access(address, size2, type);
                address+=size2;
                size-=size2;
            }
        }
        return null;
    }

    @Override
    public Void accessLogDisabled() throws Throwable {
        return log.accessLogDisabled();
    }

    @Override
    public Void accessLogEnabled() throws Throwable {
        return log.accessLogEnabled();
    }

    @Override
    public Void elapsedCycles(long elapsedCycles) throws Throwable {
        return log.elapsedCycles(elapsedCycles);
    }

    @Override
    public Void end() throws Throwable {
        return log.end();
    }

    @Override
    public Void userData(long userData) throws Throwable {
        return log.userData(userData);
    }
}
