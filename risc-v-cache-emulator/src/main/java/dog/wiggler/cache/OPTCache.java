package dog.wiggler.cache;

import dog.wiggler.memory.AccessType;
import dog.wiggler.memory.CollapseElapsedCyclesLog;
import dog.wiggler.memory.Log;
import dog.wiggler.memory.LogInputStream;
import dog.wiggler.memory.LogOutputStream;
import dog.wiggler.memory.LogVisitor;
import dog.wiggler.memory.Logs;
import dog.wiggler.tree23.InsertAtIndex;
import dog.wiggler.tree23.Leaf1;
import dog.wiggler.tree23.LeafPath;
import dog.wiggler.tree23.NormalizedTree;
import dog.wiggler.tree23.RemoveLeaf;
import dog.wiggler.tree23.Selector;
import dog.wiggler.tree23.Tree;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * Creates a new memory access log by applying the OTP cache to an existing memory access log.
 * Line size must be a power of two. This will be checked by the preprocess step.
 */
public class OPTCache {
    private class TempLog implements Log {
        private final @NotNull SeekableByteChannel tempChannel;

        public TempLog(@NotNull SeekableByteChannel tempChannel) {
            this.tempChannel=tempChannel;
        }

        @Override
        public Void access(long address, int size, @NotNull AccessType type) throws Throwable {
            write(Logs.encodeAccess(address, size, type));
            return null;
        }

        @Override
        public Void accessLogDisabled() throws Throwable {
            write(Logs.encodeAccessLogDisabled());
            return null;
        }

        @Override
        public Void accessLogEnabled() throws Throwable {
            write(Logs.encodeAccessLogEnabled());
            return null;
        }

        @Override
        public void close() {
        }

        @Override
        public Void elapsedCycles(long elapsedCycles) throws Throwable {
            write(Logs.encodeElapsedCycles(elapsedCycles));
            return null;
        }

        @Override
        public Void end() {
            return null;
        }

        @Override
        public Void userData(long userData) throws Throwable {
            write(Logs.encodeUserData(userData));
            return null;
        }

        private void write(long data) throws Throwable {
            tempWrite(tempChannel, 2L*tempEntries, data);
            ++tempEntries;
        }
    }

    private static final int PAGE_SIZE;
    private static final long PAGE_SIZE_MASK;
    private static final int PAGE_SIZE_SHIFT=12;

    static {
        PAGE_SIZE=1<<PAGE_SIZE_SHIFT;
        PAGE_SIZE_MASK=PAGE_SIZE-1;
    }

    private final int cacheSizeInLines;
    private final @NotNull CacheType cacheType;
    private final @NotNull Path inputLogPath;
    private final int lineSizeInBytes;
    private final @NotNull Path outputLogPath;
    private final @NotNull ByteBuffer tempBuffer
            =ByteBuffer.allocateDirect(PAGE_SIZE)
            .order(ByteOrder.LITTLE_ENDIAN);
    private long tempBufferAddress=-1L;
    private boolean tempBufferDirty;
    private long tempEntries;
    private final @NotNull Path tempPath;

    private OPTCache(
            int cacheSizeInLines,
            @NotNull CacheType cacheType,
            @NotNull Path inputLogPath,
            int lineSizeInBytes,
            @NotNull Path outputLogPath,
            @NotNull Path tempPath) {
        if (0>=cacheSizeInLines) {
            throw new IllegalArgumentException(
                    "invalid cache size, cache size in lines: %d"
                            .formatted(cacheSizeInLines));
        }
        this.cacheSizeInLines=cacheSizeInLines;
        this.cacheType=Objects.requireNonNull(cacheType, "cacheType");
        this.inputLogPath=Objects.requireNonNull(inputLogPath, "inputLogPath");
        this.lineSizeInBytes=lineSizeInBytes;
        this.outputLogPath=Objects.requireNonNull(outputLogPath, "outputLogPath");
        this.tempPath=Objects.requireNonNull(tempPath, "tempPath");
    }

    private void preprocess(
            @NotNull SeekableByteChannel tempChannel)
            throws Throwable {
        try (var inputLog=LogInputStream.factory(inputLogPath)
                .get();
             var outputLog=new CachePreprocessorLog(
                     cacheType,
                     lineSizeInBytes,
                     true,
                     new CollapseElapsedCyclesLog(
                             new TempLog(tempChannel)))) {
            while (inputLog.hasNext()) {
                inputLog.readNext(outputLog);
            }
            outputLog.end();
        }
    }

    private void processBackward(
            @NotNull SeekableByteChannel tempChannel)
            throws Throwable {
        class Leaf extends Leaf1<Void, Leaf> {
            @Override
            public Void aggregate() {
                return null;
            }

            @Override
            protected @NotNull Leaf self() {
                return this;
            }
        }
        class StackIndexVisitor implements LogVisitor<@NotNull Long> {
            private final @NotNull Map<@NotNull Long, @NotNull Leaf> stackHash=new HashMap<>();
            private @Nullable NormalizedTree<Void, Leaf> stackTree=null;

            @Override
            public @NotNull Long access(long address, int size, @NotNull AccessType type) throws Throwable {
                if (cacheType.notCached(type)) {
                    return -1L;
                }
                /*
                 * Preprocess ensures that no access crosses cache line boundaries.
                 * Loads will always be full lines.
                 * Partial stores will be preceded by a full load.
                 * Nonetheless, we'll process partial stores as full stores to simplify the forward phase.
                 */
                address&=(-lineSizeInBytes);
                var leaf=stackHash.get(address);
                long stackIndex;
                if (null==leaf) {
                    stackIndex=-1L;
                    leaf=new Leaf();
                    stackHash.put(address, leaf);
                }
                else {
                    stackIndex=LeafPath.leafPath(leaf, new LeafPath.Index<>()).index;
                    stackTree=RemoveLeaf.removeLeaf(Tree.VOID_AGGREGATOR, leaf);
                }
                stackTree=InsertAtIndex.insertAtIndex(
                        Tree.VOID_AGGREGATOR,
                        0,
                        leaf,
                        stackTree);
                return stackIndex;
            }

            @Override
            public @NotNull Long accessLogDisabled() {
                return -1L;
            }

            @Override
            public @NotNull Long accessLogEnabled() {
                return -1L;
            }

            @Override
            public @NotNull Long elapsedCycles(long elapsedCycles) {
                return -1L;
            }

            @Override
            public @NotNull Long end() {
                return -1L;
            }

            @Override
            public @NotNull Long userData(long userData) {
                return -1L;
            }
        }
        var stackIndexVisitor=new StackIndexVisitor();
        for (long entryIndex=tempEntries-1L; 0L<=entryIndex; --entryIndex) {
            long logData=tempRead(tempChannel, 2L*entryIndex);
            long stackIndex=Logs.visit(logData, stackIndexVisitor);
            tempWrite(tempChannel, 2L*entryIndex+1L, stackIndex);
        }
        // noinspection ResultOfMethodCallIgnored
        stackIndexVisitor.end();
    }

    private void processForward(
            @NotNull SeekableByteChannel tempChannel)
            throws Throwable {
        class Leaf extends Leaf1<@NotNull Long, Leaf> {
            /**
             * Non-address values pad out the aggregate.
             * Indices according to the aggregate represent forward distances.
             */
            public final boolean address;
            public boolean dirty;
            public final long value;

            public Leaf(boolean address, long value) {
                this.address=address;
                this.value=value;
            }

            @Override
            public @NotNull Long aggregate() {
                return address
                        ?1L
                        :value;
            }

            @Override
            protected @NotNull Leaf self() {
                return this;
            }
        }
        try (var outputLog=CollapseElapsedCyclesLog.factory(
                        LogOutputStream.factory(outputLogPath))
                .get()) {
            class OPTCacheVisitor implements LogVisitor<Void> {
                /**
                 * The number of lines that's holding data.
                 * This is the sum of the size of the infinite stack,
                 * and the number of address leaves in the finite stack.
                 */
                private int usedLines;
                public long forwardDistance;
                /**
                 * Addresses that are in the cache, and have finite forward distance.
                 * The last leaf is never padding.
                 * There are no consecutive paddings.
                 */
                private @Nullable NormalizedTree<@NotNull Long, Leaf> stackFinite;
                /**
                 * Addresses that are in the cache, and have infinite forward distance.
                 */
                private final @NotNull TreeMap<@NotNull Long, @NotNull Leaf> stackInfinite=new TreeMap<>();

                @Override
                public Void access(long address, int size, @NotNull AccessType type) throws Throwable {
                    if (cacheType.notCached(type)) {
                        return outputLog.access(address, size, type);
                    }
                    address&=(-lineSizeInBytes);
                    // If the address is in the cache than it's current forward distance is 0.
                    // So it's the first leaf of the finite stack.
                    var firstLeaf=(null==stackFinite)
                            ?null
                            :Selector.selectLeaf(Selector.firstSelector(), stackFinite);
                    @NotNull Leaf leaf;
                    if ((null==firstLeaf) || (!firstLeaf.address)) {
                        // not in the cache
                        // decrease the forward distance of all finite distances, by shrinking the padding
                        if (null!=firstLeaf) {
                            stackFinite=RemoveLeaf.removeLeaf(Tree.LONG_SUM_AGGREGATOR, firstLeaf);
                            if (1<firstLeaf.value) {
                                stackFinite=InsertAtIndex.insertAtIndex(
                                        Tree.LONG_SUM_AGGREGATOR,
                                        0,
                                        new Leaf(false, firstLeaf.value-1L),
                                        stackFinite);
                            }
                        }
                        // evict the farthest line
                        if (cacheSizeInLines<=usedLines) {
                            @NotNull Leaf lastLeaf;
                            if (stackInfinite.isEmpty()) {
                                Objects.requireNonNull(stackFinite, "stackInfinite");
                                lastLeaf=Selector.selectLeaf(
                                        Selector.lastSelector(),
                                        stackFinite);
                                stackFinite=RemoveLeaf.removeLeaf(
                                        Tree.LONG_SUM_AGGREGATOR,
                                        lastLeaf);
                                // remove the last entry if it's a padding
                                if (null!=stackFinite) {
                                    var lastLeaf2=Selector.selectLeaf(
                                            Selector.lastSelector(),
                                            stackFinite);
                                    if (!lastLeaf2.address) {
                                        stackFinite=RemoveLeaf.removeLeaf(
                                                Tree.LONG_SUM_AGGREGATOR,
                                                lastLeaf2);
                                    }
                                }
                            }
                            else {
                                lastLeaf=stackInfinite.pollLastEntry().getValue();
                            }
                            if (lastLeaf.dirty) {
                                outputLog.access(
                                        lastLeaf.value,
                                        lineSizeInBytes,
                                        AccessType.STORE);
                            }
                            --usedLines;
                        }
                        leaf=new Leaf(true, address);
                        if (type.load()) {
                            // load new line
                            outputLog.access(
                                    address,
                                    lineSizeInBytes,
                                    type);
                        }
                        else {
                            // store new line, it's never partial
                            leaf.dirty=true;
                        }
                        ++usedLines;
                    }
                    else {
                        // in the cache
                        leaf=firstLeaf;
                        // decrease the forward distance of all finite lines
                        stackFinite=RemoveLeaf.removeLeaf(Tree.LONG_SUM_AGGREGATOR, leaf);
                        if (type.store()) {
                            leaf.dirty=true;
                        }
                    }
                    // insert leaf into the stack
                    if (-1L==forwardDistance) {
                        stackInfinite.put(leaf.value, leaf);
                    }
                    else {
                        // insert leaf so that it's index by the aggregate value is equal to the forward distance
                        long treeAggregate=(null==stackFinite)
                                ?0L
                                :stackFinite.aggregate();
                        if (forwardDistance>=treeAggregate) {
                            // the forward distance is larger than all in the tree
                            if (forwardDistance>treeAggregate) {
                                // needs padding
                                stackFinite=InsertAtIndex.insertAtIndex(
                                        Tree.LONG_SUM_AGGREGATOR,
                                        Tree.size(stackFinite),
                                        new Leaf(false, forwardDistance-treeAggregate),
                                        stackFinite);
                            }
                            stackFinite=InsertAtIndex.insertAtIndex(
                                    Tree.LONG_SUM_AGGREGATOR,
                                    Tree.size(stackFinite),
                                    leaf,
                                    stackFinite);
                        }
                        else {
                            Objects.requireNonNull(stackFinite, "stackFinite");
                            var leafAtForwardDistance=Selector.selectLeaf(
                                    Selector.intervalSelector(forwardDistance),
                                    stackFinite);
                            var index=LeafPath.leafPath(leafAtForwardDistance, new LeafPath.Index<>()).index;
                            if (leafAtForwardDistance.address) {
                                // increase the forward distance of this and all larger
                                // it was decreased already, this restores the original value
                                stackFinite=InsertAtIndex.insertAtIndex(
                                        Tree.LONG_SUM_AGGREGATOR,
                                        index,
                                        leaf,
                                        stackFinite);
                            }
                            else {
                                // the padding must be decreased, maybe splitting it
                                var paddingAggregate=LeafPath.leafPath(
                                        leafAtForwardDistance,
                                        new LeafPath.Interval<>())
                                        .aggregate;
                                stackFinite=RemoveLeaf.removeLeaf(
                                        Tree.LONG_SUM_AGGREGATOR,
                                        leafAtForwardDistance);
                                // inserting stuff, in reverse order, so the value of index need not be changed
                                // 1, the padding after the address leaf, this cannot be empty
                                //   if this would be empty we would have selected the leaf after this
                                // 2, the address leaf
                                // 3, the padding before the address leaf, this may be empty
                                // paddingAggregate <= forwardDistance < paddingAggregate+leafAtForwardDistance.value
                                stackFinite=InsertAtIndex.insertAtIndex(
                                        Tree.LONG_SUM_AGGREGATOR,
                                        index,
                                        new Leaf(
                                                false,
                                                paddingAggregate+leafAtForwardDistance.value-forwardDistance),
                                        stackFinite);
                                stackFinite=InsertAtIndex.insertAtIndex(
                                        Tree.LONG_SUM_AGGREGATOR,
                                        index,
                                        leaf,
                                        stackFinite);
                                if (paddingAggregate<forwardDistance) {
                                    stackFinite=InsertAtIndex.insertAtIndex(
                                            Tree.LONG_SUM_AGGREGATOR,
                                            index,
                                            new Leaf(
                                                    false,
                                                    forwardDistance-paddingAggregate),
                                            stackFinite);
                                }
                            }
                        }
                    }
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
            }
            var visitor=new OPTCacheVisitor();
            for (long entryIndex=0L; tempEntries>entryIndex; ++entryIndex) {
                long logData=tempRead(tempChannel, 2L*entryIndex);
                visitor.forwardDistance=tempRead(tempChannel, 2L*entryIndex+1L);
                Logs.visit(
                        logData,
                        visitor);
            }
            visitor.end();
        }
    }

    public void run() throws Throwable {
        try (var tempChannel=Files.newByteChannel(
                tempPath,
                StandardOpenOption.CREATE,
                StandardOpenOption.READ,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE)) {
            preprocess(tempChannel);
            processBackward(tempChannel);
            processForward(tempChannel);
        }
        finally {
            Files.deleteIfExists(tempPath);
        }
    }

    public static void run(
            int cacheSizeInLines,
            @NotNull CacheType cacheType,
            @NotNull Path inputLogPath,
            int lineSizeInBytes,
            @NotNull Path outputLogPath,
            @NotNull Path tempPath)
            throws Throwable {
        new OPTCache(cacheSizeInLines, cacheType, inputLogPath, lineSizeInBytes, outputLogPath, tempPath)
                .run();
    }

    private long tempRead(
            @NotNull SeekableByteChannel tempChannel,
            long halfEntryAddress)
            throws Throwable {
        tempReadBuffer(tempChannel, halfEntryAddress);
        int bufferAddress=(int)((8L*halfEntryAddress)&PAGE_SIZE_MASK);
        return tempBuffer.getLong(bufferAddress);
    }

    private void tempReadBuffer(
            @NotNull SeekableByteChannel tempChannel,
            long halfEntryAddress)
            throws Throwable {
        long address=(8L*halfEntryAddress)&(~PAGE_SIZE_MASK);
        if (address!=tempBufferAddress) {
            if (tempBufferDirty) {
                tempChannel.position(tempBufferAddress);
                tempBuffer.clear();
                tempChannel.write(tempBuffer);
                tempBufferDirty=false;
            }
            if (tempChannel.size()<address+PAGE_SIZE) {
                tempChannel.truncate(address+PAGE_SIZE);
            }
            tempBufferAddress=address;
            tempChannel.position(tempBufferAddress);
            tempBuffer.clear();
            tempChannel.read(tempBuffer);
        }
    }

    private void tempWrite(
            @NotNull SeekableByteChannel tempChannel,
            long halfEntryAddress,
            long data)
            throws Throwable {
        tempReadBuffer(tempChannel, halfEntryAddress);
        int bufferAddress=(int)((8L*halfEntryAddress)&PAGE_SIZE_MASK);
        tempBuffer.putLong(bufferAddress, data);
        tempBufferDirty=true;
    }
}
