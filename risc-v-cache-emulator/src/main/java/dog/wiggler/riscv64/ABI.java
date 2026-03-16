package dog.wiggler.riscv64;

/**
 * Risc-v LP64D ABI values.
 * @see <a href="https://riscv.org/wp-content/uploads/2024/12/riscv-calling.pdf">https://riscv.org/wp-content/uploads/2024/12/riscv-calling.pdf</a>.
 */
public class ABI {
    // register for first integer parameter
    public static final int REGISTER_A0=10;
    // register for first floating point parameter
    public static final int REGISTER_FA0=10;
    // register for return address
    public static final int REGISTER_RA=1;
    // register for stack pointer
    public static final int REGISTER_SP=2;

    private ABI() {
    }
}
