package dog.wiggler.cache;

import dog.wiggler.memory.AccessType;
import dog.wiggler.memory.Log;
import org.jetbrains.annotations.NotNull;

import java.util.Deque;
import java.util.LinkedList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestLog implements Log {
    public final @NotNull Deque<@NotNull String> log=new LinkedList<>();
    private final boolean print;

    public TestLog(boolean print) {
        this.print=print;
    }

    public TestLog() {
        this(false);
    }

    @Override
    public Void access(long address, int size, @NotNull AccessType type) {
        addLog("A,0x%x,0x%x,%s"
                .formatted(
                        address,
                        size,
                        switch (type) {
                            case LOAD_DATA -> "LD";
                            case LOAD_INSTRUCTION -> "LI";
                            case STORE -> "S";
                        }));
        return null;
    }

    @Override
    public Void accessLogDisabled() {
        addLog("ALD");
        return null;
    }

    @Override
    public Void accessLogEnabled() {
        addLog("ALE");
        return null;
    }

    private void addLog(@NotNull String value) {
        log.addLast(value);
        if (print) {
            System.out.println(value);
        }
    }

    public void assertLog(String @NotNull ... entries) {
        for (var entry: entries) {
            assertEquals(
                    entry,
                    log.removeFirst(),
                    log.toString());
        }
        assertTrue(log.isEmpty(), log.toString());
    }

    @Override
    public void close() {
    }

    @Override
    public Void elapsedCycles(long elapsedCycles) {
        addLog("EC,%d".formatted(elapsedCycles));
        return null;
    }

    @Override
    public Void end() {
        addLog("E");
        return null;
    }

    @Override
    public Void userData(long userData) {
        addLog("UD,%d".formatted(userData));
        return null;
    }
}
