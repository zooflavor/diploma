package dog.wiggler;

import org.jetbrains.annotations.NotNull;

public class Main {
    private static final @NotNull String CACHE="cache";
    private static final @NotNull String HELP="help";
    private static final @NotNull String NAME="wiggler";
    private static final @NotNull String RUN="run";

    public static void cache(String[] args) throws Throwable {
    }

    public static void run(String[] args) throws Throwable {
    }

    public static void help() {
        System.out.printf("%s%n", Main.class.getName());
        System.out.printf("  %s %s%n", NAME, HELP);
        System.out.printf("    prints this screen%n");
        System.out.printf("  %s %s FILE%n", NAME, CACHE);
        System.out.printf("    process memory log FILE%n");
        System.out.printf("  %s %s FILE%n", NAME, RUN);
        System.out.printf("    runs FILE%n");
    }

    public static void main(String[] args) throws Throwable {
        if (0==args.length) {
            help();
            return;
        }
        switch (args[0]) {
            case CACHE -> cache(args);
            case RUN -> run(args);
            default -> help();
        }
    }
}
