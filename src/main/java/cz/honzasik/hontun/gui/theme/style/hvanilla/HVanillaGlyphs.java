package cz.honzasik.hontun.gui.theme.style.hvanilla;

import cz.honzasik.hontun.gui.render.pixel.PixelGlyph;
import cz.honzasik.hontun.gui.theme.icons.HontunBuiltinIcons;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.renderer.packer.GuiTexture;

import java.util.IdentityHashMap;
import java.util.Map;

public final class HVanillaGlyphs {
    public static final PixelGlyph TRI_DOWN = of("#####", ".###.", "..#..");
    public static final PixelGlyph TRI_UP = of("..#..", ".###.", "#####");
    public static final PixelGlyph TRI_RIGHT = of("#..", "##.", "###", "##.", "#..");
    public static final PixelGlyph TRI_LEFT = of("..#", ".##", "###", ".##", "..#");
    public static final PixelGlyph CHECK = of("......#", ".....##", "#...##.", "##.##..", ".###...");
    public static final PixelGlyph BOOKMARK_FILLED = of("#######", "#######", "#######", "#######", "#######", "###.###", "##...##", "#.....#");
    public static final PixelGlyph BOOKMARK_OUTLINE = of("#######", "#.....#", "#.....#", "#.....#", "#.....#", "#..#..#", "#.#.#.#", "##...##");
    public static final PixelGlyph MINUS = of(".......", ".......", ".......", "#######", ".......", ".......", ".......", ".......");
    public static final PixelGlyph COPY = of("#####...", "#...#...", "#..#####", "#..#...#", "####...#", "...#...#", "...#####");
    public static final PixelGlyph PASTE = of("..###..", "#######", "#.....#", "#.###.#", "#.....#", "#.###.#", "#.....#", "#######");
    public static final PixelGlyph MOVE = of("#...#...", ".#...#..", "..#...#.", "...#...#", "..#...#.", ".#...#..", "#...#...");
    public static final PixelGlyph EYE = of("..####..", ".#....#.", "#..##..#", "#..##..#", ".#....#.", "..####..");
    public static final PixelGlyph GEAR = of("..#.#..", ".#####.", "##...##", ".#...#.", "##...##", ".#####.", "..#.#..");
    public static final PixelGlyph CUBE = of("..######", ".#....##", "######.#", "#....#.#", "#....#.#", "#....##.", "######..");
    public static final PixelGlyph SWORD = of("......##", ".....###", "....###.", "#..###..", ".####...", "..##....", ".#.##...", "#...#...");
    public static final PixelGlyph QUESTION = of(".###.", "#...#", "....#", "...#.", "..#..", "..#..", ".....", "..#..");
    public static final PixelGlyph GRID = of("###.###", "###.###", "###.###", ".......", "###.###", "###.###", "###.###");
    public static final PixelGlyph PLUS = of("...#...", "...#...", "...#...", "#######", "...#...", "...#...", "...#...");
    public static final PixelGlyph SEARCH = of(".####...", "#....#..", "#....#..", "#....#..", "#....#..", ".######.", ".....##.");
    public static final PixelGlyph REFRESH = of("..#####.", ".##...#.", "###.....", "........", "........", ".....###", ".#...##.", ".#####..");
    public static final PixelGlyph PENCIL = of(".....###", ".....###", "....####", "...###..", "..###...", "..##....", "###.....", "###.....");
    public static final PixelGlyph PERSON = of("..###.", "..###.", "..###.", "......", ".#####", ".#####", ".#####");
    public static final PixelGlyph BRUSH = of(".......#", "......##", ".....##.", "....##..", "..###...", ".####...", "####....", "###.....");
    public static final PixelGlyph PEOPLE = of(".##..##.", ".##..##.", "........", "####.###", "####.###", "####.###");
    public static final PixelGlyph MOUSE = of(".##.##.", "#..#..#", "#..#..#", "#######", "#.....#", "#.....#", "#.....#", ".#####.");
    public static final PixelGlyph CHEVRON = of("#...", ".#..", "..#.", "...#", "..#.", ".#..", "#...");
    public static final PixelGlyph CIRCLE = of("..##..", ".####.", "######", "######", ".####.", "..##..");

    private static final Map<GuiTexture, PixelGlyph> BY_TEXTURE = new IdentityHashMap<>();
    private static boolean complete;

    private HVanillaGlyphs() {}

    private static PixelGlyph of(String... rows) {
        int w = 0;
        for (String row : rows) w = Math.max(w, row.length());
        return new PixelGlyph(w, rows.length, rows);
    }

    public static PixelGlyph forTexture(GuiTexture texture) {
        if (texture == null) return null;
        if (!complete) build();
        return BY_TEXTURE.get(texture);
    }

    private static synchronized void build() {
        if (complete) return;

        boolean ok = true;

        ok &= put(HontunBuiltinIcons.ARROW, TRI_UP);
        ok &= put(HontunBuiltinIcons.BOOKMARK_NO, BOOKMARK_OUTLINE);
        ok &= put(HontunBuiltinIcons.BOOKMARK_YES, BOOKMARK_FILLED);
        ok &= put(HontunBuiltinIcons.BRUSH, BRUSH);
        ok &= put(HontunBuiltinIcons.CHEVRON, CHEVRON);
        ok &= put(HontunBuiltinIcons.COPY, COPY);
        ok &= put(HontunBuiltinIcons.CUBE, CUBE);
        ok &= put(HontunBuiltinIcons.EDIT, PENCIL);
        ok &= put(HontunBuiltinIcons.EYE, EYE);
        ok &= put(HontunBuiltinIcons.GRID, GRID);
        ok &= put(HontunBuiltinIcons.IMPORT, PASTE);
        ok &= put(HontunBuiltinIcons.MINUS, MINUS);
        ok &= put(HontunBuiltinIcons.MOUSE, MOUSE);
        ok &= put(HontunBuiltinIcons.MOVEMENT, MOVE);
        ok &= put(HontunBuiltinIcons.PEOPLE, PEOPLE);
        ok &= put(HontunBuiltinIcons.PERSON, PERSON);
        ok &= put(HontunBuiltinIcons.PLUS, PLUS);
        ok &= put(HontunBuiltinIcons.QUESTION_MARK, QUESTION);
        ok &= put(HontunBuiltinIcons.RESET, REFRESH);
        ok &= put(HontunBuiltinIcons.SEARCH, SEARCH);
        ok &= put(HontunBuiltinIcons.SETTING, GEAR);
        ok &= put(HontunBuiltinIcons.SWORD, SWORD);
        ok &= put(HontunBuiltinIcons.TICK, CHECK);

        ok &= put(GuiRenderer.CIRCLE, CIRCLE);
        ok &= put(GuiRenderer.TRIANGLE, TRI_DOWN);
        ok &= put(GuiRenderer.EDIT, PENCIL);
        ok &= put(GuiRenderer.RESET, REFRESH);
        ok &= put(GuiRenderer.FAVORITE_YES, BOOKMARK_FILLED);
        ok &= put(GuiRenderer.FAVORITE_NO, BOOKMARK_OUTLINE);
        ok &= put(GuiRenderer.COPY, COPY);
        ok &= put(GuiRenderer.PASTE, PASTE);

        complete = ok;
    }

    private static boolean put(HontunBuiltinIcons icon, PixelGlyph glyph) {
        try {
            BY_TEXTURE.put(icon.texture(), glyph);
            return true;
        } catch (IllegalStateException e) {
            return false;
        }
    }

    private static boolean put(GuiTexture texture, PixelGlyph glyph) {
        if (texture == null) return false;
        BY_TEXTURE.put(texture, glyph);
        return true;
    }
}
