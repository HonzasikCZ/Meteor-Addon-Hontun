package cz.honzasik.hontun.commands.argument;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import java.util.Arrays;
import java.util.Collection;

public class TimeRangeArgumentType implements ArgumentType<TickTimeRange> {
    private static final Collection<String> EXAMPLES = Arrays.asList("3..5", "3s..5s", "60t..5s", "100s", "100t", "0");

    public static TimeRangeArgumentType timeRange() {
        return new TimeRangeArgumentType();
    }

    public static TickTimeRange getTimeRange(final CommandContext<?> context, final String name) {
        return context.getArgument(name, TickTimeRange.class);
    }

    public static final SimpleCommandExceptionType MIN_LARGER_THAN_MAX_EXCEPTION = new SimpleCommandExceptionType(new LiteralMessage("Min is larger than max"));

    @Override
    public TickTimeRange parse(final StringReader reader) throws CommandSyntaxException {
        int start = reader.getCursor();

        long timeRange = parseTimePoint(reader);

        if (!reader.canRead(2) || reader.peek() != '.' || reader.peek(1) != '.') {
            return new TickTimeRange(timeRange);
        }

        reader.skip();
        reader.skip();

        long max = parseTimePoint(reader);

        if (timeRange > max) {
            reader.setCursor(start);
            throw MIN_LARGER_THAN_MAX_EXCEPTION.create();
        }

        return new TickTimeRange(timeRange, max);
    }

    public static final SimpleCommandExceptionType INVALID_TIME_UNIT_EXCEPTION = new SimpleCommandExceptionType(new LiteralMessage("Expected time unit"));

    public static long parseTimePoint(final StringReader reader) throws CommandSyntaxException {
        int start = reader.getCursor();

        StringBuilder amount = new StringBuilder();

        if (reader.peek() == '-') {
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.integerTooLow().createWithContext(reader, "negative value", 0);
        }

        while (reader.canRead() && reader.peek() >= '0' && reader.peek() <= '9') {
            amount.append(reader.read());
        }

        int parsedAmount;
        try {
            parsedAmount = Integer.parseInt(amount.toString());
        } catch (final NumberFormatException ex) {
            reader.setCursor(start);
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.readerInvalidInt().createWithContext(reader, amount.toString());
        }

        int timeUnitStart = reader.getCursor();
        StringBuilder timeUnit = new StringBuilder();

        while (reader.canRead() && Character.isLetter(reader.peek())) {
            timeUnit.append(reader.read());
        }

        TimeUnit parsedTimeUnit = TimeUnit.fromString(timeUnit.toString());

        if (parsedTimeUnit == null) {
            reader.setCursor(timeUnitStart);
            throw INVALID_TIME_UNIT_EXCEPTION.create();
        }

        return parsedTimeUnit.toTicks(parsedAmount);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        return o instanceof TimeRangeArgumentType;
    }

    @Override
    public String toString() {
        return "timeRange()";
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
