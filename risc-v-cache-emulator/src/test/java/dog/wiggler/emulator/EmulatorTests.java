package dog.wiggler.emulator;

import dog.wiggler.elf.SymbolTableEntry;
import dog.wiggler.function.BiConsumer;
import dog.wiggler.function.BiFunction;
import dog.wiggler.function.Consumer;
import dog.wiggler.function.Function;
import dog.wiggler.function.Runnable;
import dog.wiggler.function.Supplier;
import dog.wiggler.function.TriFunction;
import dog.wiggler.memory.AccessType;
import dog.wiggler.memory.Log;
import dog.wiggler.memory.LogInputStream;
import dog.wiggler.memory.LogOutputStream;
import dog.wiggler.memory.Logs;
import dog.wiggler.memory.Memory;
import dog.wiggler.memory.MemoryMappedMemory;
import dog.wiggler.riscv64.Casts;
import dog.wiggler.riscv64.abi.ABI;
import dog.wiggler.riscv64.abi.FunctionCallParameters;
import dog.wiggler.riscv64.abi.PrimitiveType;
import dog.wiggler.riscv64.abi.PrimitiveValue;
import dog.wiggler.riscv64.abi.StackOverflowException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.EOFException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.TreeMap;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@ParameterizedClass
@MethodSource("parameters")
@TestMethodOrder(MethodOrderer.MethodName.class)
public class EmulatorTests {
    public record MemorySettings(
            boolean allowMisalignedAccess,
            @Nullable Path memoryImagePath,
            long memoryImageSize) {
        public @NotNull Supplier<@NotNull Memory> factory() {
            return (null==memoryImagePath())
                    ?MemoryMappedMemory.factory(allowMisalignedAccess(), memoryImageSize())
                    :MemoryMappedMemory.factory(allowMisalignedAccess(), memoryImagePath(), memoryImageSize());
        }
    }

    public static final @NotNull List<@NotNull PrimitiveValue<@NotNull Double>> DOUBLES;
    public static final @NotNull Path ELF_PATH=Paths.get("../c/out").toAbsolutePath();
    public static final @NotNull List<@NotNull String> EXECUTABLE_IMAGE_OPTIONS=List.of(
            "clang-O0",
            "clang-O1",
            "clang-O2",
            "gcc-O0",
            "gcc-O1",
            "gcc-O2");
    public static final @NotNull List<@NotNull PrimitiveValue<@NotNull Float>> FLOATS;
    public static final @NotNull List<@NotNull PrimitiveValue<@NotNull Short>> SINT16S;
    public static final @NotNull List<@NotNull PrimitiveValue<@NotNull Integer>> SINT32S;
    public static final @NotNull List<@NotNull PrimitiveValue<@NotNull Long>> SINT64S;
    public static final @NotNull List<@NotNull PrimitiveValue<@NotNull Byte>> SINT8S;
    public static final boolean SLOW=false;
    public static final @NotNull List<@NotNull PrimitiveValue<@NotNull Short>> UINT16S;
    public static final @NotNull List<@NotNull PrimitiveValue<@NotNull Integer>> UINT32S;
    public static final @NotNull List<@NotNull PrimitiveValue<@NotNull Long>> UINT64S;
    public static final @NotNull List<@NotNull PrimitiveValue<@NotNull Byte>> UINT8S;

    static {
        var doubles0=new HashSet<@NotNull Double>();
        doubles0.add(0.0);
        doubles0.add(1.0);
        doubles0.add(2.0);
        doubles0.add(12345.0);
        doubles0.add(Math.E);
        doubles0.add(Math.PI);
        doubles0.add(Double.MAX_VALUE);
        doubles0.add(Double.MIN_VALUE);
        doubles0.add(Double.NaN);
        doubles0.add(Double.POSITIVE_INFINITY);
        if (SLOW) {
            for (int ii=64; 0<=ii; --ii) {
                doubles0.add((double)ii);
            }
            long value0=Double.doubleToRawLongBits(1.0)&0x800fffffffffffffL;
            long value1=Double.doubleToRawLongBits(3.0)&0x800fffffffffffffL;
            long value2=Double.doubleToRawLongBits(5.0)&0x800fffffffffffffL;
            for (int ii=Double.MIN_EXPONENT; Double.MAX_EXPONENT>=ii; ii+=127) {
                doubles0.add(Double.longBitsToDouble(value0|(((long)ii)<<52)));
                doubles0.add(Double.longBitsToDouble(value1|(((long)ii)<<52)));
                doubles0.add(Double.longBitsToDouble(value2|(((long)ii)<<52)));
            }
        }
        var doubles1=new HashSet<@NotNull Double>();
        for (double value: doubles0) {
            doubles1.add(value);
            doubles1.add(-value);
            doubles1.add(1.0/value);
        }
        DOUBLES=List.copyOf(doubles1.stream()
                .sorted()
                .distinct()
                .map(PrimitiveType.dfloat()::value)
                .toList());

        var floats0=new HashSet<@NotNull Float>();
        floats0.add(0.0f);
        floats0.add(1.0f);
        floats0.add(2.0f);
        floats0.add(12345.0f);
        floats0.add((float)Math.E);
        floats0.add((float)Math.PI);
        floats0.add(Float.MAX_VALUE);
        floats0.add(Float.MIN_VALUE);
        floats0.add(Float.POSITIVE_INFINITY);
        if (SLOW) {
            for (int ii=32; 0<=ii; --ii) {
                floats0.add((float)ii);
            }
            int value0=Float.floatToRawIntBits(1.0f)&0x807fffff;
            int value1=Float.floatToRawIntBits(3.0f)&0x807fffff;
            int value2=Float.floatToRawIntBits(5.0f)&0x807fffff;
            for (int ii=Float.MIN_EXPONENT; Float.MAX_EXPONENT>=ii; ii+=15) {
                floats0.add(Float.intBitsToFloat(value0|(ii<<23)));
                floats0.add(Float.intBitsToFloat(value1|(ii<<23)));
                floats0.add(Float.intBitsToFloat(value2|(ii<<23)));
            }
        }
        var floats1=new HashSet<@NotNull Float>();
        for (float value: floats0) {
            floats1.add(value);
            floats1.add(-value);
            floats1.add(1.0f/value);
        }
        FLOATS=List.copyOf(floats1.stream()
                .sorted()
                .distinct()
                .map(PrimitiveType.sfloat()::value)
                .toList());

        var int16s0=new HashSet<@NotNull Short>();
        int16s0.add((short)0);
        int16s0.add((short)1);
        int16s0.add((short)2);
        int16s0.add((short)12345);
        int16s0.add(Short.MAX_VALUE);
        int16s0.add(Short.MIN_VALUE);
        if (SLOW) {
            int16s0.add((short)32);
            for (int ii=15; 0<=ii; --ii) {
                int16s0.add((short)ii);
                int16s0.add((short)(1<<ii));
                int16s0.add((short)(3<<ii));
                int16s0.add((short)(5<<ii));
            }
        }
        var int16s1=new HashSet<@NotNull Short>();
        for (int value: int16s0) {
            int16s1.add((short)value);
            int16s1.add((short)-value);
            int16s1.add((short)~value);
        }
        SINT16S=List.copyOf(int16s1.stream()
                .sorted()
                .distinct()
                .map(PrimitiveType.sint16()::value)
                .toList());
        UINT16S=List.copyOf(int16s1.stream()
                .sorted()
                .distinct()
                .map(PrimitiveType.uint16()::value)
                .toList());

        var int32s0=new HashSet<@NotNull Integer>();
        int32s0.add(0);
        int32s0.add(1);
        int32s0.add(2);
        int32s0.add(12345);
        int32s0.add(Integer.MAX_VALUE);
        int32s0.add(Integer.MIN_VALUE);
        if (SLOW) {
            int32s0.add(32);
            for (int ii=31; 0<=ii; --ii) {
                int32s0.add(ii);
                int32s0.add(1<<ii);
                int32s0.add(3<<ii);
                int32s0.add(5<<ii);
            }
        }
        var int32s1=new HashSet<@NotNull Integer>();
        for (int value: int32s0) {
            int32s1.add(value);
            int32s1.add(-value);
            int32s1.add(~value);
        }
        SINT32S=List.copyOf(int32s1.stream()
                .sorted()
                .distinct()
                .map(PrimitiveType.sint32()::value)
                .toList());
        UINT32S=List.copyOf(int32s1.stream()
                .sorted()
                .distinct()
                .map(PrimitiveType.uint32()::value)
                .toList());

        var int64s0=new HashSet<@NotNull Long>();
        int64s0.add(0L);
        int64s0.add(1L);
        int64s0.add(2L);
        int64s0.add(12345L);
        int64s0.add(Long.MAX_VALUE);
        int64s0.add(Long.MIN_VALUE);
        if (SLOW) {
            int64s0.add(64L);
            for (int ii=63; 0<=ii; --ii) {
                int64s0.add((long)ii);
                int64s0.add(1L<<ii);
                int64s0.add(3L<<ii);
                int64s0.add(5L<<ii);
            }
        }
        var int64s1=new HashSet<@NotNull Long>();
        for (long value: int64s0) {
            int64s1.add(value);
            int64s1.add(-value);
            int64s1.add(~value);
        }
        SINT64S=List.copyOf(int64s1.stream()
                .sorted()
                .distinct()
                .map(PrimitiveType.sint64()::value)
                .toList());
        UINT64S=List.copyOf(int64s1.stream()
                .sorted()
                .distinct()
                .map(PrimitiveType.uint64()::value)
                .toList());

        var int8s0=new HashSet<@NotNull Byte>();
        int8s0.add((byte)0);
        int8s0.add((byte)1);
        int8s0.add((byte)2);
        int8s0.add((byte)123);
        int8s0.add(Byte.MAX_VALUE);
        int8s0.add(Byte.MIN_VALUE);
        if (SLOW) {
            int8s0.add((byte)32);
            for (int ii=7; 0<=ii; --ii) {
                int8s0.add((byte)ii);
                int8s0.add((byte)(1<<ii));
                int8s0.add((byte)(3<<ii));
                int8s0.add((byte)(5<<ii));
            }
        }
        var int8s1=new HashSet<@NotNull Byte>();
        for (int value: int8s0) {
            int8s1.add((byte)value);
            int8s1.add((byte)-value);
            int8s1.add((byte)~value);
        }
        SINT8S=List.copyOf(int8s1.stream()
                .sorted()
                .distinct()
                .map(PrimitiveType.sint8()::value)
                .toList());
        UINT8S=List.copyOf(int8s1.stream()
                .sorted()
                .distinct()
                .map(PrimitiveType.uint8()::value)
                .toList());
    }

    private Emulator emulator;
    private final @NotNull String executableImageOption;
    private final @NotNull MemorySettings memorySettings;

    public EmulatorTests(
            @NotNull String executableImageOption,
            @NotNull MemorySettings memorySettings) {
        this.executableImageOption=executableImageOption;
        this.memorySettings=memorySettings;
    }

    @AfterEach
    public void afterEach() throws Throwable {
        if (null!=emulator) {
            emulator.close();
        }
    }

    public <J, K> void assertFunction1(
            @NotNull Function<J, K> expected,
            @NotNull String function,
            @NotNull PrimitiveType<K> resultType,
            @NotNull Iterable<@NotNull PrimitiveValue<J>> parameters)
            throws Throwable {
        for (var parameter: parameters) {
            assertEquals(
                    resultType.value(expected.apply(parameter.value())),
                    callFunction(true, function, resultType, parameter),
                    String.format("function=%s, parameter=%s", function, parameter));
        }
    }

    public <J, K, L>
    void assertFunction2(
            @NotNull BiFunction<J, K, L> expected,
            @NotNull String function,
            @NotNull PrimitiveType<L> resultType,
            @NotNull Iterable<@NotNull PrimitiveValue<J>> parameters0,
            @NotNull Iterable<@NotNull PrimitiveValue<K>> parameters1)
            throws Throwable {
        for (var parameter0: parameters0) {
            for (var parameter1: parameters1) {
                assertEquals(
                        expected.apply(parameter0.value(), parameter1.value()),
                        callFunction(true, function, resultType, parameter0, parameter1)
                                .value(),
                        String.format("function=%s, parameter0=%s, parameter1=%s", function, parameter0, parameter1));
            }
        }
    }

    public <J, K, L, M>
    void assertFunction3(
            @NotNull TriFunction<J, K, L, M> expected,
            @NotNull String function,
            @NotNull PrimitiveType<M> resultType,
            @NotNull Iterable<@NotNull PrimitiveValue<J>> parameters0,
            @NotNull Iterable<@NotNull PrimitiveValue<K>> parameters1,
            @NotNull Iterable<@NotNull PrimitiveValue<L>> parameters2)
            throws Throwable {
        for (var parameter0: parameters0) {
            for (var parameter1: parameters1) {
                for (var parameter2: parameters2) {
                    assertEquals(
                            expected.apply(parameter0.value(), parameter1.value(), parameter2.value()),
                            callFunction(true, function, resultType, parameter0, parameter1, parameter2)
                                    .value(),
                            String.format(
                                    "function=%s, parameter0=%s, parameter1=%s, parameter2=%s",
                                    function, parameter0, parameter1, parameter2));
                }
            }
        }
    }

    @BeforeEach
    public void beforeEach() throws Throwable {
        emulator=emulator(null, Log::noOp, null);
    }

    public <J> @NotNull PrimitiveValue<J> callFunction(
            boolean reset,
            @NotNull String function,
            @NotNull PrimitiveType<J> resultType,
            @NotNull List<@NotNull PrimitiveValue<?>> parameters)
            throws Throwable {
        SymbolTableEntry functionSymbol=emulator.elfHeader().symbolTable().get(function);
        assertNotNull(functionSymbol, function);
        if (reset) {
            emulator.reset();
        }
        FunctionCallParameters.create()
                .addAll(parameters)
                .setParameters(emulator);
        emulator.hart.setReturnAddress(emulator.heapAndStack, IOMap.EXIT_OK);
        emulator.hart.setPc(functionSymbol.value());
        emulator.run();
        assertEquals(0, emulator.exit.code());
        return resultType.value(resultType.functionCallResult(emulator.hart));
    }

    public <J> @NotNull PrimitiveValue<J> callFunction(
            boolean reset,
            @NotNull String function,
            @NotNull PrimitiveType<J> resultType,
            @NotNull PrimitiveValue<?> @NotNull ... parameters)
            throws Throwable {
        return callFunction(reset, function, resultType, List.of(parameters));
    }

    private @NotNull Emulator emulator(
            @Nullable Input input,
            @NotNull Supplier<? extends @NotNull Log> logFactory,
            @Nullable Output output)
            throws Throwable {
        if (null==input) {
            input=Input.empty();
        }
        if (null==output) {
            output=Output.refuse();
        }
        Path imageFile=imagePath(executableImageOption, "emulator-tests");
        boolean error=true;
        Emulator emulator=Emulator
                .factory(
                        input,
                        logFactory,
                        memorySettings.factory(),
                        output)
                .get();
        try {
            emulator.loadELFAndReset(imageFile);
            error=false;
            return emulator;
        }
        finally {
            if (error) {
                emulator.close();
            }
        }
    }

    public static @NotNull Path imagePath(
            @NotNull String executableImageOption,
            @NotNull String imageName) {
        return ELF_PATH.resolve(imageName+".riscv64-"+executableImageOption+".elf");
    }

    public static @NotNull Iterable<@NotNull PrimitiveValue<@NotNull Integer>> int32s(
            int from,
            int to,
            @NotNull PrimitiveType<@NotNull Integer> type) {
        return ()->new Iterator<>() {
            private int next=from;

            @Override
            public boolean hasNext() {
                return to>next;
            }

            @Override
            public @NotNull PrimitiveValue<@NotNull Integer> next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                int result=next;
                ++next;
                return type.value(result);
            }
        };
    }

    public static @NotNull Stream<@NotNull Arguments> parameters() {
        @NotNull List<@NotNull Arguments> result=new ArrayList<>();
        for (var executableImageOptions: EXECUTABLE_IMAGE_OPTIONS) {
            for (var memorySettings: List.of(
                    new MemorySettings(
                            false,
                            null,
                            1L<<24),
                    new MemorySettings(
                            true,
                            null,
                            1L<<24),
                    new MemorySettings(
                            false,
                            Paths.get("../memory.image").toAbsolutePath(),
                            1L<<31))) {
                result.add(
                        Arguments.of(
                                executableImageOptions,
                                memorySettings));
            }
        }
        return result.stream();
    }

    public static @NotNull Random random() {
        return new Random(267238775);
    }

    @Test
    public void testAdd() throws Throwable {
        assertFunction2(Double::sum, "add_double", PrimitiveType.dfloat(), DOUBLES, DOUBLES);
        assertFunction2(Float::sum, "add_float", PrimitiveType.sfloat(), FLOATS, FLOATS);
        assertFunction2((x, y)->(short)(x+y), "add_int16", PrimitiveType.sint16(), SINT16S, SINT16S);
        assertFunction2(Integer::sum, "add_int32", PrimitiveType.sint32(), SINT32S, SINT32S);
        assertFunction2(Long::sum, "add_int64", PrimitiveType.sint64(), SINT64S, SINT64S);
        assertFunction2((x, y)->(byte)(x+y), "add_int8", PrimitiveType.sint8(), SINT8S, SINT8S);
        assertFunction2((x, y)->(short)(x+y), "add_uint16", PrimitiveType.uint16(), UINT16S, UINT16S);
        assertFunction2(Integer::sum, "add_uint32", PrimitiveType.uint32(), UINT32S, UINT32S);
        assertFunction2(Long::sum, "add_uint64", PrimitiveType.uint64(), UINT64S, UINT64S);
        assertFunction2((x, y)->(byte)(x+y), "add_uint8", PrimitiveType.uint8(), UINT8S, UINT8S);
    }

    @Test
    public void testAddArray() throws Throwable {
        testAddArray("add_array_double", Random::nextDouble, Double::sum, PrimitiveType.dfloat());
        testAddArray("add_array_float", Random::nextFloat, Float::sum, PrimitiveType.sfloat());
        testAddArray("add_array_int16", (random)->(short)random.nextInt(), (x, y)->(short)(x+y), PrimitiveType.sint16());
        testAddArray("add_array_int32", Random::nextInt, Integer::sum, PrimitiveType.sint32());
        testAddArray("add_array_int64", Random::nextLong, Long::sum, PrimitiveType.sint64());
        testAddArray("add_array_int8", (random)->(byte)random.nextInt(), (x, y)->(byte)(x+y), PrimitiveType.sint8());
        testAddArray("add_array_uint16", (random)->(short)random.nextInt(), (x, y)->(short)(x+y), PrimitiveType.uint16());
        testAddArray("add_array_uint32", Random::nextInt, Integer::sum, PrimitiveType.uint32());
        testAddArray("add_array_uint64", Random::nextLong, Long::sum, PrimitiveType.uint64());
        testAddArray("add_array_uint8", (random)->(byte)random.nextInt(), (x, y)->(byte)(x+y), PrimitiveType.uint8());
    }

    private <J> void testAddArray(
            @NotNull String function,
            @NotNull Function<@NotNull Random, J> next,
            @NotNull BiFunction<J, J, J> operator,
            @NotNull PrimitiveType<J> type)
            throws Throwable {
        final int size=16;
        emulator.reset();
        long baseAddress=emulator.heapAndStack.malloc(emulator.hart, 8L*size*type.size());
        assertNotEquals(0L, baseAddress);
        Random random=random();
        for (int ii=memorySettings.allowMisalignedAccess()?7:0; 0<=ii; --ii) {
            long input0=baseAddress+ii;
            long input1=input0+(long)type.size()*size;
            long output=input1+(long)type.size()*size;
            List<J> expectedInput0=new ArrayList<>(size);
            List<J> expectedInput1=new ArrayList<>(size);
            List<J> expectedOutput=new ArrayList<>(size);
            for (int jj=0; size>jj; ++jj) {
                J value0=next.apply(random);
                J value1=next.apply(random);
                type.store(emulator.memoryLog, input0+(long)type.size()*jj, value0);
                type.store(emulator.memoryLog, input1+(long)type.size()*jj, value1);
                expectedInput0.add(value0);
                expectedInput1.add(value1);
                expectedOutput.add(operator.apply(value0, value1));
            }
            callFunction(
                    true,
                    function,
                    PrimitiveType.voidType(),
                    PrimitiveType.uint64().value(input0),
                    PrimitiveType.uint64().value(input1),
                    PrimitiveType.uint64().value(output),
                    PrimitiveType.uint32().value(size));
            for (int jj=0; size>jj; ++jj) {
                assertEquals(
                        expectedInput0.get(jj),
                        type.load(emulator.memoryLog, input0+(long)type.size()*jj),
                        String.format("ii=%s, jj=%s", ii, jj));
                assertEquals(
                        expectedInput1.get(jj),
                        type.load(emulator.memoryLog, input1+(long)type.size()*jj),
                        String.format("ii=%s, jj=%s", ii, jj));
                assertEquals(
                        expectedOutput.get(jj),
                        type.load(emulator.memoryLog, output+(long)type.size()*jj),
                        String.format("ii=%s, jj=%s", ii, jj));
            }
        }
    }

    @Test
    public void testAndBitwise() throws Throwable {
        assertFunction2((x, y)->(short)(x&y), "and_bitwise_int16", PrimitiveType.sint16(), SINT16S, SINT16S);
        assertFunction2((x, y)->x&y, "and_bitwise_int32", PrimitiveType.sint32(), SINT32S, SINT32S);
        assertFunction2((x, y)->x&y, "and_bitwise_int64", PrimitiveType.sint64(), SINT64S, SINT64S);
        assertFunction2((x, y)->(byte)(x&y), "and_bitwise_int8", PrimitiveType.sint8(), SINT8S, SINT8S);
        assertFunction2((x, y)->(short)(x&y), "and_bitwise_uint16", PrimitiveType.uint16(), UINT16S, UINT16S);
        assertFunction2((x, y)->x&y, "and_bitwise_uint32", PrimitiveType.uint32(), UINT32S, UINT32S);
        assertFunction2((x, y)->x&y, "and_bitwise_uint64", PrimitiveType.uint64(), UINT64S, UINT64S);
        assertFunction2((x, y)->(byte)(x&y), "and_bitwise_uint8", PrimitiveType.uint8(), UINT8S, UINT8S);
    }

    @Test
    public void testAndLogical() throws Throwable {
        assertFunction2((x, y)->((0.0==x) || (0.0==y))?0.0:1.0, "and_logical_double", PrimitiveType.dfloat(), DOUBLES, DOUBLES);
        assertFunction2((x, y)->((0.0f==x) || (0.0f==y))?0.0f:1.0f, "and_logical_float", PrimitiveType.sfloat(), FLOATS, FLOATS);
        assertFunction2((x, y)->(short)(((0==x) || (0==y))?0:1), "and_logical_int16", PrimitiveType.sint16(), SINT16S, SINT16S);
        assertFunction2((x, y)->((0==x) || (0==y))?0:1, "and_logical_int32", PrimitiveType.sint32(), SINT32S, SINT32S);
        assertFunction2((x, y)->((0L==x) || (0L==y))?0L:1L, "and_logical_int64", PrimitiveType.sint64(), SINT64S, SINT64S);
        assertFunction2((x, y)->(byte)(((0==x) || (0==y))?0:1), "and_logical_int8", PrimitiveType.sint8(), SINT8S, SINT8S);
        assertFunction2((x, y)->((0L==x) || (0L==y))?0L:1L, "and_logical_ptr", PrimitiveType.uint64(), UINT64S, UINT64S);
        assertFunction2((x, y)->(short)(((0==x) || (0==y))?0:1), "and_logical_uint16", PrimitiveType.uint16(), UINT16S, UINT16S);
        assertFunction2((x, y)->((0==x) || (0==y))?0:1, "and_logical_uint32", PrimitiveType.uint32(), UINT32S, UINT32S);
        assertFunction2((x, y)->((0L==x) || (0L==y))?0L:1L, "and_logical_uint64", PrimitiveType.uint64(), UINT64S, UINT64S);
        assertFunction2((x, y)->(byte)(((0==x) || (0==y))?0:1), "and_logical_uint8", PrimitiveType.uint8(), UINT8S, UINT8S);
    }

    @Test
    public void testCasts() throws Throwable {
        testCasts(
                "cast_double",
                (value)->value,
                (value)->(float)value.doubleValue(),
                (value)->(short)value.doubleValue(),
                (value)->(int)value.doubleValue(),
                (value)->(long)value.doubleValue(),
                (value)->(byte)value.doubleValue(),
                Casts::castDoubleToUint64,
                (value)->List.of(
                        (short)value.doubleValue(),
                        (short)Casts.castDoubleToUint64(value)),
                Casts::castDoubleToUint32,
                Casts::castDoubleToUint64,
                (value)->List.of(
                        (byte)value.doubleValue(),
                        (byte)Casts.castDoubleToUint64(value)),
                DOUBLES);
        testCasts(
                "cast_float",
                (value)->(double)value,
                (value)->value,
                (value)->(short)value.floatValue(),
                (value)->(int)value.floatValue(),
                (value)->(long)value.floatValue(),
                (value)->(byte)value.floatValue(),
                (value)->Casts.castDoubleToUint64(value),
                (value)->List.of(
                        (short)value.floatValue(),
                        (short)Casts.castDoubleToUint64(value)),
                (value)->Casts.castDoubleToUint32(value),
                (value)->Casts.castDoubleToUint64(value),
                (value)->List.of(
                        (byte)value.floatValue(),
                        (byte)Casts.castDoubleToUint64(value)),
                FLOATS);
        testCasts(
                "cast_int16",
                (value)->(double)value,
                (value)->(float)value,
                (value)->value,
                (value)->(int)value,
                (value)->(long)value,
                (value)->(byte)value.shortValue(),
                (value)->(long)value,
                List::of,
                (value)->(int)value,
                (value)->(long)value,
                (value)->List.of((byte)value.shortValue()),
                SINT16S);
        testCasts(
                "cast_int32",
                (value)->(double)value,
                (value)->(float)value,
                (value)->(short)value.intValue(),
                (value)->value,
                (value)->(long)value,
                (value)->(byte)value.intValue(),
                (value)->(long)value,
                (value)->List.of((short)value.intValue()),
                (value)->value,
                (value)->(long)value,
                (value)->List.of((byte)value.intValue()),
                SINT32S);
        testCasts(
                "cast_int64",
                (value)->(double)value,
                (value)->(float)value,
                (value)->(short)value.longValue(),
                (value)->(int)value.longValue(),
                (value)->value,
                (value)->(byte)value.longValue(),
                (value)->value,
                (value)->List.of((short)value.longValue()),
                (value)->(int)value.longValue(),
                (value)->value,
                (value)->List.of((byte)value.longValue()),
                SINT64S);
        testCasts(
                "cast_int8",
                (value)->(double)value,
                (value)->(float)value,
                (value)->(short)value,
                (value)->(int)value,
                (value)->(long)value,
                (value)->value,
                (value)->(long)value,
                (value)->List.of((short)value),
                (value)->(int)value,
                (value)->(long)value,
                List::of,
                SINT8S);
        testCasts(
                "cast_ptr",
                Casts::castUint64ToDouble,
                (value)->(float)Casts.castUint64ToDouble(value),
                (value)->(short)value.longValue(),
                (value)->(int)value.longValue(),
                (value)->value,
                (value)->(byte)value.longValue(),
                (value)->value,
                (value)->List.of((short)value.longValue()),
                (value)->(int)value.longValue(),
                (value)->value,
                (value)->List.of((byte)value.longValue()),
                UINT64S);
        testCasts(
                "cast_uint16",
                (value)->(double)(value&0xffff),
                (value)->(float)(value&0xffff),
                (value)->value,
                (value)->value&0xffff,
                (value)->value&0xffffL,
                (value)->(byte)value.shortValue(),
                (value)->value&0xffffL,
                List::of,
                (value)->value&0xffff,
                (value)->value&0xffffL,
                (value)->List.of((byte)value.shortValue()),
                UINT16S);
        testCasts(
                "cast_uint32",
                (value)->(double)(value&0xffffffffL),
                (value)->(float)(value&0xffffffffL),
                (value)->(short)value.intValue(),
                (value)->value,
                (value)->value&0xffffffffL,
                (value)->(byte)value.intValue(),
                (value)->value&0xffffffffL,
                (value)->List.of((short)value.intValue()),
                (value)->value,
                (value)->value&0xffffffffL,
                (value)->List.of((byte)(value&0xff)),
                UINT32S);
        testCasts(
                "cast_uint64",
                Casts::castUint64ToDouble,
                (value)->(float)Casts.castUint64ToDouble(value),
                (value)->(short)value.longValue(),
                (value)->(int)value.longValue(),
                (value)->value,
                (value)->(byte)value.longValue(),
                (value)->value,
                (value)->List.of((short)value.longValue()),
                (value)->(int)value.longValue(),
                (value)->value,
                (value)->List.of((byte)value.longValue()),
                UINT64S);
        testCasts(
                "cast_uint8",
                (value)->(double)(value&0xff),
                (value)->(float)(value&0xff),
                (value)->(short)(value&0xff),
                (value)->value&0xff,
                (value)->value&0xffL,
                (value)->value,
                (value)->value&0xffL,
                (value)->List.of((short)(value&0xff)),
                (value)->value&0xff,
                (value)->value&0xffL,
                List::of,
                UINT8S);
    }

    private <J> void testCasts(
            @NotNull String function,
            @NotNull Function<J, @NotNull Double> toDouble,
            @NotNull Function<J, @NotNull Float> toFloat,
            @NotNull Function<J, @NotNull Short> toInt16,
            @NotNull Function<J, @NotNull Integer> toInt32,
            @NotNull Function<J, @NotNull Long> toInt64,
            @NotNull Function<J, @NotNull Byte> toInt8,
            @NotNull Function<J, @NotNull Long> toPtr,
            @NotNull Function<J, @NotNull List<@NotNull Short>> toUint16,
            @NotNull Function<J, @NotNull Integer> toUint32,
            @NotNull Function<J, @NotNull Long> toUint64,
            @NotNull Function<J, @NotNull List<@NotNull Byte>> toUint8,
            @NotNull Iterable<@NotNull PrimitiveValue<J>> values)
            throws Throwable {
        emulator.reset();
        long casts=emulator.heapAndStack.malloc(emulator.hart, 72);
        assertNotEquals(0L, casts);
        for (var value: values) {
            callFunction(
                    true,
                    function,
                    PrimitiveType.voidType(),
                    PrimitiveType.uint64().value(casts),
                    value);
            assertEquals(
                    toDouble.apply(value.value()),
                    Double.valueOf(emulator.memoryLog.loadDouble(casts)),
                    String.format("function=%s, value=%s", function, value));
            assertEquals(
                    toFloat.apply(value.value()),
                    Float.valueOf(emulator.memoryLog.loadFloat(casts+8)),
                    String.format("function=%s, value=%s", function, value));
            assertEquals(
                    toInt16.apply(value.value()),
                    Short.valueOf(emulator.memoryLog.loadInt16(casts+12)),
                    String.format("function=%s, value=%s", function, value));
            assertEquals(
                    toInt32.apply(value.value()),
                    Integer.valueOf(emulator.memoryLog.loadInt32(casts+16, false)),
                    String.format("function=%s, value=%s", function, value));
            assertEquals(
                    toInt64.apply(value.value()),
                    Long.valueOf(emulator.memoryLog.loadInt64(casts+24)),
                    String.format("function=%s, value=%s", function, value));
            assertEquals(
                    toInt8.apply(value.value()),
                    Byte.valueOf(emulator.memoryLog.loadInt8(casts+32)),
                    String.format("function=%s, value=%s", function, value));
            assertEquals(
                    toPtr.apply(value.value()),
                    Long.valueOf(emulator.memoryLog.loadInt64(casts+40)),
                    String.format("function=%s, value=%s", function, value));
            List<Short> expectedUint16s=toUint16.apply(value.value());
            Short actualUint16=emulator.memoryLog.loadInt16(casts+48);
            assertTrue(
                    expectedUint16s.contains(actualUint16),
                    String.format("value=%s, expected=%s, actual=%s", value, expectedUint16s, actualUint16));
            assertEquals(
                    toUint32.apply(value.value()),
                    Integer.valueOf(emulator.memoryLog.loadInt32(casts+52, false)),
                    String.format("value=%s", value));
            assertEquals(
                    toUint64.apply(value.value()),
                    Long.valueOf(emulator.memoryLog.loadInt64(casts+56)),
                    String.format("value=%s", value));
            List<Byte> expectedUint8s=toUint8.apply(value.value());
            Byte actualUint8=emulator.memoryLog.loadInt8(casts+64);
            assertTrue(
                    expectedUint8s.contains(actualUint8),
                    String.format("value=%s, expected=%s, actual=%s", value, expectedUint8s, actualUint8));
        }
    }

    @Test
    public void testConditionals() throws Throwable {
        for (String impl: List.of("expression", "statement1", "statement2")) {
            assertFunction3((x, y, z)->(0.0==x)?z:y, String.format("conditional_%s_double", impl), PrimitiveType.dfloat(), DOUBLES, DOUBLES.subList(0, 4), DOUBLES.subList(0, 4));
            assertFunction3((x, y, z)->(0.0f==x)?z:y, String.format("conditional_%s_float", impl), PrimitiveType.sfloat(), FLOATS, FLOATS.subList(0, 4), FLOATS.subList(0, 4));
            assertFunction3((x, y, z)->(0==x)?z:y, String.format("conditional_%s_int16", impl), PrimitiveType.sint16(), SINT16S, SINT16S.subList(0, 4), SINT16S.subList(0, 4));
            assertFunction3((x, y, z)->(0==x)?z:y, String.format("conditional_%s_int32", impl), PrimitiveType.sint32(), SINT32S, SINT32S.subList(0, 4), SINT32S.subList(0, 4));
            assertFunction3((x, y, z)->(0L==x)?z:y, String.format("conditional_%s_int64", impl), PrimitiveType.sint64(), SINT64S, SINT64S.subList(0, 4), SINT64S.subList(0, 4));
            assertFunction3((x, y, z)->(0==x)?z:y, String.format("conditional_%s_int8", impl), PrimitiveType.sint8(), SINT8S, SINT8S.subList(0, 4), SINT8S.subList(0, 4));
            assertFunction3((x, y, z)->(0L==x)?z:y, String.format("conditional_%s_ptr", impl), PrimitiveType.uint64(), UINT64S, UINT64S.subList(0, 4), UINT64S.subList(0, 4));
            assertFunction3((x, y, z)->(0==x)?z:y, String.format("conditional_%s_uint16", impl), PrimitiveType.uint16(), UINT16S, UINT16S.subList(0, 4), UINT16S.subList(0, 4));
            assertFunction3((x, y, z)->(0==x)?z:y, String.format("conditional_%s_uint32", impl), PrimitiveType.uint32(), UINT32S, UINT32S.subList(0, 4), UINT32S.subList(0, 4));
            assertFunction3((x, y, z)->(0L==x)?z:y, String.format("conditional_%s_uint64", impl), PrimitiveType.uint64(), UINT64S, UINT64S.subList(0, 4), UINT64S.subList(0, 4));
            assertFunction3((x, y, z)->(0==x)?z:y, String.format("conditional_%s_uint8", impl), PrimitiveType.uint8(), UINT8S, UINT8S.subList(0, 4), UINT8S.subList(0, 4));
        }
    }

    @Test
    public void testConstants() throws Throwable {
        testConstants(
                "const_double_", PrimitiveType.dfloat(),
                0.0, -0.0, 1.0, -1.0, Double.POSITIVE_INFINITY,
                Double.NEGATIVE_INFINITY, 333333.33333333333333333, 3.1415926535897932384626433);
        testConstants(
                "const_float_", PrimitiveType.sfloat(),
                0.0f, -0.0f, 1.0f, -1.0f, Float.POSITIVE_INFINITY,
                Float.NEGATIVE_INFINITY, 333333.33333333333333333f, 3.1415926535897932384626433f);
        testConstants(
                "const_int16_", PrimitiveType.sint16(),
                (short)0x0000, (short)0xffff, (short)0x0001, (short)0x8000);
        testConstants(
                "const_int32_", PrimitiveType.sint32(),
                0x00000000, 0xffffffff, 0x00000001, 0x80000000);
        testConstants(
                "const_int64_", PrimitiveType.sint64(),
                0x0000000000000000L, 0xffffffffffffffffL, 0x0000000000000001L, 0x8000000000000000L);
        testConstants(
                "const_int8_", PrimitiveType.sint8(),
                (byte)0x00, (byte)0xff, (byte)0x01, (byte)0x80);
        testConstants(
                "const_ptr_", PrimitiveType.uint64(),
                0L, 0x0000000000000000L, 0xffffffffffffffffL, 0x0000000000000001L, 0x8000000000000000L);
        testConstants(
                "const_uint16_", PrimitiveType.uint16(),
                (short)0x0000, (short)0xffff, (short)0x0001, (short)0x8000);
        testConstants(
                "const_uint32_", PrimitiveType.uint32(),
                0x00000000, 0xffffffff, 0x00000001, 0x80000000);
        testConstants(
                "const_uint64_", PrimitiveType.uint64(),
                0x0000000000000000L, 0xffffffffffffffffL, 0x0000000000000001L, 0x8000000000000000L);
        testConstants(
                "const_uint8_", PrimitiveType.uint8(),
                (byte)0x00, (byte)0xff, (byte)0x01, (byte)0x80);
    }

    @SafeVarargs
    private <J> void testConstants(
            @NotNull String functionName,
            @NotNull PrimitiveType<J> resultType,
            @NotNull J @NotNull ... values)
            throws Throwable {
        for (int ii=0; values.length>ii; ++ii) {
            assertEquals(
                    resultType.value(values[ii]),
                    callFunction(true, functionName+ii, resultType));
        }
    }

    @Test
    public void testDivide() throws Throwable {
        assertFunction2((x, y)->x/y, "divide_double", PrimitiveType.dfloat(), DOUBLES, DOUBLES);
        assertFunction2((x, y)->x/y, "divide_float", PrimitiveType.sfloat(), FLOATS, FLOATS);
        assertFunction2((x, y)->(short)((0==y)?-1:(x/y)), "divide_int16", PrimitiveType.sint16(), SINT16S, SINT16S);
        assertFunction2((x, y)->(0==y)?-1:(x/y), "divide_int32", PrimitiveType.sint32(), SINT32S, SINT32S);
        assertFunction2((x, y)->(0L==y)?-1L:(x/y), "divide_int64", PrimitiveType.sint64(), SINT64S, SINT64S);
        assertFunction2((x, y)->(byte)((0==y)?-1:(x/y)), "divide_int8", PrimitiveType.sint8(), SINT8S, SINT8S);
        assertFunction2((x, y)->(short)((0==y)?-1:((x&0xffff)/(y&0xffff))), "divide_uint16", PrimitiveType.uint16(), UINT16S, UINT16S);
        assertFunction2((x, y)->(0==y)?-1:Integer.divideUnsigned(x, y), "divide_uint32", PrimitiveType.uint32(), UINT32S, UINT32S);
        assertFunction2((x, y)->(0L==y)?-1L:Long.divideUnsigned(x, y), "divide_uint64", PrimitiveType.uint64(), UINT64S, UINT64S);
        assertFunction2((x, y)->(byte)((0==y)?-1:((x&0xff)/(y&0xff))), "divide_uint8", PrimitiveType.uint8(), UINT8S, UINT8S);
    }

    @Test
    public void testEqual() throws Throwable {
        assertFunction2((x, y)->(x.doubleValue()==y.doubleValue())?1.0:0.0, "equal_double", PrimitiveType.dfloat(), DOUBLES, DOUBLES);
        assertFunction2((x, y)->(x.floatValue()==y.floatValue())?1.0f:0.0f, "equal_float", PrimitiveType.sfloat(), FLOATS, FLOATS);
        assertFunction2((x, y)->(short)((x.shortValue()==y.shortValue())?1:0), "equal_int16", PrimitiveType.sint16(), SINT16S, SINT16S);
        assertFunction2((x, y)->(x.intValue()==y.intValue())?1:0, "equal_int32", PrimitiveType.sint32(), SINT32S, SINT32S);
        assertFunction2((x, y)->(x.longValue()==y.longValue())?1L:0L, "equal_int64", PrimitiveType.sint64(), SINT64S, SINT64S);
        assertFunction2((x, y)->(byte)((x.byteValue()==y.byteValue())?1:0), "equal_int8", PrimitiveType.sint8(), SINT8S, SINT8S);
        assertFunction2((x, y)->(x.longValue()==y.longValue())?1L:0L, "equal_ptr", PrimitiveType.uint64(), UINT64S, UINT64S);
        assertFunction2((x, y)->(short)((x.shortValue()==y.shortValue())?1:0), "equal_uint16", PrimitiveType.uint16(), UINT16S, UINT16S);
        assertFunction2((x, y)->(x.intValue()==y.intValue())?1:0, "equal_uint32", PrimitiveType.uint32(), UINT32S, UINT32S);
        assertFunction2((x, y)->(x.longValue()==y.longValue())?1L:0L, "equal_uint64", PrimitiveType.uint64(), UINT64S, UINT64S);
        assertFunction2((x, y)->(byte)((x.byteValue()==y.byteValue())?1:0), "equal_uint8", PrimitiveType.uint8(), UINT8S, UINT8S);
    }

    @Test
    public void testExit() throws Throwable {
        emulator.reset();
        emulator.hart.xRegisters.setInt32(emulator.heapAndStack, ABI.REGISTER_A0, 13);
        emulator.hart.setPc(IOMap.EXIT);
        emulator.run();
        assertEquals(13, emulator.exit.code());
    }

    @Test
    public void testExitExits() throws Throwable {
        emulator.reset();
        emulator.hart.setReturnAddress(emulator.heapAndStack, IOMap.EXIT_OK);
        emulator.hart.setPc(emulator.elfHeader().symbolTable().get("exit_forever").value());
        emulator.run();
        assertEquals(13, emulator.exit.code());
    }

    @Test
    public void testExitOk() throws Throwable {
        emulator.reset();
        emulator.hart.xRegisters.setInt32(emulator.heapAndStack, ABI.REGISTER_A0, 13);
        emulator.hart.setPc(IOMap.EXIT_OK);
        emulator.run();
        assertEquals(0, emulator.exit.code());
    }

    @Test
    public void testFactorial() throws Throwable {
        Function<Integer, Integer> factorial=(value)->{
            int result=1;
            for (; 0<value; --value) {
                result*=value;
            }
            return result;
        };
        for (int ii=0; 9>=ii; ++ii) {
            for (int value=0; 9>value; ++value) {
                assertEquals(
                        PrimitiveType.uint32().value(factorial.apply(value)),
                        callFunction(
                                true,
                                "factorial"+ii,
                                PrimitiveType.uint32(),
                                PrimitiveType.uint32().value(value)),
                        String.format("ii=%s, value=%s", ii, value));
            }
        }
    }

    @Test
    public void testFunctionPointerCall() throws Throwable {
        testFunctionPointerCall("add_int64", Long::sum);
        testFunctionPointerCall("multiply_int64", (x, y)->x*y);
    }

    private void testFunctionPointerCall(
            @NotNull String function,
            @NotNull BiFunction<@NotNull Long, @NotNull Long, @NotNull Long> operator)
            throws Throwable {
        assertFunction3(
                (x, y, z)->operator.apply(y, z),
                "function_pointer_call",
                PrimitiveType.uint64(),
                List.of(PrimitiveType.uint64().value(emulator.elfHeader().symbolTable().get(function).value())),
                UINT64S,
                UINT64S);
    }

    @Test
    public void testGreater() throws Throwable {
        assertFunction2((x, y)->(x>y)?1.0:0.0, "greater_double", PrimitiveType.dfloat(), DOUBLES, DOUBLES);
        assertFunction2((x, y)->(x>y)?1.0f:0.0f, "greater_float", PrimitiveType.sfloat(), FLOATS, FLOATS);
        assertFunction2((x, y)->(short)((x>y)?1:0), "greater_int16", PrimitiveType.sint16(), SINT16S, SINT16S);
        assertFunction2((x, y)->(x>y)?1:0, "greater_int32", PrimitiveType.sint32(), SINT32S, SINT32S);
        assertFunction2((x, y)->(x>y)?1L:0L, "greater_int64", PrimitiveType.sint64(), SINT64S, SINT64S);
        assertFunction2((x, y)->(byte)((x>y)?1:0), "greater_int8", PrimitiveType.sint8(), SINT8S, SINT8S);
        assertFunction2((x, y)->(0<Long.compareUnsigned(x, y))?1L:0L, "greater_ptr", PrimitiveType.uint64(), UINT64S, UINT64S);
        assertFunction2((x, y)->(short)((0<Short.compareUnsigned(x, y))?1:0), "greater_uint16", PrimitiveType.uint16(), UINT16S, UINT16S);
        assertFunction2((x, y)->(0<Integer.compareUnsigned(x, y))?1:0, "greater_uint32", PrimitiveType.uint32(), UINT32S, UINT32S);
        assertFunction2((x, y)->(0<Long.compareUnsigned(x, y))?1L:0L, "greater_uint64", PrimitiveType.uint64(), UINT64S, UINT64S);
        assertFunction2((x, y)->(byte)((0<Byte.compareUnsigned(x, y))?1:0), "greater_uint8", PrimitiveType.uint8(), UINT8S, UINT8S);
    }

    @Test
    public void testGreaterOrEqual() throws Throwable {
        assertFunction2((x, y)->(x>=y)?1.0:0.0, "greater_or_equal_double", PrimitiveType.dfloat(), DOUBLES, DOUBLES);
        assertFunction2((x, y)->(x>=y)?1.0f:0.0f, "greater_or_equal_float", PrimitiveType.sfloat(), FLOATS, FLOATS);
        assertFunction2((x, y)->(short)((x>=y)?1:0), "greater_or_equal_int16", PrimitiveType.sint16(), SINT16S, SINT16S);
        assertFunction2((x, y)->(x>=y)?1:0, "greater_or_equal_int32", PrimitiveType.sint32(), SINT32S, SINT32S);
        assertFunction2((x, y)->(x>=y)?1L:0L, "greater_or_equal_int64", PrimitiveType.sint64(), SINT64S, SINT64S);
        assertFunction2((x, y)->(byte)((x>=y)?1:0), "greater_or_equal_int8", PrimitiveType.sint8(), SINT8S, SINT8S);
        assertFunction2((x, y)->(0<=Long.compareUnsigned(x, y))?1L:0L, "greater_or_equal_ptr", PrimitiveType.uint64(), UINT64S, UINT64S);
        assertFunction2((x, y)->(short)((0<=Short.compareUnsigned(x, y))?1:0), "greater_or_equal_uint16", PrimitiveType.uint16(), UINT16S, UINT16S);
        assertFunction2((x, y)->(0<=Integer.compareUnsigned(x, y))?1:0, "greater_or_equal_uint32", PrimitiveType.uint32(), UINT32S, UINT32S);
        assertFunction2((x, y)->(0<=Long.compareUnsigned(x, y))?1L:0L, "greater_or_equal_uint64", PrimitiveType.uint64(), UINT64S, UINT64S);
        assertFunction2((x, y)->(byte)((0<=Byte.compareUnsigned(x, y))?1:0), "greater_or_equal_uint8", PrimitiveType.uint8(), UINT8S, UINT8S);
    }

    @Test
    public void testLess() throws Throwable {
        assertFunction2((x, y)->(x<y)?1.0:0.0, "less_double", PrimitiveType.dfloat(), DOUBLES, DOUBLES);
        assertFunction2((x, y)->(x<y)?1.0f:0.0f, "less_float", PrimitiveType.sfloat(), FLOATS, FLOATS);
        assertFunction2((x, y)->(short)((x<y)?1:0), "less_int16", PrimitiveType.sint16(), SINT16S, SINT16S);
        assertFunction2((x, y)->(x<y)?1:0, "less_int32", PrimitiveType.sint32(), SINT32S, SINT32S);
        assertFunction2((x, y)->(x<y)?1L:0L, "less_int64", PrimitiveType.sint64(), SINT64S, SINT64S);
        assertFunction2((x, y)->(byte)((x<y)?1:0), "less_int8", PrimitiveType.sint8(), SINT8S, SINT8S);
        assertFunction2((x, y)->(0>Long.compareUnsigned(x, y))?1L:0L, "less_ptr", PrimitiveType.uint64(), UINT64S, UINT64S);
        assertFunction2((x, y)->(short)((0>Short.compareUnsigned(x, y))?1:0), "less_uint16", PrimitiveType.uint16(), UINT16S, UINT16S);
        assertFunction2((x, y)->(0>Integer.compareUnsigned(x, y))?1:0, "less_uint32", PrimitiveType.uint32(), UINT32S, UINT32S);
        assertFunction2((x, y)->(0>Long.compareUnsigned(x, y))?1L:0L, "less_uint64", PrimitiveType.uint64(), UINT64S, UINT64S);
        assertFunction2((x, y)->(byte)((0>Byte.compareUnsigned(x, y))?1:0), "less_uint8", PrimitiveType.uint8(), UINT8S, UINT8S);
    }

    @Test
    public void testLessOrEqual() throws Throwable {
        assertFunction2((x, y)->(x<=y)?1.0:0.0, "less_or_equal_double", PrimitiveType.dfloat(), DOUBLES, DOUBLES);
        assertFunction2((x, y)->(x<=y)?1.0f:0.0f, "less_or_equal_float", PrimitiveType.sfloat(), FLOATS, FLOATS);
        assertFunction2((x, y)->(short)((x<=y)?1:0), "less_or_equal_int16", PrimitiveType.sint16(), SINT16S, SINT16S);
        assertFunction2((x, y)->(x<=y)?1:0, "less_or_equal_int32", PrimitiveType.sint32(), SINT32S, SINT32S);
        assertFunction2((x, y)->(x<=y)?1L:0L, "less_or_equal_int64", PrimitiveType.sint64(), SINT64S, SINT64S);
        assertFunction2((x, y)->(byte)((x<=y)?1:0), "less_or_equal_int8", PrimitiveType.sint8(), SINT8S, SINT8S);
        assertFunction2((x, y)->(0>=Long.compareUnsigned(x, y))?1L:0L, "less_or_equal_ptr", PrimitiveType.uint64(), UINT64S, UINT64S);
        assertFunction2((x, y)->(short)((0>=Short.compareUnsigned(x, y))?1:0), "less_or_equal_uint16", PrimitiveType.uint16(), UINT16S, UINT16S);
        assertFunction2((x, y)->(0>=Integer.compareUnsigned(x, y))?1:0, "less_or_equal_uint32", PrimitiveType.uint32(), UINT32S, UINT32S);
        assertFunction2((x, y)->(0>=Long.compareUnsigned(x, y))?1L:0L, "less_or_equal_uint64", PrimitiveType.uint64(), UINT64S, UINT64S);
        assertFunction2((x, y)->(byte)((0>=Byte.compareUnsigned(x, y))?1:0), "less_or_equal_uint8", PrimitiveType.uint8(), UINT8S, UINT8S);
    }

    @Test
    public void testMallocAlignment() throws Throwable {
        BiConsumer<Long, Long> check=(address, size)->{
            assertNotEquals(0L, address.longValue());
            assertEquals(0L, address%Long.highestOneBit(Math.min(8, size)));
        };
        for (long size0=32L; 0L<size0; --size0) {
            emulator.reset();
            long address0=emulator.heapAndStack.malloc(emulator.hart, size0);
            check.accept(address0, size0);
            for (long size1=32L; 0L<size0; --size0) {
                long address1=emulator.heapAndStack.malloc(emulator.hart, size1);
                check.accept(address1, size1);
                assertTrue(address0+size0<=address1);
                long address2=emulator.heapAndStack.malloc(emulator.hart, size0);
                check.accept(address2, size0);
                assertTrue(address1+size1<=address2);
            }
        }
    }

    @Test
    public void testMallocFailure() throws Throwable {
        emulator.reset();
        assertEquals(0L, emulator.heapAndStack.malloc(emulator.hart, 0L));
        assertEquals(0L, emulator.heapAndStack.malloc(emulator.hart, -1L));
        assertEquals(0L, emulator.heapAndStack.malloc(emulator.hart, Long.MAX_VALUE));
    }

    @Test
    public void testMallocFreeDoesNothing() throws Throwable {
        emulator.reset();
        long address0=emulator.heapAndStack.malloc(emulator.hart, 1L);
        assertNotEquals(0L, address0);
        emulator.heapAndStack.free(address0);
        long address1=emulator.heapAndStack.malloc(emulator.hart, 1L);
        assertNotEquals(0L, address1);
        assertEquals(address0+8L, address1);
    }

    @Test
    public void testMallocHasMemory() throws Throwable {
        emulator.reset();
        assertTrue((1L<<16)<emulator.heapAndStack.getStackPointer(emulator.hart)-emulator.heapStart);
    }

    @Test
    public void testMallocOutOfMemory() throws Throwable {
        emulator.reset();
        long address0=0L;
        long size0=0L;
        for (long size1=Long.highestOneBit(emulator.heapAndStack.getStackPointer(emulator.hart)-emulator.heapStart);
             0L<size1; ) {
            long address1=emulator.heapAndStack.malloc(emulator.hart, size1);
            if (0L==address1) {
                size1/=2L;
            }
            else {
                assertTrue(address0+size0<=address1);
                address0=address1;
                size0=size1;
            }
        }
        assertEquals(0L, emulator.heapAndStack.malloc(emulator.hart, 1L));
        try {
            emulator.heapAndStack.setStackPointer(
                    emulator.hart,
                    emulator.heapAndStack.getStackPointer(emulator.hart)-16L);
            fail();
        }
        catch (StackOverflowException ignore) {
        }
    }

    @Test
    public void testMemoryAccessLog() throws Throwable {
        Deque<Long> filtered=new ArrayDeque<>();
        long p0;
        long p1;
        long p2;
        long p3;
        Path logPath=Files.createTempFile("memory-access-", ".log");
        try {
            emulator=emulator(
                    null,
                    LogOutputStream.factory(logPath),
                    null);
            p0=emulator.heapAndStack.malloc(emulator.hart, 2);
            assertNotEquals(0L, p0);
            p1=emulator.heapAndStack.malloc(emulator.hart, 4);
            assertNotEquals(0L, p1);
            p2=emulator.heapAndStack.malloc(emulator.hart, 8);
            assertNotEquals(0L, p2);
            p3=emulator.heapAndStack.malloc(emulator.hart, 1);
            assertNotEquals(0L, p3);
            emulator.memoryLog.accessLogDisabled();
            emulator.memoryLog.storeInt16(p0, (short)0);
            emulator.memoryLog.storeInt32(p1, 0);
            emulator.memoryLog.storeInt64(p2, 0L);
            emulator.memoryLog.storeInt8(p3, (byte)0);
            emulator.memoryLog.accessLogEnabled();
            callFunction(
                    false,
                    "memory_access",
                    PrimitiveType.voidType(),
                    PrimitiveType.uint64().value(p0),
                    PrimitiveType.uint64().value(p1),
                    PrimitiveType.uint64().value(p2),
                    PrimitiveType.uint64().value(p3));
            emulator.memoryLog.accessLogDisabled();
            assertEquals((short)28, emulator.memoryLog.loadInt16(p0));
            assertEquals(28, emulator.memoryLog.loadInt32(p1, false));
            assertEquals(28L, emulator.memoryLog.loadInt64(p2));
            assertEquals((byte)28, emulator.memoryLog.loadInt8(p3));
            emulator.close();
            try (LogInputStream logStream=LogInputStream.factory(logPath).get()) {
                assertEquals(
                        Logs.encodeAccessLogDisabled(),
                        logStream.readNext());
                assertEquals(
                        Logs.encodeAccessLogDisabled(),
                        logStream.readNext());
                assertEquals(
                        Logs.encodeAccessLogEnabled(),
                        logStream.readNext());
                assertEquals(
                        Logs.encodeAccess(
                                emulator.elfHeader().symbolTable().get("memory_access").value(),
                                4,
                                AccessType.LOAD_INSTRUCTION),
                        logStream.readNext());
                while (logStream.hasNext()) {
                    long log=logStream.readNext();
                    switch (Logs.decodeType(log)) {
                        case ACCESS_LOAD_DATA, ACCESS_LOAD_INSTRUCTION, ACCESS_STORE -> {
                            long address=Logs.decodeAddress(log);
                            if ((address==p0)
                                    || (address==p1)
                                    || (address==p2)
                                    || (address==p3)) {
                                filtered.add(log);
                            }
                        }
                        case ACCESS_LOG_DISABLED, ACCESS_LOG_ENABLED, ELAPSED_CYCLES -> {
                        }
                        case USER_DATA -> filtered.add(log);
                        default -> throw new IllegalStateException("unexpected type");
                    }
                }
                try {
                    logStream.readNext();
                    fail();
                }
                catch (EOFException ignore) {
                }
            }
        }
        finally {
            Files.deleteIfExists(logPath);
        }
        assertEquals(2*(1+7*4*2), filtered.size());
        for (int ii=1; 4>ii; ii+=2) {
            assertEquals(
                    Logs.encodeUserData(ii),
                    filtered.removeFirst().longValue());
            for (int jj=7; 0<jj; --jj) {
                assertEquals(
                        Logs.encodeAccess(p0, 2, AccessType.LOAD_DATA),
                        filtered.removeFirst().longValue());
                assertEquals(
                        Logs.encodeAccess(p0, 2, AccessType.STORE),
                        filtered.removeFirst().longValue());
                assertEquals(
                        Logs.encodeAccess(p1, 4, AccessType.LOAD_DATA),
                        filtered.removeFirst().longValue());
                assertEquals(
                        Logs.encodeAccess(p1, 4, AccessType.STORE),
                        filtered.removeFirst().longValue());
                assertEquals(
                        Logs.encodeAccess(p2, 8, AccessType.LOAD_DATA),
                        filtered.removeFirst().longValue());
                assertEquals(
                        Logs.encodeAccess(p2, 8, AccessType.STORE),
                        filtered.removeFirst().longValue());
                assertEquals(
                        Logs.encodeAccess(p3, 1, AccessType.LOAD_DATA),
                        filtered.removeFirst().longValue());
                assertEquals(
                        Logs.encodeAccess(p3, 1, AccessType.STORE),
                        filtered.removeFirst().longValue());
            }
        }
        assertTrue(filtered.isEmpty());
    }

    @Test
    public void testMultiply() throws Throwable {
        assertFunction2((x, y)->x*y, "multiply_double", PrimitiveType.dfloat(), DOUBLES, DOUBLES);
        assertFunction2((x, y)->x*y, "multiply_float", PrimitiveType.sfloat(), FLOATS, FLOATS);
        assertFunction2((x, y)->(short)(x*y), "multiply_int16", PrimitiveType.sint16(), SINT16S, SINT16S);
        assertFunction2((x, y)->x*y, "multiply_int32", PrimitiveType.sint32(), SINT32S, SINT32S);
        assertFunction2((x, y)->x*y, "multiply_int64", PrimitiveType.sint64(), SINT64S, SINT64S);
        assertFunction2((x, y)->(byte)(x*y), "multiply_int8", PrimitiveType.sint8(), SINT8S, SINT8S);
        assertFunction2((x, y)->(short)(x*y), "multiply_uint16", PrimitiveType.uint16(), UINT16S, UINT16S);
        assertFunction2((x, y)->x*y, "multiply_uint32", PrimitiveType.uint32(), UINT32S, UINT32S);
        assertFunction2((x, y)->x*y, "multiply_uint64", PrimitiveType.uint64(), UINT64S, UINT64S);
        assertFunction2((x, y)->(byte)(x*y), "multiply_uint8", PrimitiveType.uint8(), UINT8S, UINT8S);
    }

    @Test
    public void testNegate() throws Throwable {
        assertFunction1((x)->-x, "negate_double", PrimitiveType.dfloat(), DOUBLES);
        assertFunction1((x)->-x, "negate_float", PrimitiveType.sfloat(), FLOATS);
        assertFunction1((x)->(short)(-x), "negate_int16", PrimitiveType.sint16(), SINT16S);
        assertFunction1((x)->-x, "negate_int32", PrimitiveType.sint32(), SINT32S);
        assertFunction1((x)->-x, "negate_int64", PrimitiveType.sint64(), SINT64S);
        assertFunction1((x)->(byte)(-x), "negate_int8", PrimitiveType.sint8(), SINT8S);
        assertFunction1((x)->(short)(-x), "negate_uint16", PrimitiveType.uint16(), UINT16S);
        assertFunction1((x)->-x, "negate_uint32", PrimitiveType.uint32(), UINT32S);
        assertFunction1((x)->-x, "negate_uint64", PrimitiveType.uint64(), UINT64S);
        assertFunction1((x)->(byte)(-x), "negate_uint8", PrimitiveType.uint8(), UINT8S);
    }

    @Test
    public void testNotBitwise() throws Throwable {
        assertFunction1((x)->(short)(~x), "not_bitwise_int16", PrimitiveType.sint16(), SINT16S);
        assertFunction1((x)->~x, "not_bitwise_int32", PrimitiveType.sint32(), SINT32S);
        assertFunction1((x)->~x, "not_bitwise_int64", PrimitiveType.sint64(), SINT64S);
        assertFunction1((x)->(byte)(~x), "not_bitwise_int8", PrimitiveType.sint8(), SINT8S);
        assertFunction1((x)->(short)(~x), "not_bitwise_uint16", PrimitiveType.uint16(), UINT16S);
        assertFunction1((x)->~x, "not_bitwise_uint32", PrimitiveType.uint32(), UINT32S);
        assertFunction1((x)->~x, "not_bitwise_uint64", PrimitiveType.uint64(), UINT64S);
        assertFunction1((x)->(byte)(~x), "not_bitwise_uint8", PrimitiveType.uint8(), UINT8S);
    }

    @Test
    public void testNotLogical() throws Throwable {
        assertFunction1((x)->(0.0==x)?1.0:0.0, "not_logical_double", PrimitiveType.dfloat(), DOUBLES);
        assertFunction1((x)->(0.0f==x)?1.0f:0.0f, "not_logical_float", PrimitiveType.sfloat(), FLOATS);
        assertFunction1((x)->(short)((0==x)?1:0), "not_logical_int16", PrimitiveType.sint16(), SINT16S);
        assertFunction1((x)->(0==x)?1:0, "not_logical_int32", PrimitiveType.sint32(), SINT32S);
        assertFunction1((x)->(0L==x)?1L:0L, "not_logical_int64", PrimitiveType.sint64(), SINT64S);
        assertFunction1((x)->(byte)((0==x)?1:0), "not_logical_int8", PrimitiveType.sint8(), SINT8S);
        assertFunction1((x)->(0L==x)?1L:0L, "not_logical_ptr", PrimitiveType.uint64(), UINT64S);
        assertFunction1((x)->(short)((0==x)?1:0), "not_logical_uint16", PrimitiveType.uint16(), UINT16S);
        assertFunction1((x)->(0==x)?1:0, "not_logical_uint32", PrimitiveType.uint32(), UINT32S);
        assertFunction1((x)->(0L==x)?1L:0L, "not_logical_uint64", PrimitiveType.uint64(), UINT64S);
        assertFunction1((x)->(byte)((0==x)?1:0), "not_logical_uint8", PrimitiveType.uint8(), UINT8S);
    }

    @Test
    public void testNotEqual() throws Throwable {
        assertFunction2((x, y)->(x.doubleValue()!=y.doubleValue())?1.0:0.0, "not_equal_double", PrimitiveType.dfloat(), DOUBLES, DOUBLES);
        assertFunction2((x, y)->(x.floatValue()!=y.floatValue())?1.0f:0.0f, "not_equal_float", PrimitiveType.sfloat(), FLOATS, FLOATS);
        assertFunction2((x, y)->(short)((x.shortValue()!=y.shortValue())?1:0), "not_equal_int16", PrimitiveType.sint16(), SINT16S, SINT16S);
        assertFunction2((x, y)->(x.intValue()!=y.intValue())?1:0, "not_equal_int32", PrimitiveType.sint32(), SINT32S, SINT32S);
        assertFunction2((x, y)->(x.longValue()!=y.longValue())?1L:0L, "not_equal_int64", PrimitiveType.sint64(), SINT64S, SINT64S);
        assertFunction2((x, y)->(byte)((x.byteValue()!=y.byteValue())?1:0), "not_equal_int8", PrimitiveType.sint8(), SINT8S, SINT8S);
        assertFunction2((x, y)->(x.longValue()!=y.longValue())?1L:0L, "not_equal_ptr", PrimitiveType.uint64(), UINT64S, UINT64S);
        assertFunction2((x, y)->(short)((x.shortValue()!=y.shortValue())?1:0), "not_equal_uint16", PrimitiveType.uint16(), UINT16S, UINT16S);
        assertFunction2((x, y)->(x.intValue()!=y.intValue())?1:0, "not_equal_uint32", PrimitiveType.uint32(), UINT32S, UINT32S);
        assertFunction2((x, y)->(x.longValue()!=y.longValue())?1L:0L, "not_equal_uint64", PrimitiveType.uint64(), UINT64S, UINT64S);
        assertFunction2((x, y)->(byte)((x.byteValue()!=y.byteValue())?1:0), "not_equal_uint8", PrimitiveType.uint8(), UINT8S, UINT8S);
    }

    @Test
    public void testNotNotLogical() throws Throwable {
        assertFunction1((x)->(0.0==x)?0.0:1.0, "not_not_logical_double", PrimitiveType.dfloat(), DOUBLES);
        assertFunction1((x)->(0.0f==x)?0.0f:1.0f, "not_not_logical_float", PrimitiveType.sfloat(), FLOATS);
        assertFunction1((x)->(short)((0==x)?0:1), "not_not_logical_int16", PrimitiveType.sint16(), SINT16S);
        assertFunction1((x)->(0==x)?0:1, "not_not_logical_int32", PrimitiveType.sint32(), SINT32S);
        assertFunction1((x)->(0L==x)?0L:1L, "not_not_logical_int64", PrimitiveType.sint64(), SINT64S);
        assertFunction1((x)->(byte)((0==x)?0:1), "not_not_logical_int8", PrimitiveType.sint8(), SINT8S);
        assertFunction1((x)->(0L==x)?0L:1L, "not_not_logical_ptr", PrimitiveType.uint64(), UINT64S);
        assertFunction1((x)->(short)((0==x)?0:1), "not_not_logical_uint16", PrimitiveType.uint16(), UINT16S);
        assertFunction1((x)->(0==x)?0:1, "not_not_logical_uint32", PrimitiveType.uint32(), UINT32S);
        assertFunction1((x)->(0L==x)?0L:1L, "not_not_logical_uint64", PrimitiveType.uint64(), UINT64S);
        assertFunction1((x)->(byte)((0==x)?0:1), "not_not_logical_uint8", PrimitiveType.uint8(), UINT8S);
    }


    @Test
    public void testOrBitwise() throws Throwable {
        assertFunction2((x, y)->(short)(x|y), "or_bitwise_int16", PrimitiveType.sint16(), SINT16S, SINT16S);
        assertFunction2((x, y)->x|y, "or_bitwise_int32", PrimitiveType.sint32(), SINT32S, SINT32S);
        assertFunction2((x, y)->x|y, "or_bitwise_int64", PrimitiveType.sint64(), SINT64S, SINT64S);
        assertFunction2((x, y)->(byte)(x|y), "or_bitwise_int8", PrimitiveType.sint8(), SINT8S, SINT8S);
        assertFunction2((x, y)->(short)(x|y), "or_bitwise_uint16", PrimitiveType.uint16(), UINT16S, UINT16S);
        assertFunction2((x, y)->x|y, "or_bitwise_uint32", PrimitiveType.uint32(), UINT32S, UINT32S);
        assertFunction2((x, y)->x|y, "or_bitwise_uint64", PrimitiveType.uint64(), UINT64S, UINT64S);
        assertFunction2((x, y)->(byte)(x|y), "or_bitwise_uint8", PrimitiveType.uint8(), UINT8S, UINT8S);
    }

    @Test
    public void testOrLogical() throws Throwable {
        assertFunction2((x, y)->((0.0!=x) || (0.0!=y))?1.0:0.0, "or_logical_double", PrimitiveType.dfloat(), DOUBLES, DOUBLES);
        assertFunction2((x, y)->((0.0f!=x) || (0.0f!=y))?1.0f:0.0f, "or_logical_float", PrimitiveType.sfloat(), FLOATS, FLOATS);
        assertFunction2((x, y)->(short)(((0!=x) || (0!=y))?1:0), "or_logical_int16", PrimitiveType.sint16(), SINT16S, SINT16S);
        assertFunction2((x, y)->((0!=x) || (0!=y))?1:0, "or_logical_int32", PrimitiveType.sint32(), SINT32S, SINT32S);
        assertFunction2((x, y)->((0L!=x) || (0L!=y))?1L:0L, "or_logical_int64", PrimitiveType.sint64(), SINT64S, SINT64S);
        assertFunction2((x, y)->(byte)(((0!=x) || (0!=y))?1:0), "or_logical_int8", PrimitiveType.sint8(), SINT8S, SINT8S);
        assertFunction2((x, y)->((0L!=x) || (0L!=y))?1L:0L, "or_logical_ptr", PrimitiveType.uint64(), UINT64S, UINT64S);
        assertFunction2((x, y)->(short)(((0!=x) || (0!=y))?1:0), "or_logical_uint16", PrimitiveType.uint16(), UINT16S, UINT16S);
        assertFunction2((x, y)->((0!=x) || (0!=y))?1:0, "or_logical_uint32", PrimitiveType.uint32(), UINT32S, UINT32S);
        assertFunction2((x, y)->((0L!=x) || (0L!=y))?1L:0L, "or_logical_uint64", PrimitiveType.uint64(), UINT64S, UINT64S);
        assertFunction2((x, y)->(byte)(((0!=x) || (0!=y))?1:0), "or_logical_uint8", PrimitiveType.uint8(), UINT8S, UINT8S);
    }

    @Test
    public void testParametersAbi() throws Throwable {
        double[] coefficients=new double[20];
        BiFunction<Integer, Double, Double> apply1=(coefficientsSize, xx)->{
            double x2=1.0;
            double result=0.0;
            for (int ii=0; coefficientsSize>ii; ++ii) {
                result+=coefficients[ii]*x2;
                x2*=xx;
            }
            return result;
        };
        // noinspection ExtractMethodRecommender
        BiFunction<Integer, Double, Double> apply2=(coefficientsSize, xx)->{
            List<PrimitiveValue<?>> parameters=new ArrayList<>(coefficientsSize+1);
            for (int ii=0; coefficientsSize>ii; ++ii) {
                double coefficient=coefficients[ii];
                switch (ii%4) {
                    case 0 -> parameters.add(PrimitiveType.dfloat().value(coefficient));
                    case 1 -> parameters.add(PrimitiveType.uint64().value((long)coefficient));
                    case 2 -> parameters.add(PrimitiveType.sfloat().value((float)coefficient));
                    default -> parameters.add(PrimitiveType.uint32().value((int)coefficient));
                }
            }
            parameters.add(PrimitiveType.dfloat().value(xx));
            return callFunction(true, "parameters_abi"+coefficientsSize, PrimitiveType.dfloat(), parameters)
                    .value();
        };
        Function<Double, Double> fix=(value)->(-0.0==value)?0.0:value;
        BiConsumer<Integer, Double> test2=(coefficientsSize, xx)->
                assertEquals(fix.apply(apply1.apply(coefficientsSize, xx)), fix.apply(apply2.apply(coefficientsSize, xx)));
        Consumer<Integer> test1=(coefficientsSize)->{
            test2.accept(coefficientsSize, -1.0);
            test2.accept(coefficientsSize, 1.0);
        };
        Runnable test0=()->{
            test1.accept(8);
            test1.accept(19);
            test1.accept(20);
        };
        test0.run();
        for (int i0=0; coefficients.length>i0; ++i0) {
            coefficients[i0]=1.0;
            test0.run();
            for (int i1=i0+1; coefficients.length>i1; ++i1) {
                coefficients[i1]=1.0;
                test0.run();
                for (int i2=i1+1; coefficients.length>i2; ++i2) {
                    coefficients[i2]=1.0;
                    test0.run();
                    coefficients[i2]=0.0;
                }
                coefficients[i1]=0.0;
            }
            coefficients[i0]=0.0;
        }
    }

    @Test
    public void testParametersAbiAllTypes() throws Throwable {
        callFunction(
                true,
                "parameters_abi_all_types",
                PrimitiveType.voidType(),
                PrimitiveType.uint64().value(1L),
                PrimitiveType.uint64().value(2L),
                PrimitiveType.uint64().value(3L),
                PrimitiveType.uint64().value(4L),
                PrimitiveType.uint64().value(5L),
                PrimitiveType.uint64().value(6L),
                PrimitiveType.uint64().value(7L),
                PrimitiveType.uint64().value(8L),
                PrimitiveType.dfloat().value(9.0),
                PrimitiveType.dfloat().value(10.0),
                PrimitiveType.dfloat().value(11.0),
                PrimitiveType.dfloat().value(12.0),
                PrimitiveType.dfloat().value(13.0),
                PrimitiveType.dfloat().value(14.0),
                PrimitiveType.dfloat().value(15.0),
                PrimitiveType.dfloat().value(16.0),
                PrimitiveType.dfloat().value(17.0),
                PrimitiveType.sfloat().value(18.0f),
                PrimitiveType.sint16().value((short)19),
                PrimitiveType.sint32().value(20),
                PrimitiveType.sint64().value(21L),
                PrimitiveType.sint8().value((byte)22),
                PrimitiveType.uint16().value((short)23),
                PrimitiveType.uint32().value(24),
                PrimitiveType.uint64().value(25L),
                PrimitiveType.uint8().value((byte)26));
    }

    @Test
    public void testReadInputWriteOutput() throws Throwable {
        final int IOSIZE=256;
        Deque<Number> inputValues=new ArrayDeque<>();
        Deque<Number> outputValues=new ArrayDeque<>();
        Random random=random();
        for (int ii=IOSIZE; 0<ii; --ii) {
            inputValues.addLast(random.nextDouble());
            inputValues.addLast(random.nextFloat());
            for (int jj=2; 0<jj; --jj) {
                inputValues.addLast((short)random.nextInt());
                inputValues.addLast(random.nextInt());
                inputValues.addLast(random.nextLong());
                inputValues.addLast((byte)random.nextInt());
            }
        }
        Input input=new Input() {
            private <T> T read(int remainder, Class<T> type) {
                assertFalse(inputValues.isEmpty());
                assertEquals(10*IOSIZE, inputValues.size()+outputValues.size());
                assertEquals(remainder, outputValues.size()%10);
                Number value=inputValues.removeFirst();
                outputValues.addLast(value);
                return type.cast(value);
            }

            @Override
            public double readDouble() {
                return read(0, Double.class);
            }

            @Override
            public float readFloat() {
                return read(1, Float.class);
            }

            @Override
            public short readInt16() {
                return read(2, Short.class);
            }

            @Override
            public int readInt32() {
                return read(3, Integer.class);
            }

            @Override
            public long readInt64() {
                return read(4, Long.class);
            }

            @Override
            public byte readInt8() {
                return read(5, Byte.class);
            }

            @Override
            public short readUint16() {
                return read(6, Short.class);
            }

            @Override
            public int readUint32() {
                return read(7, Integer.class);
            }

            @Override
            public long readUint64() {
                return read(8, Long.class);
            }

            @Override
            public byte readUint8() {
                return read(9, Byte.class);
            }
        };
        Output output=new Output() {
            private void write(int remainder, Number value) {
                assertTrue(inputValues.isEmpty());
                assertFalse(outputValues.isEmpty());
                assertEquals(outputValues.removeLast(), value);
                assertEquals(remainder, outputValues.size()%10);
            }

            @Override
            public void writeDouble(double value) {
                write(0, value);
            }

            @Override
            public void writeFloat(float value) {
                write(1, value);
            }

            @Override
            public void writeInt16(short value) {
                write(2, value);
            }

            @Override
            public void writeInt32(int value) {
                write(3, value);
            }

            @Override
            public void writeInt64(long value) {
                write(4, value);
            }

            @Override
            public void writeInt8(byte value) {
                write(5, value);
            }

            @Override
            public void writeUint16(short value) {
                write(6, value);
            }

            @Override
            public void writeUint32(int value) {
                write(7, value);
            }

            @Override
            public void writeUint64(long value) {
                write(8, value);
            }

            @Override
            public void writeUint8(byte value) {
                write(9, value);
            }
        };
        emulator=emulator(input, Log::noOp, output);
        callFunction(true, "read_input_write_output", PrimitiveType.voidType());
        assertTrue(inputValues.isEmpty());
        assertTrue(outputValues.isEmpty());
    }

    @Test
    public void testRemainder() throws Throwable {
        assertFunction2((x, y)->(short)((0==y)?x:(x%y)), "remainder_int16", PrimitiveType.sint16(), SINT16S, SINT16S);
        assertFunction2((x, y)->(0==y)?x:(x%y), "remainder_int32", PrimitiveType.sint32(), SINT32S, SINT32S);
        assertFunction2((x, y)->(0L==y)?x:(x%y), "remainder_int64", PrimitiveType.sint64(), SINT64S, SINT64S);
        assertFunction2((x, y)->(byte)((0==y)?x:(x%y)), "remainder_int8", PrimitiveType.sint8(), SINT8S, SINT8S);
        assertFunction2((x, y)->(short)((0==y)?x:((x&0xffff)%(y&0xffff))), "remainder_uint16", PrimitiveType.uint16(), UINT16S, UINT16S);
        assertFunction2((x, y)->(0==y)?x:Integer.remainderUnsigned(x, y), "remainder_uint32", PrimitiveType.uint32(), UINT32S, UINT32S);
        assertFunction2((x, y)->(0L==y)?x:Long.remainderUnsigned(x, y), "remainder_uint64", PrimitiveType.uint64(), UINT64S, UINT64S);
        assertFunction2((x, y)->(byte)((0==y)?x:((x&0xff)%(y&0xff))), "remainder_uint8", PrimitiveType.uint8(), UINT8S, UINT8S);
    }

    @Test
    public void testShiftLeft() throws Throwable {
        assertFunction2((x, y)->(short)(((0<=y) && (16>y))?x<<y:0), "shift_left_int16", PrimitiveType.sint16(), SINT16S, int32s(-1, 18, PrimitiveType.sint32()));
        assertFunction2((x, y)->x<<(y&0x1f), "shift_left_int32", PrimitiveType.sint32(), SINT32S, int32s(-1, 34, PrimitiveType.sint32()));
        assertFunction2((x, y)->x<<(y&0x3f), "shift_left_int64", PrimitiveType.sint64(), SINT64S, int32s(-1, 66, PrimitiveType.sint32()));
        assertFunction2((x, y)->(byte)(((0<=y) && (8>y))?x<<y:0), "shift_left_int8", PrimitiveType.sint8(), SINT8S, int32s(-1, 10, PrimitiveType.sint32()));
        assertFunction2((x, y)->(short)(((0<=y) && (16>y))?x<<y:0), "shift_left_uint16", PrimitiveType.uint16(), UINT16S, int32s(-1, 18, PrimitiveType.uint32()));
        assertFunction2((x, y)->x<<(y&0x1f), "shift_left_uint32", PrimitiveType.uint32(), UINT32S, int32s(-1, 34, PrimitiveType.uint32()));
        assertFunction2((x, y)->x<<(y&0x3f), "shift_left_uint64", PrimitiveType.uint64(), UINT64S, int32s(-1, 66, PrimitiveType.uint32()));
        assertFunction2((x, y)->(byte)(((0<=y) && (8>y))?x<<y:0), "shift_left_uint8", PrimitiveType.uint8(), UINT8S, int32s(-1, 10, PrimitiveType.uint32()));
    }

    @Test
    public void testShiftRight() throws Throwable {
        assertFunction2((x, y)->(short)(((0<=y) && (16>y))?x>>y:((0>x)?-1:0)), "shift_right_int16", PrimitiveType.sint16(), SINT16S, int32s(-1, 18, PrimitiveType.sint32()));
        assertFunction2((x, y)->x>>(y&0x1f), "shift_right_int32", PrimitiveType.sint32(), SINT32S, int32s(-1, 34, PrimitiveType.sint32()));
        assertFunction2((x, y)->x>>(y&0x3f), "shift_right_int64", PrimitiveType.sint64(), SINT64S, int32s(-1, 66, PrimitiveType.sint32()));
        assertFunction2((x, y)->(byte)(((0<=y) && (8>y))?x>>y:((0>x)?-1:0)), "shift_right_int8", PrimitiveType.sint8(), SINT8S, int32s(-1, 10, PrimitiveType.sint32()));
        assertFunction2((x, y)->(short)((x&0xffff) >>> (y&0x1f)), "shift_right_uint16", PrimitiveType.uint16(), UINT16S, int32s(-1, 18, PrimitiveType.uint32()));
        assertFunction2((x, y)->x >>> (y&0x1f), "shift_right_uint32", PrimitiveType.uint32(), UINT32S, int32s(-1, 34, PrimitiveType.uint32()));
        assertFunction2((x, y)->x >>> (y&0x3f), "shift_right_uint64", PrimitiveType.uint64(), UINT64S, int32s(-1, 66, PrimitiveType.uint32()));
        assertFunction2((x, y)->(byte)((x&0xff) >>> (y&0x1f)), "shift_right_uint8", PrimitiveType.uint8(), UINT8S, int32s(-1, 10, PrimitiveType.uint32()));
    }

    @Test
    public void testSizeOfs() throws Throwable {
        Map<Integer, Long> map=new TreeMap<>();
        map.put(-1, -1L);
        map.put(0, 1L);
        map.put(1, 2L);
        map.put(2, 4L);
        map.put(3, 8L);
        map.put(4, 4L);
        map.put(5, 8L);
        map.put(6, 8L);
        map.put(7, 8L);
        map.put(8, 8L);
        map.put(9, 8L);
        map.put(10, 8L);
        map.put(11, 47L);
        map.put(12, 94L);
        map.put(13, 188L);
        map.put(14, 376L);
        map.put(15, 188L);
        map.put(16, 376L);
        map.put(17, 376L);
        map.put(18, 72L);
        map.put(19, -1L);
        assertFunction1(
                map::get,
                "sizeofs",
                PrimitiveType.uint64(),
                map.keySet()
                        .stream()
                        .map(PrimitiveType.uint32()::value)
                        .toList());
    }

    @Test
    public void testStackOverflow() throws Throwable {
        try {
            callFunction(
                    true,
                    "stack_overflow",
                    PrimitiveType.voidType(),
                    PrimitiveType.uint64().value(1L<<Memory.ADDRESS_BITS));
            emulator.run();
            fail();
        }
        catch (StackOverflowException ignore) {
        }
    }

    @Test
    public void testSubtract() throws Throwable {
        assertFunction2((x, y)->x-y, "subtract_double", PrimitiveType.dfloat(), DOUBLES, DOUBLES);
        assertFunction2((x, y)->x-y, "subtract_float", PrimitiveType.sfloat(), FLOATS, FLOATS);
        assertFunction2((x, y)->(short)(x-y), "subtract_int16", PrimitiveType.sint16(), SINT16S, SINT16S);
        assertFunction2((x, y)->x-y, "subtract_int32", PrimitiveType.sint32(), SINT32S, SINT32S);
        assertFunction2((x, y)->x-y, "subtract_int64", PrimitiveType.sint64(), SINT64S, SINT64S);
        assertFunction2((x, y)->(byte)(x-y), "subtract_int8", PrimitiveType.sint8(), SINT8S, SINT8S);
        assertFunction2((x, y)->(short)(x-y), "subtract_uint16", PrimitiveType.uint16(), UINT16S, UINT16S);
        assertFunction2((x, y)->x-y, "subtract_uint32", PrimitiveType.uint32(), UINT32S, UINT32S);
        assertFunction2((x, y)->x-y, "subtract_uint64", PrimitiveType.uint64(), UINT64S, UINT64S);
        assertFunction2((x, y)->(byte)(x-y), "subtract_uint8", PrimitiveType.uint8(), UINT8S, UINT8S);
    }

    @Test
    public void testXorBitwise() throws Throwable {
        assertFunction2((x, y)->(short)(x^y), "xor_bitwise_int16", PrimitiveType.sint16(), SINT16S, SINT16S);
        assertFunction2((x, y)->x^y, "xor_bitwise_int32", PrimitiveType.sint32(), SINT32S, SINT32S);
        assertFunction2((x, y)->x^y, "xor_bitwise_int64", PrimitiveType.sint64(), SINT64S, SINT64S);
        assertFunction2((x, y)->(byte)(x^y), "xor_bitwise_int8", PrimitiveType.sint8(), SINT8S, SINT8S);
        assertFunction2((x, y)->(short)(x^y), "xor_bitwise_uint16", PrimitiveType.uint16(), UINT16S, UINT16S);
        assertFunction2((x, y)->x^y, "xor_bitwise_uint32", PrimitiveType.uint32(), UINT32S, UINT32S);
        assertFunction2((x, y)->x^y, "xor_bitwise_uint64", PrimitiveType.uint64(), UINT64S, UINT64S);
        assertFunction2((x, y)->(byte)(x^y), "xor_bitwise_uint8", PrimitiveType.uint8(), UINT8S, UINT8S);
    }
}
