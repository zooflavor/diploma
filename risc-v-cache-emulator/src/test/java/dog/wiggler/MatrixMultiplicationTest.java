package dog.wiggler;

import dog.wiggler.memory.Log;
import dog.wiggler.memory.MemoryMappedMemory;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests the matrix multiplication C code.
 */
@ParameterizedClass
@MethodSource("parameters")
public class MatrixMultiplicationTest {
    private final @NotNull String executableImageOption;
    private final @NotNull String imageName;
    private final long seed;

    public MatrixMultiplicationTest(
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
                    "matrix-multiplication-by-definition",
                    "matrix-multiplication-halving",
                    "matrix-multiplication-power-of-2")) {
                for (var seed: LongStream.range(1, 17).toArray()) {
                    result.add(Arguments.of(executableImageOption, imageName, seed));
                }
            }
        }
        return result.stream();
    }

    @Test
    public void test() throws Throwable {
        var random=new Random(seed);
        int size0=random.nextInt(1, 17);
        int size1=random.nextInt(1, 17);
        int size2=random.nextInt(1, 17);
        var input=new ArrayDeque<@NotNull Number>(3+size0*size1+size1*size2);
        input.add((long)size0);
        input.add((long)size1);
        input.add((long)size2);
        double[][] matrix0=new double[size0][size1];
        double[][] matrix1=new double[size1][size2];
        for (int rr=0; size0>rr; ++rr) {
            for (int cc=0; size1>cc; ++cc) {
                double dd=128.0*random.nextDouble()-64.0;
                matrix0[rr][cc]=dd;
                input.add(dd);
            }
        }
        for (int rr=0; size1>rr; ++rr) {
            for (int cc=0; size2>cc; ++cc) {
                double dd=128.0*random.nextDouble()-64.0;
                matrix1[rr][cc]=dd;
                input.add(dd);
            }
        }
        var expectedOutput=new ArrayList<@NotNull Number>(size0*size2);
        for (int rr=0; size0>rr; ++rr) {
            for (int cc=0; size2>cc; ++cc) {
                double dd=0.0;
                for (int ii=0; size1>ii; ++ii) {
                    dd+=matrix0[rr][ii]*matrix1[ii][cc];
                }
                expectedOutput.add(dd);
            }
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
