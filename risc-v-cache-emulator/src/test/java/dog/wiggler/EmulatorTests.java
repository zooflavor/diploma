package dog.wiggler;

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
import dog.wiggler.memory.LogVisitor;
import dog.wiggler.memory.Logs;
import dog.wiggler.memory.MemoryMappedMemory;
import dog.wiggler.riscv64.Hart;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
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
import java.util.Set;
import java.util.TreeMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(Parameterized.class)
public class EmulatorTests {
    public static final List<PrimitiveValue.DFloat> DOUBLES;
    public static final List<String> EXECUTABLE_IMAGE_OPTIONS=List.of(
            "clang-O0",
            "clang-O1",
            "clang-O2",
            "gcc-O0",
            "gcc-O1",
            "gcc-O2");
    public static final List<PrimitiveValue.SFloat> FLOATS;
    public static final Path MEMORY_IMAGE_PATH;
    public static final long MEMORY_IMAGE_SIZE=1L<<34;
    public static final Path ROOT_PATH;
    public static final List<PrimitiveValue.SInt16> SINT16S;
    public static final List<PrimitiveValue.SInt32> SINT32S;
    public static final List<PrimitiveValue.SInt64> SINT64S;
    public static final List<PrimitiveValue.SInt8> SINT8S;
    public static final boolean SLOW=false;
    public static final List<PrimitiveValue.UInt16> UINT16S;
    public static final List<PrimitiveValue.UInt32> UINT32S;
    public static final List<PrimitiveValue.UInt64> UINT64S;
    public static final List<PrimitiveValue.UInt8> UINT8S;

    static {
        Set<Double> doubles0=new HashSet<>();
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
        Set<Double> doubles1=new HashSet<>();
        for (double value: doubles0) {
            doubles1.add(value);
            doubles1.add(-value);
            doubles1.add(1.0/value);
        }
        DOUBLES=List.copyOf(doubles1.stream()
                .sorted()
                .distinct()
                .map(PrimitiveValue.DFloat::new)
                .toList());

        Set<Float> floats0=new HashSet<>();
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
        Set<Float> floats1=new HashSet<>();
        for (float value: floats0) {
            floats1.add(value);
            floats1.add(-value);
            floats1.add(1.0f/value);
        }
        FLOATS=List.copyOf(floats1.stream()
                .sorted()
                .distinct()
                .map(PrimitiveValue.SFloat::new)
                .toList());

        Set<Short> int16s0=new HashSet<>();
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
        Set<Short> int16s1=new HashSet<>();
        for (int value: int16s0) {
            int16s1.add((short)value);
            int16s1.add((short)-value);
            int16s1.add((short)~value);
        }
        SINT16S=List.copyOf(int16s1.stream()
                .sorted()
                .distinct()
                .map(PrimitiveValue.SInt16::new)
                .toList());
        UINT16S=List.copyOf(int16s1.stream()
                .sorted()
                .distinct()
                .map(PrimitiveValue.UInt16::new)
                .toList());

        Set<Integer> int32s0=new HashSet<>();
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
        Set<Integer> int32s1=new HashSet<>();
        for (int value: int32s0) {
            int32s1.add(value);
            int32s1.add(-value);
            int32s1.add(~value);
        }
        SINT32S=List.copyOf(int32s1.stream()
                .sorted()
                .distinct()
                .map(PrimitiveValue.SInt32::new)
                .toList());
        UINT32S=List.copyOf(int32s1.stream()
                .sorted()
                .distinct()
                .map(PrimitiveValue.UInt32::new)
                .toList());

        Set<Long> int64s0=new HashSet<>();
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
        Set<Long> int64s1=new HashSet<>();
        for (long value: int64s0) {
            int64s1.add(value);
            int64s1.add(-value);
            int64s1.add(~value);
        }
        SINT64S=List.copyOf(int64s1.stream()
                .sorted()
                .distinct()
                .map(PrimitiveValue.SInt64::new)
                .toList());
        UINT64S=List.copyOf(int64s1.stream()
                .sorted()
                .distinct()
                .map(PrimitiveValue.UInt64::new)
                .toList());

        Set<Byte> int8s0=new HashSet<>();
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
        Set<Byte> int8s1=new HashSet<>();
        for (int value: int8s0) {
            int8s1.add((byte)value);
            int8s1.add((byte)-value);
            int8s1.add((byte)~value);
        }
        SINT8S=List.copyOf(int8s1.stream()
                .sorted()
                .distinct()
                .map(PrimitiveValue.SInt8::new)
                .toList());
        UINT8S=List.copyOf(int8s1.stream()
                .sorted()
                .distinct()
                .map(PrimitiveValue.UInt8::new)
                .toList());

        try {
            MEMORY_IMAGE_PATH=Paths.get("../memory.image").toAbsolutePath().toRealPath();
            ROOT_PATH=Paths.get("../c/out").toAbsolutePath().toRealPath();
        }
        catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public Emulator emulator;
    public final String executableImageOption;

    public EmulatorTests(String executableImageOption) {
        this.executableImageOption=executableImageOption;
    }

    @After
    public void after() throws Throwable {
        if (null!=emulator) {
            emulator.close();
        }
    }

    public <J, K, V extends PrimitiveValue<J>, W extends PrimitiveValue<K>> void assertFunction1(
            Function<J, K> expected, String function, PrimitiveType<K, W> resultType, Iterable<V> parameters)
            throws Throwable {
        for (V parameter: parameters) {
            assertEquals(
                    String.format("function=%s, parameter=%s", function, parameter),
                    resultType.java(expected.apply(parameter.java())),
                    callFunction(true, function, resultType, parameter));
        }
    }

    public <J, K, L, V extends PrimitiveValue<J>, W extends PrimitiveValue<K>, X extends PrimitiveValue<L>>
    void assertFunction2(
            BiFunction<J, K, L> expected, String function, PrimitiveType<L, X> resultType,
            Iterable<V> parameters0, Iterable<W> parameters1) throws Throwable {
        for (V parameter0: parameters0) {
            for (W parameter1: parameters1) {
                assertEquals(
                        String.format("function=%s, parameter0=%s, parameter1=%s", function, parameter0, parameter1),
                        resultType.java(expected.apply(parameter0.java(), parameter1.java())),
                        callFunction(true, function, resultType, parameter0, parameter1));
            }
        }
    }

    public <J, K, L, M, V extends PrimitiveValue<J>, W extends PrimitiveValue<K>,
            X extends PrimitiveValue<L>, Y extends PrimitiveValue<M>>
    void assertFunction3(
            TriFunction<J, K, L, M> expected, String function, PrimitiveType<M, Y> resultType,
            Iterable<V> parameters0, Iterable<W> parameters1, Iterable<X> parameters2) throws Throwable {
        for (V parameter0: parameters0) {
            for (W parameter1: parameters1) {
                for (X parameter2: parameters2) {
                    assertEquals(
                            String.format(
                                    "function=%s, parameter0=%s, parameter1=%s, parameter2=%s",
                                    function, parameter0, parameter1, parameter2),
                            resultType.java(expected.apply(parameter0.java(), parameter1.java(), parameter2.java())),
                            callFunction(true, function, resultType, parameter0, parameter1, parameter2));
                }
            }
        }
    }

    @Before
    public void before() throws Throwable {
        emulator=emulator(null, null, null);
    }

    public <J, V extends PrimitiveValue<J>> V callFunction(
            boolean reset, String function, PrimitiveType<J, V> resultType, List<PrimitiveValue<?>> parameters)
            throws Throwable {
        SymbolTableEntry functionSymbol=emulator.elfHeader.symbolTable.get(function);
        assertNotNull(function, functionSymbol);
        if (reset) {
            emulator.reset();
        }
        FunctionCallParameters.create()
                .addAll(parameters)
                .setParameters(emulator);
        emulator.hart.setReturnAddress(IOMap.EXIT_OK);
        emulator.hart.setPc(functionSymbol.value);
        emulator.run();
        assertEquals(0, emulator.exit.code());
        return resultType.functionCallResult(emulator.hart);
    }

    public <J, V extends PrimitiveValue<J>> V callFunction(
            boolean reset, String function, PrimitiveType<J, V> resultType, PrimitiveValue<?>... parameters)
            throws Throwable {
        return callFunction(reset, function, resultType, List.of(parameters));
    }

    private Emulator emulator(
            Input input, Supplier<? extends Log> logFactory, Output output) throws Throwable {
        if (null==input) {
            input=Input.supplier(()->{
                fail();
                return null;
            });
        }
        if (null==output) {
            output=Output.consumer((value)->fail());
        }
        Path imageFile=ROOT_PATH.resolve("emulator-tests.riscv64-"+executableImageOption+".elf");
        Emulator emulator=Emulator
                .factory(
                        input,
                        logFactory,
                        MemoryMappedMemory.factory(MEMORY_IMAGE_PATH, MEMORY_IMAGE_SIZE),
                        output)
                .get();
        emulator.loadELFAndReset(imageFile);
        return emulator;
    }

    public static <V extends PrimitiveValue<Integer>> Iterable<V> int32s(
            int from, int to, PrimitiveType<Integer, V> type) {
        return ()->new Iterator<>() {
            private int next=from;

            @Override
            public boolean hasNext() {
                return to>next;
            }

            @Override
            public V next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                int result=next;
                ++next;
                return type.java(result);
            }
        };
    }

    @Parameterized.Parameters(name="{0}")
    public static List<String> parameters() {
        return EXECUTABLE_IMAGE_OPTIONS;
    }

    public static Random random() {
        return new Random(267238775);
    }

    @Test
    public void testAdd() throws Throwable {
        assertFunction2(Double::sum, "add_double", PrimitiveType.DFLOAT, DOUBLES, DOUBLES);
        assertFunction2(Float::sum, "add_float", PrimitiveType.SFLOAT, FLOATS, FLOATS);
        assertFunction2((x, y)->(short)(x+y), "add_int16", PrimitiveType.SINT16, SINT16S, SINT16S);
        assertFunction2(Integer::sum, "add_int32", PrimitiveType.SINT32, SINT32S, SINT32S);
        assertFunction2(Long::sum, "add_int64", PrimitiveType.SINT64, SINT64S, SINT64S);
        assertFunction2((x, y)->(byte)(x+y), "add_int8", PrimitiveType.SINT8, SINT8S, SINT8S);
        assertFunction2((x, y)->(short)(x+y), "add_uint16", PrimitiveType.UINT16, UINT16S, UINT16S);
        assertFunction2(Integer::sum, "add_uint32", PrimitiveType.UINT32, UINT32S, UINT32S);
        assertFunction2(Long::sum, "add_uint64", PrimitiveType.UINT64, UINT64S, UINT64S);
        assertFunction2((x, y)->(byte)(x+y), "add_uint8", PrimitiveType.UINT8, UINT8S, UINT8S);
    }

    @Test
    public void testAddArray() throws Throwable {
        testAddArray("add_array_double", Random::nextDouble, Double::sum, PrimitiveType.DFLOAT);
        testAddArray("add_array_float", Random::nextFloat, Float::sum, PrimitiveType.SFLOAT);
        testAddArray("add_array_int16", (random)->(short)random.nextInt(), (x, y)->(short)(x+y), PrimitiveType.SINT16);
        testAddArray("add_array_int32", Random::nextInt, Integer::sum, PrimitiveType.SINT32);
        testAddArray("add_array_int64", Random::nextLong, Long::sum, PrimitiveType.SINT64);
        testAddArray("add_array_int8", (random)->(byte)random.nextInt(), (x, y)->(byte)(x+y), PrimitiveType.SINT8);
        testAddArray("add_array_uint16", (random)->(short)random.nextInt(), (x, y)->(short)(x+y), PrimitiveType.UINT16);
        testAddArray("add_array_uint32", Random::nextInt, Integer::sum, PrimitiveType.UINT32);
        testAddArray("add_array_uint64", Random::nextLong, Long::sum, PrimitiveType.UINT64);
        testAddArray("add_array_uint8", (random)->(byte)random.nextInt(), (x, y)->(byte)(x+y), PrimitiveType.UINT8);
    }

    private <J, V extends PrimitiveValue<J>> void testAddArray(
            String function, Function<Random, J> next, BiFunction<J, J, J> operator, PrimitiveType<J, V> type)
            throws Throwable {
        emulator.reset();
        long baseAddress=emulator.heapAndStack.malloc(emulator.hart, 1L<<32);
        assertNotEquals(0L, baseAddress);
        baseAddress&=((-1L)<<30);
        baseAddress+=1L<<32;
        baseAddress-=1L<<30;
        Random random=random();
        int size=16;
        for (int ii=8; 0<ii; --ii) {
            long input0=baseAddress-ii;
            long input1=input0+(long)type.size()*size;
            long output=input1+(long)type.size()*size;
            List<V> expected=new ArrayList<>(size);
            for (int jj=0; size>jj; ++jj) {
                V value0=type.java(next.apply(random));
                V value1=type.java(next.apply(random));
                type.store(emulator.memoryLog, input0+(long)type.size()*jj, value0);
                type.store(emulator.memoryLog, input1+(long)type.size()*jj, value1);
                expected.add(type.java(operator.apply(value0.java(), value1.java())));
            }
            callFunction(
                    true,
                    function,
                    PrimitiveType.VOID,
                    PrimitiveValue.uint64(input0),
                    PrimitiveValue.uint64(input1),
                    PrimitiveValue.uint64(output),
                    PrimitiveValue.uint32(size));
            for (int jj=0; size>jj; ++jj) {
                assertEquals(
                        String.format("ii=%s, jj=%s", ii, jj),
                        expected.get(jj),
                        type.load(emulator.memoryLog, output+(long)type.size()*jj));
            }
        }
    }

    @Test
    public void testAndBitwise() throws Throwable {
        assertFunction2((x, y)->(short)(x&y), "and_bitwise_int16", PrimitiveType.SINT16, SINT16S, SINT16S);
        assertFunction2((x, y)->x&y, "and_bitwise_int32", PrimitiveType.SINT32, SINT32S, SINT32S);
        assertFunction2((x, y)->x&y, "and_bitwise_int64", PrimitiveType.SINT64, SINT64S, SINT64S);
        assertFunction2((x, y)->(byte)(x&y), "and_bitwise_int8", PrimitiveType.SINT8, SINT8S, SINT8S);
        assertFunction2((x, y)->(short)(x&y), "and_bitwise_uint16", PrimitiveType.UINT16, UINT16S, UINT16S);
        assertFunction2((x, y)->x&y, "and_bitwise_uint32", PrimitiveType.UINT32, UINT32S, UINT32S);
        assertFunction2((x, y)->x&y, "and_bitwise_uint64", PrimitiveType.UINT64, UINT64S, UINT64S);
        assertFunction2((x, y)->(byte)(x&y), "and_bitwise_uint8", PrimitiveType.UINT8, UINT8S, UINT8S);
    }

    @Test
    public void testAndLogical() throws Throwable {
        assertFunction2((x, y)->((0.0==x) || (0.0==y))?0.0:1.0, "and_logical_double", PrimitiveType.DFLOAT, DOUBLES, DOUBLES);
        assertFunction2((x, y)->((0.0f==x) || (0.0f==y))?0.0f:1.0f, "and_logical_float", PrimitiveType.SFLOAT, FLOATS, FLOATS);
        assertFunction2((x, y)->(short)(((0==x) || (0==y))?0:1), "and_logical_int16", PrimitiveType.SINT16, SINT16S, SINT16S);
        assertFunction2((x, y)->((0==x) || (0==y))?0:1, "and_logical_int32", PrimitiveType.SINT32, SINT32S, SINT32S);
        assertFunction2((x, y)->((0L==x) || (0L==y))?0L:1L, "and_logical_int64", PrimitiveType.SINT64, SINT64S, SINT64S);
        assertFunction2((x, y)->(byte)(((0==x) || (0==y))?0:1), "and_logical_int8", PrimitiveType.SINT8, SINT8S, SINT8S);
        assertFunction2((x, y)->((0L==x) || (0L==y))?0L:1L, "and_logical_ptr", PrimitiveType.UINT64, UINT64S, UINT64S);
        assertFunction2((x, y)->(short)(((0==x) || (0==y))?0:1), "and_logical_uint16", PrimitiveType.UINT16, UINT16S, UINT16S);
        assertFunction2((x, y)->((0==x) || (0==y))?0:1, "and_logical_uint32", PrimitiveType.UINT32, UINT32S, UINT32S);
        assertFunction2((x, y)->((0L==x) || (0L==y))?0L:1L, "and_logical_uint64", PrimitiveType.UINT64, UINT64S, UINT64S);
        assertFunction2((x, y)->(byte)(((0==x) || (0==y))?0:1), "and_logical_uint8", PrimitiveType.UINT8, UINT8S, UINT8S);
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

    private <J, V extends PrimitiveValue<J>> void testCasts(
            String function, Function<J, Double> toDouble, Function<J, Float> toFloat, Function<J, Short> toInt16,
            Function<J, Integer> toInt32, Function<J, Long> toInt64, Function<J, Byte> toInt8, Function<J, Long> toPtr,
            Function<J, List<Short>> toUint16, Function<J, Integer> toUint32, Function<J, Long> toUint64,
            Function<J, List<Byte>> toUint8, Iterable<V> values) throws Throwable {
        emulator.reset();
        long casts=emulator.heapAndStack.malloc(emulator.hart, 72);
        assertNotEquals(0L, casts);
        for (V value: values) {
            callFunction(true, function, PrimitiveType.VOID, PrimitiveValue.uint64(casts), value);
            assertEquals(
                    String.format("function=%s, value=%s", function, value),
                    toDouble.apply(value.java()),
                    Double.valueOf(emulator.memoryLog.loadDouble(casts)));
            assertEquals(
                    String.format("function=%s, value=%s", function, value),
                    toFloat.apply(value.java()),
                    Float.valueOf(emulator.memoryLog.loadFloat(casts+8)));
            assertEquals(
                    String.format("function=%s, value=%s", function, value),
                    toInt16.apply(value.java()),
                    Short.valueOf(emulator.memoryLog.loadInt16(casts+12)));
            assertEquals(
                    String.format("function=%s, value=%s", function, value),
                    toInt32.apply(value.java()),
                    Integer.valueOf(emulator.memoryLog.loadInt32(casts+16, false)));
            assertEquals(
                    String.format("function=%s, value=%s", function, value),
                    toInt64.apply(value.java()),
                    Long.valueOf(emulator.memoryLog.loadInt64(casts+24)));
            assertEquals(
                    String.format("function=%s, value=%s", function, value),
                    toInt8.apply(value.java()),
                    Byte.valueOf(emulator.memoryLog.loadInt8(casts+32)));
            assertEquals(
                    String.format("function=%s, value=%s", function, value),
                    toPtr.apply(value.java()),
                    Long.valueOf(emulator.memoryLog.loadInt64(casts+40)));
            List<Short> expectedUint16s=toUint16.apply(value.java());
            Short actualUint16=emulator.memoryLog.loadInt16(casts+48);
            assertTrue(
                    String.format("value=%s, expected=%s, actual=%s", value, expectedUint16s, actualUint16),
                    expectedUint16s.contains(actualUint16));
            assertEquals(
                    String.format("value=%s", value),
                    toUint32.apply(value.java()),
                    Integer.valueOf(emulator.memoryLog.loadInt32(casts+52, false)));
            assertEquals(
                    String.format("value=%s", value),
                    toUint64.apply(value.java()),
                    Long.valueOf(emulator.memoryLog.loadInt64(casts+56)));
            List<Byte> expectedUint8s=toUint8.apply(value.java());
            Byte actualUint8=emulator.memoryLog.loadInt8(casts+64);
            assertTrue(
                    String.format("value=%s, expected=%s, actual=%s", value, expectedUint8s, actualUint8),
                    expectedUint8s.contains(actualUint8));
        }
    }

    @Test
    public void testConditionals() throws Throwable {
        for (String impl: List.of("expression", "statement1", "statement2")) {
            assertFunction3((x, y, z)->(0.0==x)?z:y, String.format("conditional_%s_double", impl), PrimitiveType.DFLOAT, DOUBLES, DOUBLES.subList(0, 4), DOUBLES.subList(0, 4));
            assertFunction3((x, y, z)->(0.0f==x)?z:y, String.format("conditional_%s_float", impl), PrimitiveType.SFLOAT, FLOATS, FLOATS.subList(0, 4), FLOATS.subList(0, 4));
            assertFunction3((x, y, z)->(0==x)?z:y, String.format("conditional_%s_int16", impl), PrimitiveType.SINT16, SINT16S, SINT16S.subList(0, 4), SINT16S.subList(0, 4));
            assertFunction3((x, y, z)->(0==x)?z:y, String.format("conditional_%s_int32", impl), PrimitiveType.SINT32, SINT32S, SINT32S.subList(0, 4), SINT32S.subList(0, 4));
            assertFunction3((x, y, z)->(0L==x)?z:y, String.format("conditional_%s_int64", impl), PrimitiveType.SINT64, SINT64S, SINT64S.subList(0, 4), SINT64S.subList(0, 4));
            assertFunction3((x, y, z)->(0==x)?z:y, String.format("conditional_%s_int8", impl), PrimitiveType.SINT8, SINT8S, SINT8S.subList(0, 4), SINT8S.subList(0, 4));
            assertFunction3((x, y, z)->(0L==x)?z:y, String.format("conditional_%s_ptr", impl), PrimitiveType.UINT64, UINT64S, UINT64S.subList(0, 4), UINT64S.subList(0, 4));
            assertFunction3((x, y, z)->(0==x)?z:y, String.format("conditional_%s_uint16", impl), PrimitiveType.UINT16, UINT16S, UINT16S.subList(0, 4), UINT16S.subList(0, 4));
            assertFunction3((x, y, z)->(0==x)?z:y, String.format("conditional_%s_uint32", impl), PrimitiveType.UINT32, UINT32S, UINT32S.subList(0, 4), UINT32S.subList(0, 4));
            assertFunction3((x, y, z)->(0L==x)?z:y, String.format("conditional_%s_uint64", impl), PrimitiveType.UINT64, UINT64S, UINT64S.subList(0, 4), UINT64S.subList(0, 4));
            assertFunction3((x, y, z)->(0==x)?z:y, String.format("conditional_%s_uint8", impl), PrimitiveType.UINT8, UINT8S, UINT8S.subList(0, 4), UINT8S.subList(0, 4));
        }
    }

    @Test
    public void testConstants() throws Throwable {
        testConstants(
                "const_double_", PrimitiveType.DFLOAT,
                0.0, -0.0, 1.0, -1.0, Double.POSITIVE_INFINITY,
                Double.NEGATIVE_INFINITY, 333333.33333333333333333, 3.1415926535897932384626433);
        testConstants(
                "const_float_", PrimitiveType.SFLOAT,
                0.0f, -0.0f, 1.0f, -1.0f, Float.POSITIVE_INFINITY,
                Float.NEGATIVE_INFINITY, 333333.33333333333333333f, 3.1415926535897932384626433f);
        testConstants(
                "const_int16_", PrimitiveType.SINT16,
                (short)0x0000, (short)0xffff, (short)0x0001, (short)0x8000);
        testConstants(
                "const_int32_", PrimitiveType.SINT32,
                0x00000000, 0xffffffff, 0x00000001, 0x80000000);
        testConstants(
                "const_int64_", PrimitiveType.SINT64,
                0x0000000000000000L, 0xffffffffffffffffL, 0x0000000000000001L, 0x8000000000000000L);
        testConstants(
                "const_int8_", PrimitiveType.SINT8,
                (byte)0x00, (byte)0xff, (byte)0x01, (byte)0x80);
        testConstants(
                "const_ptr_", PrimitiveType.UINT64,
                0L, 0x0000000000000000L, 0xffffffffffffffffL, 0x0000000000000001L, 0x8000000000000000L);
        testConstants(
                "const_uint16_", PrimitiveType.UINT16,
                (short)0x0000, (short)0xffff, (short)0x0001, (short)0x8000);
        testConstants(
                "const_uint32_", PrimitiveType.UINT32,
                0x00000000, 0xffffffff, 0x00000001, 0x80000000);
        testConstants(
                "const_uint64_", PrimitiveType.UINT64,
                0x0000000000000000L, 0xffffffffffffffffL, 0x0000000000000001L, 0x8000000000000000L);
        testConstants(
                "const_uint8_", PrimitiveType.UINT8,
                (byte)0x00, (byte)0xff, (byte)0x01, (byte)0x80);
    }

    @SafeVarargs
    private <J, V extends PrimitiveValue<J>> void testConstants(
            String functionName, PrimitiveType<J, V> resultType, J... values) throws Throwable {
        for (int ii=0; values.length>ii; ++ii) {
            assertEquals(
                    resultType.java(values[ii]),
                    callFunction(true, functionName+ii, resultType));
        }
    }

    @Test
    public void testDivide() throws Throwable {
        assertFunction2((x, y)->x/y, "divide_double", PrimitiveType.DFLOAT, DOUBLES, DOUBLES);
        assertFunction2((x, y)->x/y, "divide_float", PrimitiveType.SFLOAT, FLOATS, FLOATS);
        assertFunction2((x, y)->(short)((0==y)?-1:(x/y)), "divide_int16", PrimitiveType.SINT16, SINT16S, SINT16S);
        assertFunction2((x, y)->(0==y)?-1:(x/y), "divide_int32", PrimitiveType.SINT32, SINT32S, SINT32S);
        assertFunction2((x, y)->(0L==y)?-1L:(x/y), "divide_int64", PrimitiveType.SINT64, SINT64S, SINT64S);
        assertFunction2((x, y)->(byte)((0==y)?-1:(x/y)), "divide_int8", PrimitiveType.SINT8, SINT8S, SINT8S);
        assertFunction2((x, y)->(short)((0==y)?-1:((x&0xffff)/(y&0xffff))), "divide_uint16", PrimitiveType.UINT16, UINT16S, UINT16S);
        assertFunction2((x, y)->(0==y)?-1:Integer.divideUnsigned(x, y), "divide_uint32", PrimitiveType.UINT32, UINT32S, UINT32S);
        assertFunction2((x, y)->(0L==y)?-1L:Long.divideUnsigned(x, y), "divide_uint64", PrimitiveType.UINT64, UINT64S, UINT64S);
        assertFunction2((x, y)->(byte)((0==y)?-1:((x&0xff)/(y&0xff))), "divide_uint8", PrimitiveType.UINT8, UINT8S, UINT8S);
    }

    @Test
    public void testEqual() throws Throwable {
        assertFunction2((x, y)->(x.doubleValue()==y.doubleValue())?1.0:0.0, "equal_double", PrimitiveType.DFLOAT, DOUBLES, DOUBLES);
        assertFunction2((x, y)->(x.floatValue()==y.floatValue())?1.0f:0.0f, "equal_float", PrimitiveType.SFLOAT, FLOATS, FLOATS);
        assertFunction2((x, y)->(short)((x.shortValue()==y.shortValue())?1:0), "equal_int16", PrimitiveType.SINT16, SINT16S, SINT16S);
        assertFunction2((x, y)->(x.intValue()==y.intValue())?1:0, "equal_int32", PrimitiveType.SINT32, SINT32S, SINT32S);
        assertFunction2((x, y)->(x.longValue()==y.longValue())?1L:0L, "equal_int64", PrimitiveType.SINT64, SINT64S, SINT64S);
        assertFunction2((x, y)->(byte)((x.byteValue()==y.byteValue())?1:0), "equal_int8", PrimitiveType.SINT8, SINT8S, SINT8S);
        assertFunction2((x, y)->(x.longValue()==y.longValue())?1L:0L, "equal_ptr", PrimitiveType.UINT64, UINT64S, UINT64S);
        assertFunction2((x, y)->(short)((x.shortValue()==y.shortValue())?1:0), "equal_uint16", PrimitiveType.UINT16, UINT16S, UINT16S);
        assertFunction2((x, y)->(x.intValue()==y.intValue())?1:0, "equal_uint32", PrimitiveType.UINT32, UINT32S, UINT32S);
        assertFunction2((x, y)->(x.longValue()==y.longValue())?1L:0L, "equal_uint64", PrimitiveType.UINT64, UINT64S, UINT64S);
        assertFunction2((x, y)->(byte)((x.byteValue()==y.byteValue())?1:0), "equal_uint8", PrimitiveType.UINT8, UINT8S, UINT8S);
    }

    @Test
    public void testExit() throws Throwable {
        emulator.reset();
        emulator.hart.registersXs.setInt32(Hart.REGISTER_A0, 13);
        emulator.hart.setPc(IOMap.EXIT);
        emulator.run();
        assertEquals(13, emulator.exit.code());
    }

    @Test
    public void testExitExits() throws Throwable {
        emulator.reset();
        emulator.hart.setReturnAddress(IOMap.EXIT_OK);
        emulator.hart.setPc(emulator.elfHeader.symbolTable.get("exit_forever").value);
        emulator.run();
        assertEquals(13, emulator.exit.code());
    }

    @Test
    public void testExitOk() throws Throwable {
        emulator.reset();
        emulator.hart.registersXs.setInt32(0, 13);
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
                        String.format("ii=%s, value=%s", ii, value),
                        PrimitiveValue.uint32(factorial.apply(value)),
                        callFunction(true, "factorial"+ii, PrimitiveType.UINT32, PrimitiveValue.uint32(value)));
            }
        }
    }

    @Test
    public void testFunctionPointerCall() throws Throwable {
        testFunctionPointerCall("add_int64", Long::sum);
        testFunctionPointerCall("multiply_int64", (x, y)->x*y);
    }

    private void testFunctionPointerCall(String function, BiFunction<Long, Long, Long> operator) throws Throwable {
        assertFunction3(
                (x, y, z)->operator.apply(y, z),
                "function_pointer_call",
                PrimitiveType.UINT64,
                List.of(PrimitiveValue.uint64(emulator.elfHeader.symbolTable.get(function).value)),
                UINT64S,
                UINT64S);
    }

    @Test
    public void testGreater() throws Throwable {
        assertFunction2((x, y)->(x>y)?1.0:0.0, "greater_double", PrimitiveType.DFLOAT, DOUBLES, DOUBLES);
        assertFunction2((x, y)->(x>y)?1.0f:0.0f, "greater_float", PrimitiveType.SFLOAT, FLOATS, FLOATS);
        assertFunction2((x, y)->(short)((x>y)?1:0), "greater_int16", PrimitiveType.SINT16, SINT16S, SINT16S);
        assertFunction2((x, y)->(x>y)?1:0, "greater_int32", PrimitiveType.SINT32, SINT32S, SINT32S);
        assertFunction2((x, y)->(x>y)?1L:0L, "greater_int64", PrimitiveType.SINT64, SINT64S, SINT64S);
        assertFunction2((x, y)->(byte)((x>y)?1:0), "greater_int8", PrimitiveType.SINT8, SINT8S, SINT8S);
        assertFunction2((x, y)->(0<Long.compareUnsigned(x, y))?1L:0L, "greater_ptr", PrimitiveType.UINT64, UINT64S, UINT64S);
        assertFunction2((x, y)->(short)((0<Short.compareUnsigned(x, y))?1:0), "greater_uint16", PrimitiveType.UINT16, UINT16S, UINT16S);
        assertFunction2((x, y)->(0<Integer.compareUnsigned(x, y))?1:0, "greater_uint32", PrimitiveType.UINT32, UINT32S, UINT32S);
        assertFunction2((x, y)->(0<Long.compareUnsigned(x, y))?1L:0L, "greater_uint64", PrimitiveType.UINT64, UINT64S, UINT64S);
        assertFunction2((x, y)->(byte)((0<Byte.compareUnsigned(x, y))?1:0), "greater_uint8", PrimitiveType.UINT8, UINT8S, UINT8S);
    }

    @Test
    public void testGreaterOrEqual() throws Throwable {
        assertFunction2((x, y)->(x>=y)?1.0:0.0, "greater_or_equal_double", PrimitiveType.DFLOAT, DOUBLES, DOUBLES);
        assertFunction2((x, y)->(x>=y)?1.0f:0.0f, "greater_or_equal_float", PrimitiveType.SFLOAT, FLOATS, FLOATS);
        assertFunction2((x, y)->(short)((x>=y)?1:0), "greater_or_equal_int16", PrimitiveType.SINT16, SINT16S, SINT16S);
        assertFunction2((x, y)->(x>=y)?1:0, "greater_or_equal_int32", PrimitiveType.SINT32, SINT32S, SINT32S);
        assertFunction2((x, y)->(x>=y)?1L:0L, "greater_or_equal_int64", PrimitiveType.SINT64, SINT64S, SINT64S);
        assertFunction2((x, y)->(byte)((x>=y)?1:0), "greater_or_equal_int8", PrimitiveType.SINT8, SINT8S, SINT8S);
        assertFunction2((x, y)->(0<=Long.compareUnsigned(x, y))?1L:0L, "greater_or_equal_ptr", PrimitiveType.UINT64, UINT64S, UINT64S);
        assertFunction2((x, y)->(short)((0<=Short.compareUnsigned(x, y))?1:0), "greater_or_equal_uint16", PrimitiveType.UINT16, UINT16S, UINT16S);
        assertFunction2((x, y)->(0<=Integer.compareUnsigned(x, y))?1:0, "greater_or_equal_uint32", PrimitiveType.UINT32, UINT32S, UINT32S);
        assertFunction2((x, y)->(0<=Long.compareUnsigned(x, y))?1L:0L, "greater_or_equal_uint64", PrimitiveType.UINT64, UINT64S, UINT64S);
        assertFunction2((x, y)->(byte)((0<=Byte.compareUnsigned(x, y))?1:0), "greater_or_equal_uint8", PrimitiveType.UINT8, UINT8S, UINT8S);
    }

    @Test
    public void testLess() throws Throwable {
        assertFunction2((x, y)->(x<y)?1.0:0.0, "less_double", PrimitiveType.DFLOAT, DOUBLES, DOUBLES);
        assertFunction2((x, y)->(x<y)?1.0f:0.0f, "less_float", PrimitiveType.SFLOAT, FLOATS, FLOATS);
        assertFunction2((x, y)->(short)((x<y)?1:0), "less_int16", PrimitiveType.SINT16, SINT16S, SINT16S);
        assertFunction2((x, y)->(x<y)?1:0, "less_int32", PrimitiveType.SINT32, SINT32S, SINT32S);
        assertFunction2((x, y)->(x<y)?1L:0L, "less_int64", PrimitiveType.SINT64, SINT64S, SINT64S);
        assertFunction2((x, y)->(byte)((x<y)?1:0), "less_int8", PrimitiveType.SINT8, SINT8S, SINT8S);
        assertFunction2((x, y)->(0>Long.compareUnsigned(x, y))?1L:0L, "less_ptr", PrimitiveType.UINT64, UINT64S, UINT64S);
        assertFunction2((x, y)->(short)((0>Short.compareUnsigned(x, y))?1:0), "less_uint16", PrimitiveType.UINT16, UINT16S, UINT16S);
        assertFunction2((x, y)->(0>Integer.compareUnsigned(x, y))?1:0, "less_uint32", PrimitiveType.UINT32, UINT32S, UINT32S);
        assertFunction2((x, y)->(0>Long.compareUnsigned(x, y))?1L:0L, "less_uint64", PrimitiveType.UINT64, UINT64S, UINT64S);
        assertFunction2((x, y)->(byte)((0>Byte.compareUnsigned(x, y))?1:0), "less_uint8", PrimitiveType.UINT8, UINT8S, UINT8S);
    }

    @Test
    public void testLessOrEqual() throws Throwable {
        assertFunction2((x, y)->(x<=y)?1.0:0.0, "less_or_equal_double", PrimitiveType.DFLOAT, DOUBLES, DOUBLES);
        assertFunction2((x, y)->(x<=y)?1.0f:0.0f, "less_or_equal_float", PrimitiveType.SFLOAT, FLOATS, FLOATS);
        assertFunction2((x, y)->(short)((x<=y)?1:0), "less_or_equal_int16", PrimitiveType.SINT16, SINT16S, SINT16S);
        assertFunction2((x, y)->(x<=y)?1:0, "less_or_equal_int32", PrimitiveType.SINT32, SINT32S, SINT32S);
        assertFunction2((x, y)->(x<=y)?1L:0L, "less_or_equal_int64", PrimitiveType.SINT64, SINT64S, SINT64S);
        assertFunction2((x, y)->(byte)((x<=y)?1:0), "less_or_equal_int8", PrimitiveType.SINT8, SINT8S, SINT8S);
        assertFunction2((x, y)->(0>=Long.compareUnsigned(x, y))?1L:0L, "less_or_equal_ptr", PrimitiveType.UINT64, UINT64S, UINT64S);
        assertFunction2((x, y)->(short)((0>=Short.compareUnsigned(x, y))?1:0), "less_or_equal_uint16", PrimitiveType.UINT16, UINT16S, UINT16S);
        assertFunction2((x, y)->(0>=Integer.compareUnsigned(x, y))?1:0, "less_or_equal_uint32", PrimitiveType.UINT32, UINT32S, UINT32S);
        assertFunction2((x, y)->(0>=Long.compareUnsigned(x, y))?1L:0L, "less_or_equal_uint64", PrimitiveType.UINT64, UINT64S, UINT64S);
        assertFunction2((x, y)->(byte)((0>=Byte.compareUnsigned(x, y))?1:0), "less_or_equal_uint8", PrimitiveType.UINT8, UINT8S, UINT8S);
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
        assertEquals(address0+1, address1);
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
                    emulator.heapAndStack.getStackPointer(emulator.hart)-1L);
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
            emulator.memoryLog.disableAccessLog();
            emulator.memoryLog.storeInt16(p0, (short)0);
            emulator.memoryLog.storeInt32(p1, 0);
            emulator.memoryLog.storeInt64(p2, 0L);
            emulator.memoryLog.storeInt8(p3, (byte)0);
            emulator.memoryLog.enableAccessLog();
            callFunction(
                    false,
                    "memory_access",
                    PrimitiveType.VOID,
                    PrimitiveValue.uint64(p0),
                    PrimitiveValue.uint64(p1),
                    PrimitiveValue.uint64(p2),
                    PrimitiveValue.uint64(p3));
            emulator.memoryLog.disableAccessLog();
            //assertEquals((short)28, emulator.memoryLog.loadInt16(p0));
            //assertEquals(28, emulator.memoryLog.loadInt32(p1, false));
            //assertEquals(28L, emulator.memoryLog.loadInt64(p2));
            //assertEquals((byte)28, emulator.memoryLog.loadInt8(p3));
            emulator.close();
            try (LogInputStream logStream=LogInputStream.factory(logPath).get()) {
                assertEquals(
                        Logs.encodeAccess(
                                emulator.elfHeader.symbolTable.get("memory_access").value,
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
                        case ELAPSED_CYCLES -> {
                        }
                        case USER_DATA -> filtered.add(log);
                        default -> throw new IllegalStateException("unexpected type");
                    }
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
        assertFunction2((x, y)->x*y, "multiply_double", PrimitiveType.DFLOAT, DOUBLES, DOUBLES);
        assertFunction2((x, y)->x*y, "multiply_float", PrimitiveType.SFLOAT, FLOATS, FLOATS);
        assertFunction2((x, y)->(short)(x*y), "multiply_int16", PrimitiveType.SINT16, SINT16S, SINT16S);
        assertFunction2((x, y)->x*y, "multiply_int32", PrimitiveType.SINT32, SINT32S, SINT32S);
        assertFunction2((x, y)->x*y, "multiply_int64", PrimitiveType.SINT64, SINT64S, SINT64S);
        assertFunction2((x, y)->(byte)(x*y), "multiply_int8", PrimitiveType.SINT8, SINT8S, SINT8S);
        assertFunction2((x, y)->(short)(x*y), "multiply_uint16", PrimitiveType.UINT16, UINT16S, UINT16S);
        assertFunction2((x, y)->x*y, "multiply_uint32", PrimitiveType.UINT32, UINT32S, UINT32S);
        assertFunction2((x, y)->x*y, "multiply_uint64", PrimitiveType.UINT64, UINT64S, UINT64S);
        assertFunction2((x, y)->(byte)(x*y), "multiply_uint8", PrimitiveType.UINT8, UINT8S, UINT8S);
    }

    @Test
    public void testNegate() throws Throwable {
        assertFunction1((x)->-x, "negate_double", PrimitiveType.DFLOAT, DOUBLES);
        assertFunction1((x)->-x, "negate_float", PrimitiveType.SFLOAT, FLOATS);
        assertFunction1((x)->(short)(-x), "negate_int16", PrimitiveType.SINT16, SINT16S);
        assertFunction1((x)->-x, "negate_int32", PrimitiveType.SINT32, SINT32S);
        assertFunction1((x)->-x, "negate_int64", PrimitiveType.SINT64, SINT64S);
        assertFunction1((x)->(byte)(-x), "negate_int8", PrimitiveType.SINT8, SINT8S);
        assertFunction1((x)->(short)(-x), "negate_uint16", PrimitiveType.UINT16, UINT16S);
        assertFunction1((x)->-x, "negate_uint32", PrimitiveType.UINT32, UINT32S);
        assertFunction1((x)->-x, "negate_uint64", PrimitiveType.UINT64, UINT64S);
        assertFunction1((x)->(byte)(-x), "negate_uint8", PrimitiveType.UINT8, UINT8S);
    }

    @Test
    public void testNotBitwise() throws Throwable {
        assertFunction1((x)->(short)(~x), "not_bitwise_int16", PrimitiveType.SINT16, SINT16S);
        assertFunction1((x)->~x, "not_bitwise_int32", PrimitiveType.SINT32, SINT32S);
        assertFunction1((x)->~x, "not_bitwise_int64", PrimitiveType.SINT64, SINT64S);
        assertFunction1((x)->(byte)(~x), "not_bitwise_int8", PrimitiveType.SINT8, SINT8S);
        assertFunction1((x)->(short)(~x), "not_bitwise_uint16", PrimitiveType.UINT16, UINT16S);
        assertFunction1((x)->~x, "not_bitwise_uint32", PrimitiveType.UINT32, UINT32S);
        assertFunction1((x)->~x, "not_bitwise_uint64", PrimitiveType.UINT64, UINT64S);
        assertFunction1((x)->(byte)(~x), "not_bitwise_uint8", PrimitiveType.UINT8, UINT8S);
    }

    @Test
    public void testNotLogical() throws Throwable {
        assertFunction1((x)->(0.0==x)?1.0:0.0, "not_logical_double", PrimitiveType.DFLOAT, DOUBLES);
        assertFunction1((x)->(0.0f==x)?1.0f:0.0f, "not_logical_float", PrimitiveType.SFLOAT, FLOATS);
        assertFunction1((x)->(short)((0==x)?1:0), "not_logical_int16", PrimitiveType.SINT16, SINT16S);
        assertFunction1((x)->(0==x)?1:0, "not_logical_int32", PrimitiveType.SINT32, SINT32S);
        assertFunction1((x)->(0L==x)?1L:0L, "not_logical_int64", PrimitiveType.SINT64, SINT64S);
        assertFunction1((x)->(byte)((0==x)?1:0), "not_logical_int8", PrimitiveType.SINT8, SINT8S);
        assertFunction1((x)->(0L==x)?1L:0L, "not_logical_ptr", PrimitiveType.UINT64, UINT64S);
        assertFunction1((x)->(short)((0==x)?1:0), "not_logical_uint16", PrimitiveType.UINT16, UINT16S);
        assertFunction1((x)->(0==x)?1:0, "not_logical_uint32", PrimitiveType.UINT32, UINT32S);
        assertFunction1((x)->(0L==x)?1L:0L, "not_logical_uint64", PrimitiveType.UINT64, UINT64S);
        assertFunction1((x)->(byte)((0==x)?1:0), "not_logical_uint8", PrimitiveType.UINT8, UINT8S);
    }

    @Test
    public void testNotEqual() throws Throwable {
        assertFunction2((x, y)->(x.doubleValue()!=y.doubleValue())?1.0:0.0, "not_equal_double", PrimitiveType.DFLOAT, DOUBLES, DOUBLES);
        assertFunction2((x, y)->(x.floatValue()!=y.floatValue())?1.0f:0.0f, "not_equal_float", PrimitiveType.SFLOAT, FLOATS, FLOATS);
        assertFunction2((x, y)->(short)((x.shortValue()!=y.shortValue())?1:0), "not_equal_int16", PrimitiveType.SINT16, SINT16S, SINT16S);
        assertFunction2((x, y)->(x.intValue()!=y.intValue())?1:0, "not_equal_int32", PrimitiveType.SINT32, SINT32S, SINT32S);
        assertFunction2((x, y)->(x.longValue()!=y.longValue())?1L:0L, "not_equal_int64", PrimitiveType.SINT64, SINT64S, SINT64S);
        assertFunction2((x, y)->(byte)((x.byteValue()!=y.byteValue())?1:0), "not_equal_int8", PrimitiveType.SINT8, SINT8S, SINT8S);
        assertFunction2((x, y)->(x.longValue()!=y.longValue())?1L:0L, "not_equal_ptr", PrimitiveType.UINT64, UINT64S, UINT64S);
        assertFunction2((x, y)->(short)((x.shortValue()!=y.shortValue())?1:0), "not_equal_uint16", PrimitiveType.UINT16, UINT16S, UINT16S);
        assertFunction2((x, y)->(x.intValue()!=y.intValue())?1:0, "not_equal_uint32", PrimitiveType.UINT32, UINT32S, UINT32S);
        assertFunction2((x, y)->(x.longValue()!=y.longValue())?1L:0L, "not_equal_uint64", PrimitiveType.UINT64, UINT64S, UINT64S);
        assertFunction2((x, y)->(byte)((x.byteValue()!=y.byteValue())?1:0), "not_equal_uint8", PrimitiveType.UINT8, UINT8S, UINT8S);
    }

    @Test
    public void testNotNotLogical() throws Throwable {
        assertFunction1((x)->(0.0==x)?0.0:1.0, "not_not_logical_double", PrimitiveType.DFLOAT, DOUBLES);
        assertFunction1((x)->(0.0f==x)?0.0f:1.0f, "not_not_logical_float", PrimitiveType.SFLOAT, FLOATS);
        assertFunction1((x)->(short)((0==x)?0:1), "not_not_logical_int16", PrimitiveType.SINT16, SINT16S);
        assertFunction1((x)->(0==x)?0:1, "not_not_logical_int32", PrimitiveType.SINT32, SINT32S);
        assertFunction1((x)->(0L==x)?0L:1L, "not_not_logical_int64", PrimitiveType.SINT64, SINT64S);
        assertFunction1((x)->(byte)((0==x)?0:1), "not_not_logical_int8", PrimitiveType.SINT8, SINT8S);
        assertFunction1((x)->(0L==x)?0L:1L, "not_not_logical_ptr", PrimitiveType.UINT64, UINT64S);
        assertFunction1((x)->(short)((0==x)?0:1), "not_not_logical_uint16", PrimitiveType.UINT16, UINT16S);
        assertFunction1((x)->(0==x)?0:1, "not_not_logical_uint32", PrimitiveType.UINT32, UINT32S);
        assertFunction1((x)->(0L==x)?0L:1L, "not_not_logical_uint64", PrimitiveType.UINT64, UINT64S);
        assertFunction1((x)->(byte)((0==x)?0:1), "not_not_logical_uint8", PrimitiveType.UINT8, UINT8S);
    }


    @Test
    public void testOrBitwise() throws Throwable {
        assertFunction2((x, y)->(short)(x|y), "or_bitwise_int16", PrimitiveType.SINT16, SINT16S, SINT16S);
        assertFunction2((x, y)->x|y, "or_bitwise_int32", PrimitiveType.SINT32, SINT32S, SINT32S);
        assertFunction2((x, y)->x|y, "or_bitwise_int64", PrimitiveType.SINT64, SINT64S, SINT64S);
        assertFunction2((x, y)->(byte)(x|y), "or_bitwise_int8", PrimitiveType.SINT8, SINT8S, SINT8S);
        assertFunction2((x, y)->(short)(x|y), "or_bitwise_uint16", PrimitiveType.UINT16, UINT16S, UINT16S);
        assertFunction2((x, y)->x|y, "or_bitwise_uint32", PrimitiveType.UINT32, UINT32S, UINT32S);
        assertFunction2((x, y)->x|y, "or_bitwise_uint64", PrimitiveType.UINT64, UINT64S, UINT64S);
        assertFunction2((x, y)->(byte)(x|y), "or_bitwise_uint8", PrimitiveType.UINT8, UINT8S, UINT8S);
    }

    @Test
    public void testOrLogical() throws Throwable {
        assertFunction2((x, y)->((0.0!=x) || (0.0!=y))?1.0:0.0, "or_logical_double", PrimitiveType.DFLOAT, DOUBLES, DOUBLES);
        assertFunction2((x, y)->((0.0f!=x) || (0.0f!=y))?1.0f:0.0f, "or_logical_float", PrimitiveType.SFLOAT, FLOATS, FLOATS);
        assertFunction2((x, y)->(short)(((0!=x) || (0!=y))?1:0), "or_logical_int16", PrimitiveType.SINT16, SINT16S, SINT16S);
        assertFunction2((x, y)->((0!=x) || (0!=y))?1:0, "or_logical_int32", PrimitiveType.SINT32, SINT32S, SINT32S);
        assertFunction2((x, y)->((0L!=x) || (0L!=y))?1L:0L, "or_logical_int64", PrimitiveType.SINT64, SINT64S, SINT64S);
        assertFunction2((x, y)->(byte)(((0!=x) || (0!=y))?1:0), "or_logical_int8", PrimitiveType.SINT8, SINT8S, SINT8S);
        assertFunction2((x, y)->((0L!=x) || (0L!=y))?1L:0L, "or_logical_ptr", PrimitiveType.UINT64, UINT64S, UINT64S);
        assertFunction2((x, y)->(short)(((0!=x) || (0!=y))?1:0), "or_logical_uint16", PrimitiveType.UINT16, UINT16S, UINT16S);
        assertFunction2((x, y)->((0!=x) || (0!=y))?1:0, "or_logical_uint32", PrimitiveType.UINT32, UINT32S, UINT32S);
        assertFunction2((x, y)->((0L!=x) || (0L!=y))?1L:0L, "or_logical_uint64", PrimitiveType.UINT64, UINT64S, UINT64S);
        assertFunction2((x, y)->(byte)(((0!=x) || (0!=y))?1:0), "or_logical_uint8", PrimitiveType.UINT8, UINT8S, UINT8S);
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
        BiFunction<Integer, Double, Double> apply2=(coefficientsSize, xx)->{
            List<PrimitiveValue<?>> parameters=new ArrayList<>(coefficientsSize+1);
            for (int ii=0; coefficientsSize>ii; ++ii) {
                double coefficient=coefficients[ii];
                switch (ii%4) {
                    case 0 -> parameters.add(PrimitiveValue.dfloat(coefficient));
                    case 1 -> parameters.add(PrimitiveValue.uint64((long)coefficient));
                    case 2 -> parameters.add(PrimitiveValue.sfloat((float)coefficient));
                    default -> parameters.add(PrimitiveValue.uint32((int)coefficient));
                }
            }
            parameters.add(PrimitiveValue.dfloat(xx));
            return callFunction(true, "parameters_abi"+coefficientsSize, PrimitiveType.DFLOAT, parameters)
                    .java();
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
        emulator=emulator(input, null, output);
        callFunction(true, "read_input_write_output", PrimitiveType.VOID);
        assertTrue(inputValues.isEmpty());
        assertTrue(outputValues.isEmpty());
    }

    @Test
    public void testRemainder() throws Throwable {
        assertFunction2((x, y)->(short)((0==y)?x:(x%y)), "remainder_int16", PrimitiveType.SINT16, SINT16S, SINT16S);
        assertFunction2((x, y)->(0==y)?x:(x%y), "remainder_int32", PrimitiveType.SINT32, SINT32S, SINT32S);
        assertFunction2((x, y)->(0L==y)?x:(x%y), "remainder_int64", PrimitiveType.SINT64, SINT64S, SINT64S);
        assertFunction2((x, y)->(byte)((0==y)?x:(x%y)), "remainder_int8", PrimitiveType.SINT8, SINT8S, SINT8S);
        assertFunction2((x, y)->(short)((0==y)?x:((x&0xffff)%(y&0xffff))), "remainder_uint16", PrimitiveType.UINT16, UINT16S, UINT16S);
        assertFunction2((x, y)->(0==y)?x:Integer.remainderUnsigned(x, y), "remainder_uint32", PrimitiveType.UINT32, UINT32S, UINT32S);
        assertFunction2((x, y)->(0L==y)?x:Long.remainderUnsigned(x, y), "remainder_uint64", PrimitiveType.UINT64, UINT64S, UINT64S);
        assertFunction2((x, y)->(byte)((0==y)?x:((x&0xff)%(y&0xff))), "remainder_uint8", PrimitiveType.UINT8, UINT8S, UINT8S);
    }

    @Test
    public void testShiftLeft() throws Throwable {
        assertFunction2((x, y)->(short)(((0<=y) && (16>y))?x<<y:0), "shift_left_int16", PrimitiveType.SINT16, SINT16S, int32s(-1, 18, PrimitiveType.SINT32));
        assertFunction2((x, y)->x<<(y&0x1f), "shift_left_int32", PrimitiveType.SINT32, SINT32S, int32s(-1, 34, PrimitiveType.SINT32));
        assertFunction2((x, y)->x<<(y&0x3f), "shift_left_int64", PrimitiveType.SINT64, SINT64S, int32s(-1, 66, PrimitiveType.SINT32));
        assertFunction2((x, y)->(byte)(((0<=y) && (8>y))?x<<y:0), "shift_left_int8", PrimitiveType.SINT8, SINT8S, int32s(-1, 10, PrimitiveType.SINT32));
        assertFunction2((x, y)->(short)(((0<=y) && (16>y))?x<<y:0), "shift_left_uint16", PrimitiveType.UINT16, UINT16S, int32s(-1, 18, PrimitiveType.UINT32));
        assertFunction2((x, y)->x<<(y&0x1f), "shift_left_uint32", PrimitiveType.UINT32, UINT32S, int32s(-1, 34, PrimitiveType.UINT32));
        assertFunction2((x, y)->x<<(y&0x3f), "shift_left_uint64", PrimitiveType.UINT64, UINT64S, int32s(-1, 66, PrimitiveType.UINT32));
        assertFunction2((x, y)->(byte)(((0<=y) && (8>y))?x<<y:0), "shift_left_uint8", PrimitiveType.UINT8, UINT8S, int32s(-1, 10, PrimitiveType.UINT32));
    }

    @Test
    public void testShiftRight() throws Throwable {
        assertFunction2((x, y)->(short)(((0<=y) && (16>y))?x>>y:((0>x)?-1:0)), "shift_right_int16", PrimitiveType.SINT16, SINT16S, int32s(-1, 18, PrimitiveType.SINT32));
        assertFunction2((x, y)->x>>(y&0x1f), "shift_right_int32", PrimitiveType.SINT32, SINT32S, int32s(-1, 34, PrimitiveType.SINT32));
        assertFunction2((x, y)->x>>(y&0x3f), "shift_right_int64", PrimitiveType.SINT64, SINT64S, int32s(-1, 66, PrimitiveType.SINT32));
        assertFunction2((x, y)->(byte)(((0<=y) && (8>y))?x>>y:((0>x)?-1:0)), "shift_right_int8", PrimitiveType.SINT8, SINT8S, int32s(-1, 10, PrimitiveType.SINT32));
        assertFunction2((x, y)->(short)((x&0xffff) >>> (y&0x1f)), "shift_right_uint16", PrimitiveType.UINT16, UINT16S, int32s(-1, 18, PrimitiveType.UINT32));
        assertFunction2((x, y)->x >>> (y&0x1f), "shift_right_uint32", PrimitiveType.UINT32, UINT32S, int32s(-1, 34, PrimitiveType.UINT32));
        assertFunction2((x, y)->x >>> (y&0x3f), "shift_right_uint64", PrimitiveType.UINT64, UINT64S, int32s(-1, 66, PrimitiveType.UINT32));
        assertFunction2((x, y)->(byte)((x&0xff) >>> (y&0x1f)), "shift_right_uint8", PrimitiveType.UINT8, UINT8S, int32s(-1, 10, PrimitiveType.UINT32));
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
                PrimitiveType.UINT64,
                map.keySet()
                        .stream()
                        .map(PrimitiveValue::uint32)
                        .toList());
    }

    @Test
    public void testStackOverflow() throws Throwable {
        try {
            callFunction(true, "stack_overflow", PrimitiveType.VOID, PrimitiveValue.uint64(1L<<48));
            emulator.run();
            fail();
        }
        catch (StackOverflowException ignore) {
        }
    }

    @Test
    public void testSubtract() throws Throwable {
        assertFunction2((x, y)->x-y, "subtract_double", PrimitiveType.DFLOAT, DOUBLES, DOUBLES);
        assertFunction2((x, y)->x-y, "subtract_float", PrimitiveType.SFLOAT, FLOATS, FLOATS);
        assertFunction2((x, y)->(short)(x-y), "subtract_int16", PrimitiveType.SINT16, SINT16S, SINT16S);
        assertFunction2((x, y)->x-y, "subtract_int32", PrimitiveType.SINT32, SINT32S, SINT32S);
        assertFunction2((x, y)->x-y, "subtract_int64", PrimitiveType.SINT64, SINT64S, SINT64S);
        assertFunction2((x, y)->(byte)(x-y), "subtract_int8", PrimitiveType.SINT8, SINT8S, SINT8S);
        assertFunction2((x, y)->(short)(x-y), "subtract_uint16", PrimitiveType.UINT16, UINT16S, UINT16S);
        assertFunction2((x, y)->x-y, "subtract_uint32", PrimitiveType.UINT32, UINT32S, UINT32S);
        assertFunction2((x, y)->x-y, "subtract_uint64", PrimitiveType.UINT64, UINT64S, UINT64S);
        assertFunction2((x, y)->(byte)(x-y), "subtract_uint8", PrimitiveType.UINT8, UINT8S, UINT8S);
    }

    @Test
    public void testXorBitwise() throws Throwable {
        assertFunction2((x, y)->(short)(x^y), "xor_bitwise_int16", PrimitiveType.SINT16, SINT16S, SINT16S);
        assertFunction2((x, y)->x^y, "xor_bitwise_int32", PrimitiveType.SINT32, SINT32S, SINT32S);
        assertFunction2((x, y)->x^y, "xor_bitwise_int64", PrimitiveType.SINT64, SINT64S, SINT64S);
        assertFunction2((x, y)->(byte)(x^y), "xor_bitwise_int8", PrimitiveType.SINT8, SINT8S, SINT8S);
        assertFunction2((x, y)->(short)(x^y), "xor_bitwise_uint16", PrimitiveType.UINT16, UINT16S, UINT16S);
        assertFunction2((x, y)->x^y, "xor_bitwise_uint32", PrimitiveType.UINT32, UINT32S, UINT32S);
        assertFunction2((x, y)->x^y, "xor_bitwise_uint64", PrimitiveType.UINT64, UINT64S, UINT64S);
        assertFunction2((x, y)->(byte)(x^y), "xor_bitwise_uint8", PrimitiveType.UINT8, UINT8S, UINT8S);
    }
}
