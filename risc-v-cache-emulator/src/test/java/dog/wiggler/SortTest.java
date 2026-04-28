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
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ParameterizedClass
@MethodSource("parameters")
@TestMethodOrder(MethodOrderer.MethodName.class)
public class SortTest {
    private final @NotNull String executableImageOption;
    private final @NotNull String imageName;
    private final long seed;

    public SortTest(
            @NotNull String executableImageOption,
            @NotNull String imageName,
            long seed) {
        this.executableImageOption=executableImageOption;
        this.imageName=imageName;
        this.seed=seed;
    }

    public static @NotNull Stream<@NotNull Arguments> parameters() {
        @NotNull List<@NotNull Arguments> result=new ArrayList<>();
        for (var executableImageOption: EmulatorTests.EXECUTABLE_IMAGE_OPTIONS) {
            for (var imageName: List.of(
                    "funnelsort",
                    "mergesort")) {
                for (var seed: LongStream.range(1, 33).toArray()) {
                    result.add(Arguments.of(executableImageOption, imageName, seed));
                }
            }
        }
        return result.stream();
    }

    @Test
    public void test() throws Throwable {
        var random=new Random(seed);
        int size=random.nextInt(0, 1024);
        var input=new ArrayDeque<@NotNull Number>(1+size);
        input.add((long)size);
        long[] array=new long[size];
        for (int ii=0; size>ii; ++ii) {
            long ll=random.nextInt(3*size+1);
            array[ii]=ll;
            input.add(ll);
        }
        var expectedOutput=new ArrayList<@NotNull Number>(size);
        Arrays.sort(array);
        for (var value: array) {
            expectedOutput.add(value);
        }
        var actualOutput=new ArrayList<@NotNull Number>(expectedOutput.size());
        try (var emulator=Emulator.factory(
                        Input.supplier(input::removeFirst),
                        Log::noOp,
                        MemoryMappedMemory.factory(false, 1L<<24),
                        Output.consumer(actualOutput::add))
                .get()) {
            emulator.loadELFAndReset(EmulatorTests.imagePath(executableImageOption, imageName));
            emulator.run();
            assertEquals(0, emulator.exit.code());
        }
        assertEquals(expectedOutput, actualOutput);
    }
}
