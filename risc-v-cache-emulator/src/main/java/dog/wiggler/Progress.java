package dog.wiggler;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.Instant;

@FunctionalInterface
public interface Progress {
    @NotNull Progress NO_OP=(label, percent)->{
    };

    class SystemOut implements AutoCloseable, Progress {
        private @Nullable String lastLabel;
        private long lastPercent=Long.MIN_VALUE;
        private int printedCharacters;
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
