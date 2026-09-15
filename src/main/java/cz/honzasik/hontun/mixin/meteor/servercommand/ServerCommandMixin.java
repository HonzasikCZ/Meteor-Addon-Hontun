package cz.honzasik.hontun.mixin.meteor.servercommand;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import cz.honzasik.hontun.commands.PluginScanner;
import cz.honzasik.hontun.modules.ChannelFetcher;
import cz.honzasik.hontun.utils.VersionKeeper;
import cz.honzasik.hontun.utils.WorldInfo;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.commands.commands.ServerCommand;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.InetAddress;
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
        Command self = (Command) (Object) this;

        String ipv4 = "";
        try {
            ipv4 = InetAddress.getByName(server.ip).getHostAddress();
        } catch (Throwable ignored) {
        }
        self.info("IP: %s%s", server.ip, ipv4.isEmpty() ? "" : " (" + ipv4 + ")");
        try {
            self.info("Port: %d", ServerAddress.parseString(server.ip).getPort());
        } catch (Throwable ignored) {
        }

        String brand = mc.getConnection() != null ? mc.getConnection().serverBrand() : null;
        self.info("Type: %s", brand != null ? brand : "unknown");
        self.info("Motd: %s", server.motd != null ? server.motd.getString() : "unknown");
        self.info("Version: %s", server.version != null ? server.version.getString() : "unknown");
        if (!"unknown".equals(VersionKeeper.version)) {
            self.info("Real version: %s", VersionKeeper.version);
        }
        self.info("Protocol version: %d", server.protocol);

        if (mc.level != null) {
            self.info("Difficulty: %s", mc.level.getDifficulty().getDisplayName().getString());
            self.info("Day: %d", mc.level.getGameTime() / 24000L);
        }

        String perms;
        try {
            perms = formatPerms();
        } catch (Throwable t) {
            perms = "unknown";
        }
        self.info("Permission level: %s", perms);

        if (WorldInfo.captured) {
            self.info("Dimension: %s", WorldInfo.dimension);
            self.info("Hashed seed: %s", String.valueOf(WorldInfo.hashedSeed));
            self.info("Secure chat: %s", WorldInfo.secureChat ? "enabled" : "disabled");
            self.info("Hardcore: %s", WorldInfo.hardcore ? "enabled" : "disabled");
            self.info("Max players: %d", WorldInfo.maxPlayers);
            self.info("Render distance: %d", WorldInfo.chunkRadius);
            self.info("Simulation distance: %d", WorldInfo.simulationDistance);

            List<String> lv = new ArrayList<>();
            synchronized (WorldInfo.levels) {
                lv.addAll(WorldInfo.levels);
            }
            if (!lv.isEmpty()) {
                self.info("Server dimensions (%d):", lv.size());
                for (String d : lv) {
                    self.info(" - %s", d);
                }
            }
        }

        Set<String> all = new LinkedHashSet<>();
        synchronized (ChannelFetcher.REGISTERED) {
            all.addAll(ChannelFetcher.REGISTERED);
        }
        synchronized (ChannelFetcher.SEEN) {
            all.addAll(ChannelFetcher.SEEN);
        }
        if (all.isEmpty()) {
            self.info("Plugin channels: none detected.");
        } else {
            self.info("Plugin channels (%d):", all.size());
            for (String ch : all) {
                self.info(" - %s", ch);
            }
        }
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
            .executes(c -> { PluginScanner.INSTANCE.run(true, true, false, true); return 1; })
            .then(LiteralArgumentBuilder.<ClientSuggestionProvider>literal("default")
                .executes(c -> { PluginScanner.INSTANCE.run(true, true, false, false); return 1; }))
            .then(LiteralArgumentBuilder.<ClientSuggestionProvider>literal("bypass")
                .executes(c -> { PluginScanner.INSTANCE.run(false, false, true, false); return 1; })));

        builder.then(LiteralArgumentBuilder.<ClientSuggestionProvider>literal("channels")
            .executes(c -> { hontun$printChannels(); return 1; }));
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
