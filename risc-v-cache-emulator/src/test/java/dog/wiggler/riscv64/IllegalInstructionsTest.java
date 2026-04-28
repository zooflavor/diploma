package dog.wiggler.riscv64;

import dog.wiggler.emulator.Emulator;
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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * These are to push the coverage of the tests.
 */
@ParameterizedClass
@MethodSource("parameters")
@TestMethodOrder(MethodOrderer.MethodName.class)
public class IllegalInstructionsTest {
    private final int illegalInstruction;

    public IllegalInstructionsTest(int illegalInstruction) {
        this.illegalInstruction=illegalInstruction;
    }

    public static @NotNull Stream<@NotNull Arguments> parameters() {
        @NotNull List<@NotNull Arguments> result=new ArrayList<>();
        result.add(Arguments.of(0x00000000)); // Instructions - nothing for opcode 0x00
        result.add(Arguments.of(0xffffff83)); // RV64IMFD - opcode 0x03
        result.add(Arguments.of(0xffffff87)); // RV64IMFD - opcode 0x07
        result.add(Arguments.of(0xffff9f93)); // RV64IMFD - opcode 0x13 - slli, shift amount too large
        result.add(Arguments.of(0xffffdf93)); // RV64IMFD - opcode 0x13 - invalid shift right
        result.add(Arguments.of(0xffff9f9b)); // RV64IMFD - opcode 0x1b - slliw, shift amount too large
        result.add(Arguments.of(0xffffdf9b)); // RV64IMFD - opcode 0x1b - invalid shift right
        result.add(Arguments.of(0xffffef9b)); // RV64IMFD - opcode 0x1b - funct3=6
        result.add(Arguments.of(0xffffffa3)); // RV64IMFD - opcode 0x23
        result.add(Arguments.of(0xffffffa7)); // RV64IMFD - opcode 0x27
        result.add(Arguments.of(0xffff8fb3)); // RV64IMFD - opcode 0x33 - funct3=0
        result.add(Arguments.of(0xffff9fb3)); // RV64IMFD - opcode 0x33 - funct3=1
        result.add(Arguments.of(0xffffafb3)); // RV64IMFD - opcode 0x33 - funct3=2
        result.add(Arguments.of(0xffffbfb3)); // RV64IMFD - opcode 0x33 - funct3=3
        result.add(Arguments.of(0xffffcfb3)); // RV64IMFD - opcode 0x33 - funct3=4
        result.add(Arguments.of(0xffffdfb3)); // RV64IMFD - opcode 0x33 - funct3=5
        result.add(Arguments.of(0xffffefb3)); // RV64IMFD - opcode 0x33 - funct3=6
        result.add(Arguments.of(0xffffffb3)); // RV64IMFD - opcode 0x33 - funct3=7
        result.add(Arguments.of(0xffff8fbb)); // RV64IMFD - opcode 0x3b - funct3=0
        result.add(Arguments.of(0xffff9fbb)); // RV64IMFD - opcode 0x3b - funct3=1
        result.add(Arguments.of(0xffffafbb)); // RV64IMFD - opcode 0x3b - funct3=2
        result.add(Arguments.of(0xffffcfbb)); // RV64IMFD - opcode 0x3b - funct3=4
        result.add(Arguments.of(0xffffdfbb)); // RV64IMFD - opcode 0x3b - funct3=5
        result.add(Arguments.of(0xffffefbb)); // RV64IMFD - opcode 0x3b - funct3=6
        result.add(Arguments.of(0xffffffbb)); // RV64IMFD - opcode 0x3b - funct3=7
        result.add(Arguments.of(0xffffffc3)); // RV64IMFD - opcode 0x43
        result.add(Arguments.of(0x05ffffd3)); // RV64IMFD - opcode 0x53 - funct7=0x02
        result.add(Arguments.of(0x21ffffd3)); // RV64IMFD - opcode 0x53 - funct7=0x10
        result.add(Arguments.of(0x23ffffd3)); // RV64IMFD - opcode 0x53 - funct7=0x11
        result.add(Arguments.of(0x41ffffd3)); // RV64IMFD - opcode 0x53 - funct7=0x20
        result.add(Arguments.of(0x43ffffd3)); // RV64IMFD - opcode 0x53 - funct7=0x21
        result.add(Arguments.of(0xa1ffffd3)); // RV64IMFD - opcode 0x53 - funct7=0x50
        result.add(Arguments.of(0xa3ffffd3)); // RV64IMFD - opcode 0x53 - funct7=0x51
        result.add(Arguments.of(0xc1ffffd3)); // RV64IMFD - opcode 0x53 - funct7=0x60
        result.add(Arguments.of(0xc3ffffd3)); // RV64IMFD - opcode 0x53 - funct7=0x61
        result.add(Arguments.of(0xd1ffffd3)); // RV64IMFD - opcode 0x53 - funct7=0x68
        result.add(Arguments.of(0xd3ffffd3)); // RV64IMFD - opcode 0x53 - funct7=0x69
        result.add(Arguments.of(0xf1ffffd3)); // RV64IMFD - opcode 0x53 - funct7=0x78
        result.add(Arguments.of(0xf3ffffd3)); // RV64IMFD - opcode 0x53 - funct7=0x79
        result.add(Arguments.of(0xffffafe3)); // RV64IMFD - opcode 0x63 - funct3=2
        result.add(Arguments.of(0xffffafe7)); // RV64IMFD - opcode 0x67 - funct3=2
        return result.stream();
    }

    @Test
    public void test() throws Throwable {
        try (var emulator=Emulator.factory(
                        Input.empty(),
                        Log::noOp,
                        MemoryMappedMemory.factory(false, 1L<<20),
                        Output.refuse())
                .get()) {
            emulator.reset();
            long address=emulator.hart.getPc();
            emulator.memoryLog.storeInt32(address, illegalInstruction);
            try {
                emulator.run();
                fail();
            }
            catch (IllegalInstructionException ex) {
                assertTrue(
                        (null!=ex.getMessage())
                                && ex.getMessage().startsWith(
                                "illegal instruction 0x%08x at 0x%012x,"
                                        .formatted(illegalInstruction, address)),
                        ex.getMessage());
            }
        }
    }
}
