package dog.wiggler.riscv64.abi;

/**
 * RISC-V LP64D ABI values.
 * The ABI describes the function call conventions.
 * @see <a href="https://riscv.org/wp-content/uploads/2024/12/riscv-calling.pdf">https://riscv.org/wp-content/uploads/2024/12/riscv-calling.pdf</a>.
 */
public class ABI {
    // number of registers to pass arguments
    public static final int ARGUMENT_REGISTERS=8;
    // number of floating-point registers to pass arguments
    public static final int FLOAT_ARGUMENT_REGISTERS=8;
    // register for first integer argument
    public static final int REGISTER_A0=10;
    // register for first floating-point argument
    public static final int REGISTER_FA0=10;
    // register for return address
    public static final int REGISTER_RA=1;
    // register for stack pointer
    public static final int REGISTER_SP=2;

    private ABI() {
    }
}
