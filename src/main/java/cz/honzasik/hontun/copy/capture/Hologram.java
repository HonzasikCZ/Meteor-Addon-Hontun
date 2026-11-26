package cz.honzasik.hontun.copy.capture;

import cz.honzasik.hontun.copy.StyledText;

import java.util.List;

public record Hologram(Kind kind, String dimension, double x, double y, double z, float yaw, float pitch,
                       List<Line> lines, Props props, ItemInfo item, String block, boolean blockDefault) {

    public enum Kind { ARMOR_STANDS, TEXT_DISPLAY, ITEM_DISPLAY, BLOCK_DISPLAY }

    public enum Type { TEXT, ICON, HEAD, SMALLHEAD, ENTITY }

    public record Line(Type type, StyledText text, ItemInfo item, String entity, double top, double at, double dx, double dz, float yaw, boolean rider) {}

    public record Props(String billboard, float sx, float sy, float sz, float tx, float ty, float tz, boolean rotated,
                        int brightness, float viewRange, float shadowRadius, float shadowStrength, int lineWidth,
                        int background, int opacity, boolean shadow, boolean seeThrough, boolean defaultBackground,
                        String align, String context, int glow) {}

    public boolean displays() {
        return kind != Kind.ARMOR_STANDS;
    }

    public boolean sameSpot(Hologram o) {
        return o != null && kind == o.kind && dimension.equals(o.dimension)
            && Math.abs(x - o.x) < 0.05 && Math.abs(y - o.y) < 0.05 && Math.abs(z - o.z) < 0.05;
    }

    public String firstText() {
        for (Line l : lines) {
            if (l.type == Type.TEXT && l.text != null && !l.text.isBlank()) return l.text.plain();
        }
        return null;
    }

    public long textLines() {
        return lines.stream().filter(l -> l.type == Type.TEXT).count();
    }
}
