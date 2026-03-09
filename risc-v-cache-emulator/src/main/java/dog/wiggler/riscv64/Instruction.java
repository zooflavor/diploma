package dog.wiggler.riscv64;

import dog.wiggler.memory.Memory;

@FunctionalInterface
public interface Instruction {
    @FunctionalInterface
    interface BType extends Type {
        void execute(
                int funct3, Hart hart, int imm, int instruction, Memory memory, int opcode, int rs1, int rs2)
                throws Throwable;

        default Instruction instruction() {
            return (hart, instruction, memory, opcode)->execute(
                    (instruction>>12)&7,
                    hart,
                    ((instruction&0x80000000)>>19)
                            |((instruction&0x80)<<4)
                            |((instruction&0x7e000000) >>> 20)
                            |((instruction&0xf00) >>> 7),
                    instruction,
                    memory,
                    opcode,
                    (instruction>>15)&0x1f,
                    (instruction>>20)&0x1f);
        }
    }

    @FunctionalInterface
    interface IType extends Type {
        void execute(
                int funct3, Hart hart, int imm, int instruction, Memory memory, int opcode, int rd, int rs1)
                throws Throwable;

        default Instruction instruction() {
            return (hart, instruction, memory, opcode)->execute(
                    (instruction>>12)&7,
                    hart,
                    instruction>>20,
                    instruction,
                    memory,
                    opcode,
                    (instruction>>7)&0x1f,
                    (instruction>>15)&0x1f);
        }
    }

    @FunctionalInterface
    interface MAType extends Type {
        void execute(
                int fmt, Hart hart, int instruction, Memory memory,
                int opcode, int rd, int rm, int rs1, int rs2, int rs3) throws Throwable;

        @Override
        default Instruction instruction() {
            return (hart, instruction, memory, opcode)->execute(
                    (instruction >>> 25)&3,
                    hart,
                    instruction,
                    memory,
                    opcode,
                    (instruction>>7)&0x1f,
                    (instruction>>12)&7,
                    (instruction>>15)&0x1f,
                    (instruction>>20)&0x1f,
                    instruction >>> 27);
        }
    }

    @FunctionalInterface
    interface JType extends Type {
        void execute(
                Hart hart, int imm, int instruction, Memory memory, int opcode, int rd)
                throws Throwable;

        default Instruction instruction() {
            return (hart, instruction, memory, opcode)->execute(
                    hart,
                    ((instruction&0x80000000)>>11)
                            |(instruction&0xff000)
                            |((instruction&0x100000)>>>9)
                            |((instruction&0x7fe00000)>>>20),
                    instruction,
                    memory,
                    opcode,
                    (instruction>>7)&0x1f);
        }
    }

    @FunctionalInterface
    interface RType extends Type {
        void execute(
                int funct3, int funct7, Hart hart, int instruction, Memory memory,
                int opcode, int rd, int rs1, int rs2) throws Throwable;

        @Override
        default Instruction instruction() {
            return (hart, instruction, memory, opcode)->execute(
                    (instruction>>12)&7,
                    instruction >>> 25,
                    hart,
                    instruction,
                    memory,
                    opcode,
                    (instruction>>7)&0x1f,
                    (instruction>>15)&0x1f,
                    (instruction>>20)&0x1f);
        }
    }

    @FunctionalInterface
    interface SType extends Type {
        void execute(
                int funct3, Hart hart, int imm, int instruction, Memory memory, int opcode, int rs1, int rs2)
                throws Throwable;

        default Instruction instruction() {
            return (hart, instruction, memory, opcode)->execute(
                    (instruction>>12)&7,
                    hart,
                    ((instruction&0xfe000000)>>20)|((instruction>>7)&0x1f),
                    instruction,
                    memory,
                    opcode,
                    (instruction>>15)&0x1f,
                    (instruction>>20)&0x1f);
        }
    }

    @FunctionalInterface
    interface Type {
        Instruction instruction();
    }

    @FunctionalInterface
    interface UType extends Type {
        void execute(
                Hart hart, int imm, int instruction, Memory memory, int opcode, int rd)
                throws Throwable;

        default Instruction instruction() {
            return (hart, instruction, memory, opcode)->execute(
                    hart,
                    instruction&0xfffff000,
                    instruction,
                    memory,
                    opcode,
                    (instruction>>7)&0x1f);
        }
    }

    void execute(Hart hart, int instruction, Memory memory, int opcode) throws Throwable;
}
