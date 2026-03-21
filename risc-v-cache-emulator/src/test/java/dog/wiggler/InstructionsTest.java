package dog.wiggler;

import dog.wiggler.riscv64.Instruction;
import dog.wiggler.riscv64.Instructions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class InstructionsTest {
    @Test
    public void testDuplicateOpcode() {
        try {
            Instruction.Type instructionType=()->
                    (hart, heapAndStack, instruction, memory, opcode)->
                            fail();
            Instructions.builder()
                    .add(0, instructionType)
                    .add(0, instructionType);
            fail();
        }
        catch (IllegalStateException ex) {
            assertEquals("opcode 00 is already defined", ex.getMessage());
        }
    }
}
