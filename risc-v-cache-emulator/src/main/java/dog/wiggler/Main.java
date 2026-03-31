package dog.wiggler;

import dog.wiggler.cache.CacheType;
import dog.wiggler.cache.FIFOPolicy;
import dog.wiggler.cache.LFUPolicy;
import dog.wiggler.cache.LRUPolicy;
import dog.wiggler.cache.NWayAssociativeCache;
import dog.wiggler.cache.OPTCache;
import dog.wiggler.cache.RandomPolicy;
import dog.wiggler.cache.ReplacementPolicy;
import dog.wiggler.cache.WriteMiss;
import dog.wiggler.cache.WritePolicy;
import dog.wiggler.elf.ELF;
import dog.wiggler.emulator.Emulator;
import dog.wiggler.function.Supplier;
import dog.wiggler.memory.AccessType;
import dog.wiggler.memory.LogInputStream;
import dog.wiggler.memory.LogOutputStream;
import dog.wiggler.memory.LogVisitor;
import dog.wiggler.memory.MemoryMappedMemory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    private static final @NotNull String ACCESS_TYPE_BOTH="both";
    private static final @NotNull String ACCESS_TYPE_DATA="data";
    private static final @NotNull String ACCESS_TYPE_INSTRUCTION="instruction";
    private static final @NotNull String COMMAND_CACHE="cache";
    private static final @NotNull String COMMAND_ELF="elf";
    private static final @NotNull String COMMAND_HELP="help";
    private static final @NotNull String COMMAND_RUN="run";
    private static final @NotNull String COMMAND_STATISTICS="statistics";
    private static final @NotNull String NAME="run-emulator";
    private static final @NotNull String REPLACEMENT_POLICY_FIFO="fifo";
    private static final @NotNull String REPLACEMENT_POLICY_LFU="lfu";
    private static final @NotNull String REPLACEMENT_POLICY_LRU="lru";
    private static final @NotNull String REPLACEMENT_POLICY_OPT="opt";
    private static final @NotNull String REPLACEMENT_POLICY_RANDOM="random";
    private static final @NotNull String WRITE_MISS_ALLOCATE="allocate";
    private static final @NotNull String WRITE_MISS_DONT_ALLOCATE="dont-allocate";
    private static final @NotNull String WRITE_POLICY_BACK="back";
    private static final @NotNull String WRITE_POLICY_THROUGH="through";

    private static void cache(String[] args) throws Throwable {
        if ((8>args.length) || (11<args.length)) {
            help();
            return;
        }
        if (REPLACEMENT_POLICY_OPT.equals(args[1])) {
            cacheOpt(args);
            return;
        }
        if (10>args.length) {
            help();
            return;
        }
        @NotNull Supplier<@NotNull ReplacementPolicy> replacementPolicyFactory;
        switch (args[1]) {
            case REPLACEMENT_POLICY_FIFO -> replacementPolicyFactory=FIFOPolicy::new;
            case REPLACEMENT_POLICY_LFU -> replacementPolicyFactory=LFUPolicy::new;
            case REPLACEMENT_POLICY_LRU -> replacementPolicyFactory=LRUPolicy::new;
            case REPLACEMENT_POLICY_RANDOM -> {
                if (10==args.length) {
                    replacementPolicyFactory=RandomPolicy::new;
                }
                else {
                    long randomSeed=Long.parseLong(args[10]);
                    replacementPolicyFactory=()->new RandomPolicy(randomSeed);
                }
            }
            default -> {
                help();
                return;
            }
        }
        int cacheSize=Integer.parseInt(args[2]);
        int associativity=Integer.parseInt(args[3]);
        int lineSize=Integer.parseInt(args[4]);
        @NotNull CacheType cacheType=cacheType(args[5]);
        @NotNull WriteMiss writeMiss=switch (args[6]) {
            case WRITE_MISS_ALLOCATE -> WriteMiss.ALLOCATE;
            case WRITE_MISS_DONT_ALLOCATE -> WriteMiss.DON_T_ALLOCATE;
            default -> {
                help();
                throw new RuntimeException();
            }
        };
        @NotNull WritePolicy writePolicy=switch (args[7]) {
            case WRITE_POLICY_BACK -> WritePolicy.WRITE_BACK;
            case WRITE_POLICY_THROUGH -> WritePolicy.WRITE_THROUGH;
            default -> {
                help();
                throw new RuntimeException();
            }
        };
        @NotNull Path inputLogFile=Paths.get(args[8]).toAbsolutePath();
        @NotNull Path outputLogFile=Paths.get(args[9]).toAbsolutePath();
        NWayAssociativeCache.run(
                associativity,
                cacheSize,
                cacheType,
                inputLogFile,
                lineSize,
                outputLogFile,
                replacementPolicyFactory,
                writeMiss,
                writePolicy);
    }

    private static void cacheOpt(String[] args) throws Throwable {
        if (8!=args.length) {
            help();
            return;
        }
        int cacheSize=Integer.parseInt(args[2]);
        int lineSize=Integer.parseInt(args[3]);
        @NotNull CacheType cacheType=cacheType(args[4]);
        @NotNull Path inputLogFile=Paths.get(args[5]).toAbsolutePath();
        @NotNull Path outputLogFile=Paths.get(args[6]).toAbsolutePath();
        @NotNull Path tempFile=Paths.get(args[7]).toAbsolutePath();
        OPTCache.run(
                cacheSize,
                cacheType,
                inputLogFile,
                lineSize,
                outputLogFile,
                tempFile);
    }

    private static @NotNull CacheType cacheType(@NotNull String value) {
        return switch (value) {
            case ACCESS_TYPE_BOTH -> CacheType.BOTH;
            case ACCESS_TYPE_DATA -> CacheType.DATA;
            case ACCESS_TYPE_INSTRUCTION -> CacheType.INSTRUCTION;
            default -> throw new IllegalArgumentException("invalid cache type %s".formatted(value));
        };
    }

    private static void elf(String[] args) throws Throwable {
        if (2!=args.length) {
            help();
            return;
        }
        ELF.read(Paths.get(args[1]))
                .print();
    }

    private static void help() {
        System.out.printf("%s%n", Main.class.getName());
        System.out.printf("  %s %s%n", NAME, COMMAND_HELP);
        System.out.printf("    prints this screen%n");
        System.out.printf("  %s %s %s cache-size line-size instruction-type input-log-file output-log-file temp-file%n", NAME, COMMAND_CACHE, REPLACEMENT_POLICY_OPT);
        System.out.printf("  %s %s replacement-policy cache-size associativity line-size instruction-type write-miss write-policy input-log-file output-log-file (random-seed)%n", NAME, COMMAND_CACHE);
        System.out.printf("    process memory log file through a cache%n");
        System.out.printf("    replacement policy can be: %s, %s, %s, %s, %s%n", REPLACEMENT_POLICY_FIFO, REPLACEMENT_POLICY_LFU, REPLACEMENT_POLICY_LRU, REPLACEMENT_POLICY_OPT, REPLACEMENT_POLICY_RANDOM);
        System.out.printf("    cache-size is in cache lines%n");
        System.out.printf("    associativity is in cache lines%n");
        System.out.printf("      associativity = 1 means a direct mapped cache%n");
        System.out.printf("      associativity = cache-size means a fully associative cache%n");
        System.out.printf("    line-size is in bytes%n");
        System.out.printf("    instruction-type can be: %s, %s, %s%n", ACCESS_TYPE_BOTH, ACCESS_TYPE_DATA, ACCESS_TYPE_INSTRUCTION);
        System.out.printf("      %s will process data loads, instruction loads, and stores%n", ACCESS_TYPE_BOTH);
        System.out.printf("      %s will process data loads, and stores, and leaves instruction loads untouched%n", ACCESS_TYPE_DATA);
        System.out.printf("      %s will process instruction loads, leaves data loads and stores untouched%n", ACCESS_TYPE_INSTRUCTION);
        System.out.printf("      input-log-file will be read, and fed to the cache%n");
        System.out.printf("      output-log-file will be fed the output of the cache algorithm%n");
        System.out.printf("    write-miss can be: %s, %s%n", WRITE_MISS_ALLOCATE, WRITE_MISS_DONT_ALLOCATE);
        System.out.printf("    write-policy can be: %s, %s%n", WRITE_POLICY_BACK, WRITE_POLICY_THROUGH);
        System.out.printf("    temp-file: temporary storage file to calculate forward distances%n");
        System.out.printf("    random-seed: initial seed for the random replacement policy%n");
        System.out.printf("      when it's not specified the seed is calculated from system time%n");
        System.out.printf("  %s %s program-image%n", NAME, COMMAND_ELF);
        System.out.printf("    prints the ELF header of the program image%n");
        System.out.printf("  %s %s program-image log-file memory-size [memory-file]%n", NAME, COMMAND_RUN);
        System.out.printf("    runs program-image%n");
        System.out.printf("    logs the memory accesses to log-file%n");
        System.out.printf("    uses memory-size bytes of memory to run the program%n");
        System.out.printf("    memory-size must be a decimal integer, optionally followed by kb, mb, or gb%n");
        System.out.printf("    if memory-file is specified it will be used to back the memory%n");
        System.out.printf("    if memory-file is not specified memory will be allocated from system memory%n");
        System.out.printf("    standard input is read by line to provide input to the program%n");
        System.out.printf("    standard output is used to print the output, each value on a separate line%n");
        System.out.printf("  %s %s memory-log%n", NAME, COMMAND_STATISTICS);
        System.out.printf("    print some statistics of the memory-log access log file%n");
        System.out.printf("    prints in CSV format%n");
        System.out.printf("    prints the elapsed cycles, and number of memory accesses on every user data entry%n");
        System.out.printf("    prints the elapsed cycles, and number of memory accesses at the end of the file%n");
        System.out.printf("    the columns are user data, elapsed cycles, instruction loads, data loads, stores,%n");
        System.out.printf("      data accesses, and total memory accesses%n");
        System.exit(1);
    }

    public static void main(String[] args) throws Throwable {
        if (0==args.length) {
            help();
            return;
        }
        switch (args[0]) {
            case COMMAND_CACHE -> cache(args);
            case COMMAND_ELF -> elf(args);
            case COMMAND_RUN -> run(args);
            case COMMAND_STATISTICS -> statistics(args);
            default -> help();
        }
    }

    private static void run(String[] args) throws Throwable {
        if ((4!=args.length) && (5!=args.length)) {
            help();
            return;
        }
        @NotNull Path programImage=Paths.get(args[1]);
        @NotNull Path logFile=Paths.get(args[2]);
        // noinspection ExtractMethodRecommender
        @NotNull String memorySizeString=args[3];
        long memorySizeFactor;
        if (memorySizeString.endsWith("b")) {
            if (memorySizeString.endsWith("kb")) {
                memorySizeFactor=1L<<10;
            }
            else if (memorySizeString.endsWith("mb")) {
                memorySizeFactor=1L<<20;
            }
            else if (memorySizeString.endsWith("gb")) {
                memorySizeFactor=1L<<30;
            }
            else {
                throw new IllegalArgumentException("invalid memory size: %s".formatted(memorySizeString));
            }
            memorySizeString=memorySizeString.substring(0, memorySizeString.length()-2);
        }
        else {
            memorySizeFactor=1;
        }
        long memorySize=Long.parseLong(memorySizeString)*memorySizeFactor;
        @Nullable Path memoryFile=(4==args.length)
                ?null
                :(Paths.get(args[4]));
        int exitCode;
        try (var emulator=Emulator.factory(
                        new InputStreamInput(System.in),
                        LogOutputStream.factory(logFile),
                        (null==memoryFile)
                                ?MemoryMappedMemory.factory(false, memorySize)
                                :MemoryMappedMemory.factory(false, memoryFile, memorySize),
                        new AppendableOutput(System.out))
                .get()) {
            emulator.loadELFAndReset(programImage);
            emulator.run();
            exitCode=emulator.exit.code();
        }
        System.exit(exitCode);
    }

    private static void statistics(String[] args) throws Throwable {
        if (2!=args.length) {
            help();
            return;
        }
        var memoryLog=Paths.get(args[1]);
        class StatisticsVisitor implements LogVisitor<Void> {
            public long dataLoads;
            public long elapsedCycles;
            public long instructionLoads;
            public long stores;

            @Override
            public Void access(long address, int size, @NotNull AccessType type) {
                return switch(type) {
                    case LOAD_DATA -> {
                        ++dataLoads;
                        yield null;
                    }
                    case LOAD_INSTRUCTION -> {
                        ++instructionLoads;
                        yield null;
                    }
                    case STORE -> {
                        ++stores;
                        yield null;
                    }
                };
            }

            @Override
            public Void accessLogDisabled() {
                printStatistics("access log disabled");
                return null;
            }

            @Override
            public Void accessLogEnabled() {
                printStatistics("access log enabled");
                return null;
            }

            @Override
            public Void elapsedCycles(long elapsedCycles) {
                this.elapsedCycles=elapsedCycles;
                return null;
            }

            @Override
            public Void end() {
                return null;
            }

            public void printStatistics(@NotNull Object userData) {
                System.out.printf(
                        "%s,%d,%d,%d,%d,%d,%d%n",
                        userData,
                        elapsedCycles,
                        instructionLoads,
                        dataLoads,
                        stores,
                        dataLoads+stores,
                        instructionLoads+dataLoads+stores);
            }

            @Override
            public Void userData(long userData) {
                printStatistics(userData);
                return null;
            }
        }
        try (var log=LogInputStream.factory(memoryLog)
                        .get()) {
            var visitor=new StatisticsVisitor();
            System.out.printf("user data,elapsed cycles,instruction loads,data loads,stores,data accesses,total accesses%n");
            while (log.hasNext()) {
                log.readNext(visitor);
            }
            visitor.printStatistics("end");
        }
    }
}
