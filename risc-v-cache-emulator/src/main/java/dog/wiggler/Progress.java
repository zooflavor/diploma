package dog.wiggler;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.Instant;

/**
 * Prints out progress info. A progress info is a label and a percent of the work done.
 */
@FunctionalInterface
public interface Progress {
    /**
     * Prints nowhere.
     */
    @NotNull Progress NO_OP=(label, percent)->{
    };

    /**
     * Prints to the system out.
     * Updates the printing only when the label or the percent changes.
     * Changing labels causes a new line to be emitted.
     * Closing the progress causes a new line to be emitted.
     */
    class SystemOut implements AutoCloseable, Progress {
        /**
         * The label currently printed.
         */
        private @Nullable String lastLabel;
        /**
         * The percent currently printed.
         * {@link Long#MIN_VALUE} means it's not printed.
         */
        private long lastPercent=Long.MIN_VALUE;
        /**
         * The number of characters printed on the line.
         */
        private int printedCharacters;
        /**
         */
        private final @NotNull Instant startTime=Instant.now();

        @Override
        public void close() {
            System.out.println();
            printedCharacters=0;
        }

        public void progress(
                @NotNull String label,
                long percent) {
            if (label.equals(lastLabel) && (percent==lastPercent)) {
                return;
            }
            if ((0<printedCharacters) && (!label.equals(lastLabel))) {
                System.out.println();
                printedCharacters=0;
            }
            lastLabel=label;
            lastPercent=percent;
            var sb=new StringBuilder(3*printedCharacters);
            for (; 0<printedCharacters; --printedCharacters) {
                sb.append("\u0008 \u0008");
            }
            var duration=Duration.between(startTime, Instant.now());
            var string="%s, %3d%%, started %2d:%02d:%02d ago"
                    .formatted(
                            label,
                            percent,
                            duration.toHoursPart(),
                            duration.toMinutesPart(),
                            duration.toSecondsPart());
            System.out.print(sb);
            System.out.print(string);
            System.out.flush();
            printedCharacters=string.length();
        }
    }

    void progress(
            @NotNull String label,
            long percent);
}
