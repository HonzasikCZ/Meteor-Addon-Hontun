package cz.honzasik.hontun.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import cz.honzasik.hontun.inspect.ScoreboardInspector;
import cz.honzasik.hontun.inspect.TabInspector;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.network.chat.MutableComponent;

import java.util.List;
import java.util.function.Supplier;

public class InspectCommand extends Command {
    private record Target(String name, Supplier<MutableComponent> run) {}

    private static final List<Target> TARGETS = List.of(
        new Target("scoreboard", ScoreboardInspector::inspect),
        new Target("tab", TabInspector::inspect)
    );

    public InspectCommand() {
        super("inspect", "Shows what the server really sent for the scoreboard and more.");
    }

    @Override
    public void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder) {
        for (Target target : TARGETS) {
            builder.then(literal(target.name()).executes(ctx -> {
                MutableComponent msg = target.run().get();
                if (msg == null) error("Not connected to a server.");
                else info(msg);
                return SINGLE_SUCCESS;
            }));
        }
    }
}
