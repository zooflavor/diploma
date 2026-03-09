package dog.wiggler.riscv64;

public abstract class Instructions {
    private final Instruction[] instructions=new Instruction[128];

    public Instructions() {
    }

    protected void add(int opcode, Instruction.Type instruction) {
        if (null!=instructions[opcode]) {
            throw new IllegalStateException(String.format("opcode %02x is already defined", opcode));
        }
        instructions[opcode]=instruction.instruction();
    }

    public Instruction getInstruction(int opcode) {
        return instructions[opcode];
    }
}
