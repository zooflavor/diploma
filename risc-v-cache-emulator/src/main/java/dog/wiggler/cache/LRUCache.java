package dog.wiggler.cache;

import dog.wiggler.memory.AccessType;
import dog.wiggler.memory.Log;
import dog.wiggler.memory.Logs;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class LRUCache implements Log {
    private static class CacheLine {
        public long address;
        public boolean dirty;
        public CacheLine lessRecentlyUsed;
        public CacheLine moreRecentlyUsed;
    }

    private final CacheCallback cacheCallback;
    private long cacheHits;
    private final int cacheLines;
    private long cacheMisses;
    private long elapsedCycles;
    private CacheLine leastRecentlyUsedLine;
    private final int lineBytes;
    private final long lineMaskHigh;
    private final Map<Long, CacheLine> linesByAddress;
    private long loadStores;
    private final Log log;
    private CacheLine mostRecentlyUsedLine;
    private final WriteMiss writeMiss;
    private final WritePolicy writePolicy;

    private LRUCache(
            CacheCallback cacheCallback, int cacheLines, int lineBytes,
            Log log, WriteMiss writeMiss, WritePolicy writePolicy) {
        this.cacheCallback=Objects.requireNonNull(cacheCallback, "cacheCallback");
        this.cacheLines=cacheLines;
        this.lineBytes=lineBytes;
        this.log=Objects.requireNonNull(log, "log");
        this.writeMiss=Objects.requireNonNull(writeMiss, "writeAllocate");
        this.writePolicy=Objects.requireNonNull(writePolicy, "writePolicy");
        Logs.log2Checked(cacheLines);
        Logs.log2Checked(lineBytes);
        lineMaskHigh=-lineBytes;
        linesByAddress=HashMap.newHashMap(cacheLines);
    }

    @Override
    public Void access(long address, int size, @NotNull AccessType type) throws Throwable {
        while (0<size) {
            long lineAddress=address&lineMaskHigh;
            int size2=Math.min(size, (int)(lineAddress+lineBytes-address));
            access(address, lineAddress, size2, type);
            address+=size2;
            size-=size2;
        }
        callbackStatistics();
        return null;
    }

    private void access(long address, long lineAddress, int size, AccessType type) throws Throwable {
        CacheLine line=linesByAddress.get(lineAddress);
        if (null!=line) {
            ++cacheHits;
            unlink(line);
            linkMostRecentlyUsed(line);
            if (type.store()) {
                storePolicy(line);
            }
        }
        else {
            ++cacheMisses;
            if (type.load()) {
                line=allocate(lineAddress);
                load(line);
            }
            else if (lineBytes==size) {
                line=allocate(lineAddress);
                storePolicy(line);
            }
            else if (writeMiss.allocate()) {
                line=allocate(lineAddress);
                load(line);
                storePolicy(line);
            }
            else {
                ++loadStores;
                log.access(address, size, AccessType.STORE);
            }
        }
    }

    @Override
    public Void accessLogDisabled() {
        return null;
    }

    @Override
    public Void accessLogEnabled() {
        return null;
    }

    private CacheLine allocate(long lineAddress) throws Throwable {
        CacheLine line;
        if (cacheLines>linesByAddress.size()) {
            line=new CacheLine();
        }
        else {
            line=leastRecentlyUsedLine;
            unlink(line);
            linesByAddress.remove(line.address);
            if (line.dirty) {
                line.dirty=false;
                ++loadStores;
                log.access(line.address, lineBytes, AccessType.STORE);
            }
        }
        line.address=lineAddress;
        linesByAddress.put(lineAddress, line);
        linkMostRecentlyUsed(line);
        return line;
    }

    private void callbackStatistics() throws Throwable {
        cacheCallback.statistics(cacheHits, cacheMisses, elapsedCycles, loadStores);
    }

    @Override
    public void close() throws IOException {
        log.close();
    }

    @Override
    public Void elapsedCycles(long elapsedCycles) throws Throwable {
        this.elapsedCycles=elapsedCycles;
        callbackStatistics();
        return log.elapsedCycles(elapsedCycles);
    }

    @Override
    public Void end() throws Throwable {
        cacheCallback.end();
        return log.end();
    }

    private void load(CacheLine line) throws Throwable {
        ++loadStores;
        log.access(line.address, lineBytes, AccessType.LOAD_DATA);
    }

    private void linkMostRecentlyUsed(CacheLine line) {
        if (null==mostRecentlyUsedLine) {
            leastRecentlyUsedLine=line;
        }
        else {
            mostRecentlyUsedLine.moreRecentlyUsed=line;
            line.lessRecentlyUsed=mostRecentlyUsedLine;
        }
        mostRecentlyUsedLine=line;
    }

    private void storePolicy(CacheLine line) throws Throwable {
        if (writePolicy.back()) {
            line.dirty=true;
        }
        else {
            ++loadStores;
            log.access(line.address, lineBytes, AccessType.STORE);
        }
    }

    private void unlink(CacheLine line) {
        if (null==line.lessRecentlyUsed) {
            leastRecentlyUsedLine=line.moreRecentlyUsed;
        }
        else {
            line.lessRecentlyUsed.moreRecentlyUsed=line.moreRecentlyUsed;
        }
        if (null==line.moreRecentlyUsed) {
            mostRecentlyUsedLine=line.lessRecentlyUsed;
        }
        else {
            line.moreRecentlyUsed.lessRecentlyUsed=line.lessRecentlyUsed;
        }
        line.lessRecentlyUsed=null;
        line.moreRecentlyUsed=null;
    }

    @Override
    public Void userData(long userData) throws Throwable {
        cacheCallback.userData(userData);
        return log.userData(userData);
    }
}
