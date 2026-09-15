package cz.honzasik.hontun.modules;

import cz.honzasik.hontun.Hontun;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket.Action;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket.Entry;
import net.minecraft.world.level.GameType;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class GamemodeNotify extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> message = sgGeneral.add(new BoolSetting.Builder()
        .name("message")
        .description("Puts a message in chat.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> notification = sgGeneral.add(new BoolSetting.Builder()
        .name("notification")
        .description("Notifies you with a toast.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> important = sgGeneral.add(new BoolSetting.Builder()
        .name("important")
        .description("Colours the toast red so it stands out.")
        .defaultValue(false)
        .visible(notification::get)
        .build()
    );

    private static final long COOLDOWN_MS = 500L;
    private static final int MAX_PER_PACKET = 16;
    private final Map<UUID, Long> lastAlert = new ConcurrentHashMap<>();

    public GamemodeNotify() {
        super(Hontun.CATEGORY, "gamemode-notify", "Alerts you when someone changes their gamemode.");
    }

    @Override
    public void onDeactivate() {
        lastAlert.clear();
    }

    @EventHandler
    public void onPacket(PacketEvent.Receive event) {
        if (!Utils.canUpdate()) return;
        if (!(event.packet instanceof ClientboundPlayerInfoUpdatePacket packet)) return;
        if (!packet.actions().contains(Action.UPDATE_GAME_MODE)) return;

        ClientPacketListener conn = Minecraft.getInstance().getConnection();
        if (conn == null) return;

        long now = System.currentTimeMillis();
        int done = 0;

        for (Entry entry : packet.entries()) {
            if (done >= MAX_PER_PACKET) break;

            if (packet.newEntries().contains(entry)) continue;

            GameType mode = entry.gameMode();
            if (mode == null) continue;

            UUID id = entry.profileId();
            Long last = lastAlert.get(id);
            if (last != null && now - last < COOLDOWN_MS) continue;
            lastAlert.put(id, now);
            done++;

            String name = resolveName(conn, entry);

            if (message.get()) {
                info("%s has switched to %s mode!", name, mode.getName());
            }
            if (notification.get()) {
                Component title = Component.literal("Gamemode Notifier")
                        .withStyle(important.get() ? ChatFormatting.RED : ChatFormatting.WHITE);
                Component body = Component.literal(name + ": ").append(mode.getLongDisplayName());
                Minecraft.getInstance().execute(() -> SystemToast.add(
                        Minecraft.getInstance().gui.toastManager(),
                        SystemToast.SystemToastId.NARRATOR_TOGGLE,
                        title, body));
            }
        }
    }

    private static String resolveName(ClientPacketListener conn, Entry entry) {
        PlayerInfo info = conn.getPlayerInfo(entry.profileId());
        if (info != null) return info.getProfile().name();
        if (entry.profile() != null) return entry.profile().name();
        return entry.profileId().toString();
    }
}
