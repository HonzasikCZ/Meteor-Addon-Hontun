package cz.honzasik.hontun.modules;

import cz.honzasik.hontun.Hontun;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.render.Render2DEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.Renderer2D;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.NametagUtils;
import meteordevelopment.meteorclient.utils.render.RenderUtils;
import meteordevelopment.meteorclient.utils.render.WireframeEntityRenderer;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundBlockDestructionPacket;
import net.minecraft.network.protocol.game.ClientboundLevelEventPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class BypassESP extends Module {
    public enum Targets { Players, PlayersAndMobs, AllLiving }

    public enum Mode {
        Box, Wireframe, _2D;

        @Override
        public String toString() {
            return this == _2D ? "2D" : name();
        }
    }

    private static final int MAX_MARKS = 1024;
    private static final int LEVEL_EVENT_BLOCK_BREAK = 2001;
    private static final long INFER_PERSIST = 3000L;
    private static final double SNAP_RADIUS_SQ = 16.0;
    private static final long SNAP_STALE_MS = 200L;

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgInfer = settings.createGroup("Inference");
    private final SettingGroup sgRender = settings.createGroup("Render");

    private final Setting<Targets> targets = sgGeneral.add(new EnumSetting.Builder<Targets>()
        .name("targets")
        .description("Which entities to keep rendered through anti-ESP hiding.")
        .defaultValue(Targets.Players)
        .build());

    private final Setting<Integer> range = sgGeneral.add(new IntSetting.Builder()
        .name("range")
        .description("Only track entities this many blocks away at most.")
        .defaultValue(256)
        .min(0).max(2048)
        .sliderRange(16, 512)
        .build());

    private final Setting<Double> persist = sgGeneral.add(new DoubleSetting.Builder()
        .name("persist-seconds")
        .description("Keep an entity rendered this long after the server stops sending it (defeats re-hide and removal).")
        .defaultValue(12.0)
        .min(0).max(120)
        .sliderRange(0, 60)
        .build());

    private final Setting<Boolean> soundMarks = sgInfer.add(new BoolSetting.Builder()
        .name("sound-marks")
        .description("Infer acting hidden players from positioned side channels the server cannot suppress: action sounds (block break/place, combat, bow, eat) and projectile spawns (arrows, tridents, snowballs, pearls appear at the shooter). Snap a nearby hidden entity's box onto the leaked coordinate so it keeps tracking that player through walls, and drop a standalone mark when no tracked entity is close.")
        .defaultValue(false)
        .build());

    private final Setting<Mode> mode = sgRender.add(new EnumSetting.Builder<Mode>()
        .name("mode")
        .description("How tracked entities are drawn: Box = 3D box through walls, Wireframe = the entity's exact model outline so you can tell what it is (falls back to a box once the server has fully removed the entity), 2D = flat screen-space box like a normal ESP.")
        .defaultValue(Mode.Box)
        .build());

    private final Setting<Boolean> boxes = sgRender.add(new BoolSetting.Builder()
        .name("boxes")
        .description("Draw a box around each tracked entity.")
        .defaultValue(true)
        .build());

    private final Setting<Boolean> tracers = sgRender.add(new BoolSetting.Builder()
        .name("tracers")
        .description("Draw a line from the camera to each tracked entity.")
        .defaultValue(false)
        .build());

    private final Setting<ShapeMode> shapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>()
        .name("shape-mode")
        .description("How the boxes are drawn.")
        .defaultValue(ShapeMode.Both)
        .visible(() -> boxes.get())
        .build());

    private final Setting<Boolean> fade = sgRender.add(new BoolSetting.Builder()
        .name("fade")
        .description("Fade a mark out the longer it is since the server last confirmed its position.")
        .defaultValue(true)
        .build());

    private final Setting<SettingColor> sideColor = sgRender.add(new ColorSetting.Builder()
        .name("side-color")
        .description("Box fill colour.")
        .defaultValue(new SettingColor(62, 140, 255, 50))
        .visible(() -> boxes.get())
        .build());

    private final Setting<SettingColor> lineColor = sgRender.add(new ColorSetting.Builder()
        .name("line-color")
        .description("Box outline, tracer and sound-mark colour.")
        .defaultValue(new SettingColor(62, 140, 255, 220))
        .build());

    private static final class Mark {
        volatile double x, y, z, w, h;
        volatile long seen;
        volatile boolean player;
    }

    private static final class Infer {
        final double x, y, z;
        volatile long seen;
        Infer(double x, double y, double z, long seen) { this.x = x; this.y = y; this.z = z; this.seen = seen; }
    }

    private final Map<Integer, Mark> marks = new ConcurrentHashMap<>();
    private final Map<Long, Infer> inferred = new ConcurrentHashMap<>();
    private final Set<Integer> knownProjectiles = ConcurrentHashMap.newKeySet();
    private volatile double sx, sy, sz;
    private volatile boolean hasSelf;
    private final Vector3d proj = new Vector3d();
    private final Vector3d p1 = new Vector3d();
    private final Vector3d p2 = new Vector3d();

    public BypassESP() {
        super(Hontun.CATEGORY, "BypassESP", "Keeps players and entities rendered even when a server anti-ESP plugin tries to hide them: renders what the client receives, holds the last known spot after the server stops sending it, and can mark action-sound coordinates to catch acting hidden players.");
    }

    @Override
    public void onActivate() {
        marks.clear();
        inferred.clear();
        knownProjectiles.clear();
        hasSelf = false;
    }

    @Override
    public void onDeactivate() {
        marks.clear();
        inferred.clear();
        knownProjectiles.clear();
        hasSelf = false;
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            if (!marks.isEmpty()) marks.clear();
            if (!inferred.isEmpty()) inferred.clear();
            hasSelf = false;
            return;
        }
        long now = System.currentTimeMillis();
        sx = mc.player.getX();
        sy = mc.player.getY();
        sz = mc.player.getZ();
        hasSelf = true;
        double maxSq = (double) range.get() * range.get();
        boolean mobs = targets.get() != Targets.Players;
        boolean infer = soundMarks.get();
        if (infer && mc.gameMode != null && mc.gameMode.isDestroying() && mc.hitResult instanceof BlockHitResult bhr) {
            BlockPos bp = bhr.getBlockPos();
            markCoordinate(bp.getX() + 0.5, bp.getY() + 0.5, bp.getZ() + 0.5);
        }
        Set<Integer> presentProjectiles = infer ? new HashSet<>() : null;
        for (Entity entity : mc.level.entitiesForRendering()) {
            if (entity == mc.player) continue;
            boolean wanted = entity instanceof Player || (mobs && entity instanceof LivingEntity);
            if (!wanted) {
                if (infer && entity instanceof Projectile && entity.distanceToSqr(mc.player) <= maxSq) {
                    int id = entity.getId();
                    presentProjectiles.add(id);
                    if (knownProjectiles.add(id)) {
                        markCoordinate(entity.getX(), entity.getY(), entity.getZ());
                    }
                }
                continue;
            }
            if (entity.distanceToSqr(mc.player) > maxSq) continue;
            Mark m = marks.computeIfAbsent(entity.getId(), k -> new Mark());
            m.x = entity.getX();
            m.y = entity.getY();
            m.z = entity.getZ();
            m.w = entity.getBbWidth();
            m.h = entity.getBbHeight();
            m.player = entity instanceof Player;
            m.seen = now;
        }
        if (infer) knownProjectiles.retainAll(presentProjectiles);
        else if (!knownProjectiles.isEmpty()) knownProjectiles.clear();
        long keep = (long) (persist.get() * 1000.0);
        Iterator<Mark> it = marks.values().iterator();
        while (it.hasNext()) {
            if (now - it.next().seen > keep) it.remove();
        }
        if (marks.size() > MAX_MARKS) marks.clear();
        Iterator<Infer> ii = inferred.values().iterator();
        while (ii.hasNext()) {
            if (now - ii.next().seen > INFER_PERSIST) ii.remove();
        }
        if (inferred.size() > MAX_MARKS) inferred.clear();
    }

    @EventHandler
    private void onReceive(PacketEvent.Receive event) {
        if (!soundMarks.get() || !hasSelf) return;
        if (event.packet instanceof ClientboundSoundPacket s) {
            SoundSource src = s.getSource();
            if (src != SoundSource.PLAYERS && src != SoundSource.HOSTILE && src != SoundSource.NEUTRAL && src != SoundSource.BLOCKS) return;
            markCoordinate(s.getX(), s.getY(), s.getZ());
        } else if (event.packet instanceof ClientboundLevelEventPacket le) {
            if (le.getType() != LEVEL_EVENT_BLOCK_BREAK) return;
            BlockPos p = le.getPos();
            markCoordinate(p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5);
        } else if (event.packet instanceof ClientboundBlockDestructionPacket bd) {
            BlockPos p = bd.getPos();
            markCoordinate(p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5);
        }
    }

    private void markCoordinate(double x, double y, double z) {
        double dx = x - sx, dy = y - sy, dz = z - sz;
        double distSq = dx * dx + dy * dy + dz * dz;
        if (distSq < 2.25) return;
        double maxSq = (double) range.get() * range.get();
        if (distSq > maxSq) return;
        long now = System.currentTimeMillis();
        Mark best = null;
        double bestSq = SNAP_RADIUS_SQ;
        for (Mark m : marks.values()) {
            if (now - m.seen < SNAP_STALE_MS) continue;
            double mx = m.x - x, mz = m.z - z;
            double hSq = mx * mx + mz * mz;
            if (hSq < bestSq) {
                bestSq = hSq;
                best = m;
            }
        }
        if (best != null) {
            best.x = x;
            best.z = z;
            best.y = y;
            best.seen = now;
            return;
        }
        long key = (((long) Math.floor(x)) << 42) ^ (((long) Math.floor(y)) << 21) ^ ((long) Math.floor(z));
        inferred.put(key, new Infer(x, y, z, now));
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        long now = System.currentTimeMillis();
        double keep = Math.max(1.0, persist.get() * 1000.0);
        Vec3 cam = RenderUtils.center;
        Color side = sideColor.get();
        Color line = lineColor.get();
        Mode md = mode.get();
        for (Map.Entry<Integer, Mark> entry : marks.entrySet()) {
            Mark m = entry.getValue();
            double age = now - m.seen;
            double f = fade.get() ? Math.max(0.0, 1.0 - age / keep) : 1.0;
            if (f <= 0.0) continue;
            double x1 = m.x - m.w / 2.0, z1 = m.z - m.w / 2.0;
            double x2 = m.x + m.w / 2.0, z2 = m.z + m.w / 2.0;
            double y2 = m.y + m.h;
            Color s = new Color(side.r, side.g, side.b, (int) (side.a * f));
            Color l = new Color(line.r, line.g, line.b, (int) (line.a * f));
            if (boxes.get() && md != Mode._2D) {
                Entity live = md == Mode.Wireframe ? mc.level.getEntity(entry.getKey()) : null;
                if (live != null) {
                    WireframeEntityRenderer.render(event, live, 1.0, s, l, shapeMode.get());
                } else {
                    event.renderer.box(x1, m.y, z1, x2, y2, z2, s, l, shapeMode.get(), 0);
                }
            }
            if (tracers.get() && cam != null) {
                event.renderer.line(cam.x, cam.y, cam.z, m.x, m.y + m.h / 2.0, m.z, l);
            }
        }
        if (soundMarks.get()) {
            for (Infer in : inferred.values()) {
                double age = now - in.seen;
                double f = Math.max(0.0, 1.0 - age / (double) INFER_PERSIST);
                if (f <= 0.0) continue;
                Color l = new Color(line.r, line.g, line.b, (int) (line.a * f * 0.8));
                double bx = Math.floor(in.x), by = Math.floor(in.y), bz = Math.floor(in.z);
                event.renderer.box(bx + 0.2, by, bz + 0.2, bx + 0.8, by + 0.8, bz + 0.8, new Color(line.r, line.g, line.b, (int) (40 * f)), l, ShapeMode.Both, 0);
            }
        }
    }

    @EventHandler
    private void onRender2D(Render2DEvent event) {
        if (mode.get() != Mode._2D || !boxes.get()) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        long now = System.currentTimeMillis();
        double keep = Math.max(1.0, persist.get() * 1000.0);
        Color side = sideColor.get();
        Color line = lineColor.get();
        ShapeMode shape = shapeMode.get();
        Renderer2D.COLOR.begin();
        for (Mark m : marks.values()) {
            double age = now - m.seen;
            double f = fade.get() ? Math.max(0.0, 1.0 - age / keep) : 1.0;
            if (f <= 0.0) continue;
            double hw = m.w / 2.0;
            double minX = m.x - hw, maxX = m.x + hw;
            double minY = m.y, maxY = m.y + m.h;
            double minZ = m.z - hw, maxZ = m.z + hw;
            p1.set(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE);
            p2.set(0.0, 0.0, 0.0);
            if (corner(minX, minY, minZ) || corner(maxX, minY, minZ) || corner(minX, minY, maxZ) || corner(maxX, minY, maxZ)
                || corner(minX, maxY, minZ) || corner(maxX, maxY, minZ) || corner(minX, maxY, maxZ) || corner(maxX, maxY, maxZ)) continue;
            if (shape != ShapeMode.Lines && side.a > 0) {
                Color s = new Color(side.r, side.g, side.b, (int) (side.a * f));
                Renderer2D.COLOR.quad(p1.x, p1.y, p2.x - p1.x, p2.y - p1.y, s);
            }
            if (shape != ShapeMode.Sides) {
                Color l = new Color(line.r, line.g, line.b, (int) (line.a * f));
                Renderer2D.COLOR.line(p1.x, p1.y, p1.x, p2.y, l);
                Renderer2D.COLOR.line(p2.x, p1.y, p2.x, p2.y, l);
                Renderer2D.COLOR.line(p1.x, p1.y, p2.x, p1.y, l);
                Renderer2D.COLOR.line(p1.x, p2.y, p2.x, p2.y, l);
            }
        }
        Renderer2D.COLOR.render();
    }

    private boolean corner(double x, double y, double z) {
        proj.set(x, y, z);
        if (!NametagUtils.to2D(proj, 1.0)) return true;
        if (proj.x < p1.x) p1.x = proj.x;
        if (proj.y < p1.y) p1.y = proj.y;
        if (proj.z < p1.z) p1.z = proj.z;
        if (proj.x > p2.x) p2.x = proj.x;
        if (proj.y > p2.y) p2.y = proj.y;
        if (proj.z > p2.z) p2.z = proj.z;
        return false;
    }
}
