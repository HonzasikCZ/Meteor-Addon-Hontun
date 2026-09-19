package cz.honzasik.hontun.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;

public class ToggleTab extends Command {
    public static boolean shown = false;

    public ToggleTab() {
        super("toggletab", "Keeps the player list (tab) visible without holding the key.");
    }

    @Override
    public void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder) {
        builder.executes(ctx -> {
            shown = !shown;
            info("Tab list %s.", shown ? "locked visible" : "back to normal");
            return SINGLE_SUCCESS;
        });
    }
}
