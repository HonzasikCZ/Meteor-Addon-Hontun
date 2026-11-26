package cz.honzasik.hontun.copy;

import cz.honzasik.hontun.copy.capture.Hologram;
import cz.honzasik.hontun.copy.capture.HologramScanner;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.game.GameLeftEvent;
import meteordevelopment.meteorclient.events.meteor.MouseClickEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.utils.misc.input.KeyAction;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.List;

public final class HologramPicker {
    public static final HologramPicker INSTANCE = new HologramPicker();

    private static final Color SIDE = new Color(80, 160, 255, 35);
    private static final Color LINE = new Color(80, 160, 255, 255);
    private static final int LEFT = 0;

    private boolean subscribed;
    private boolean active;
    private List<Target> targets = List.of();
    private HologramScanner.Found aimed;
    private int ticks;

    private HologramPicker() {}

    public boolean active() {
        return active;
    }

    public List<Target> targets() {
        return targets;
    }

    public void start(List<Target> targets) {
        if (!subscribed) {
            MeteorClient.EVENT_BUS.subscribe(this);
            subscribed = true;
        }
        this.targets = List.copyOf(targets);
        this.active = true;
        this.aimed = null;
    }

    public void stop() {
        active = false;
        aimed = null;
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (!active) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            stop();
            return;
        }
        if (ticks++ % 4 != 0) return;
        aimed = HologramScanner.aimed(HologramScanner.scan(), 1.0f);
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (!active || aimed == null) return;
        event.renderer.box(aimed.box(), SIDE, LINE, ShapeMode.Both, 0);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onMouse(MouseClickEvent event) {
        if (!active || event.action != KeyAction.Press || event.button() != LEFT) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.gui.screen() != null || mc.level == null || mc.player == null) return;
        HologramScanner.Found found = HologramScanner.aimed(HologramScanner.scan(), 1.0f);
        if (found == null) return;
        event.cancel();
        aimed = found;
        Hologram h = found.hologram();
        Copier.Report report = Copier.hologram(h, targets);
        ChatUtils.sendMsg(0, "Copy", ChatFormatting.LIGHT_PURPLE, Copier.describe(report.wrote() ? describe(h) : "Nothing was copied", report));
    }

    @EventHandler
    private void onLeave(GameLeftEvent event) {
        if (!active) return;
        stop();
    }

    private static String describe(Hologram h) {
        return "Hologram copied";
    }

    public static Component hint() {
        return Component.literal("Aim at a hologram (it gets a blue box) and left-click to copy it. Use .copy holograms off to stop.");
    }
}
