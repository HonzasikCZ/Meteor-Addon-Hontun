package cz.honzasik.hontun.commands;

import cz.honzasik.hontun.commands.argument.TickTimeRange;
import cz.honzasik.hontun.commands.argument.TimeRangeArgumentType;
import cz.honzasik.hontun.utils.MCUtil;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.commands.Commands;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.client.multiplayer.PlayerInfo;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.function.BiConsumer;

import static cz.honzasik.hontun.commands.argument.TimeRangeArgumentType.timeRange;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;

public class ForEach extends Command {
    private static final Deque<CommandContext> COMMANDS = new ArrayDeque<>();

    public ForEach() {
        super("foreach", "Executes a command for each player / iteration, optionally with delays.");
        MeteorClient.EVENT_BUS.subscribe(this);
    }

    @Override
    public void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder) {
        builder.then(literal("players")
            .then(argument("command", greedyString())
                .executes(ctx -> {
                    executePlayers(TickTimeRange.NULL, StringArgumentType.getString(ctx, "command"));
                    return SINGLE_SUCCESS;
                }))
            .then(argument("delayBetweenCommands", timeRange())
                .then(argument("command", greedyString())
                    .executes(ctx -> {
                        executePlayers(
                            TimeRangeArgumentType.getTimeRange(ctx, "delayBetweenCommands"),
                            StringArgumentType.getString(ctx, "command"));
                        return SINGLE_SUCCESS;
                    })))
        );

        builder.then(argument("iterations", integer(1))
            .then(argument("command", greedyString())
                .executes(ctx -> {
                    execute(
                        IntegerArgumentType.getInteger(ctx, "iterations"),
                        TickTimeRange.NULL,
                        StringArgumentType.getString(ctx, "command"));
                    return SINGLE_SUCCESS;
                }))
            .then(argument("delayBetweenCommands", timeRange())
                .then(argument("command", greedyString())
                    .executes(ctx -> {
                        execute(
                            IntegerArgumentType.getInteger(ctx, "iterations"),
                            TimeRangeArgumentType.getTimeRange(ctx, "delayBetweenCommands"),
                            StringArgumentType.getString(ctx, "command"));
                        return SINGLE_SUCCESS;
                    })))
        );
    }

    @EventHandler
    private void postTick(TickEvent.Post event) {
        if (!Utils.canUpdate()) {
            if (!COMMANDS.isEmpty()) COMMANDS.clear();
            return;
        }

        long currentTime = MCUtil.MC.clientTickCount;

        for (Iterator<CommandContext> iterator = COMMANDS.iterator(); iterator.hasNext();) {
            CommandContext ctx = iterator.next();

            if (ctx.executeTime <= currentTime) {
                iterator.remove();
                ctx.execute(this);
            }
        }
    }

    private void execute(int iterations, TickTimeRange timeRange, String commandTemplate) {
        long lastDelayTicks = 0;

        for (int i = 0; i < iterations; i++) {
            String command = commandTemplate.replace("%iteration%", String.valueOf(i));
            COMMANDS.add(new CommandContext(command, MCUtil.MC.clientTickCount + lastDelayTicks));
            lastDelayTicks += timeRange.getRandomPoint();
        }
    }

    private void executePlayers(TickTimeRange timeRange, String commandTemplate) {
        long lastDelayTicks = 0;

        for (PlayerInfo entry : MCUtil.getPlayNetHandler().getOnlinePlayers()) {
            String name = entry.getProfile().name();

            String command = commandTemplate.replace("%player%", name);
            COMMANDS.add(new CommandContext(command, MCUtil.MC.clientTickCount + lastDelayTicks));
            lastDelayTicks += timeRange.getRandomPoint();
        }
    }

    private record CommandContext(String command, CommandType type, long executeTime) {
        private CommandContext(String command, long executeTime) {
            this(command, CommandType.fromCommand(command), executeTime);
        }

        private void execute(ForEach module) {
            type.execute(module, command);
        }
    }

    private enum CommandType {
        METEOR((m, message) -> {
            try {
                Commands.dispatch(message.substring(1));
            } catch (CommandSyntaxException e) {
                m.error(e.getMessage());
            }
        }),
        SERVER((m, command) -> MCUtil.sendCommand(command));

        private final BiConsumer<ForEach, String> dispatcher;

        CommandType(BiConsumer<ForEach, String> executor) {
            this.dispatcher = executor;
        }

        private void execute(ForEach module, String command) {
            dispatcher.accept(module, command);
        }

        private static CommandType fromCommand(String command) {
            return command.charAt(0) == '.' ? METEOR : SERVER;
        }
    }
}
