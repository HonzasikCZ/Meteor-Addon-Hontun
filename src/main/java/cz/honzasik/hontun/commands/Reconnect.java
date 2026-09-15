package cz.honzasik.hontun.commands;

import cz.honzasik.hontun.utils.MCUtil;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.systems.accounts.types.CrackedAccount;
import meteordevelopment.meteorclient.utils.Utils;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static com.mojang.brigadier.arguments.StringArgumentType.word;

public class Reconnect extends Command {
    public Reconnect() {
        super("reconnect", "Reconnects to the server you are playing on, optionally with another name.", "rejoin");
    }

    @Override
    public void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder) {
        builder.executes(ctx -> {
            execute(null);
            return SINGLE_SUCCESS;
        });
        builder.then(argument("new_name", word()).executes(ctx -> {
            execute(StringArgumentType.getString(ctx, "new_name"));
            return SINGLE_SUCCESS;
        }));
    }

    private void execute(@Nullable String newName) {
        if (!Utils.canUpdate()) return;

        if (MCUtil.MC.hasSingleplayerServer()) {
            warning("You are in Singleplayer!");
            return;
        }

        if (newName != null) {
            new CrackedAccount(newName).login();
        }

        ServerData server = MCUtil.MC.getCurrentServer();
        if (server == null) {
            warning("No current server entry.");
            return;
        }

        MCUtil.MC.disconnect(new JoinMultiplayerScreen(new TitleScreen()), false);

        CompletableFuture.delayedExecutor(100, TimeUnit.MILLISECONDS).execute(() -> MCUtil.forceMainThread(() ->

            ConnectScreen.startConnecting(new JoinMultiplayerScreen(new TitleScreen()),
                MCUtil.MC,
                ServerAddress.parseString(server.ip),
                server,
                false,
                null
            )
        ));
    }
}
