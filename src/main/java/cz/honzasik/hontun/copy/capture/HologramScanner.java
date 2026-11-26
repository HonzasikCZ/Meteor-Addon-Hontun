package cz.honzasik.hontun.copy.capture;

import com.mojang.math.Transformation;
import cz.honzasik.hontun.copy.StyledText;
import cz.honzasik.hontun.mixin.client.copy.BlockDisplayInvoker;
import cz.honzasik.hontun.mixin.client.copy.DisplayInvoker;
import cz.honzasik.hontun.mixin.client.copy.ItemDisplayInvoker;
import cz.honzasik.hontun.mixin.client.copy.TextDisplayInvoker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

public final class HologramScanner {
    public static final double RANGE = 64.0;
    public static final double RIDER_ITEM_TOP = 0.55;
    public static final double LOOSE_ITEM_TOP = 0.2;
    private static final double COLUMN = 0.05;
    private static final double GAP = 1.3;
    private static final double PX = 0.025;

    public record Found(Hologram hologram, AABB box) {}

    private record Part(Hologram.Type type, StyledText text, ItemInfo item, String entity, double x, double top, double at,
                        double z, float yaw, int widthPx, boolean empty, boolean rider) {}

    private HologramScanner() {}

    public static List<Found> scan() {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null) return List.of();
        String dim = level.dimension().identifier().toString();

        List<Part> parts = new ArrayList<>();
        List<Display.TextDisplay> texts = new ArrayList<>();
        List<Display.ItemDisplay> itemDisplays = new ArrayList<>();
        List<Display.BlockDisplay> blockDisplays = new ArrayList<>();
        List<ItemEntity> loose = new ArrayList<>();
        for (Entity e : level.entitiesForRendering()) {
            if (e.isRemoved()) continue;
            if (e instanceof ArmorStand stand) part(mc, stand, parts);
            else if (e instanceof Display.TextDisplay td) texts.add(td);
            else if (e instanceof Display.ItemDisplay id) itemDisplays.add(id);
            else if (e instanceof Display.BlockDisplay bd) blockDisplays.add(bd);
            else if (e instanceof ItemEntity ie && ie.getVehicle() == null && ie.isNoGravity() && !ie.getItem().isEmpty()) loose.add(ie);
            else if (e instanceof LivingEntity le && !(le instanceof Player) && le.getVehicle() == null && le.isSilent() && le.isNoGravity()) {
                String type = BuiltInRegistries.ENTITY_TYPE.getKey(le.getType()).toString();
                parts.add(new Part(Hologram.Type.ENTITY, null, null, type, le.getX(), le.getY() + 0.25 + le.getBbHeight(), le.getY(),
                    le.getZ(), le.getYRot(), 16, true, false));
            }
        }

        List<Found> out = new ArrayList<>();
        Set<Entity> claimed = new HashSet<>();
        for (Display.TextDisplay td : texts) out.add(textDisplay(mc, td, dim, loose, itemDisplays, claimed));

        for (ItemEntity ie : loose) {
            if (claimed.contains(ie)) continue;
            parts.add(new Part(Hologram.Type.ICON, null, ItemInfo.of(ie.getItem()), null, ie.getX(), ie.getY() + LOOSE_ITEM_TOP, ie.getY(),
                ie.getZ(), 0f, 16, false, false));
        }
        out.addAll(columns(parts, dim));

        for (Display.ItemDisplay id : itemDisplays) {
            if (claimed.contains(id)) continue;
            ItemDisplayInvoker ii = (ItemDisplayInvoker) id;
            ItemInfo item = ItemInfo.of(ii.hontun$item());
            if (item == null) continue;
            Hologram.Props props = props(id, null, ii);
            Hologram h = new Hologram(Hologram.Kind.ITEM_DISPLAY, dim, id.getX(), id.getY(), id.getZ(), id.getYRot(), id.getXRot(), List.of(), props, item, null, true);
            out.add(new Found(h, box(id, props, -0.5, -0.5, -0.5, 0.5, 0.5, 0.5).inflate(0.05)));
        }

        for (Display.BlockDisplay bd : blockDisplays) {
            BlockState state = ((BlockDisplayInvoker) bd).hontun$block();
            if (state == null || state.isAir()) continue;
            Hologram.Props props = props(bd, null, null);
            String block = BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();
            boolean plain = state == state.getBlock().defaultBlockState();
            Hologram h = new Hologram(Hologram.Kind.BLOCK_DISPLAY, dim, bd.getX(), bd.getY(), bd.getZ(), bd.getYRot(), bd.getXRot(), List.of(), props, null, block, plain);
            out.add(new Found(h, box(bd, props, 0, 0, 0, 1, 1, 1).inflate(0.05)));
        }
        return out;
    }

    public static Found aimed(List<Found> all, float tickDelta) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return null;
        Vec3 from = mc.player.getEyePosition(tickDelta);
        Vec3 view = mc.player.getViewVector(tickDelta);
        Vec3 to = from.add(view.scale(RANGE));
        double limit = RANGE * RANGE;
        HitResult wall = mc.level.clip(new ClipContext(from, to, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, mc.player));
        if (wall != null && wall.getType() == HitResult.Type.BLOCK) limit = wall.getLocation().distanceToSqr(from);

        Found best = null;
        double bestDist = Double.MAX_VALUE;
        Found inside = null;
        double insideAngle = Double.MAX_VALUE;
        for (Found f : all) {
            if (f.box.contains(from)) {
                Vec3 toCenter = f.box.getCenter().subtract(from);
                double len = toCenter.length();
                if (len < 1e-6) continue;
                double dot = view.dot(toCenter) / len;
                if (dot <= 0) continue;
                double angle = Math.acos(Math.min(1.0, dot));
                if (angle < insideAngle) {
                    insideAngle = angle;
                    inside = f;
                }
                continue;
            }
            Optional<Vec3> hit = f.box.clip(from, to);
            if (hit.isEmpty()) continue;
            double d = hit.get().distanceToSqr(from);
            if (d > limit + 1e-6) continue;
            if (d < bestDist) {
                bestDist = d;
                best = f;
            }
        }
        return best != null ? best : inside;
    }

    private static void part(Minecraft mc, ArmorStand stand, List<Part> parts) {
        if (!stand.isInvisible()) return;
        ItemEntity rider = null;
        for (Entity p : stand.getPassengers()) {
            if (p instanceof ItemEntity ie) {
                rider = ie;
                break;
            }
        }
        ItemStack head = stand.getItemBySlot(EquipmentSlot.HEAD);
        Component name = stand.getCustomName();
        boolean named = name != null && stand.isCustomNameVisible() && !name.getString().isEmpty();
        if (rider != null && !rider.getItem().isEmpty()) {
            parts.add(new Part(Hologram.Type.ICON, null, ItemInfo.of(rider.getItem()), null, stand.getX(), rider.getY() + RIDER_ITEM_TOP,
                rider.getY(), stand.getZ(), stand.getYRot(), 16, false, true));
        } else if (!head.isEmpty()) {
            boolean small = stand.isSmall();
            parts.add(new Part(small ? Hologram.Type.SMALLHEAD : Hologram.Type.HEAD, null, ItemInfo.of(head), null, stand.getX(),
                stand.getY() + (small ? 1.1875 : 2.0), stand.getY(), stand.getZ(), stand.getYRot(), 16, false, false));
        } else if (named) {
            parts.add(new Part(Hologram.Type.TEXT, StyledText.of(name), null, null, stand.getX(), stand.getY() + stand.getBbHeight() + 0.5,
                stand.getY(), stand.getZ(), stand.getYRot(), mc.font.width(name), false, false));
        } else if (stand.isMarker() && stand.isSmall()) {
            parts.add(new Part(Hologram.Type.TEXT, StyledText.EMPTY, null, null, stand.getX(), stand.getY() + 0.5, stand.getY(), stand.getZ(),
                stand.getYRot(), 0, true, false));
        }
    }

    private static List<Found> columns(List<Part> parts, String dim) {
        List<List<Part>> columns = new ArrayList<>();
        for (Part p : parts) {
            List<Part> home = null;
            for (List<Part> c : columns) {
                Part first = c.getFirst();
                if (Math.abs(first.x - p.x) < COLUMN && Math.abs(first.z - p.z) < COLUMN) {
                    home = c;
                    break;
                }
            }
            if (home == null) {
                home = new ArrayList<>();
                columns.add(home);
            }
            home.add(p);
        }
        List<Found> out = new ArrayList<>();
        for (List<Part> column : columns) {
            column.sort(Comparator.comparingDouble(Part::top).reversed());
            List<Part> run = new ArrayList<>();
            for (Part p : column) {
                if (!run.isEmpty() && bottom(run.getLast()) - p.top > GAP) {
                    stack(run, dim, out);
                    run = new ArrayList<>();
                }
                run.add(p);
            }
            stack(run, dim, out);
        }
        return out;
    }

    private static double bottom(Part p) {
        return p.type == Hologram.Type.ENTITY ? p.at + 0.25 : p.top;
    }

    private static void stack(List<Part> run, String dim, List<Found> out) {
        if (run.isEmpty() || run.stream().allMatch(Part::empty)) return;
        Part top = run.getFirst();
        float yaw = 0f;
        for (Part p : run) {
            if (p.type == Hologram.Type.HEAD || p.type == Hologram.Type.SMALLHEAD || p.type == Hologram.Type.ENTITY) {
                yaw = p.yaw;
                break;
            }
        }
        List<Hologram.Line> lines = new ArrayList<>();
        int width = 0;
        double low = top.top;
        for (Part p : run) {
            lines.add(new Hologram.Line(p.type, p.text, p.item, p.entity, p.top, p.at, p.x - top.x, p.z - top.z, p.yaw, p.rider));
            width = Math.max(width, p.widthPx);
            low = Math.min(low, Math.min(p.top, p.at));
        }
        Hologram h = new Hologram(Hologram.Kind.ARMOR_STANDS, dim, top.x, top.top, top.z, yaw, 0f, List.copyOf(lines), null, null, null, true);
        double half = Math.max(0.3, width * PX / 2.0 + 0.1);
        AABB box = new AABB(top.x - half, low - 0.35, top.z - half, top.x + half, top.top + 0.05, top.z + half);
        out.add(new Found(h, box));
    }

    private static Found textDisplay(Minecraft mc, Display.TextDisplay td, String dim, List<ItemEntity> loose,
                                     List<Display.ItemDisplay> itemDisplays, Set<Entity> claimed) {
        TextDisplayInvoker ti = (TextDisplayInvoker) td;
        Hologram.Props props = props(td, ti, null);
        Component text = ti.hontun$text();
        int lineWidth = Math.max(1, ti.hontun$lineWidth());
        List<FormattedText> split = mc.font.getSplitter().splitLines(text == null ? Component.empty() : text, lineWidth, Style.EMPTY);
        List<Hologram.Line> lines = new ArrayList<>();
        int widthPx = 0;
        for (FormattedText seq : split) {
            lines.add(new Hologram.Line(Hologram.Type.TEXT, StyledText.of(seq), null, null, 0, 0, 0, 0, 0f, false));
            widthPx = Math.max(widthPx, mc.font.width(seq));
        }
        if (lines.isEmpty()) lines.add(new Hologram.Line(Hologram.Type.TEXT, StyledText.EMPTY, null, null, 0, 0, 0, 0, 0f, false));
        int rows = lines.size();

        if (cmiText(props)) {
            double cmiRow = 0.25 * props.sy();
            double ox = td.getX() + props.tx();
            double oz = td.getZ() + props.tz();
            double bottom = td.getY() + props.ty();
            for (ItemEntity ie : loose) {
                if (claimed.contains(ie)) continue;
                if (Math.abs(ie.getX() - ox) > 0.02 || Math.abs(ie.getZ() - oz) > 0.02) continue;
                if (ie.getY() < bottom - 0.4 || ie.getY() > bottom + rows * cmiRow + 0.4) continue;
                int fromBottom = (int) Math.round((ie.getY() - bottom + 0.2 - cmiRow / 1.8) / cmiRow);
                Hologram.Line icon = new Hologram.Line(Hologram.Type.ICON, null, ItemInfo.of(ie.getItem()), null, ie.getY(), ie.getY(),
                    ie.getX() - td.getX(), ie.getZ() - td.getZ(), 0f, false);
                if (place(lines, rows - 1 - fromBottom, icon)) claimed.add(ie);
            }
            for (Display.ItemDisplay id : itemDisplays) {
                if (claimed.contains(id)) continue;
                if (Math.abs(id.getX() - td.getX()) > 0.02 || Math.abs(id.getZ() - td.getZ()) > 0.02 || Math.abs(id.getY() - td.getY()) > 0.02) continue;
                ItemDisplayInvoker ii = (ItemDisplayInvoker) id;
                ItemInfo item = ItemInfo.of(ii.hontun$item());
                if (item == null) continue;
                Hologram.Props ip = props(id, null, ii);
                if (Math.abs((ip.tz() - props.tz()) - 0.004f) > 0.002f) continue;
                int fromBottom = (int) Math.round((ip.ty() - props.ty() - cmiRow / 1.8) / cmiRow);
                Hologram.Line icon = new Hologram.Line(Hologram.Type.SMALLHEAD, null, item, null, id.getY() + ip.ty(), id.getY() + ip.ty(),
                    0, 0, id.getYRot(), false);
                if (place(lines, rows - 1 - fromBottom, icon)) claimed.add(id);
            }
        }

        Hologram h = new Hologram(Hologram.Kind.TEXT_DISPLAY, dim, td.getX(), td.getY(), td.getZ(), td.getYRot(), td.getXRot(), List.copyOf(lines), props, null, null, true);
        double half = (widthPx + 2) * PX / 2.0;
        double height = rows * (mc.font.lineHeight + 1) * PX;
        AABB box = "fixed".equals(props.billboard())
            ? box(td, props, -half, 0, -0.05, half, height, 0.05)
            : flat(td, props, half, height);
        return new Found(h, box.inflate(0.05));
    }

    private static boolean cmiText(Hologram.Props p) {
        float tz = p.tz();
        boolean depth = Math.abs(tz - 0.001f) < 0.0004f || Math.abs(tz - 0.006f) < 0.0004f || Math.abs(tz - 0.011f) < 0.0004f || Math.abs(tz - 0.016f) < 0.0004f;
        return depth && mark(p.sx()) && mark(p.sy());
    }

    private static boolean mark(float s) {
        double rounded = Math.round(s * 1000.0) / 1000.0;
        double d = s - rounded;
        return d > 0.4e-6 && d < 1.6e-6;
    }

    private static boolean place(List<Hologram.Line> lines, int row, Hologram.Line icon) {
        if (row < 0 || row >= lines.size()) return false;
        Hologram.Line at = lines.get(row);
        if (at.type() != Hologram.Type.TEXT || (at.text() != null && !at.text().isBlank())) return false;
        lines.set(row, icon);
        return true;
    }

    private static AABB flat(Display d, Hologram.Props p, double half, double height) {
        double sx = Math.abs(p.sx());
        double sy = Math.abs(p.sy());
        double cx = d.getX() + p.tx();
        double cz = d.getZ() + p.tz();
        double y0 = d.getY() + p.ty();
        double w = half * Math.max(sx, Math.abs(p.sz()));
        return new AABB(cx - w, y0, cz - w, cx + w, y0 + height * sy, cz + w);
    }

    private static AABB box(Display d, Hologram.Props p, double x0, double y0, double z0, double x1, double y1, double z1) {
        Transformation t = DisplayInvoker.hontun$transformation(d.getEntityData());
        Matrix4f m = new Matrix4f(t.getMatrix());
        boolean fixed = "fixed".equals(p.billboard());
        Quaternionf q = new Quaternionf().rotationYXZ((float) Math.toRadians(-d.getYRot()), (float) Math.toRadians(d.getXRot()), 0f);
        double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE, minZ = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE, maxY = -Double.MAX_VALUE, maxZ = -Double.MAX_VALUE;
        double radius = 0;
        for (int i = 0; i < 8; i++) {
            Vector3f v = new Vector3f((float) ((i & 1) == 0 ? x0 : x1), (float) ((i & 2) == 0 ? y0 : y1), (float) ((i & 4) == 0 ? z0 : z1));
            m.transformPosition(v);
            if (fixed) q.transform(v);
            radius = Math.max(radius, v.length());
            minX = Math.min(minX, v.x);
            minY = Math.min(minY, v.y);
            minZ = Math.min(minZ, v.z);
            maxX = Math.max(maxX, v.x);
            maxY = Math.max(maxY, v.y);
            maxZ = Math.max(maxZ, v.z);
        }
        double ex = d.getX();
        double ey = d.getY();
        double ez = d.getZ();
        if (!fixed) return new AABB(ex - radius, ey - radius, ez - radius, ex + radius, ey + radius, ez + radius);
        return new AABB(ex + minX, ey + minY, ez + minZ, ex + maxX, ey + maxY, ez + maxZ);
    }

    private static Hologram.Props props(Display d, TextDisplayInvoker ti, ItemDisplayInvoker ii) {
        DisplayInvoker di = (DisplayInvoker) d;
        Transformation t = DisplayInvoker.hontun$transformation(d.getEntityData());
        Vector3fc tr = t.translation();
        Vector3fc sc = t.scale();
        boolean rotated = !identity(t.leftRotation()) || !identity(t.rightRotation());
        String billboard = di.hontun$billboard().name().toLowerCase(Locale.ROOT);
        int lineWidth = 200;
        int background = 0x40000000;
        int opacity = 255;
        boolean shadow = false;
        boolean seeThrough = false;
        boolean defaultBackground = false;
        String align = "center";
        if (ti != null) {
            byte flags = ti.hontun$flags();
            lineWidth = ti.hontun$lineWidth();
            background = ti.hontun$background();
            opacity = ti.hontun$opacity() & 0xFF;
            shadow = (flags & Display.TextDisplay.FLAG_SHADOW) != 0;
            seeThrough = (flags & Display.TextDisplay.FLAG_SEE_THROUGH) != 0;
            defaultBackground = (flags & Display.TextDisplay.FLAG_USE_DEFAULT_BACKGROUND) != 0;
            align = Display.TextDisplay.getAlign(flags).name().toLowerCase(Locale.ROOT);
        }
        String context = ii == null ? null : ii.hontun$context().getSerializedName();
        return new Hologram.Props(billboard, sc.x(), sc.y(), sc.z(), tr.x(), tr.y(), tr.z(), rotated,
            di.hontun$brightness(), di.hontun$viewRange(), di.hontun$shadowRadius(), di.hontun$shadowStrength(),
            lineWidth, background, opacity, shadow, seeThrough, defaultBackground, align, context, di.hontun$glowColor());
    }

    private static boolean identity(Quaternionfc q) {
        return Math.abs(q.x()) < 1e-4 && Math.abs(q.y()) < 1e-4 && Math.abs(q.z()) < 1e-4;
    }
}
