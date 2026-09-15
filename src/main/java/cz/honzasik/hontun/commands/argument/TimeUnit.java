package cz.honzasik.hontun.commands.argument;

public enum TimeUnit {
    TICKS("t", 1),
    SECONDS("s", 20);

    final String suffix;
    final int ticksPerUnit;

    TimeUnit(String suffix, int ticksPerUnit) {
        this.suffix = suffix;
        this.ticksPerUnit = ticksPerUnit;
    }

    public long toTicks(long time) {
        return time * ticksPerUnit;
    }

    public static TimeUnit fromString(String string) {
        if (string.isEmpty()) return SECONDS;

        for (TimeUnit unit : TimeUnit.values()) {
            if (string.equalsIgnoreCase(unit.suffix)) {
                return unit;
            }
        }

        return null;
    }
}
