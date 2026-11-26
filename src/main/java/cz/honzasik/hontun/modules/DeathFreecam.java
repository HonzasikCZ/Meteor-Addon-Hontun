package cz.honzasik.hontun.modules;

import com.mojang.blaze3d.platform.InputConstants;
import cz.honzasik.hontun.Hontun;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.KeybindSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.Freecam;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class DeathFreecam extends Module {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgRender = settings.createGroup("Marker");

    private final Setting<Keybind> returnKey = sgGeneral.add(new KeybindSetting.Builder()
        .name("return-key")
        .description("While flying after death, press this to go back to the death screen so you can respawn.")
        .defaultValue(Keybind.fromKey(InputConstants.KEY_R))
        .build());

    private final Setting<Boolean> auto = sgGeneral.add(new BoolSetting.Builder()
        .name("auto-freecam")
        .description("Enter freecam automatically the moment you die, without clicking the button.")
        .defaultValue(false)
        .build());

    private final Setting<Boolean> bypassRespawn = sgGeneral.add(new BoolSetting.Builder()
        .name("bypass-insta-respawn")
        .description("Show the death screen with the Freecam button even on servers that respawn you instantly (doImmediateRespawn), so you can fly your death spot before respawning.")
        .defaultValue(false)
        .build());

    private final Setting<Boolean> waypoint = sgGeneral.add(new BoolSetting.Builder()
        .name("keep-waypoint")
        .description("Keep the death marker after you respawn so you can find your way back to your items; it clears when you get close or die again.")
        .defaultValue(true)
        .build());

    private final Setting<Double> sprintSpeed = sgGeneral.add(new DoubleSetting.Builder()
        .name("sprint-speed")
        .description("Extra fly-speed multiplier while holding the sprint key during freecam.")
        .defaultValue(2.0)
        .min(1.0).max(10.0)
        .sliderRange(1.0, 5.0)
        .build());

    private final Setting<Boolean> levelView = sgGeneral.add(new BoolSetting.Builder()
        .name("level-view")
        .description("Start the freecam looking level at the horizon instead of tilted from the death angle.")
        .defaultValue(true)
        .build());

    private final Setting<Boolean> beam = sgRender.add(new BoolSetting.Builder()
        .name("beam")
        .description("Draw a tall beam at the death spot so it is visible from far away.")
        .defaultValue(true)
        .build());

    private final Setting<SettingColor> color = sgRender.add(new ColorSetting.Builder()
        .name("marker-color")
        .description("Colour of the death marker box and beam.")
        .defaultValue(new SettingColor(255, 170, 0, 255))
        .build());

    private boolean hasDeath;
    private double dx, dy, dz;
    private ResourceKey<Level> deathDim;
    private boolean wasDead;
    private boolean inFreecam;
    private boolean returnKeyWasDown;
    private Screen deathScreen;
    private LocalPlayer autoFor;
    private Double savedSpeed;
    private boolean sprintState;

    public DeathFreecam() {
        super(Hontun.CATEGORY, "DeathFreecam", "After you die, fly a free camera around instead of being stuck on the death screen: adds a Freecam button to the death screen, marks where you died, and brings you back with a key.");
    }

    @Override
    public void onActivate() {
        hasDeath = false;
        inFreecam = false;
        wasDead = false;
        returnKeyWasDown = false;
        deathScreen = null;
        autoFor = null;
        savedSpeed = null;
        sprintState = false;
    }

    @Override
    public void onDeactivate() {
        disableFreecam();
        hasDeath = false;
        inFreecam = false;
        wasDead = false;
        deathScreen = null;
        autoFor = null;
    }

    public static boolean flying() {
        Modules modules = Modules.get();
        if (modules == null) return false;
        DeathFreecam module = (DeathFreecam) modules.get(DeathFreecam.class);
        return module != null && module.isActive() && module.inFreecam;
    }

    public static boolean deadFreecam() {
        Modules modules = Modules.get();
        if (modules == null) return false;
        DeathFreecam module = (DeathFreecam) modules.get(DeathFreecam.class);
        if (module == null || !module.isActive()) return false;
        if (module.inFreecam) return true;
        LocalPlayer player = Minecraft.getInstance().player;
        return player != null && player.isDeadOrDying() && freecamActive();
    }

    public static boolean bypassInstaRespawn() {
        Modules modules = Modules.get();
        if (modules == null) return false;
        DeathFreecam module = (DeathFreecam) modules.get(DeathFreecam.class);
        return module != null && module.isActive() && module.bypassRespawn.get();
    }

    public boolean takeAuto() {
        Minecraft mc = Minecraft.getInstance();
        if (!auto.get() || mc.player == null || autoFor == mc.player) return false;
        autoFor = mc.player;
        return true;
    }

    public void setDeathScreen(Screen screen) {
        deathScreen = screen;
    }

    public void enterFreecam() {
        Minecraft mc = Minecraft.getInstance();
        if (!isActive() || mc.player == null) return;
        if (!inFreecam) {
            Freecam fc = (Freecam) Modules.get().get(Freecam.class);
            if (fc != null && !fc.isActive()) fc.toggle();
            if (fc != null) {
                if (levelView.get()) {
                    fc.pitch = 0.0f;
                    fc.lastPitch = 0.0f;
                }
                savedSpeed = readSpeed(fc);
                sprintState = false;
            }
            inFreecam = true;
            info("Death Freecam active — press " + returnKey.get() + " to go back to the death screen.");
        }
        mc.gui.setScreen(null);
    }

    private void exitFreecam() {
        Minecraft mc = Minecraft.getInstance();
        disableFreecam();
        inFreecam = false;
        if (deathScreen != null && mc.player != null && mc.player.isDeadOrDying()) {
            mc.gui.setScreen(deathScreen);
        }
    }

    private void disableFreecam() {
        Freecam fc = (Freecam) Modules.get().get(Freecam.class);
        if (fc == null) return;
        if (savedSpeed != null) {
            applySpeed(fc, savedSpeed);
            savedSpeed = null;
        }
        if (fc.isActive()) fc.toggle();
        sprintState = false;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void applySpeed(Freecam fc, double value) {
        try {
            Setting s = fc.settings.get("speed");
            if (s != null) s.set(value);
        } catch (Throwable ignored) {
        }
    }

    private static Double readSpeed(Freecam fc) {
        try {
            Setting<?> s = fc.settings.get("speed");
            if (s != null) {
                Object v = s.get();
                if (v instanceof Number) return ((Number) v).doubleValue();
            }
        } catch (Throwable ignored) {
        }
        return null;
    }

    private static boolean freecamActive() {
        Freecam fc = (Freecam) Modules.get().get(Freecam.class);
        return fc != null && fc.isActive();
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            if (inFreecam) disableFreecam();
            hasDeath = false;
            inFreecam = false;
            wasDead = false;
            deathScreen = null;
            return;
        }
        boolean deadNow = mc.player.isDeadOrDying();
        if (deadNow && !wasDead) {
            dx = mc.player.getX();
            dy = mc.player.getY();
            dz = mc.player.getZ();
            deathDim = mc.level.dimension();
            hasDeath = true;
        }
        if (!deadNow && wasDead) {
            if (inFreecam) {
                disableFreecam();
                inFreecam = false;
            }
            if (!waypoint.get()) hasDeath = false;
        }
        wasDead = deadNow;
        if (hasDeath && !deadNow && waypoint.get() && deathDim != null && mc.level.dimension().equals(deathDim)) {
            double ex = mc.player.getX() - dx, ey = mc.player.getY() - dy, ez = mc.player.getZ() - dz;
            if (ex * ex + ey * ey + ez * ez < 9.0) hasDeath = false;
        }
        if (inFreecam && !freecamActive()) {
            disableFreecam();
            inFreecam = false;
        }
        if (inFreecam && savedSpeed != null) {
            boolean sprint = mc.options.keySprint.isDown();
            if (sprint != sprintState) {
                sprintState = sprint;
                Freecam fc = (Freecam) Modules.get().get(Freecam.class);
                if (fc != null) applySpeed(fc, sprint ? savedSpeed * sprintSpeed.get() : savedSpeed);
            }
        }
        boolean down = returnKey.get().isPressed();
        if (inFreecam && down && !returnKeyWasDown && mc.gui.screen() == null) exitFreecam();
        returnKeyWasDown = down;
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (!hasDeath || mc.level == null || deathDim == null || !mc.level.dimension().equals(deathDim)) return;
        Color line = color.get();
        Color fill = new Color(line.r, line.g, line.b, 40);
        Vec3 cam = mc.gameRenderer.mainCamera().position();
        if (!cameraInside(cam, 0.45, 2.0)) {
            event.renderer.box(dx - 0.4, dy, dz - 0.4, dx + 0.4, dy + 2.0, dz + 0.4, fill, line, ShapeMode.Both, 0);
        }
        if (beam.get() && !cameraInside(cam, 0.17, 64.0)) {
            event.renderer.box(dx - 0.12, dy, dz - 0.12, dx + 0.12, dy + 64.0, dz + 0.12, fill, line, ShapeMode.Both, 0);
        }
    }

    private boolean cameraInside(Vec3 cam, double radius, double height) {
        return Math.abs(cam.x - dx) < radius && Math.abs(cam.z - dz) < radius && cam.y > dy - 0.05 && cam.y < dy + height + 0.05;
    }
}
