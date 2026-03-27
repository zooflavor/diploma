package dog.wiggler;

import dog.wiggler.cache.CacheType;
import dog.wiggler.cache.OPTCache;
import dog.wiggler.emulator.Emulator;
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
    private static final @NotNull String BOTH="both";
    private static final @NotNull String CACHE="cache";
    private static final @NotNull String DATA="data";
    private static final @NotNull String HELP="help";
    private static final @NotNull String INSTRUCTION="instruction";
    private static final @NotNull String NAME="run-emulator";
    private static final @NotNull String OPT="opt";
    private static final @NotNull String RUN="run";
    private static final @NotNull String STATISTICS="statistics";

    private static void cache(String[] args) throws Throwable {
        if (8>args.length) {
            help();
            return;
        }
        switch (args[1]) {
            case OPT -> cacheOpt(args);
            default -> help();
        }
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
            case BOTH -> CacheType.BOTH;
            case DATA -> CacheType.DATA;
            case INSTRUCTION -> CacheType.INSTRUCTION;
            default -> throw new IllegalArgumentException("invalid cache type %s".formatted(value));
        };
    }

    private static void help() {
        System.out.printf("%s%n", Main.class.getName());
        System.out.printf("  %s %s%n", NAME, HELP);
        System.out.printf("    prints this screen%n");
        System.out.printf("  %s %s %s cache-size line-size instruction-type input-log-file output-log-file temp-file%n", NAME, CACHE, OPT);
        System.out.printf("  %s %s replacement-policy cache-size line-size instruction-type input-log-file output-log-file policy-dependent parameters%n", NAME, CACHE);
        System.out.printf("    process memory log file through a cache%n");
        System.out.printf("    replacement policy can be: opt, lru%n");
        System.out.printf("    cache-size is in cache lines%n");
        System.out.printf("    line-size is in bytes%n");
        System.out.printf("    instruction-type can be: %s, %s, %s%n", BOTH, DATA, INSTRUCTION);
        System.out.printf("      %s will process data loads, instruction loads, and stores%n", BOTH);
        System.out.printf("      %s will process data loads, and stores, and leaves instruction loads untouched%n", DATA);
        System.out.printf("      %s will process instruction loads, leaves data loads and stores untouched%n", INSTRUCTION);
        System.out.printf("      input-log-file will be read, and fed to the cache%n");
        System.out.printf("      output-log-file will be fed the output of the cache algorithm%n");
        System.out.printf("    parameters for %s policy:%n", OPT);
        System.out.printf("      temp-file: temporary storage file to calculate forward distances%n");
        System.out.printf("  %s %s program-image log-file memory-size [memory-file]%n", NAME, RUN);
        System.out.printf("    runs program-image%n");
        System.out.printf("    logs the memory accesses to log-file%n");
        System.out.printf("    uses memory-size bytes of memory to run the program%n");
        System.out.printf("    memory-size must be a decimal integer, optionally followed by kb, mb, or gb%n");
        System.out.printf("    if memory-file is specified it will be used to back the memory%n");
        System.out.printf("    if memory-file is not specified memory will be allocated from system memory%n");
        System.out.printf("    standard input is read by line to provide input to the program%n");
        System.out.printf("    standard output is used to print the output, each value on a separate line%n");
        System.out.printf("  %s %s memory-log%n", NAME, STATISTICS);
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
            case CACHE -> cache(args);
            case RUN -> run(args);
            case STATISTICS -> statistics(args);
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
