package cz.honzasik.hontun.modules;

import cz.honzasik.hontun.Hontun;
import cz.honzasik.hontun.captcha.CaptchaMap;
import cz.honzasik.hontun.captcha.CaptchaModel;
import cz.honzasik.hontun.captcha.CaptchaNet;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.KeybindSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ClientboundMapItemDataPacket;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

import java.util.Optional;

public class CaptchaSolver extends Module {
    public enum Mode { Send, Suggest }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Mode> mode = sgGeneral.add(new EnumSetting.Builder<Mode>()
        .name("mode")
        .description("Send = type the solved code into chat automatically when confident. Suggest = only show the guess and you type it.")
        .defaultValue(Mode.Send)
        .build());

    private final Setting<Double> minConfidence = sgGeneral.add(new DoubleSetting.Builder()
        .name("min-confidence")
        .description("Only send automatically when the model is at least this sure of the whole code.")
        .defaultValue(0.8)
        .min(0).max(1)
        .sliderRange(0, 1)
        .visible(() -> mode.get() == Mode.Send)
        .build());

    private final Setting<Keybind> solveKey = sgGeneral.add(new KeybindSetting.Builder()
        .name("solve-key")
        .description("Solve the current map on demand, ignoring the confidence gate and cooldown.")
        .defaultValue(Keybind.fromKey(-1))
        .build());

    private volatile byte[] pending;
    private boolean prevPressed;
    private String lastSent;
    private long lastSendAt;

    public CaptchaSolver() {
        super(Hontun.CATEGORY, "captcha-solver", "Reads a map captcha (Sonar, LimboFilter and similar) and types the code for you.");
        runInMainMenu = true;
    }

    @Override
    public void onActivate() {
        pending = null;
        lastSent = null;
        prevPressed = false;
        CaptchaModel.get();
    }

    @EventHandler
    private void onReceive(PacketEvent.Receive event) {
        if (!(event.packet instanceof ClientboundMapItemDataPacket p)) return;
        Optional<MapItemSavedData.MapPatch> cp = p.colorPatch();
        if (cp.isEmpty()) return;
        MapItemSavedData.MapPatch patch = cp.get();
        if (patch.startX() != 0 || patch.startY() != 0 || patch.width() != 128 || patch.height() != 128) return;
        byte[] colors = patch.mapColors();
        if (colors == null || colors.length != 128 * 128) return;
        pending = colors.clone();
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        boolean pressed = solveKey.get().isSet() && solveKey.get().isPressed();
        boolean force = pressed && !prevPressed;
        prevPressed = pressed;

        byte[] colors = pending;
        if (colors == null) return;
        pending = null;

        CaptchaNet net = CaptchaModel.get();
        if (net == null) {
            if (force) warning("Captcha model is not loaded.");
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.player.connection == null) return;

        CaptchaNet.Result r = net.recognizeGray(CaptchaMap.toGray(colors, net));
        if (r.text.isEmpty()) {
            if (force) warning("Could not read a code on the map.");
            return;
        }
        int pct = Math.round(r.confidence * 100);

        boolean doSend = mode.get() == Mode.Send && (force || r.confidence >= minConfidence.get());
        if (doSend) {
            long now = System.currentTimeMillis();
            if (!force && r.text.equals(lastSent) && now - lastSendAt < 5000) return;
            lastSent = r.text;
            lastSendAt = now;
            mc.player.connection.sendChat(r.text);
            Hontun.LOG.info("[Hontun] captcha solved: {} ({}%)", r.text, pct);
            info("Solved captcha: (highlight)%s(default) (%d%%).", r.text, pct);
        } else {
            info("Captcha guess: (highlight)%s(default) (%d%%) - type it if it is right.", r.text, pct);
        }
    }
}
