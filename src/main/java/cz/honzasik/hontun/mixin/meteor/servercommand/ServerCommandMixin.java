package cz.honzasik.hontun.mixin.meteor.servercommand;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import cz.honzasik.hontun.commands.AnticheatProbe;
import cz.honzasik.hontun.gui.screen.server.ServerInfoScreen;
import cz.honzasik.hontun.commands.PluginScanner;
import cz.honzasik.hontun.commands.PlayerRoster;
import cz.honzasik.hontun.commands.SoftwareProbe;
import cz.honzasik.hontun.commands.VersionProbe;
import cz.honzasik.hontun.mixin.network.custompayload.ClientListenerConnectionAccessor;
import cz.honzasik.hontun.modules.AnticheatDetect;
import cz.honzasik.hontun.modules.ChannelFetcher;
import net.minecraft.network.Connection;
import cz.honzasik.hontun.utils.HontunChat;
import cz.honzasik.hontun.utils.HontunTheme;
import cz.honzasik.hontun.utils.RealVersion;
import cz.honzasik.hontun.utils.ResourcePackInfo;
import cz.honzasik.hontun.utils.ServerSoftware;
import cz.honzasik.hontun.utils.VersionKeeper;
import cz.honzasik.hontun.utils.WorldInfo;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.commands.commands.ServerCommand;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Mixin(value = ServerCommand.class, remap = false)
public abstract class ServerCommandMixin {
    @Shadow @Final private List<String> plugins;
    @Shadow private boolean tick;
    @Shadow private int ticks;

    @Shadow public abstract String formatPerms();

    @Inject(method = "basicInfo", at = @At("HEAD"), cancellable = true)
    private void hontun$basicInfo(CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.hasSingleplayerServer()) return;
        ServerData server = mc.getCurrentServer();
        if (server == null) return;

        ci.cancel();
        hontun$open(ServerInfoScreen.Page.OVERVIEW);
    }

    private boolean hontun$open(ServerInfoScreen.Page page) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.hasSingleplayerServer() || mc.getCurrentServer() == null) return false;
        String perms;
        try {
            perms = formatPerms();
        } catch (Throwable t) {
            perms = "unknown";
        }
        ServerInfoScreen.open(page, perms);
        return true;
    }

    @Inject(method = "printPlugins", at = @At("HEAD"), cancellable = true)
    private void hontun$filterAndFallback(CallbackInfo ci) {
        plugins.removeIf(p -> PluginScanner.EXCLUDE_NS.contains(p.toLowerCase()));

        if (plugins.isEmpty()) {
            tick = false;
            ticks = 0;
            plugins.clear();
            PluginScanner.INSTANCE.run(false, false, true, false);
            ci.cancel();
        }
    }

    @Inject(method = "build", at = @At("TAIL"))
    private void hontun$addPluginMethods(LiteralArgumentBuilder<ClientSuggestionProvider> builder, CallbackInfo ci) {
        builder.then(LiteralArgumentBuilder.<ClientSuggestionProvider>literal("plugins")
            .executes(c -> { PluginScanner.INSTANCE.run(true, true, false, true); hontun$open(ServerInfoScreen.Page.PLUGINS); return 1; })
            .then(LiteralArgumentBuilder.<ClientSuggestionProvider>literal("default")
                .executes(c -> { PluginScanner.INSTANCE.run(true, true, false, false); hontun$open(ServerInfoScreen.Page.PLUGINS); return 1; }))
            .then(LiteralArgumentBuilder.<ClientSuggestionProvider>literal("bypass")
                .executes(c -> { PluginScanner.INSTANCE.run(false, false, true, false); hontun$open(ServerInfoScreen.Page.PLUGINS); return 1; })));

        builder.then(LiteralArgumentBuilder.<ClientSuggestionProvider>literal("channels")
            .executes(c -> { if (!hontun$open(ServerInfoScreen.Page.CHANNELS)) hontun$printChannels(); return 1; }));

        builder.then(LiteralArgumentBuilder.<ClientSuggestionProvider>literal("players")
            .executes(c -> { PlayerRoster.INSTANCE.run(); hontun$open(ServerInfoScreen.Page.PLAYERS); return 1; }));

        builder.then(LiteralArgumentBuilder.<ClientSuggestionProvider>literal("software")
            .executes(c -> { if (!hontun$open(ServerInfoScreen.Page.SOFTWARE)) SoftwareProbe.INSTANCE.run(); return 1; }));

        builder.then(LiteralArgumentBuilder.<ClientSuggestionProvider>literal("anticheat")
            .executes(c -> { if (!hontun$open(ServerInfoScreen.Page.ANTICHEAT)) AnticheatProbe.run(); return 1; }));

        builder.then(LiteralArgumentBuilder.<ClientSuggestionProvider>literal("version")
            .executes(c -> { if (!hontun$open(ServerInfoScreen.Page.VERSION)) VersionProbe.run(); return 1; }));
    }

    private void hontun$printChannels() {
        Command self = (Command) (Object) this;
        Set<String> all = new LinkedHashSet<>();
        synchronized (ChannelFetcher.REGISTERED) {
            all.addAll(ChannelFetcher.REGISTERED);
        }
        synchronized (ChannelFetcher.SEEN) {
            all.addAll(ChannelFetcher.SEEN);
        }
        if (all.isEmpty()) {
            self.info("Plugin channels: none detected.");
            return;
        }
        self.info("Plugin channels (%d):", all.size());
        for (String ch : all) {
            self.info(" - %s", ch);
        }
    }
}
