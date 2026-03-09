package dog.wiggler.riscv64;

import dog.wiggler.Casts;

public class RV64IMFD extends Instructions {
    public RV64IMFD() {
        add(0x03, opcode03());
        add(0x07, opcode07());
        add(0x13, opcode13());
        add(0x17, opcode17());
        add(0x1b, opcode1b());
        add(0x23, opcode23());
        add(0x27, opcode27());
        add(0x33, opcode33());
        add(0x37, opcode37());
        add(0x3b, opcode3b());
        add(0x43, opcode43());
        add(0x53, opcode53());
        add(0x63, opcode63());
        add(0x67, opcode67());
        add(0x6f, opcode6f());
    }

    private static Instruction.IType opcode03() {
        return (funct3, hart, imm, instruction, memory, opcode, rd, rs1)->{
            switch (funct3) {
                // LB
                case 0 -> hart.registersXs.setInt64(rd, memory.loadInt8(hart.registersXs.getInt64(rs1)+imm));
                // LH
                case 1 -> hart.registersXs.setInt64(rd, memory.loadInt16(hart.registersXs.getInt64(rs1)+imm));
                // LW
                case 2 -> hart.registersXs.setInt64(rd, memory.loadInt32(hart.registersXs.getInt64(rs1)+imm, false));
                // LD
                case 3 -> hart.registersXs.setInt64(rd, memory.loadInt64(hart.registersXs.getInt64(rs1)+imm));
                // LBU
                case 4 -> hart.registersXs.setInt64(rd, memory.loadInt8(hart.registersXs.getInt64(rs1)+imm)&0xffL);
                // LHU
                case 5 -> hart.registersXs.setInt64(rd, memory.loadInt16(hart.registersXs.getInt64(rs1)+imm)&0xffffL);
                // LWU
                case 6 -> hart.registersXs.setInt64(
                        rd, memory.loadInt32(hart.registersXs.getInt64(rs1)+imm, false)&0xffffffffL);
                default -> throw new IllegalInstructionException(
                        "illegal instruction 0x%08x at 0x%012x, opcode: 0x%02x, funct3=%x",
                        instruction, hart.getPc(), opcode, funct3);

            }
            hart.incPc();
        };
    }

    private static Instruction.IType opcode07() {
        return (funct3, hart, imm, instruction, memory, opcode, rd, rs1)->{
            switch (funct3) {
                // FLW
                case 2 -> hart.registersFxs.setInt32(rd, memory.loadInt32(hart.registersXs.getInt64(rs1)+imm, false));
                // FLD
                case 3 -> hart.registersFxs.setInt64(rd, memory.loadInt64(hart.registersXs.getInt64(rs1)+imm));
                default -> throw new IllegalInstructionException(
                        "illegal instruction 0x%08x at 0x%012x, opcode: 0x%02x, funct3=%x",
                        instruction, hart.getPc(), opcode, funct3);
            }
            hart.incPc();
        };
    }

    private static Instruction.IType opcode13() {
        return (funct3, hart, imm, instruction, memory, opcode, rd, rs1)->{
            switch (funct3) {
                // ADDI
                case 0 -> hart.registersXs.setInt64(rd, hart.registersXs.getInt64(rs1)+imm);
                // SLLI
                case 1 -> {
                    if (0!=(imm&0xfc0)) {
                        throw new IllegalInstructionException(
                                "illegal instruction 0x%08x at 0x%012x, opcode: 0x%02x, funct3=%x, imm=%03x",
                                instruction, hart.getPc(), opcode, funct3, imm);
                    }
                    hart.registersXs.setInt64(rd, hart.registersXs.getInt64(rs1)<<(imm&0x3f));
                }
                //SLTIU
                case 3 -> hart.registersXs.setInt64(
                        rd, (0>Long.compareUnsigned(hart.registersXs.getInt64(rs1), imm))?1L:0L);
                // XORI
                case 4 -> hart.registersXs.setInt64(rd, hart.registersXs.getInt64(rs1)^imm);
                case 5 -> {
                    switch (imm&0xfc0) {
                        // SRLI
                        case 0 -> hart.registersXs.setInt64(rd, hart.registersXs.getInt64(rs1) >>> (imm&0x3f));
                        // SRAI
                        case 0x400 -> hart.registersXs.setInt64(rd, hart.registersXs.getInt64(rs1)>>(imm&0x3f));
                        default -> throw new IllegalInstructionException(
                                "illegal instruction 0x%08x at 0x%012x, opcode: 0x%02x, funct3=%x, imm=%03x",
                                instruction, hart.getPc(), opcode, funct3, imm);
                    }
                }
                // ANDI
                case 7 -> hart.registersXs.setInt64(rd, hart.registersXs.getInt64(rs1)&imm);
                default -> throw new IllegalInstructionException(
                        "illegal instruction 0x%08x at 0x%012x, opcode: 0x%02x, funct3=%x",
                        instruction, hart.getPc(), opcode, funct3);

            }
            hart.incPc();
        };
    }

    private static Instruction.UType opcode17() {
        return (hart, imm, instruction, memory, opcode, rd)->{
            // AUIPC
            hart.registersXs.setInt64(rd, hart.getPc()+imm);
            hart.incPc();
        };
    }

    private static Instruction.IType opcode1b() {
        return (funct3, hart, imm, instruction, memory, opcode, rd, rs1)->{
            switch (funct3) {
                // ADDIW
                case 0 -> hart.registersXs.setInt64(rd, (int)hart.registersXs.getInt64(rs1)+imm);
                // SLLIW
                case 1 -> {
                    if (0!=(imm&0xfe0)) {
                        throw new IllegalInstructionException(
                                "illegal instruction 0x%08x at 0x%012x, opcode: 0x%02x, funct3=%x, imm=%03x",
                                instruction, hart.getPc(), opcode, funct3, imm);
                    }
                    int result=hart.registersXs.getInt32(rs1)<<(imm&0x1f);
                    hart.registersXs.setInt64(rd, result);
                }
                case 5 -> {
                    switch (imm&0xfe0) {
                        // SRLIW
                        case 0x000 -> hart.registersXs.setInt64(rd, hart.registersXs.getInt32(rs1)>>>(imm&0x1f));
                        // SRAIW
                        case 0x400 -> hart.registersXs.setInt64(rd, hart.registersXs.getInt32(rs1)>>(imm&0x1f));
                        default -> throw new IllegalInstructionException(
                                "illegal instruction 0x%08x at 0x%012x, opcode: 0x%02x, funct3=%x, imm=%03x",
                                instruction, hart.getPc(), opcode, funct3, imm);
                    }
                }
                default -> throw new IllegalInstructionException(
                        "illegal instruction 0x%08x at 0x%012x, opcode: 0x%02x, funct3=%x",
                        instruction, hart.getPc(), opcode, funct3);

            }
            hart.incPc();
        };
    }

    private static Instruction.SType opcode23() {
        return (funct3, hart, imm, instruction, memory, opcode, rs1, rs2)->{
            switch (funct3) {
                // SB
                case 0 -> memory.storeInt8(hart.registersXs.getInt64(rs1)+imm, hart.registersXs.getInt8(rs2));
                // SH
                case 1 -> memory.storeInt16(hart.registersXs.getInt64(rs1)+imm, hart.registersXs.getInt16(rs2));
                // SW
                case 2 -> memory.storeInt32(hart.registersXs.getInt64(rs1)+imm, hart.registersXs.getInt32(rs2));
                // SD
                case 3 -> memory.storeInt64(hart.registersXs.getInt64(rs1)+imm, hart.registersXs.getInt64(rs2));
                default -> throw new IllegalInstructionException(
                        "illegal instruction 0x%08x at 0x%012x, opcode: 0x%02x, funct3=%x",
                        instruction, hart.getPc(), opcode, funct3);

            }
            hart.incPc();
        };
    }

    private static Instruction.SType opcode27() {
        return (funct3, hart, imm, instruction, memory, opcode, rs1, rs2)->{
            switch (funct3) {
                // FSW
                case 2 -> memory.storeInt32(hart.registersXs.getInt64(rs1)+imm, hart.registersFxs.getInt32(rs2));
                // FSD
                case 3 -> memory.storeInt64(hart.registersXs.getInt64(rs1)+imm, hart.registersFxs.getInt64(rs2));
                default -> throw new IllegalInstructionException(
                        "illegal instruction 0x%08x at 0x%012x, opcode: 0x%02x, funct3=%x",
                        instruction, hart.getPc(), opcode, funct3);
            }
            hart.incPc();
        };
    }

    private static Instruction.RType opcode33() {
        return (funct3, funct7, hart, instruction, memory, opcode, rd, rs1, rs2)->{
            switch (funct3) {
                case 0 -> {
                    switch (funct7) {
                        // ADD
                        case 0 -> hart.registersXs.setInt64(
                                rd, hart.registersXs.getInt64(rs1)+hart.registersXs.getInt64(rs2));
                        // MUL
                        case 1 -> hart.registersXs.setInt64(
                                rd, hart.registersXs.getInt64(rs1)*hart.registersXs.getInt64(rs2));
                        // SUB
                        case 0x20 -> hart.registersXs.setInt64(
                                rd, hart.registersXs.getInt64(rs1)-hart.registersXs.getInt64(rs2));
                        default -> throw new IllegalInstructionException(
                                "illegal instruction 0x%08x at 0x%012x, opcode: 0x%02x, funct3=%x, funct7=%02x",
                                instruction, hart.getPc(), opcode, funct3, funct7);
                    }
                }
                // SLL
                case 1 -> {
                    if (0!=funct7) {
                        throw new IllegalInstructionException(
                                "illegal instruction 0x%08x at 0x%012x, opcode: 0x%02x, funct3=%x, funct7=%02x",
                                instruction, hart.getPc(), opcode, funct3, funct7);
                    }
                    hart.registersXs.setInt64(
                            rd, hart.registersXs.getInt64(rs1)<<(hart.registersXs.getInt32(rs2)&0x3f));
                }
                //SLT
                case 2 -> {
                    if (0!=funct7) {
                        throw new IllegalInstructionException(
                                "illegal instruction 0x%08x at 0x%012x, opcode: 0x%02x, funct3=%x, funct7=%02x",
                                instruction, hart.getPc(), opcode, funct3, funct7);
                    }
                    hart.registersXs.setInt64(
                            rd,
                            hart.registersXs.getInt64(rs1)<hart.registersXs.getInt64(rs2)
                                    ?1L
                                    :0L);
                }
                //SLTU
                case 3 -> {
                    if (0!=funct7) {
                        throw new IllegalInstructionException(
                                "illegal instruction 0x%08x at 0x%012x, opcode: 0x%02x, funct3=%x, funct7=%02x",
                                instruction, hart.getPc(), opcode, funct3, funct7);
                    }
                    hart.registersXs.setInt64(
                            rd,
                            (0>Long.compareUnsigned(hart.registersXs.getInt64(rs1), hart.registersXs.getInt64(rs2)))
                                    ?1L
                                    :0L);
                }
                case 4 -> {
                    switch (funct7) {
                        // XOR
                        case 0 -> hart.registersXs.setInt64(
                                rd, hart.registersXs.getInt64(rs1)^hart.registersXs.getInt64(rs2));
                        // DIV
                        case 1 -> {
                            long dividend=hart.registersXs.getInt64(rs1);
                            long divisor=hart.registersXs.getInt64(rs2);
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
                            hart.registersXs.setInt64(rd, result);
                        }
                        default -> throw new IllegalInstructionException(
                                "illegal instruction 0x%08x at 0x%012x, opcode: 0x%02x, funct3=%x, funct7=%02x",
                                instruction, hart.getPc(), opcode, funct3, funct7);
                    }
                }
                case 5 -> {
                    switch (funct7) {
                        // SRL
                        case 0 -> hart.registersXs.setInt64(
                                rd, hart.registersXs.getInt64(rs1) >>> (hart.registersXs.getInt32(rs2)&0x3f));
                        // DIVU
                        case 1 -> {
                            long divisor=hart.registersXs.getInt64(rs2);
                            hart.registersXs.setInt64(
                                    rd,
                                    (0==divisor)
                                            ?-1
                                            :Long.divideUnsigned(hart.registersXs.getInt64(rs1), divisor));
                        }
                        // SRA
                        case 0x20 -> hart.registersXs.setInt64(
                                rd, hart.registersXs.getInt64(rs1)>>(hart.registersXs.getInt32(rs2)&0x3f));
                        default -> throw new IllegalInstructionException(
                                "illegal instruction 0x%08x at 0x%012x, opcode: 0x%02x, funct3=%x, funct7=%02x",
                                instruction, hart.getPc(), opcode, funct3, funct7);
                    }
                }
                case 6 -> {
                    switch (funct7) {
                        // OR
                        case 0 -> hart.registersXs.setInt64(
                                rd, hart.registersXs.getInt64(rs1)|hart.registersXs.getInt64(rs2));
                        // REM
                        case 1 -> {
                            long dividend=hart.registersXs.getInt64(rs1);
                            long divisor=hart.registersXs.getInt64(rs2);
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
                            hart.registersXs.setInt64(rd, result);
                        }
                        default -> throw new IllegalInstructionException(
                                "illegal instruction 0x%08x at 0x%012x, opcode: 0x%02x, funct3=%x, funct7=%02x",
                                instruction, hart.getPc(), opcode, funct3, funct7);
                    }
                }
                case 7 -> {
                    switch (funct7) {
                        // AND
                        case 0 -> hart.registersXs.setInt64(
                                rd, hart.registersXs.getInt64(rs1)&hart.registersXs.getInt64(rs2));
                        // REMU
                        case 1 -> {
                            long dividend=hart.registersXs.getInt64(rs1);
                            long divisor=hart.registersXs.getInt64(rs2);
                            hart.registersXs.setInt64(
                                    rd,
                                    (0==divisor)
                                            ?dividend
                                            :Long.remainderUnsigned(dividend, divisor));
                        }
                        default -> throw new IllegalInstructionException(
                                "illegal instruction 0x%08x at 0x%012x, opcode: 0x%02x, funct3=%x, funct7=%02x",
                                instruction, hart.getPc(), opcode, funct3, funct7);
                    }
                }
                default -> throw new IllegalInstructionException(
                        "illegal instruction 0x%08x at 0x%012x, opcode: 0x%02x, funct3=%x",
                        instruction, hart.getPc(), opcode, funct3);
            }
            hart.incPc();
        };
    }

    private static Instruction.UType opcode37() {
        // LUI
        return (hart, imm, instruction, memory, opcode, rd)->{
            hart.registersXs.setInt64(rd, imm);
            hart.incPc();
        };
    }

    private static Instruction.RType opcode3b() {
        return (funct3, funct7, hart, instruction, memory, opcode, rd, rs1, rs2)->{
            switch (funct3) {
                case 0 -> {
                    switch (funct7) {
                        // ADDW
                        case 0 -> hart.registersXs.setInt32(
                                rd, hart.registersXs.getInt32(rs1)+hart.registersXs.getInt32(rs2));
                        // MULW
                        case 1 -> hart.registersXs.setInt32(
                                rd, hart.registersXs.getInt32(rs1)*hart.registersXs.getInt32(rs2));
                        // SUBW
                        case 0x20 -> hart.registersXs.setInt32(
                                rd, hart.registersXs.getInt32(rs1)-hart.registersXs.getInt32(rs2));
                        default -> throw new IllegalInstructionException(
                                "illegal instruction 0x%08x at 0x%012x, opcode: 0x%02x, funct3=%x, funct7=%02x",
                                instruction, hart.getPc(), opcode, funct3, funct7);
                    }
                }
                // SLLW
                case 1 -> {
                    if (0!=funct7) {
                        throw new IllegalInstructionException(
                                "illegal instruction 0x%08x at 0x%012x, opcode: 0x%02x, funct3=%x, funct7=%02x",
                                instruction, hart.getPc(), opcode, funct3, funct7);
                    }
                    int result=hart.registersXs.getInt32(rs1)<<(hart.registersXs.getInt32(rs2)&0x1f);
                    hart.registersXs.setInt64(rd, result);
                }
                case 4 -> {
                    switch (funct7) {
                        // DIVW
                        case 1 -> {
                            int dividend=hart.registersXs.getInt32(rs1);
                            int divisor=hart.registersXs.getInt32(rs2);
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
                            hart.registersXs.setInt32(rd, result);
                        }
                        default -> throw new IllegalInstructionException(
                                "illegal instruction 0x%08x at 0x%012x, opcode: 0x%02x, funct3=%x, funct7=%02x",
                                instruction, hart.getPc(), opcode, funct3, funct7);
                    }
                }
                case 5 -> {
                    switch (funct7) {
                        // SRLW
                        case 0 -> hart.registersXs.setInt64(
                                rd, hart.registersXs.getInt32(rs1) >>> (hart.registersXs.getInt32(rs2)&0x1f));
                        // DIVUW
                        case 1 -> {
                            int divisor=hart.registersXs.getInt32(rs2);
                            hart.registersXs.setInt32(
                                    rd,
                                    (0==divisor)
                                            ?-1
                                            :Integer.divideUnsigned(hart.registersXs.getInt32(rs1), divisor));
                        }
                        // SRAW
                        case 0x20 -> hart.registersXs.setInt64(
                                rd, hart.registersXs.getInt32(rs1)>>(hart.registersXs.getInt32(rs2)&0x1f));
                        default -> throw new IllegalInstructionException(
                                "illegal instruction 0x%08x at 0x%012x, opcode: 0x%02x, funct3=%x, funct7=%02x",
                                instruction, hart.getPc(), opcode, funct3, funct7);
                    }
                }
                case 6 -> {
                    switch (funct7) {
                        // REMW
                        case 1 -> {
                            int dividend=hart.registersXs.getInt32(rs1);
                            int divisor=hart.registersXs.getInt32(rs2);
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
                            hart.registersXs.setInt32(rd, result);
                        }
                        default -> throw new IllegalInstructionException(
                                "illegal instruction 0x%08x at 0x%012x, opcode: 0x%02x, funct3=%x, funct7=%02x",
                                instruction, hart.getPc(), opcode, funct3, funct7);
                    }
                }
                case 7 -> {
                    switch (funct7) {
                        // REMUW
                        case 1 -> {
                            int dividend=hart.registersXs.getInt32(rs1);
                            int divisor=hart.registersXs.getInt32(rs2);
                            hart.registersXs.setInt32(
                                    rd,
                                    (0==divisor)
                                            ?dividend:
                                            Integer.remainderUnsigned(dividend, divisor));
                        }
                        default -> throw new IllegalInstructionException(
                                "illegal instruction 0x%08x at 0x%012x, opcode: 0x%02x, funct3=%x, funct7=%02x",
                                instruction, hart.getPc(), opcode, funct3, funct7);
                    }
                }
                default -> throw new IllegalInstructionException(
                        "illegal instruction 0x%08x at 0x%012x, opcode: 0x%02x, funct3=%x",
                        instruction, hart.getPc(), opcode, funct3);

            }
            hart.incPc();
        };
    }

    private static Instruction.MAType opcode43() {
        return (fmt, hart, instruction, memory, opcode, rd, rm, rs1, rs2, rs3)->{
            switch (fmt) {
                // FMADD.D
                case 1 -> hart.registersFxs.setDouble(
                        rd,
                        hart.registersFxs.getDouble(rs1)*hart.registersFxs.getDouble(rs2)
                                +hart.registersFxs.getDouble(rs3));
                default -> throw new IllegalInstructionException(
                        "illegal instruction 0x%08x at 0x%012x, opcode: 0x%02x, fmt=%x",
                        instruction, hart.getPc(), opcode, fmt);
            }
            hart.incPc();
        };
    }

    private static Instruction.RType opcode53() {
        return (funct3, funct7, hart, instruction, memory, opcode, rd, rs1, rs2)->{
            switch (funct7) {
                // FADD.S
                case 0 -> hart.registersFxs.setFloat(
                        rd, hart.registersFxs.getFloat(rs1)+hart.registersFxs.getFloat(rs2));
                // FADD.D
                case 1 -> hart.registersFxs.setDouble(
                        rd, hart.registersFxs.getDouble(rs1)+hart.registersFxs.getDouble(rs2));
                // FSUB.S
                case 4 -> hart.registersFxs.setFloat(
                        rd, hart.registersFxs.getFloat(rs1)-hart.registersFxs.getFloat(rs2));
                // FSUB.D
                case 5 -> hart.registersFxs.setDouble(
                        rd, hart.registersFxs.getDouble(rs1)-hart.registersFxs.getDouble(rs2));
                // FMUL.S
                case 0x8 -> hart.registersFxs.setFloat(
                        rd, hart.registersFxs.getFloat(rs1)*hart.registersFxs.getFloat(rs2));
                // FMUL.D
                case 0x9 -> hart.registersFxs.setDouble(
                        rd, hart.registersFxs.getDouble(rs1)*hart.registersFxs.getDouble(rs2));
                // FDIV.S
                case 0xc -> hart.registersFxs.setFloat(
                        rd, hart.registersFxs.getFloat(rs1)/hart.registersFxs.getFloat(rs2));
                // FDIV.D
                case 0xd -> hart.registersFxs.setDouble(
                        rd, hart.registersFxs.getDouble(rs1)/hart.registersFxs.getDouble(rs2));
                case 0x10 -> {
                    switch (funct3) {
                        // FSGNJ.S
                        case 0 -> hart.registersFxs.setInt32(
                                rd,
                                (hart.registersFxs.getInt32(rs1)&0x7fffffff)
                                        |(hart.registersFxs.getInt32(rs2)&0x80000000));
                        // FSGNJN.S
                        case 1 -> hart.registersFxs.setInt32(
                                rd,
                                (hart.registersFxs.getInt32(rs1)&0x7fffffff)
                                        |((~hart.registersFxs.getInt32(rs2))&0x80000000));
                        default -> throw new IllegalInstructionException(
                                "illegal instruction 0x%08x at 0x%012x, opcode: 0x%02x, funct3=%x funct7=0x%02x",
                                instruction, hart.getPc(), opcode, funct3, funct7);
                    }
                }
                case 0x11 -> {
                    switch (funct3) {
                        // FSGNJ.D
                        case 0 -> hart.registersFxs.setInt64(
                                rd,
                                (hart.registersFxs.getInt64(rs1)&0x7fffffffffffffffL)
                                        |(hart.registersFxs.getInt64(rs2)&0x8000000000000000L));
                        // FSGNJN.D
                        case 1 -> hart.registersFxs.setInt64(
                                rd,
                                (hart.registersFxs.getInt64(rs1)&0x7fffffffffffffffL)
                                        |((~hart.registersFxs.getInt64(rs2))&0x8000000000000000L));
                        default -> throw new IllegalInstructionException(
                                "illegal instruction 0x%08x at 0x%012x, opcode: 0x%02x, funct3=%x funct7=0x%02x",
                                instruction, hart.getPc(), opcode, funct3, funct7);
                    }
                }
                case 0x20 -> {
                    switch (rs2) {
                        // FCVT.S.D
                        case 1 -> hart.registersFxs.setFloat(rd, (float)hart.registersFxs.getDouble(rs1));
                        default -> throw new IllegalInstructionException(
                                "illegal instruction 0x%08x at 0x%012x, opcode: 0x%02x, funct3=%x funct7=0x%02x, rs2=0x%02x",
                                instruction, hart.getPc(), opcode, funct3, funct7, rs2);
                    }
                }
                case 0x21 -> {
                    switch (rs2) {
                        // FCVT.D.S
                        case 0 -> hart.registersFxs.setDouble(rd, hart.registersFxs.getFloat(rs1));
                        default -> throw new IllegalInstructionException(
                                "illegal instruction 0x%08x at 0x%012x, opcode: 0x%02x, funct3=%x funct7=0x%02x, rs2=0x%02x",
                                instruction, hart.getPc(), opcode, funct3, funct7, rs2);
                    }
                }
                case 0x50 -> {
                    switch (funct3) {
                        // FLE.S
                        case 0 -> hart.registersXs.setInt64(
                                rd, (hart.registersFxs.getFloat(rs1)<=hart.registersFxs.getFloat(rs2))?1L:0L);
                        // FLT.S
                        case 1 -> hart.registersXs.setInt64(
                                rd, (hart.registersFxs.getFloat(rs1)<hart.registersFxs.getFloat(rs2))?1L:0L);
                        // FEQ.S
                        case 2 -> hart.registersXs.setInt64(
                                rd, (hart.registersFxs.getFloat(rs1)==hart.registersFxs.getFloat(rs2))?1L:0L);
                        default -> throw new IllegalInstructionException(
                                "illegal instruction 0x%08x at 0x%012x, opcode: 0x%02x, funct3=%x funct7=0x%02x",
                                instruction, hart.getPc(), opcode, funct3, funct7);
                    }
                }
                case 0x51 -> {
                    switch (funct3) {
                        // FLE.D
                        case 0 -> hart.registersXs.setInt64(
                                rd, (hart.registersFxs.getDouble(rs1)<=hart.registersFxs.getDouble(rs2))?1L:0L);
                        // FLT.D
                        case 1 -> hart.registersXs.setInt64(
                                rd, (hart.registersFxs.getDouble(rs1)<hart.registersFxs.getDouble(rs2))?1L:0L);
                        // FEQ.D
                        case 2 -> hart.registersXs.setInt64(
                                rd, (hart.registersFxs.getDouble(rs1)==hart.registersFxs.getDouble(rs2))?1L:0L);
                        default -> throw new IllegalInstructionException(
                                "illegal instruction 0x%08x at 0x%012x, opcode: 0x%02x, funct3=%x funct7=0x%02x",
                                instruction, hart.getPc(), opcode, funct3, funct7);
                    }
                }
                case 0x60 -> {
                    switch (rs2) {
                        // FCVT.W.S
                        case 0 -> hart.registersXs.setInt32(rd, (int)hart.registersFxs.getFloat(rs1));
                        // FCVT.WU.S
                        case 1 -> hart.registersXs.setInt32(
                                rd, Casts.castDoubleToUint32(hart.registersFxs.getFloat(rs1)));
                        // FCVT.L.S
                        case 2 -> hart.registersXs.setInt64(rd, (long)hart.registersFxs.getFloat(rs1));
                        // FCVT.LU.S
                        case 3 -> hart.registersXs.setInt64(
                                rd, Casts.castDoubleToUint64(hart.registersFxs.getFloat(rs1)));
                        default -> throw new IllegalInstructionException(
                                "illegal instruction 0x%08x at 0x%012x, opcode: 0x%02x, funct3=%x funct7=0x%02x, rs2=0x%02x",
                                instruction, hart.getPc(), opcode, funct3, funct7, rs2);
                    }
                }
                case 0x61 -> {
                    switch (rs2) {
                        // FCVT.W.D
                        case 0 -> hart.registersXs.setInt32(rd, (int)hart.registersFxs.getDouble(rs1));
                        // FCVT.WU.D
                        case 1 -> hart.registersXs.setInt32(
                                rd, Casts.castDoubleToUint32(hart.registersFxs.getDouble(rs1)));
                        // FCVT.L.D
                        case 2 -> hart.registersXs.setInt64(rd, (long)hart.registersFxs.getDouble(rs1));
                        // FCVT.LU.D
                        case 3 -> hart.registersXs.setInt64(
                                rd, Casts.castDoubleToUint64(hart.registersFxs.getDouble(rs1)));
                        default -> throw new IllegalInstructionException(
                                "illegal instruction 0x%08x at 0x%012x, opcode: 0x%02x, funct3=%x funct7=0x%02x, rs2=0x%02x",
                                instruction, hart.getPc(), opcode, funct3, funct7, rs2);
                    }
                }
                case 0x68 -> {
                    switch (rs2) {
                        // FCVT.S.W
                        case 0 -> hart.registersFxs.setFloat(rd, hart.registersXs.getInt32(rs1));
                        // FCVT.S.WU
                        case 1 -> hart.registersFxs.setFloat(rd, hart.registersXs.getInt32(rs1)&0xffffffffL);
                        // FCVT.S.L
                        case 2 -> hart.registersFxs.setFloat(rd, hart.registersXs.getInt64(rs1));
                        // FCVT.S.LU
                        case 3 -> hart.registersFxs.setFloat(
                                rd, (float)Casts.castUint64ToDouble(hart.registersXs.getInt64(rs1)));
                        default -> throw new IllegalInstructionException(
                                "illegal instruction 0x%08x at 0x%012x, opcode: 0x%02x, funct3=%x funct7=0x%02x, rs2=0x%02x",
                                instruction, hart.getPc(), opcode, funct3, funct7, rs2);
                    }
                }
                case 0x69 -> {
                    switch (rs2) {
                        // FCVT.D.W
                        case 0 -> hart.registersFxs.setDouble(rd, hart.registersXs.getInt32(rs1));
                        // FCVT.D.WU
                        case 1 -> hart.registersFxs.setDouble(rd, hart.registersXs.getInt32(rs1)&0xffffffffL);
                        // FCVT.D.L
                        case 2 -> hart.registersFxs.setDouble(rd, hart.registersXs.getInt64(rs1));
                        // FCVT.D.LU
                        case 3 -> hart.registersFxs.setDouble(
                                rd, Casts.castUint64ToDouble(hart.registersXs.getInt64(rs1)));
                        default -> throw new IllegalInstructionException(
                                "illegal instruction 0x%08x at 0x%012x, opcode: 0x%02x, funct3=%x funct7=0x%02x, rs2=0x%02x",
                                instruction, hart.getPc(), opcode, funct3, funct7, rs2);
                    }
                }
                case 0x78 -> {
                    switch (funct3) {
                        // FMV.W.X
                        case 0 -> hart.registersFxs.setInt32(rd, hart.registersXs.getInt32(rs1));
                        default -> throw new IllegalInstructionException(
                                "illegal instruction 0x%08x at 0x%012x, opcode: 0x%02x, funct3=%x funct7=0x%02x",
                                instruction, hart.getPc(), opcode, funct3, funct7);
                    }
                }
                case 0x79 -> {
                    switch (funct3) {
                        // FMV.D.X
                        case 0 -> hart.registersFxs.setInt64(rd, hart.registersXs.getInt64(rs1));
                        default -> throw new IllegalInstructionException(
                                "illegal instruction 0x%08x at 0x%012x, opcode: 0x%02x, funct3=%x funct7=0x%02x",
                                instruction, hart.getPc(), opcode, funct3, funct7);
                    }
                }
                default -> throw new IllegalInstructionException(
                        "illegal instruction 0x%08x at 0x%012x, opcode: 0x%02x, funct3=%x funct7=0x%02x",
                        instruction, hart.getPc(), opcode, funct3, funct7);
            }
            hart.incPc();
        };
    }

    private static Instruction.BType opcode63() {
        return (funct3, hart, imm, instruction, memory, opcode, rs1, rs2)->{
            long v1=hart.registersXs.getInt64(rs1);
            long v2=hart.registersXs.getInt64(rs2);
            boolean branch=switch (funct3) {
                // BEQ
                case 0 -> v1==v2;
                // BNE
                case 1 -> v1!=v2;
                // BLT
                case 4 -> hart.registersXs.getInt64(rs1)<hart.registersXs.getInt64(rs2);
                // BGE
                case 5 -> hart.registersXs.getInt64(rs1)>=hart.registersXs.getInt64(rs2);
                // BLTU
                case 6 -> 0>Long.compareUnsigned(hart.registersXs.getInt64(rs1), hart.registersXs.getInt64(rs2));
                // BGEU
                case 7 -> 0<=Long.compareUnsigned(hart.registersXs.getInt64(rs1), hart.registersXs.getInt64(rs2));
                default -> throw new IllegalInstructionException(
                        "illegal instruction 0x%08x at 0x%012x, opcode: 0x%02x, funct3=%x",
                        instruction, hart.getPc(), opcode, funct3);
            };
            if (branch) {
                hart.setPc(hart.getPc()+imm);
            }
            else {
                hart.incPc();
            }
        };
    }

    private static Instruction.IType opcode67() {
        return (funct3, hart, imm, instruction, memory, opcode, rd, rs1)->{
            switch (funct3) {
                // JALR
                case 0 -> {
                    long link=hart.getPc()+4;
                    hart.setPc((hart.registersXs.getInt64(rs1)+imm)&0xfffffffffffffffeL);
                    hart.registersXs.setInt64(rd, link);
                }
                default -> throw new IllegalInstructionException(
                        "illegal instruction 0x%08x at 0x%012x, opcode: 0x%02x, funct3=%x",
                        instruction, hart.getPc(), opcode, funct3);
            }
        };
    }

    private static Instruction.JType opcode6f() {
        return (hart, imm, instruction, memory, opcode, rd)->{
            // JAL
            hart.registersXs.setInt64(rd, hart.getPc()+4);
            hart.setPc(hart.getPc()+imm);
        };
    }
}
