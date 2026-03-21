package dog.wiggler.riscv64;

import dog.wiggler.Casts;
import org.jetbrains.annotations.NotNull;

/**
 * Implementation of the instructions for RV64IMFD.
 * 64 bits,
 * integers,
 * multiplication and division,
 * single-precision floating-point,
 * and double-precision floating-point.
 */
public class RV64IMFD {
    public static @NotNull Instructions create() {
        return Instructions.builder()
                .add(0x03, opcode03())
                .add(0x07, opcode07())
                .add(0x13, opcode13())
                .add(0x17, opcode17())
                .add(0x1b, opcode1b())
                .add(0x23, opcode23())
                .add(0x27, opcode27())
                .add(0x33, opcode33())
                .add(0x37, opcode37())
                .add(0x3b, opcode3b())
                .add(0x43, opcode43())
                .add(0x53, opcode53())
                .add(0x63, opcode63())
                .add(0x67, opcode67())
                .add(0x6f, opcode6f())
                .build();
    }

    private static @NotNull Instruction.IType opcode03() {
        return (funct3, hart, heapAndStack, imm, instruction, memory, opcode, rd, rs1)->{
            switch (funct3) {
                // LB
                case 0 ->
                        hart.xRegisters.setInt64(heapAndStack, rd, memory.loadInt8(hart.xRegisters.getInt64(rs1)+imm));
                // LH
                case 1 ->
                        hart.xRegisters.setInt64(heapAndStack, rd, memory.loadInt16(hart.xRegisters.getInt64(rs1)+imm));
                // LW
                case 2 ->
                        hart.xRegisters.setInt64(heapAndStack, rd, memory.loadInt32(hart.xRegisters.getInt64(rs1)+imm, false));
                // LD
                case 3 ->
                        hart.xRegisters.setInt64(heapAndStack, rd, memory.loadInt64(hart.xRegisters.getInt64(rs1)+imm));
                // LBU
                case 4 ->
                        hart.xRegisters.setInt64(heapAndStack, rd, memory.loadInt8(hart.xRegisters.getInt64(rs1)+imm)&0xffL);
                // LHU
                case 5 ->
                        hart.xRegisters.setInt64(heapAndStack, rd, memory.loadInt16(hart.xRegisters.getInt64(rs1)+imm)&0xffffL);
                // LWU
                case 6 ->
                        hart.xRegisters.setInt64(heapAndStack, rd, memory.loadInt32(hart.xRegisters.getInt64(rs1)+imm, false)&0xffffffffL);
                default -> throw new IllegalInstructionException(
                        "illegal instruction 0x%08x at 0x%012x, opcode: 0x%02x, funct3=%x"
                                .formatted(instruction, hart.getPc(), opcode, funct3));

            }
            hart.incPc();
        };
    }

    private static @NotNull Instruction.IType opcode07() {
        return (funct3, hart, heapAndStack, imm, instruction, memory, opcode, rd, rs1)->{
            switch (funct3) {
                // FLW
                case 2 ->
                        hart.fxRegisters.setInt32(heapAndStack, rd, memory.loadInt32(hart.xRegisters.getInt64(rs1)+imm, false));
                // FLD
                case 3 ->
                        hart.fxRegisters.setInt64(heapAndStack, rd, memory.loadInt64(hart.xRegisters.getInt64(rs1)+imm));
                default -> throw new IllegalInstructionException(
                        "illegal instruction 0x%08x at 0x%012x, opcode: 0x%02x, funct3=%x"
                                .formatted(instruction, hart.getPc(), opcode, funct3));
            }
            hart.incPc();
        };
    }

    public static @NotNull Instruction.IType opcode13() {
        return (funct3, hart, heapAndStack, imm, instruction, memory, opcode, rd, rs1)->{
            switch (funct3) {
                // ADDI
                case 0 -> hart.xRegisters.setInt64(heapAndStack, rd, hart.xRegisters.getInt64(rs1)+imm);
                // SLLI
                case 1 -> {
                    if (0!=(imm&0xfc0)) {
                        throw new IllegalInstructionException(
                                "illegal instruction 0x%08x at 0x%012x, opcode: 0x%02x, funct3=%x, imm=%03x"
                                        .formatted(instruction, hart.getPc(), opcode, funct3, imm));
                    }
                    hart.xRegisters.setInt64(heapAndStack, rd, hart.xRegisters.getInt64(rs1)<<(imm&0x3f));
                }
                //SLTIU
                case 3 ->
                        hart.xRegisters.setInt64(heapAndStack, rd, (0>Long.compareUnsigned(hart.xRegisters.getInt64(rs1), imm))?1L:0L);
                // XORI
                case 4 -> hart.xRegisters.setInt64(heapAndStack, rd, hart.xRegisters.getInt64(rs1)^imm);
                case 5 -> {
                    switch (imm&0xfc0) {
                        // SRLI
                        case 0 ->
                                hart.xRegisters.setInt64(heapAndStack, rd, hart.xRegisters.getInt64(rs1) >>> (imm&0x3f));
                        // SRAI
                        case 0x400 ->
                                hart.xRegisters.setInt64(heapAndStack, rd, hart.xRegisters.getInt64(rs1)>>(imm&0x3f));
                        default -> throw new IllegalInstructionException(
                                "illegal instruction 0x%08x at 0x%012x, opcode: 0x%02x, funct3=%x, imm=%03x"
                                        .formatted(instruction, hart.getPc(), opcode, funct3, imm));
                    }
                }
                // ANDI
                case 7 -> hart.xRegisters.setInt64(heapAndStack, rd, hart.xRegisters.getInt64(rs1)&imm);
                default -> throw new IllegalInstructionException(
                        "illegal instruction 0x%08x at 0x%012x, opcode: 0x%02x, funct3=%x"
                                .formatted(instruction, hart.getPc(), opcode, funct3));

            }
            hart.incPc();
        };
    }

    private static @NotNull Instruction.UType opcode17() {
        return (hart, heapAndStack, imm, rd)->{
            // AUIPC
            hart.xRegisters.setInt64(heapAndStack, rd, hart.getPc()+imm);
            hart.incPc();
        };
    }

    private static @NotNull Instruction.IType opcode1b() {
        return (funct3, hart, heapAndStack, imm, instruction, memory, opcode, rd, rs1)->{
            switch (funct3) {
                // ADDIW
                case 0 -> hart.xRegisters.setInt64(heapAndStack, rd, (int)hart.xRegisters.getInt64(rs1)+imm);
                // SLLIW
                case 1 -> {
                    if (0!=(imm&0xfe0)) {
                        throw new IllegalInstructionException(
                                "illegal instruction 0x%08x at 0x%012x, opcode: 0x%02x, funct3=%x, imm=%03x"
                                        .formatted(instruction, hart.getPc(), opcode, funct3, imm));
                    }
                    int result=hart.xRegisters.getInt32(rs1)<<(imm&0x1f);
                    hart.xRegisters.setInt64(heapAndStack, rd, result);
                }
                case 5 -> {
                    switch (imm&0xfe0) {
                        // SRLIW
                        case 0x000 ->
                                hart.xRegisters.setInt64(heapAndStack, rd, hart.xRegisters.getInt32(rs1) >>> (imm&0x1f));
                        // SRAIW
                        case 0x400 ->
                                hart.xRegisters.setInt64(heapAndStack, rd, hart.xRegisters.getInt32(rs1)>>(imm&0x1f));
                        default -> throw new IllegalInstructionException(
                                "illegal instruction 0x%08x at 0x%012x, opcode: 0x%02x, funct3=%x, imm=%03x"
                                        .formatted(instruction, hart.getPc(), opcode, funct3, imm));
                    }
                }
                default -> throw new IllegalInstructionException(
                        "illegal instruction 0x%08x at 0x%012x, opcode: 0x%02x, funct3=%x"
                                .formatted(instruction, hart.getPc(), opcode, funct3));

            }
            hart.incPc();
        };
    }

    private static @NotNull Instruction.SType opcode23() {
        return (funct3, hart, imm, instruction, memory, opcode, rs1, rs2)->{
            switch (funct3) {
                // SB
                case 0 -> memory.storeInt8(hart.xRegisters.getInt64(rs1)+imm, hart.xRegisters.getInt8(rs2));
                // SH
                case 1 -> memory.storeInt16(hart.xRegisters.getInt64(rs1)+imm, hart.xRegisters.getInt16(rs2));
                // SW
                case 2 -> memory.storeInt32(hart.xRegisters.getInt64(rs1)+imm, hart.xRegisters.getInt32(rs2));
                // SD
                case 3 -> memory.storeInt64(hart.xRegisters.getInt64(rs1)+imm, hart.xRegisters.getInt64(rs2));
                default -> throw new IllegalInstructionException(
                        "illegal instruction 0x%08x at 0x%012x, opcode: 0x%02x, funct3=%x"
                                .formatted(instruction, hart.getPc(), opcode, funct3));

            }
            hart.incPc();
        };
    }

    private static @NotNull Instruction.SType opcode27() {
        return (funct3, hart, imm, instruction, memory, opcode, rs1, rs2)->{
            switch (funct3) {
                // FSW
                case 2 -> memory.storeInt32(hart.xRegisters.getInt64(rs1)+imm, hart.fxRegisters.getInt32(rs2));
                // FSD
                case 3 -> memory.storeInt64(hart.xRegisters.getInt64(rs1)+imm, hart.fxRegisters.getInt64(rs2));
                default -> throw new IllegalInstructionException(
                        "illegal instruction 0x%08x at 0x%012x, opcode: 0x%02x, funct3=%x"
                                .formatted(instruction, hart.getPc(), opcode, funct3));
            }
            hart.incPc();
        };
    }

    private static @NotNull Instruction.RType opcode33() {
        return (funct3, funct7, hart, heapAndStack, instruction, opcode, rd, rs1, rs2)->{
            switch (funct3) {
                case 0 -> {
                    switch (funct7) {
                        // ADD
                        case 0 ->
                                hart.xRegisters.setInt64(heapAndStack, rd, hart.xRegisters.getInt64(rs1)+hart.xRegisters.getInt64(rs2));
                        // MUL
                        case 1 ->
                                hart.xRegisters.setInt64(heapAndStack, rd, hart.xRegisters.getInt64(rs1)*hart.xRegisters.getInt64(rs2));
                        // SUB
                        case 0x20 ->
                                hart.xRegisters.setInt64(heapAndStack, rd, hart.xRegisters.getInt64(rs1)-hart.xRegisters.getInt64(rs2));
                        default -> throw new IllegalInstructionException(
                                "illegal instruction 0x%08x at 0x%012x, opcode: 0x%02x, funct3=%x, funct7=%02x"
                                        .formatted(instruction, hart.getPc(), opcode, funct3, funct7));
                    }
                }
                // SLL
                case 1 -> {
                    if (0!=funct7) {
                        throw new IllegalInstructionException(
                                "illegal instruction 0x%08x at 0x%012x, opcode: 0x%02x, funct3=%x, funct7=%02x"
                                        .formatted(instruction, hart.getPc(), opcode, funct3, funct7));
                    }
                    hart.xRegisters.setInt64(heapAndStack, rd, hart.xRegisters.getInt64(rs1)<<(hart.xRegisters.getInt32(rs2)&0x3f));
                }
                //SLT
                case 2 -> {
                    if (0!=funct7) {
                        throw new IllegalInstructionException(
                                "illegal instruction 0x%08x at 0x%012x, opcode: 0x%02x, funct3=%x, funct7=%02x"
                                        .formatted(instruction, hart.getPc(), opcode, funct3, funct7));
                    }
                    hart.xRegisters.setInt64(
                            heapAndStack,
                            rd,
                            hart.xRegisters.getInt64(rs1)<hart.xRegisters.getInt64(rs2)
                                    ?1L
                                    :0L);
                }
                //SLTU
                case 3 -> {
                    if (0!=funct7) {
                        throw new IllegalInstructionException(
                                "illegal instruction 0x%08x at 0x%012x, opcode: 0x%02x, funct3=%x, funct7=%02x"
                                        .formatted(instruction, hart.getPc(), opcode, funct3, funct7));
                    }
                    hart.xRegisters.setInt64(
                            heapAndStack,
                            rd,
                            (0>Long.compareUnsigned(hart.xRegisters.getInt64(rs1), hart.xRegisters.getInt64(rs2)))
                                    ?1L
                                    :0L);
                }
                case 4 -> {
                    switch (funct7) {
                        // XOR
                        case 0 ->
                                hart.xRegisters.setInt64(heapAndStack, rd, hart.xRegisters.getInt64(rs1)^hart.xRegisters.getInt64(rs2));
                        // DIV
                        case 1 -> {
                            long dividend=hart.xRegisters.getInt64(rs1);
                            long divisor=hart.xRegisters.getInt64(rs2);
                            long result;
                            if (0==divisor) {
                                result=-1L;
                            }
                            else if ((-1L==divisor) && (Long.MIN_VALUE==dividend)) {
                                result=dividend;
                            }
                            else {
                                result=dividend/divisor;
                            }
                            hart.xRegisters.setInt64(heapAndStack, rd, result);
                        }
                        default -> throw new IllegalInstructionException(
                                "illegal instruction 0x%08x at 0x%012x, opcode: 0x%02x, funct3=%x, funct7=%02x"
                                        .formatted(instruction, hart.getPc(), opcode, funct3, funct7));
                    }
                }
                case 5 -> {
                    switch (funct7) {
                        // SRL
                        case 0 ->
                                hart.xRegisters.setInt64(heapAndStack, rd, hart.xRegisters.getInt64(rs1) >>> (hart.xRegisters.getInt32(rs2)&0x3f));
                        // DIVU
                        case 1 -> {
                            long divisor=hart.xRegisters.getInt64(rs2);
                            hart.xRegisters.setInt64(
                                    heapAndStack,
                                    rd,
                                    (0==divisor)
                                            ?-1
                                            :Long.divideUnsigned(hart.xRegisters.getInt64(rs1), divisor));
                        }
                        // SRA
                        case 0x20 ->
                                hart.xRegisters.setInt64(heapAndStack, rd, hart.xRegisters.getInt64(rs1)>>(hart.xRegisters.getInt32(rs2)&0x3f));
                        default -> throw new IllegalInstructionException(
                                "illegal instruction 0x%08x at 0x%012x, opcode: 0x%02x, funct3=%x, funct7=%02x"
                                        .formatted(instruction, hart.getPc(), opcode, funct3, funct7));
                    }
                }
                case 6 -> {
                    switch (funct7) {
                        // OR
                        case 0 ->
                                hart.xRegisters.setInt64(heapAndStack, rd, hart.xRegisters.getInt64(rs1)|hart.xRegisters.getInt64(rs2));
                        // REM
                        case 1 -> {
                            long dividend=hart.xRegisters.getInt64(rs1);
                            long divisor=hart.xRegisters.getInt64(rs2);
                            long result;
                            if (0==divisor) {
                                result=dividend;
                            }
                            else if ((-1L==divisor) && (Long.MIN_VALUE==dividend)) {
                                result=0L;
                            }
                            else {
                                result=dividend%divisor;
                            }
                            hart.xRegisters.setInt64(heapAndStack, rd, result);
                        }
                        default -> throw new IllegalInstructionException(
                                "illegal instruction 0x%08x at 0x%012x, opcode: 0x%02x, funct3=%x, funct7=%02x"
                                        .formatted(instruction, hart.getPc(), opcode, funct3, funct7));
                    }
                }
                // funct3 has 3 bits, using default instead of case 7 to cover the default branch
                default -> {
                    switch (funct7) {
                        // AND
                        case 0 ->
                                hart.xRegisters.setInt64(heapAndStack, rd, hart.xRegisters.getInt64(rs1)&hart.xRegisters.getInt64(rs2));
                        // REMU
                        case 1 -> {
                            long dividend=hart.xRegisters.getInt64(rs1);
                            long divisor=hart.xRegisters.getInt64(rs2);
                            hart.xRegisters.setInt64(
                                    heapAndStack,
                                    rd,
                                    (0==divisor)
                                            ?dividend
                                            :Long.remainderUnsigned(dividend, divisor));
                        }
                        default -> throw new IllegalInstructionException(
                                "illegal instruction 0x%08x at 0x%012x, opcode: 0x%02x, funct3=%x, funct7=%02x"
                                        .formatted(instruction, hart.getPc(), opcode, funct3, funct7));
                    }
                }
            }
            hart.incPc();
        };
    }

    private static @NotNull Instruction.UType opcode37() {
        // LUI
        return (hart, heapAndStack, imm, rd)->{
            hart.xRegisters.setInt64(heapAndStack, rd, imm);
            hart.incPc();
        };
    }

    private static @NotNull Instruction.RType opcode3b() {
        return (funct3, funct7, hart, heapAndStack, instruction, opcode, rd, rs1, rs2)->{
            switch (funct3) {
                case 0 -> {
                    switch (funct7) {
                        // ADDW
                        case 0 ->
                                hart.xRegisters.setInt32(heapAndStack, rd, hart.xRegisters.getInt32(rs1)+hart.xRegisters.getInt32(rs2));
                        // MULW
                        case 1 ->
                                hart.xRegisters.setInt32(heapAndStack, rd, hart.xRegisters.getInt32(rs1)*hart.xRegisters.getInt32(rs2));
                        // SUBW
                        case 0x20 ->
                                hart.xRegisters.setInt32(heapAndStack, rd, hart.xRegisters.getInt32(rs1)-hart.xRegisters.getInt32(rs2));
                        default -> throw new IllegalInstructionException(
                                "illegal instruction 0x%08x at 0x%012x, opcode: 0x%02x, funct3=%x, funct7=%02x"
                                        .formatted(instruction, hart.getPc(), opcode, funct3, funct7));
                    }
                }
                // SLLW
                case 1 -> {
                    if (0!=funct7) {
                        throw new IllegalInstructionException(
                                "illegal instruction 0x%08x at 0x%012x, opcode: 0x%02x, funct3=%x, funct7=%02x"
                                        .formatted(instruction, hart.getPc(), opcode, funct3, funct7));
                    }
                    int result=hart.xRegisters.getInt32(rs1)<<(hart.xRegisters.getInt32(rs2)&0x1f);
                    hart.xRegisters.setInt64(heapAndStack, rd, result);
                }
                case 4 -> {
                    // noinspection SwitchStatementWithTooFewBranches
                    switch (funct7) {
                        // DIVW
                        case 1 -> {
                            int dividend=hart.xRegisters.getInt32(rs1);
                            int divisor=hart.xRegisters.getInt32(rs2);
                            int result;
                            if (0==divisor) {
                                result=-1;
                            }
                            else if ((-1==divisor) && (Integer.MIN_VALUE==dividend)) {
                                result=dividend;
                            }
                            else {
                                result=dividend/divisor;
                            }
                            hart.xRegisters.setInt32(heapAndStack, rd, result);
                        }
                        default -> throw new IllegalInstructionException(
                                "illegal instruction 0x%08x at 0x%012x, opcode: 0x%02x, funct3=%x, funct7=%02x"
                                        .formatted(instruction, hart.getPc(), opcode, funct3, funct7));
                    }
                }
                case 5 -> {
                    switch (funct7) {
                        // SRLW
                        case 0 ->
                                hart.xRegisters.setInt64(heapAndStack, rd, hart.xRegisters.getInt32(rs1) >>> (hart.xRegisters.getInt32(rs2)&0x1f));
                        // DIVUW
                        case 1 -> {
                            int divisor=hart.xRegisters.getInt32(rs2);
                            hart.xRegisters.setInt32(
                                    heapAndStack,
                                    rd,
                                    (0==divisor)
                                            ?-1
                                            :Integer.divideUnsigned(hart.xRegisters.getInt32(rs1), divisor));
                        }
                        // SRAW
                        case 0x20 ->
                                hart.xRegisters.setInt64(heapAndStack, rd, hart.xRegisters.getInt32(rs1)>>(hart.xRegisters.getInt32(rs2)&0x1f));
                        default -> throw new IllegalInstructionException(
                                "illegal instruction 0x%08x at 0x%012x, opcode: 0x%02x, funct3=%x, funct7=%02x"
                                        .formatted(instruction, hart.getPc(), opcode, funct3, funct7));
                    }
                }
                case 6 -> {
                    // noinspection SwitchStatementWithTooFewBranches
                    switch (funct7) {
                        // REMW
                        case 1 -> {
                            int dividend=hart.xRegisters.getInt32(rs1);
                            int divisor=hart.xRegisters.getInt32(rs2);
                            int result;
                            if (0==divisor) {
                                result=dividend;
                            }
                            else if ((-1==divisor) && (Integer.MIN_VALUE==dividend)) {
                                result=0;
                            }
                            else {
                                result=dividend%divisor;
                            }
                            hart.xRegisters.setInt32(heapAndStack, rd, result);
                        }
                        default -> throw new IllegalInstructionException(
                                "illegal instruction 0x%08x at 0x%012x, opcode: 0x%02x, funct3=%x, funct7=%02x"
                                        .formatted(instruction, hart.getPc(), opcode, funct3, funct7));
                    }
                }
                case 7 -> {
                    // noinspection SwitchStatementWithTooFewBranches
                    switch (funct7) {
                        // REMUW
                        case 1 -> {
                            int dividend=hart.xRegisters.getInt32(rs1);
                            int divisor=hart.xRegisters.getInt32(rs2);
                            hart.xRegisters.setInt32(
                                    heapAndStack,
                                    rd,
                                    (0==divisor)
                                            ?dividend:
                                            Integer.remainderUnsigned(dividend, divisor));
                        }
                        default -> throw new IllegalInstructionException(
                                "illegal instruction 0x%08x at 0x%012x, opcode: 0x%02x, funct3=%x, funct7=%02x"
                                        .formatted(instruction, hart.getPc(), opcode, funct3, funct7));
                    }
                }
                default -> throw new IllegalInstructionException(
                        "illegal instruction 0x%08x at 0x%012x, opcode: 0x%02x, funct3=%x"
                                .formatted(instruction, hart.getPc(), opcode, funct3));

            }
            hart.incPc();
        };
    }

    private static @NotNull Instruction.MAType opcode43() {
        return (fmt, hart, heapAndStack, instruction, opcode, rd, rs1, rs2, rs3)->{
            // noinspection SwitchStatementWithTooFewBranches
            switch (fmt) {
                // FMADD.D
                case 1 -> hart.fxRegisters.setDouble(
                        heapAndStack,
                        rd,
                        hart.fxRegisters.getDouble(rs1)*hart.fxRegisters.getDouble(rs2)
                                +hart.fxRegisters.getDouble(rs3));
                default -> throw new IllegalInstructionException(
                        "illegal instruction 0x%08x at 0x%012x, opcode: 0x%02x, fmt=%x"
                                .formatted(instruction, hart.getPc(), opcode, fmt));
            }
            hart.incPc();
        };
    }

    private static @NotNull Instruction.RType opcode53() {
        return (funct3, funct7, hart, heapAndStack, instruction, opcode, rd, rs1, rs2)->{
            switch (funct7) {
                // FADD.S
                case 0x00 ->
                        hart.fxRegisters.setFloat(heapAndStack, rd, hart.fxRegisters.getFloat(rs1)+hart.fxRegisters.getFloat(rs2));
                // FADD.D
                case 0x01 ->
                        hart.fxRegisters.setDouble(heapAndStack, rd, hart.fxRegisters.getDouble(rs1)+hart.fxRegisters.getDouble(rs2));
                // FSUB.S
                case 0x04 ->
                        hart.fxRegisters.setFloat(heapAndStack, rd, hart.fxRegisters.getFloat(rs1)-hart.fxRegisters.getFloat(rs2));
                // FSUB.D
                case 0x05 ->
                        hart.fxRegisters.setDouble(heapAndStack, rd, hart.fxRegisters.getDouble(rs1)-hart.fxRegisters.getDouble(rs2));
                // FMUL.S
                case 0x08 ->
                        hart.fxRegisters.setFloat(heapAndStack, rd, hart.fxRegisters.getFloat(rs1)*hart.fxRegisters.getFloat(rs2));
                // FMUL.D
                case 0x09 ->
                        hart.fxRegisters.setDouble(heapAndStack, rd, hart.fxRegisters.getDouble(rs1)*hart.fxRegisters.getDouble(rs2));
                // FDIV.S
                case 0x0c ->
                        hart.fxRegisters.setFloat(heapAndStack, rd, hart.fxRegisters.getFloat(rs1)/hart.fxRegisters.getFloat(rs2));
                // FDIV.D
                case 0x0d ->
                        hart.fxRegisters.setDouble(heapAndStack, rd, hart.fxRegisters.getDouble(rs1)/hart.fxRegisters.getDouble(rs2));
                case 0x10 -> {
                    switch (funct3) {
                        // FSGNJ.S
                        case 0 -> hart.fxRegisters.setInt32(
                                heapAndStack,
                                rd,
                                (hart.fxRegisters.getInt32(rs1)&0x7fffffff)
                                        |(hart.fxRegisters.getInt32(rs2)&0x80000000));
                        // FSGNJN.S
                        case 1 -> hart.fxRegisters.setInt32(
                                heapAndStack,
                                rd,
                                (hart.fxRegisters.getInt32(rs1)&0x7fffffff)
                                        |((~hart.fxRegisters.getInt32(rs2))&0x80000000));
                        default -> throw new IllegalInstructionException(
                                "illegal instruction 0x%08x at 0x%012x, opcode: 0x%02x, funct3=%x funct7=0x%02x"
                                        .formatted(instruction, hart.getPc(), opcode, funct3, funct7));
                    }
                }
                case 0x11 -> {
                    switch (funct3) {
                        // FSGNJ.D
                        case 0 -> hart.fxRegisters.setInt64(
                                heapAndStack,
                                rd,
                                (hart.fxRegisters.getInt64(rs1)&0x7fffffffffffffffL)
                                        |(hart.fxRegisters.getInt64(rs2)&0x8000000000000000L));
                        // FSGNJN.D
                        case 1 -> hart.fxRegisters.setInt64(
                                heapAndStack,
                                rd,
                                (hart.fxRegisters.getInt64(rs1)&0x7fffffffffffffffL)
                                        |((~hart.fxRegisters.getInt64(rs2))&0x8000000000000000L));
                        default -> throw new IllegalInstructionException(
                                "illegal instruction 0x%08x at 0x%012x, opcode: 0x%02x, funct3=%x funct7=0x%02x"
                                        .formatted(instruction, hart.getPc(), opcode, funct3, funct7));
                    }
                }
                case 0x20 -> {
                    // noinspection SwitchStatementWithTooFewBranches
                    switch (rs2) {
                        // FCVT.S.D
                        case 1 -> hart.fxRegisters.setFloat(heapAndStack, rd, (float)hart.fxRegisters.getDouble(rs1));
                        default -> throw new IllegalInstructionException(
                                "illegal instruction 0x%08x at 0x%012x, opcode: 0x%02x, funct3=%x funct7=0x%02x, rs2=0x%02x"
                                        .formatted(instruction, hart.getPc(), opcode, funct3, funct7, rs2));
                    }
                }
                case 0x21 -> {
                    // noinspection SwitchStatementWithTooFewBranches
                    switch (rs2) {
                        // FCVT.D.S
                        case 0 -> hart.fxRegisters.setDouble(heapAndStack, rd, hart.fxRegisters.getFloat(rs1));
                        default -> throw new IllegalInstructionException(
                                "illegal instruction 0x%08x at 0x%012x, opcode: 0x%02x, funct3=%x funct7=0x%02x, rs2=0x%02x"
                                        .formatted(instruction, hart.getPc(), opcode, funct3, funct7, rs2));
                    }
                }
                case 0x50 -> {
                    switch (funct3) {
                        // FLE.S
                        case 0 ->
                                hart.xRegisters.setInt64(heapAndStack, rd, (hart.fxRegisters.getFloat(rs1)<=hart.fxRegisters.getFloat(rs2))?1L:0L);
                        // FLT.S
                        case 1 ->
                                hart.xRegisters.setInt64(heapAndStack, rd, (hart.fxRegisters.getFloat(rs1)<hart.fxRegisters.getFloat(rs2))?1L:0L);
                        // FEQ.S
                        case 2 ->
                                hart.xRegisters.setInt64(heapAndStack, rd, (hart.fxRegisters.getFloat(rs1)==hart.fxRegisters.getFloat(rs2))?1L:0L);
                        default -> throw new IllegalInstructionException(
                                "illegal instruction 0x%08x at 0x%012x, opcode: 0x%02x, funct3=%x funct7=0x%02x"
                                        .formatted(instruction, hart.getPc(), opcode, funct3, funct7));
                    }
                }
                case 0x51 -> {
                    switch (funct3) {
                        // FLE.D
                        case 0 ->
                                hart.xRegisters.setInt64(heapAndStack, rd, (hart.fxRegisters.getDouble(rs1)<=hart.fxRegisters.getDouble(rs2))?1L:0L);
                        // FLT.D
                        case 1 ->
                                hart.xRegisters.setInt64(heapAndStack, rd, (hart.fxRegisters.getDouble(rs1)<hart.fxRegisters.getDouble(rs2))?1L:0L);
                        // FEQ.D
                        case 2 ->
                                hart.xRegisters.setInt64(heapAndStack, rd, (hart.fxRegisters.getDouble(rs1)==hart.fxRegisters.getDouble(rs2))?1L:0L);
                        default -> throw new IllegalInstructionException(
                                "illegal instruction 0x%08x at 0x%012x, opcode: 0x%02x, funct3=%x funct7=0x%02x"
                                        .formatted(instruction, hart.getPc(), opcode, funct3, funct7));
                    }
                }
                case 0x60 -> {
                    switch (rs2) {
                        // FCVT.W.S
                        case 0 -> hart.xRegisters.setInt32(heapAndStack, rd, (int)hart.fxRegisters.getFloat(rs1));
                        // FCVT.WU.S
                        case 1 ->
                                hart.xRegisters.setInt32(heapAndStack, rd, Casts.castDoubleToUint32(hart.fxRegisters.getFloat(rs1)));
                        // FCVT.L.S
                        case 2 -> hart.xRegisters.setInt64(heapAndStack, rd, (long)hart.fxRegisters.getFloat(rs1));
                        // FCVT.LU.S
                        case 3 ->
                                hart.xRegisters.setInt64(heapAndStack, rd, Casts.castDoubleToUint64(hart.fxRegisters.getFloat(rs1)));
                        default -> throw new IllegalInstructionException(
                                "illegal instruction 0x%08x at 0x%012x, opcode: 0x%02x, funct3=%x funct7=0x%02x, rs2=0x%02x"
                                        .formatted(instruction, hart.getPc(), opcode, funct3, funct7, rs2));
                    }
                }
                case 0x61 -> {
                    switch (rs2) {
                        // FCVT.W.D
                        case 0 -> hart.xRegisters.setInt32(heapAndStack, rd, (int)hart.fxRegisters.getDouble(rs1));
                        // FCVT.WU.D
                        case 1 ->
                                hart.xRegisters.setInt32(heapAndStack, rd, Casts.castDoubleToUint32(hart.fxRegisters.getDouble(rs1)));
                        // FCVT.L.D
                        case 2 -> hart.xRegisters.setInt64(heapAndStack, rd, (long)hart.fxRegisters.getDouble(rs1));
                        // FCVT.LU.D
                        case 3 ->
                                hart.xRegisters.setInt64(heapAndStack, rd, Casts.castDoubleToUint64(hart.fxRegisters.getDouble(rs1)));
                        default -> throw new IllegalInstructionException(
                                "illegal instruction 0x%08x at 0x%012x, opcode: 0x%02x, funct3=%x funct7=0x%02x, rs2=0x%02x"
                                        .formatted(instruction, hart.getPc(), opcode, funct3, funct7, rs2));
                    }
                }
                case 0x68 -> {
                    switch (rs2) {
                        // FCVT.S.W
                        case 0 -> hart.fxRegisters.setFloat(heapAndStack, rd, hart.xRegisters.getInt32(rs1));
                        // FCVT.S.WU
                        case 1 ->
                                hart.fxRegisters.setFloat(heapAndStack, rd, hart.xRegisters.getInt32(rs1)&0xffffffffL);
                        // FCVT.S.L
                        case 2 -> hart.fxRegisters.setFloat(heapAndStack, rd, hart.xRegisters.getInt64(rs1));
                        // FCVT.S.LU
                        case 3 ->
                                hart.fxRegisters.setFloat(heapAndStack, rd, (float)Casts.castUint64ToDouble(hart.xRegisters.getInt64(rs1)));
                        default -> throw new IllegalInstructionException(
                                "illegal instruction 0x%08x at 0x%012x, opcode: 0x%02x, funct3=%x funct7=0x%02x, rs2=0x%02x"
                                        .formatted(instruction, hart.getPc(), opcode, funct3, funct7, rs2));
                    }
                }
                case 0x69 -> {
                    switch (rs2) {
                        // FCVT.D.W
                        case 0 -> hart.fxRegisters.setDouble(heapAndStack, rd, hart.xRegisters.getInt32(rs1));
                        // FCVT.D.WU
                        case 1 ->
                                hart.fxRegisters.setDouble(heapAndStack, rd, hart.xRegisters.getInt32(rs1)&0xffffffffL);
                        // FCVT.D.L
                        case 2 -> hart.fxRegisters.setDouble(heapAndStack, rd, hart.xRegisters.getInt64(rs1));
                        // FCVT.D.LU
                        case 3 ->
                                hart.fxRegisters.setDouble(heapAndStack, rd, Casts.castUint64ToDouble(hart.xRegisters.getInt64(rs1)));
                        default -> throw new IllegalInstructionException(
                                "illegal instruction 0x%08x at 0x%012x, opcode: 0x%02x, funct3=%x funct7=0x%02x, rs2=0x%02x"
                                        .formatted(instruction, hart.getPc(), opcode, funct3, funct7, rs2));
                    }
                }
                case 0x78 -> {
                    // noinspection SwitchStatementWithTooFewBranches
                    switch (funct3) {
                        // FMV.W.X
                        case 0 -> hart.fxRegisters.setInt32(heapAndStack, rd, hart.xRegisters.getInt32(rs1));
                        default -> throw new IllegalInstructionException(
                                "illegal instruction 0x%08x at 0x%012x, opcode: 0x%02x, funct3=%x funct7=0x%02x"
                                        .formatted(instruction, hart.getPc(), opcode, funct3, funct7));
                    }
                }
                case 0x79 -> {
                    // noinspection SwitchStatementWithTooFewBranches
                    switch (funct3) {
                        // FMV.D.X
                        case 0 -> hart.fxRegisters.setInt64(heapAndStack, rd, hart.xRegisters.getInt64(rs1));
                        default -> throw new IllegalInstructionException(
                                "illegal instruction 0x%08x at 0x%012x, opcode: 0x%02x, funct3=%x funct7=0x%02x"
                                        .formatted(instruction, hart.getPc(), opcode, funct3, funct7));
                    }
                }
                default -> throw new IllegalInstructionException(
                        "illegal instruction 0x%08x at 0x%012x, opcode: 0x%02x, funct3=%x funct7=0x%02x"
                                .formatted(instruction, hart.getPc(), opcode, funct3, funct7));
            }
            hart.incPc();
        };
    }

    private static @NotNull Instruction.BType opcode63() {
        return (funct3, hart, imm, instruction, opcode, rs1, rs2)->{
            long v1=hart.xRegisters.getInt64(rs1);
            long v2=hart.xRegisters.getInt64(rs2);
            boolean branch=switch (funct3) {
                // BEQ
                case 0 -> v1==v2;
                // BNE
                case 1 -> v1!=v2;
                // BLT
                case 4 -> hart.xRegisters.getInt64(rs1)<hart.xRegisters.getInt64(rs2);
                // BGE
                case 5 -> hart.xRegisters.getInt64(rs1)>=hart.xRegisters.getInt64(rs2);
                // BLTU
                case 6 -> 0>Long.compareUnsigned(hart.xRegisters.getInt64(rs1), hart.xRegisters.getInt64(rs2));
                // BGEU
                case 7 -> 0<=Long.compareUnsigned(hart.xRegisters.getInt64(rs1), hart.xRegisters.getInt64(rs2));
                default -> throw new IllegalInstructionException(
                        "illegal instruction 0x%08x at 0x%012x, opcode: 0x%02x, funct3=%x"
                                .formatted(instruction, hart.getPc(), opcode, funct3));
            };
            if (branch) {
                hart.setPc(hart.getPc()+imm);
            }
            else {
                hart.incPc();
            }
        };
    }

    private static @NotNull Instruction.IType opcode67() {
        return (funct3, hart, heapAndStack, imm, instruction, memory, opcode, rd, rs1)->{
            // noinspection SwitchStatementWithTooFewBranches
            switch (funct3) {
                // JALR
                case 0 -> {
                    long link=hart.getPc()+4;
                    hart.setPc((hart.xRegisters.getInt64(rs1)+imm)&0xfffffffffffffffeL);
                    hart.xRegisters.setInt64(heapAndStack, rd, link);
                }
                default -> throw new IllegalInstructionException(
                        "illegal instruction 0x%08x at 0x%012x, opcode: 0x%02x, funct3=%x"
                                .formatted(instruction, hart.getPc(), opcode, funct3));
            }
        };
    }

    private static @NotNull Instruction.JType opcode6f() {
        return (hart, heapAndStack, imm, rd)->{
            // JAL
            hart.xRegisters.setInt64(heapAndStack, rd, hart.getPc()+4);
            hart.setPc(hart.getPc()+imm);
        };
    }
}
