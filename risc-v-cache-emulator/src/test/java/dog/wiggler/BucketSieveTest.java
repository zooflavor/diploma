package dog.wiggler;

import dog.wiggler.emulator.Emulator;
import dog.wiggler.emulator.EmulatorTests;
import dog.wiggler.emulator.Input;
import dog.wiggler.emulator.Output;
import dog.wiggler.memory.Log;
import dog.wiggler.memory.MemoryMappedMemory;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests the bucket sieve C code.
 */
@ParameterizedClass
@MethodSource("parameters")
@TestMethodOrder(MethodOrderer.MethodName.class)
public class BucketSieveTest {
    private static final @NotNull String IMAGE_AWARE="bucket-sieve-cache-aware";
    private static final @NotNull String IMAGE_OBLIVIOUS="bucket-sieve-cache-oblivious";

    private final int base;
    private final @NotNull String executableImageOption;
    private final int height;
    private final boolean oblivious;

    public BucketSieveTest(
            int base,
            @NotNull String executableImageOption,
            int height,
            boolean oblivious) {
        this.base=base;
        this.executableImageOption=executableImageOption;
        this.height=height;
        this.oblivious=oblivious;
    }

    /**
     * Vanilla sieve of Eratosthenes.
     */
    private @NotNull List<@NotNull Long> expectedPrimes() {
        int end=1;
        for (int ii=height; 0<ii; --ii) {
            end*=base;
        }
        boolean[] composites=new boolean[end];
        for (int ii=2; end>ii*ii; ++ii) {
            if (!composites[ii]) {
                for (int jj=ii*ii; end>jj; jj+=ii) {
                    composites[jj]=true;
                }
            }
        }
        @NotNull List<@NotNull Long> result=new ArrayList<>();
        for (int ii=2; end>ii; ++ii) {
            if (!composites[ii]) {
                result.add((long)ii);
            }
        }
        return result;
    }

    public static @NotNull Stream<@NotNull Arguments> parameters() {
        @NotNull List<@NotNull Arguments> result=new ArrayList<>();
        for (var executableImageOption: EmulatorTests.EXECUTABLE_IMAGE_OPTIONS) {
            for (var height: List.of(2, 4, 8, 10, 11)) {
                result.add(Arguments.of(2, executableImageOption, height, false));
                result.add(Arguments.of(2, executableImageOption, height, true));
            }
            for (var height: List.of(2, 4, 8)) {
                result.add(Arguments.of(3, executableImageOption, height, false));
            }
            for (var height: List.of(2, 4)) {
                result.add(Arguments.of(5, executableImageOption, height, false));
            }
            result.add(Arguments.of(8, executableImageOption, 3, false));
        }
        return result.stream();
    }

    @Test
    public void test() throws Throwable {
        @NotNull Deque<@NotNull Number> input=new ArrayDeque<>();
        if (oblivious) {
            input.addLast((long)height);
            input.addLast(1L<<23); // array size
        }
        else {
            input.addLast((long)base);
            input.addLast((long)height);
            input.addLast(4096L); // page size
        }
        var actualPrimes=new ArrayList<@NotNull Number>();
        try (var emulator=Emulator.factory(
                        Input.supplier(input::removeFirst),
                        Log::noOp,
                        MemoryMappedMemory.factory(false, 1L<<24),
                        Output.consumer(actualPrimes::add))
                .get()) {
            emulator.loadELFAndReset(
                    EmulatorTests.imagePath(
                            executableImageOption,
                            oblivious
                                    ?IMAGE_OBLIVIOUS
                                    :IMAGE_AWARE));
            emulator.run();
            assertEquals(0, emulator.exit.code());
        }
        assertEquals(expectedPrimes(), actualPrimes);
    }
}
