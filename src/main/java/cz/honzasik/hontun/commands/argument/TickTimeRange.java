package cz.honzasik.hontun.commands.argument;

import cz.honzasik.hontun.utils.Util;

public class TickTimeRange {
    public static final TickTimeRange NULL = new TickTimeRange(0);

    private final long minTimeStamp;
    private final long maxTimeStamp;

    public TickTimeRange(long point) {
        this(point, point);
    }

    public TickTimeRange(long minTimeStamp, long maxTimeStamp) {
        this.minTimeStamp = minTimeStamp;
        this.maxTimeStamp = maxTimeStamp + 1;
    }

    public long getRandomPoint() {
        return Util.RANDOM.nextLong(minTimeStamp, maxTimeStamp);
    }
}
