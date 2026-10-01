package cz.honzasik.hontun.gui.theme.style.hvanilla;

import cz.honzasik.hontun.gui.api.animation.Easing;
import cz.honzasik.hontun.gui.api.icons.HontunIcons;
import cz.honzasik.hontun.gui.api.text.RichText;
import cz.honzasik.hontun.gui.render.pixel.PixelCanvas;
import cz.honzasik.hontun.gui.render.pixel.PixelGlyph;
import cz.honzasik.hontun.gui.theme.HontunGuiTheme;
import cz.honzasik.hontun.gui.theme.HontunLightPalette;
import cz.honzasik.hontun.gui.theme.HontunSettingsWidgetFactory;
import cz.honzasik.hontun.gui.theme.icons.HontunBuiltinIcons;
import cz.honzasik.hontun.gui.theme.style.AnimRole;
import cz.honzasik.hontun.gui.theme.style.AnimSpec;
import cz.honzasik.hontun.gui.theme.style.ChipKind;
import cz.honzasik.hontun.gui.theme.style.ClickStyle;
import cz.honzasik.hontun.gui.theme.style.FontChoice;
import cz.honzasik.hontun.gui.theme.style.Knob;
import cz.honzasik.hontun.gui.theme.style.Metrics;
import cz.honzasik.hontun.gui.theme.style.Pipeline;
import cz.honzasik.hontun.gui.theme.style.StylePalette;
import cz.honzasik.hontun.gui.theme.style.WindowKind;
import cz.honzasik.hontun.gui.theme.widgets.WHontunHorizontalSeparator;
import cz.honzasik.hontun.gui.theme.widgets.WHontunLabel;
import cz.honzasik.hontun.gui.theme.widgets.WHontunModule;
import cz.honzasik.hontun.gui.theme.widgets.WHontunSwatchLabel;
import cz.honzasik.hontun.gui.theme.widgets.WHontunTooltip;
import cz.honzasik.hontun.gui.theme.widgets.WHontunTopBar;
import cz.honzasik.hontun.gui.theme.widgets.WHontunVerticalSeparator;
import cz.honzasik.hontun.gui.theme.widgets.container.WHontunSection;
import cz.honzasik.hontun.gui.theme.widgets.container.WHontunView;
import cz.honzasik.hontun.gui.theme.widgets.container.WHontunWindow;
import cz.honzasik.hontun.gui.theme.widgets.input.WHontunDropdown;
import cz.honzasik.hontun.gui.theme.widgets.input.WHontunMultiSelect;
import cz.honzasik.hontun.gui.theme.widgets.input.WHontunSearch;
import cz.honzasik.hontun.gui.theme.widgets.input.WHontunSlider;
import cz.honzasik.hontun.gui.theme.widgets.input.WHontunTextBox;
import cz.honzasik.hontun.gui.theme.widgets.pressable.WHontunButton;
import cz.honzasik.hontun.gui.theme.widgets.pressable.WHontunCheckbox;
import cz.honzasik.hontun.gui.theme.widgets.pressable.WHontunColorPicker;
import cz.honzasik.hontun.gui.theme.widgets.pressable.WHontunConfirmedButton;
import cz.honzasik.hontun.gui.theme.widgets.pressable.WHontunConfirmedMinus;
import cz.honzasik.hontun.gui.theme.widgets.pressable.WHontunFavorite;
import cz.honzasik.hontun.gui.theme.widgets.pressable.WHontunMinus;
import cz.honzasik.hontun.gui.theme.widgets.pressable.WHontunPlus;
import cz.honzasik.hontun.gui.theme.widgets.pressable.WHontunTriangle;
import cz.honzasik.hontun.gui.theme.widgets.settings.WHontunKeybind;
import cz.honzasik.hontun.gui.widget.WGuiTexture;
import cz.honzasik.hontun.utils.HontunTheme;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.renderer.packer.GuiTexture;
import meteordevelopment.meteorclient.gui.utils.Cell;
import meteordevelopment.meteorclient.gui.widgets.WLabel;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.joml.Matrix3x2fStack;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class HVanillaClickStyle extends ClickStyle {
    private static final int VOID = 0x050506;
    private static final int WHITE = 0xFFFFFF;
    private static final int ON_LIGHT = 0xE6E8EC;
    private static final int ON_DARK = 0x0C0D0F;
    private static final int TIP_FILL = 0x0C0D0F;
    private static final int BOX_FILL = 0xFF000000;
    private static final int BOX_TEXT = 0xFFE0E0E0;
    private static final int BOX_TEXT_IDLE = 0xFFA0A0A0;
    private static final int BOX_CARET = 0xFFD0D0D0;
    private static final int BOX_HINT = 0xFFA0A3AB;
    private static final int[] RUNNER_DARK = {0xFFFFFFFF, 0xAAFFFFFF, 0x55FFFFFF};
    private static final int[] RUNNER_LIGHT = {0xFF202020, 0xAA202020, 0x55202020};

    private static final String LISTEN = "Press any key";
    private static final String ELLIPSIS = "...";
    private static final RichText NO_RESULTS = RichText.of("No results");

    private static final long CARET_BLINK = 300;
    private static final long DRAG_HOLD = 150;
    private static final long ARMED_BLINK = 250;
    private static final int RUNNER_SPEED = 120;
    private static final int GRID_DOT_LIMIT = 6000;

    private boolean light;
    private int crust, mantle, base;
    private int surface0, surface1, surface2;
    private int overlay0, overlay1, overlay2;
    private int text, subtext1, textDim;
    private int accent, accentHi, accentLo, accentCorr, accentEdge, rowOn;
    private int barFill, barHover;
    private int green, red;
    private int bevel, barLit, rowEdge, rowSeam, hot, rowHot, rim, tint, tabTint;
    private int dimInk, plusInk, minusInk, tipTop, tipBottom, selectTone, stroke;
    private int stripFill, stripHover, stripLit, stripShade, stripInk, wellShade, wellLit, chipEdge, popupEdge;

    private final Color subtext1Color = new Color(255, 255, 255, 255);
    private final Color searchHotColor = new Color(255, 255, 255, 255);
    private final Color islandDimColor = new Color(0xA0, 0xA3, 0xAB, 255);
    private final Color scratch = new Color(255, 255, 255, 255);

    private final Map<WHontunWindow, int[]> boxes = new WeakHashMap<>();
    private final Set<WHontunWindow> clipped = Collections.newSetFromMap(new WeakHashMap<>());
    private final Map<WHontunTextBox, long[]> carets = new WeakHashMap<>();
    private long dragStamp;

    public HVanillaClickStyle() {
        StylePalette seed = new StylePalette();
        super.palette(seed, false, null);
        capture(seed, false);
    }

    @Override
    public HontunTheme.UiMode id() {
        return HontunTheme.UiMode.HVanilla;
    }

    @Override
    protected Metrics createMetrics() {
        Metrics m = new Metrics();
        m.windowPad = 2;
        m.windowPadL = 4;
        m.windowPadR = 4;
        m.windowPadT = -1;
        m.windowPadB = 4;
        m.headerPadH = 8;
        m.headerPadV = 6;
        m.headerSpacing = 6;
        m.topBarMarginTop = 8;
        m.topBarItemPad = 4;
        m.tabPadH = 12;
        m.tabPadV = 6;
        m.tabIconGap = 6;
        m.rowPadH = 8;
        m.rowPadV = 4;
        m.activeBarWidth = 2;
        m.activeBarInset = 0;
        m.viewSpacing = 0;
        m.sectionHeaderPadH = 8;
        m.sectionHeaderPadV = 2;
        m.sectionPad = 8;
        m.tablePad = 8;
        m.tableVSpacing = 6;
        m.tooltipPadH = 8;
        m.tooltipPadV = 8;
        m.moduleScreenPad = 6;
        m.moduleInfoSpacing = 8;
        m.separatorThickness = 2;
        m.searchHeaderPad = 4;
        m.searchViewPad = 4;
        m.searchRowPad = 2;
        m.searchResultPadH = 4;
        m.searchHintPadL = 4;
        m.searchHintPadR = 4;
        m.multiSelectItemPad = 6;
        m.multiSelectItemSpacing = 2;
        m.multiSelectCheckPad = 8;
        m.multiSelectHeaderPadV = 4;
        m.multiSelectHeaderPadL = 8;
        m.multiSelectRowScale = 12.0 / 11.0;
        m.gap = 8;
        m.dropdownPad = 4;
        return m;
    }

    @Override
    public Pipeline pipeline() {
        return Pipeline.PIXEL;
    }

    @Override
    public FontChoice font() {
        return FontChoice.MC;
    }

    @Override
    public boolean uses(Knob knob) {
        return switch (knob) {
            case ANIMATION, CORNER_RADIUS -> false;
            default -> true;
        };
    }

    @Override
    public double effectiveScale(double raw) {
        return Math.max(1, Math.round(2 * raw)) / 2.0;
    }

    @Override
    public double pixelAlpha(double a) {
        return a < 0.5 ? 0 : 1;
    }

    @Override
    public PixelGlyph glyphFor(GuiTexture texture) {
        return HVanillaGlyphs.forTexture(texture);
    }

    @Override
    public double radiusFactor() {
        return 0;
    }

    @Override
    public boolean windowShadow(HontunGuiTheme t) {
        return t.windowShadow.get();
    }

    @Override
    public double chromeMargin(HontunGuiTheme t) {
        return windowShadow(t) ? 3 * u(t) : 0;
    }

    @Override
    public double windowAlpha(double raw, boolean light) {
        return light ? Math.max(0.9, 0.7 + 0.3 * raw) : Math.max(0.72, 0.95 * raw);
    }

    @Override
    public double controlAlpha(double raw, boolean light) {
        return Math.max(0.6, raw);
    }

    @Override
    public double snapWindow(HontunGuiTheme t, double value, int gridSize) {
        int p = u(t);
        int step = gridSize > 0 ? lcm(gridSize, p) : p;
        return Math.floor(value / step + 0.5) * step;
    }

    @Override
    public AnimSpec anim(AnimRole role) {
        return switch (role) {
            case WINDOW_EXPAND, SECTION_EXPAND, MULTISELECT_EXPAND -> {
                int ms = (int) Math.round(Math.min(theme().guiAnimationDuration() * 0.4, 120));
                yield AnimSpec.of(Easing.LINEAR, ms, ms);
            }
            case DROPDOWN_OPEN -> AnimSpec.of(Easing.LINEAR, 120, 0);
            default -> AnimSpec.NONE;
        };
    }

    @Override
    public void palette(StylePalette out, boolean light, HontunGuiTheme t) {
        super.palette(out, light, t);
        capture(out, light);
    }

    private void capture(StylePalette p, boolean light) {
        this.light = light;

        crust = p.crust;
        mantle = p.mantle;
        base = p.base;
        surface0 = p.surface0;
        surface1 = p.surface1;
        surface2 = p.surface2;
        overlay0 = p.overlay0;
        overlay1 = p.overlay1;
        overlay2 = p.overlay2;
        text = p.text;
        subtext1 = p.subtext1;
        textDim = p.subtext0;

        accent = p.accent;
        accentHi = p.accentHi;
        accentLo = p.accentLo;
        accentCorr = p.accent;
        green = p.green;
        red = p.red;

        barFill = clampBrightness(accentLo, 0.48f);
        barHover = HontunTheme.lerp(barFill, accent, 0.35f);
        accentEdge = HontunTheme.darken(light ? accentCorr : accentLo, 0.6f);
        rowOn = light ? HontunTheme.darken(accentCorr, 0.9f) : accentLo;

        if (light) {
            bevel = brighten(p.userAccent, accent, 2.5);
            barLit = bevel;
            barHover = HontunTheme.lerp(barFill, bevel, 0.35f);
            rowEdge = brighten(p.userAccentHi, rowOn, 3.0);
            rowSeam = HontunTheme.darken(rowOn, 0.2f);
            hot = hotFill(accent, p.userAccent, bevel, WHITE);
            rowHot = hotFill(rowOn, p.userAccent, bevel, WHITE);
            rim = rimTone(p.userAccent);
            tint = HontunTheme.lerp(base, p.userAccent, 0.22f);
            tabTint = HontunTheme.lerp(surface1, p.userAccent, 0.3f);
            dimInk = readable(textDim, surface1, 4.5);
            plusInk = readable(green, surface1, 4.5);
            minusInk = readable(red, surface1, 4.5);
            tipTop = p.userAccent;
            tipBottom = p.userAccentLo;
            selectTone = p.userAccent;
            stroke = overlay2;
            stripFill = surface2;
            stripHover = surface1;
            stripLit = WHITE;
            stripShade = overlay2;
            stripInk = readable(textDim, surface2, 4.5);
            wellShade = HontunTheme.lerp(overlay2, textDim, 0.5f);
            wellLit = overlay2;
            chipEdge = overlay2;
            popupEdge = rim;
        } else {
            bevel = accentHi;
            barLit = accent;
            rowEdge = accentHi;
            rowSeam = accentEdge;
            hot = accentHi;
            rowHot = accent;
            rim = accent;
            tint = accentLo;
            tabTint = surface2;
            dimInk = textDim;
            plusInk = green;
            minusInk = red;
            tipTop = accent;
            tipBottom = accentLo;
            selectTone = accent;
            stroke = overlay1;
            stripFill = surface1;
            stripHover = surface2;
            stripLit = overlay1;
            stripShade = VOID;
            stripInk = textDim;
            wellShade = VOID;
            wellLit = overlay1;
            chipEdge = overlay0;
            popupEdge = accentHi;
        }

        int hi = light ? p.userAccentHi : accentHi;
        searchHotColor.set((hi >> 16) & 0xFF, (hi >> 8) & 0xFF, hi & 0xFF, 255);
        subtext1Color.set((subtext1 >> 16) & 0xFF, (subtext1 >> 8) & 0xFF, subtext1 & 0xFF, 255);
    }

    private int outline() {
        return light ? opaque(rim) : argb(0xCC, accent);
    }

    @Override
    public WWidget windowIcon(HontunGuiTheme t, WindowKind kind, Category category, WWidget fallback) {
        GuiTexture texture = switch (kind) {
            case CATEGORY -> {
                if (fallback == null || category == null) yield null;
                GuiTexture icon = HontunIcons.getCategoryIcon(category.name);
                yield icon != null ? icon : builtin(HontunBuiltinIcons.QUESTION_MARK);
            }
            case SEARCH -> builtin(HontunBuiltinIcons.SEARCH);
            case FAVORITES -> builtin(HontunBuiltinIcons.BOOKMARK_YES);
            default -> null;
        };

        if (texture == null) return fallback;

        WHVanillaIcon icon = new WHVanillaIcon(texture);
        icon.theme = t;
        return icon;
    }

    @Override
    public WWidget headerLabel(HontunGuiTheme t, WHontunWindow w, String title) {
        WHVanillaTitle label = new WHVanillaTitle(RichText.of(windowTitle(title)), 9);
        label.theme = t;
        return label;
    }

    @Override
    public WWidget sectionTitle(HontunGuiTheme t, WHontunSection s, String title) {
        WHVanillaTitle label = new WHVanillaTitle(RichText.of(title), 10);
        label.theme = t;
        return label;
    }

    @Override
    public RichText keybindLabel(HontunGuiTheme t, WHontunKeybind k, boolean listening, String key) {
        if (listening) return RichText.of(LISTEN);
        return RichText.of(k.title() + ": ").append(key);
    }

    @Override
    public Color labelColor(HontunGuiTheme t, boolean title) {
        return title ? t.accentHiColor() : subtext1Color;
    }

    @Override
    public Color helpKeyColor(HontunGuiTheme t) {
        if (t.light()) return backdropKey(HontunTheme.userAccentHi());
        return t.accentHiColor();
    }

    @Override
    public Color helpTextColor(HontunGuiTheme t) {
        if (t.light()) return backdropText(HontunTheme.ramp(id()).textDim());
        return t.textSecondaryColor();
    }

    @Override
    public Color favoriteColor(WHontunFavorite f) {
        HontunGuiTheme t = f.theme();
        if (f.checked) return f.mouseOver ? t.accentHiColor() : t.accentColor();
        return f.mouseOver ? t.textColor() : t.textSecondaryColor();
    }

    @Override
    public void moduleSize(WHontunModule m, double[] out) {
        int p = u(m.theme());
        out[0] = m.titleWidth() + 16 * p;
        out[1] = 12 * p;
    }

    @Override
    public void tabSize(WHontunTopBar.WTopBarButton b, double[] out) {
        HontunGuiTheme t = b.bar().theme();
        int p = u(t);
        out[0] = (12 + (b.hasIcon() ? 11 : 0)) * p + t.textWidth(b.text());
        out[1] = 14 * p;
    }

    @Override
    public void checkboxSize(WHontunCheckbox c, double[] out) {
        int p = u(c.theme());
        out[0] = 11 * p;
        out[1] = 11 * p;
    }

    @Override
    public void dropdownSize(WHontunDropdown<?> d, double[] out) {
        HontunGuiTheme t = d.theme();
        int p = u(t);
        double title = t.textWidth(d.titleText());
        out[0] = (title > 0 ? title + 4 * p : 0) + 22 * p + d.maxValueWidth();
        out[1] = 14 * p;
    }

    @Override
    public void buttonSize(WHontunButton b, double[] out) {
        HontunGuiTheme t = b.theme();
        int p = u(t);
        RichText label = b.displayText();
        out[1] = 14 * p;

        if (b.parent instanceof WHontunKeybind) {
            double w = label != null ? t.textWidth(label) : 0;
            out[0] = Math.max(w, t.textWidth(LISTEN)) + 12 * p;
            return;
        }

        if (label == null || isSign(label)) {
            out[0] = 14 * p;
            return;
        }

        out[0] = t.textWidth(label) + 12 * p;
    }

    @Override
    public void pressableSize(WWidget w, double[] out) {
        HontunGuiTheme t = w.theme instanceof HontunGuiTheme h ? h : theme();
        int p = u(t);

        if (w instanceof WHontunConfirmedButton b) {
            out[0] = b.getText() != null ? b.labelWidth() + 12 * p : 14 * p;
            out[1] = 14 * p;
            return;
        }

        if (w instanceof WHontunMinus || w instanceof WHontunPlus || w instanceof WHontunConfirmedMinus) {
            out[0] = 14 * p;
            out[1] = 14 * p;
        }
    }

    @Override
    public void textBoxSize(WHontunTextBox b, double[] out) {
        super.textBoxSize(b, out);
        out[1] = 14 * u(b.theme());
    }

    @Override
    public void colorPickerSize(WHontunColorPicker p, double[] out) {
        int u = u(p.theme());
        out[0] = 24 * u;
        out[1] = 12 * u;
    }

    @Override
    public double sliderHeight(WHontunSlider s) {
        return 11 * u(s.theme());
    }

    @Override
    public double favoriteSize(WHontunFavorite f) {
        return 8 * u(f.theme());
    }

    @Override
    public double scrollbarWidth(HontunGuiTheme t) {
        return 4 * u(t);
    }

    @Override
    public double popupOffset(HontunGuiTheme t) {
        return -u(t);
    }

    @Override
    public void paintWindowBack(WHontunWindow w, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme t = w.theme();
        int p = unit();
        int[] b = computeBox(w, p);
        boxes.put(w, b);

        boolean dragging = w.isDragging();
        if (dragging) dragStamp = System.currentTimeMillis();

        if (windowShadow(t)) hardShadow(b[0], b[1], b[2], b[3], (dragging ? 3 : 2) * p);

        if (b[3] > 16 * p)
            PixelCanvas.fill(b[0] + p, b[1] + 15 * p, b[2] - 2 * p, b[3] - 16 * p, alpha(t.windowOpacity(), base));

        if (w.expandAnimating()) {
            PixelCanvas.pushClip(b[0], b[1], b[2], b[3]);
            clipped.add(w);
        }
    }

    @Override
    public void paintWindowHeader(WHontunWindow w, WHontunWindow.WHontunHeader h, GuiRenderer renderer, double mouseX, double mouseY) {
        int p = unit();
        int[] b = box(w, p);
        int x = b[0], y = b[1], width = b[2];

        boolean dragging = w.isDragging();
        boolean hover = h.mouseOver && !dragging;
        int fill = hover ? barHover : barFill;

        int bx = x + p, by = y + p, bw = width - 2 * p, bh = 14 * p;
        PixelCanvas.fill(bx, by, bw, bh, opaque(fill));
        if (dragging) PixelCanvas.sunk(bx, by, bw, bh, opaque(accentEdge), opaque(barLit));
        else PixelCanvas.raised(bx, by, bw, bh, opaque(barLit), opaque(accentEdge));

        int on = onColor(fill);
        boolean shadow = on == ON_LIGHT;

        String title = windowTitle(w.titleText());
        if (title != null && !title.isEmpty()) {
            WWidget label = h.titleWidget();
            int tx = label != null ? snap(label.x) : x + 4 * p;
            String shown = fit(title, x + width - 13 * p - tx);
            if (!shown.isEmpty()) PixelCanvas.text(shown, tx, y + 4 * p, opaque(on), shadow);
        }

        boolean expanded = w.isExpanded();
        PixelGlyph tri = expanded ? HVanillaGlyphs.TRI_DOWN : HVanillaGlyphs.TRI_RIGHT;
        int gx = x + width - (expanded ? 10 : 9) * p;
        int gy = y + (expanded ? 6 : 5) * p;
        PixelCanvas.glyph(tri, gx, gy, p, argb(hover ? 0xFF : 0xCC, on), shadow);
    }

    @Override
    public void paintWindowFront(WHontunWindow w, GuiRenderer renderer, double mouseX, double mouseY) {
        if (clipped.remove(w)) PixelCanvas.popClip();

        int p = unit();
        int[] b = box(w, p);
        PixelCanvas.frame(b[0], b[1], b[2], b[3], outline());
    }

    void paintHeaderIcon(WHVanillaIcon icon, GuiRenderer renderer) {
        int p = unit();
        int x = snap(icon.x), y = snap(icon.y);
        int on = onColor(barFill);
        PixelGlyph glyph = glyphFor(icon.texture());

        if (glyph != null) glyphIn(glyph, x, y, 8 * p, 8 * p, opaque(on), on == ON_LIGHT);
        else renderer.quad(x, y, 8 * p, 8 * p, icon.texture(), color(opaque(on)));
    }

    @Override
    public void paintTopBar(WHontunTopBar b, GuiRenderer renderer, double mouseX, double mouseY) {
        int p = unit();
        int x = snap(b.x), y = snap(b.y);
        int w = snap(b.x + b.width) - x, h = snap(b.y + b.height) - y;

        hardShadow(x, y, w, h, 2 * p);
        PixelCanvas.fill(x + p, y + p, w - 2 * p, h - 2 * p, light ? opaque(surface1) : argb(0xF0, surface1));
        PixelCanvas.frame(x, y, w, h, outline());
    }

    @Override
    public void paintTab(WHontunTopBar.WTopBarButton b, GuiRenderer renderer, double mouseX, double mouseY) {
        int p = unit();
        int x = snap(b.x), y = snap(b.y);
        int w = snap(b.x + b.width) - x, h = snap(b.y + b.height) - y;

        boolean selected = b.isSelected();
        boolean hover = b.mouseOver && !selected;
        boolean pressed = b.isPressed();

        int content;
        boolean shadow;

        if (selected) {
            PixelCanvas.fill(x, y, w, h, opaque(barFill));
            PixelCanvas.sunk(x, y, w, h, opaque(accentEdge), opaque(barLit));
            content = onColor(barFill);
            shadow = content == ON_LIGHT;
        } else if (hover) {
            PixelCanvas.fill(x, y, w, h, opaque(tabTint));
            content = text;
            shadow = !light;
        } else {
            content = subtext1;
            shadow = !light;
        }

        int ty = y + (selected ? 4 : 3) * p + (pressed ? p : 0);
        int cx = x + 6 * p;

        if (b.hasIcon()) {
            icon(renderer, b.icon(), cx, ty, 8 * p, 8 * p, opaque(content), shadow);
            cx += 11 * p;
        }

        PixelCanvas.text(b.text(), cx, ty, opaque(content), shadow);
    }

    @Override
    public void paintModule(WHontunModule m, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme t = m.theme();
        int p = unit();
        int x = snap(m.x), y = snap(m.y);
        int w = snap(m.x + m.width) - x, h = snap(m.y + m.height) - y;

        boolean on = m.module().isActive();
        boolean hover = m.mouseOver && !dragActive();
        boolean pressed = m.isPressed();

        int ink;
        boolean shadow;

        if (on) {
            boolean prev = m.isPrevActive();
            boolean next = m.isNextActive();
            int fill = hover ? rowHot : rowOn;

            PixelCanvas.fill(x, y, w, h, hover || light ? opaque(fill) : argb(0xE6, fill));
            if (prev) PixelCanvas.hline(x + p, y, w - 2 * p, opaque(rowSeam));

            int sides = PixelCanvas.LEFT | PixelCanvas.RIGHT
                    | (prev ? 0 : PixelCanvas.TOP)
                    | (next ? 0 : PixelCanvas.BOTTOM);
            PixelCanvas.frame(x, y, w, h, opaque(rowEdge), sides);

            int c = onColor(fill);
            ink = opaque(c);
            shadow = c == ON_LIGHT;
        } else if (hover) {
            PixelCanvas.fill(x, y, w, h, light ? argb(0x80, WHITE) : argb(0x1A, text));
            PixelCanvas.frame(x, y, w, h, opaque(overlay2));
            ink = opaque(text);
            shadow = !light;
        } else {
            ink = opaque(subtext1);
            shadow = !light;
        }

        if (pressed) PixelCanvas.fill(x, y, w, h, argb(light ? 0x30 : 0x60, 0));

        int tw = (int) Math.round(m.titleWidth());
        int tx = switch (t.moduleAlignment.get()) {
            case Center -> x + snap((w - tw) / 2.0);
            case Right -> x + w - 4 * p - tw;
            default -> x + 4 * p;
        };

        PixelCanvas.text(m.title(), tx, y + (pressed ? 3 : 2) * p, ink, shadow);
    }

    @Override
    public void paintSectionBody(WHontunSection s, GuiRenderer renderer, double mouseX, double mouseY) {
        if (!s.isExpanded() && !s.expandAnimating()) return;

        HontunGuiTheme t = s.theme();
        int p = unit();
        int x = snap(s.x);
        int w = snap(s.x + s.width) - x;
        int y = snap(s.y + s.headerWidget().height);
        int h = snap(s.y + s.height) - y;

        if (s.expandAnimating()) h = floorTo(h, 4 * p);
        if (h <= 0) return;

        well(x, y, w, h, alpha(t.backgroundOpacity(), mantle));
    }

    @Override
    public void paintSectionHeader(WHontunSection s, WHontunSection.WHontunHeader h, GuiRenderer renderer, double mouseX, double mouseY) {
        int p = unit();
        int x = snap(h.x), y = snap(h.y);
        int w = snap(h.x + h.width) - x, hh = snap(h.y + h.height) - y;

        boolean hover = h.mouseOver;
        strip(x, y, w, hh, hover);

        PixelCanvas.text(s.titleText(), x + 4 * p, y + 2 * p, opaque(light ? accentCorr : accentHi), !light);

        if (h.openIndicator() == null) return;

        boolean expanded = s.isExpanded();
        PixelGlyph tri = expanded ? HVanillaGlyphs.TRI_DOWN : HVanillaGlyphs.TRI_RIGHT;
        int gx = x + w - (expanded ? 8 : 7) * p;
        int gy = y + (expanded ? 4 : 3) * p;
        PixelCanvas.glyph(tri, gx, gy, p, opaque(hover ? text : stripInk), !light);
    }

    @Override
    public void paintSeparatorH(WHontunHorizontalSeparator s, GuiRenderer renderer, double mouseX, double mouseY) {
        int p = unit();
        int x = snap(s.x);
        int w = snap(s.x + s.width) - x;

        if (!s.hasText()) {
            int gy = snap(s.y + s.height / 2) - p;
            PixelCanvas.hline(x, gy, w, opaque(light ? overlay1 : VOID));
            PixelCanvas.hline(x, gy + p, w, opaque(light ? WHITE : overlay0));
            return;
        }

        int tw = (int) Math.round(s.titleWidth());
        int tx = x + snap((w - tw) / 2.0);
        int ty = snap(s.y + (s.height - 9 * p) / 2.0);
        int ry = ty + 3 * p;
        int rule = argb(0x99, accent);

        int leftEnd = tx - 4 * p;
        int rightStart = tx + tw + 3 * p;

        if (!light) {
            int shade = argb(0x60, 0);
            if (leftEnd > x) PixelCanvas.hline(x + p, ry + p, leftEnd - x, shade);
            if (x + w > rightStart) PixelCanvas.hline(rightStart + p, ry + p, x + w - rightStart, shade);
        }

        if (leftEnd > x) PixelCanvas.hline(x, ry, leftEnd - x, rule);
        if (x + w > rightStart) PixelCanvas.hline(rightStart, ry, x + w - rightStart, rule);

        PixelCanvas.text(s.richText(), tx, ty, opaque(light ? accentCorr : accentHi), !light);
    }

    @Override
    public void paintSeparatorV(WHontunVerticalSeparator s, GuiRenderer renderer, double mouseX, double mouseY) {
        int p = unit();
        int x = snap(s.x + s.width / 2 - p / 2.0);
        int y = snap(s.y);
        int h = snap(s.y + s.height) - y;
        int dot = light ? overlay1 : overlay0;
        PixelCanvas.dotted(x, y, h, true, opaque(dot), 128, argb(0x80, dot));
    }

    @Override
    public void paintScrollbar(WHontunView v, GuiRenderer renderer, double mouseX, double mouseY) {
        if (!v.scrollable() || !v.hasScrollBar) return;

        int p = unit();
        int lx = snap(v.barX());
        int ly = snap(v.y);
        int lw = 4 * p;
        int lh = snap(v.y + v.height) - ly;

        PixelCanvas.fill(lx, ly, lw, lh, argb(0xC0, crust));
        PixelCanvas.vline(lx, ly, lh, opaque(light ? overlay0 : VOID));

        int ty = snap(v.barY());
        int th = Math.max(8 * p, snap(v.barY() + v.barHeight()) - ty);

        if (v.isFocused()) {
            PixelCanvas.fill(lx, ty, lw, th, opaque(hot));
            PixelCanvas.sunk(lx, ty, lw, th, opaque(accentEdge), opaque(bevel));
        } else if (v.barHovered()) {
            PixelCanvas.fill(lx, ty, lw, th, opaque(accent));
            PixelCanvas.raised(lx, ty, lw, th, opaque(bevel), opaque(accentEdge));
        } else if (light) {
            PixelCanvas.fill(lx, ty, lw, th, opaque(overlay2));
            PixelCanvas.raised(lx, ty, lw, th, opaque(overlay0), opaque(textDim));
        } else {
            PixelCanvas.fill(lx, ty, lw, th, opaque(overlay1));
            PixelCanvas.raised(lx, ty, lw, th, opaque(overlay2), opaque(crust));
        }
    }

    @Override
    public void paintButton(WHontunButton b, GuiRenderer renderer, double mouseX, double mouseY) {
        if (b.parent instanceof WHontunKeybind k) {
            paintKeybindButton(k, b);
            return;
        }

        HontunGuiTheme t = b.theme();
        int p = unit();
        int x = snap(b.x), y = snap(b.y);
        int w = snap(b.x + b.width) - x, h = snap(b.y + b.height) - y;

        boolean pressed = b.isPressed();
        buttonChrome(t, x, y, w, h, b.mouseOver, pressed, 0);

        int dy = pressed ? p : 0;
        RichText label = b.displayText();

        if (label != null) {
            String plain = label.getPlainText();
            if ("+".equals(plain)) {
                glyphIn(HVanillaGlyphs.PLUS, x, y + dy, w, h, opaque(plusInk), !light);
                return;
            }
            if ("-".equals(plain)) {
                glyphIn(HVanillaGlyphs.MINUS, x, y + dy, w, h, opaque(minusInk), !light);
                return;
            }

            double tw = t.textWidth(label);
            PixelCanvas.text(label, x + snap((w - tw) / 2), y + 3 * p + dy, opaque(buttonInk()), !light);
            return;
        }

        GuiTexture tex = b.icon();
        PixelGlyph glyph = glyphFor(tex);
        int ink = glyph == HVanillaGlyphs.PLUS ? plusInk : glyph == HVanillaGlyphs.MINUS ? minusInk : buttonInk();
        icon(renderer, tex, x, y + dy, w, h, opaque(ink), !light);
    }

    private void paintKeybindButton(WHontunKeybind k, WHontunButton b) {
        HontunGuiTheme t = b.theme();
        int p = unit();
        int x = snap(b.x), y = snap(b.y);
        int w = snap(b.x + b.width) - x, h = snap(b.y + b.height) - y;

        boolean pressed = b.isPressed();
        boolean listening = k.isListening();
        buttonChrome(t, x, y, w, h, b.mouseOver, pressed, listening ? opaque(light ? rim : accentHi) : 0);

        int ty = y + 3 * p + (pressed ? p : 0);

        if (listening) {
            double tw = t.textWidth(LISTEN);
            PixelCanvas.text(LISTEN, x + snap((w - tw) / 2), ty, opaque(accentHi), !light);
            return;
        }

        String prefix = k.title() + ": ";
        String key = String.valueOf(k.keybind());
        double pw = t.textWidth(prefix);
        double kw = t.textWidth(key);
        int tx = x + snap((w - pw - kw) / 2);
        boolean none = "none".equalsIgnoreCase(key.trim());

        PixelCanvas.text(prefix, tx, ty, opaque(dimInk), !light);
        PixelCanvas.text(key, tx + snap(pw), ty, opaque(none ? dimInk : text), !light);
    }

    @Override
    public void paintKeybindExtra(WHontunKeybind k, GuiRenderer renderer, double mouseX, double mouseY) {
        if (!k.isListening()) return;

        WHontunButton b = k.button();
        if (b == null) return;

        int p = unit();
        int x = snap(b.x), y = snap(b.y);
        int w = snap(b.x + b.width) - x, h = snap(b.y + b.height) - y;
        PixelCanvas.runner(x, y, w, h, p, System.currentTimeMillis(), RUNNER_SPEED, light ? RUNNER_LIGHT : RUNNER_DARK);
    }

    @Override
    public void paintCheckbox(WHontunCheckbox c, GuiRenderer renderer, double mouseX, double mouseY) {
        int p = unit();
        int x = snap(c.x), y = snap(c.y);
        int s = snap(c.x + c.width) - x;
        int sy = snap(c.y + c.height) - y;
        boolean hover = c.mouseOver;
        boolean pressed = c.isPressed();

        if (!c.checked) {
            PixelCanvas.fill(x, y, s, sy, opaque(light ? surface0 : crust));
            int recess = opaque(light ? overlay0 : VOID);
            PixelCanvas.hline(x + p, y + p, s - 2 * p, recess);
            PixelCanvas.vline(x + p, y + 2 * p, sy - 3 * p, recess);
            PixelCanvas.frame(x, y, s, sy, opaque(hover || pressed ? accentHi : stroke));
        } else {
            int fill = hover ? hot : accent;
            PixelCanvas.fill(x, y, s, sy, opaque(fill));
            if (pressed) PixelCanvas.sunk(x, y, s, sy, opaque(accentEdge), opaque(bevel));
            else PixelCanvas.raised(x, y, s, sy, opaque(bevel), opaque(accentEdge));
            PixelCanvas.glyph(HVanillaGlyphs.CHECK, x + 2 * p, y + (pressed ? 4 : 3) * p, p, checkInk(fill), false);
        }

        syncCheckLabel(c);
    }

    private void syncCheckLabel(WHontunCheckbox c) {
        if (!(c.parent instanceof WContainer list)) return;

        List<Cell<?>> cells = list.cells;
        for (int i = 0; i < cells.size() - 1; i++) {
            if (cells.get(i).widget() != c) continue;

            if (cells.get(i + 1).widget() instanceof WLabel label) {
                Color bright = c.theme().textColor();
                if (label.color == null || label.color == bright) label.color = c.checked ? bright : null;
            }
            return;
        }
    }

    @Override
    public void paintColorPicker(WHontunColorPicker pk, GuiRenderer renderer, double mouseX, double mouseY) {
        int p = unit();
        int x = snap(pk.x), y = snap(pk.y);
        int w = snap(pk.x + pk.width) - x, h = snap(pk.y + pk.height) - y;
        Color color = pk.color();
        boolean hover = pk.mouseOver;

        if (color.a < 255) PixelCanvas.checker(x + p, y + p, w - 2 * p, h - 2 * p, 2 * p, opaque(surface2), opaque(overlay0));
        PixelCanvas.fill(x + p, y + p, w - 2 * p, h - 2 * p, color.getPacked());
        PixelCanvas.frame(x, y, w, h, opaque(hover ? accentHi : stroke));

        if (hover) {
            int dy = pk.isPressed() ? p : 0;
            glyphIn(HVanillaGlyphs.PENCIL, x, y + dy, w, h, pickerIconTint(color).getPacked(), false);
        }
    }

    @Override
    public void paintConfirmedButton(WHontunConfirmedButton b, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme t = b.theme();
        int p = unit();
        int x = snap(b.x), y = snap(b.y);
        int w = snap(b.x + b.width) - x, h = snap(b.y + b.height) - y;

        boolean pressed = b.isPressed();
        boolean armed = b.armed();

        if (armed) armedChrome(x, y, w, h);
        else buttonChrome(t, x, y, w, h, b.mouseOver, pressed, 0);

        int dy = pressed ? p : 0;
        int ink = armed ? opaque(WHITE) : opaque(buttonInk());
        String label = b.getText();

        if (label != null) {
            double tw = t.textWidth(label);
            PixelCanvas.text(label, x + snap((w - tw) / 2), y + 3 * p + dy, ink, armed || !light);
            return;
        }

        icon(renderer, b.icon(), x, y + dy, w, h, ink, armed || !light);
    }

    @Override
    public void paintConfirmedMinus(WHontunConfirmedMinus m, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme t = m.theme();
        int p = unit();
        int x = snap(m.x), y = snap(m.y);
        int w = snap(m.x + m.width) - x, h = snap(m.y + m.height) - y;

        boolean pressed = m.isPressed();
        boolean armed = m.armed();

        if (armed) armedChrome(x, y, w, h);
        else buttonChrome(t, x, y, w, h, m.mouseOver, pressed, 0);

        glyphIn(HVanillaGlyphs.MINUS, x, y + (pressed ? p : 0), w, h, armed ? opaque(WHITE) : opaque(minusInk), armed || !light);
    }

    @Override
    public void paintMinus(WHontunMinus m, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme t = m.theme();
        int p = unit();
        int x = snap(m.x), y = snap(m.y);
        int w = snap(m.x + m.width) - x, h = snap(m.y + m.height) - y;
        boolean pressed = m.isPressed();

        buttonChrome(t, x, y, w, h, m.mouseOver, pressed, 0);
        glyphIn(HVanillaGlyphs.MINUS, x, y + (pressed ? p : 0), w, h, opaque(minusInk), !light);
    }

    @Override
    public void paintPlus(WHontunPlus pl, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme t = pl.theme();
        int p = unit();
        int x = snap(pl.x), y = snap(pl.y);
        int w = snap(pl.x + pl.width) - x, h = snap(pl.y + pl.height) - y;
        boolean pressed = pl.isPressed();

        buttonChrome(t, x, y, w, h, pl.mouseOver, pressed, 0);
        glyphIn(HVanillaGlyphs.PLUS, x, y + (pressed ? p : 0), w, h, opaque(plusInk), !light);
    }

    @Override
    public void paintFavorite(WHontunFavorite f, GuiRenderer renderer, double mouseX, double mouseY) {
        int p = unit();
        int x = snap(f.x), y = snap(f.y);
        int w = snap(f.x + f.width) - x, h = snap(f.y + f.height) - y;
        PixelGlyph glyph = f.checked ? HVanillaGlyphs.BOOKMARK_FILLED : HVanillaGlyphs.BOOKMARK_OUTLINE;

        int ink;
        boolean shadow;

        if (inTitleBar(f)) {
            int on = onColor(barFill);
            ink = f.checked || f.mouseOver ? opaque(on) : argb(0x99, on);
            shadow = on == ON_LIGHT;
        } else {
            ink = f.checked ? opaque(f.mouseOver ? accentHi : accent) : opaque(f.mouseOver ? text : textDim);
            shadow = !light;
        }

        glyphIn(glyph, x, y + (f.isPressed() ? p : 0), w, h, ink, shadow);
    }

    @Override
    public void paintTriangle(WHontunTriangle tr, GuiRenderer renderer, double mouseX, double mouseY) {
        WWidget list = tr.parent;
        WWidget owner = list != null ? list.parent : null;

        if (owner instanceof WHontunWindow.WHontunHeader h && h.openIndicator() == tr) return;
        if (owner instanceof WHontunSection.WHontunHeader h && h.openIndicator() == tr) return;

        int x = snap(tr.x), y = snap(tr.y);
        int w = snap(tr.x + tr.width) - x, h = snap(tr.y + tr.height) - y;
        boolean hover = tr.mouseOver || (list != null && list.mouseOver);
        PixelGlyph glyph = tr.rotation >= 135 ? HVanillaGlyphs.TRI_DOWN : HVanillaGlyphs.TRI_RIGHT;

        glyphIn(glyph, x, y, w, h, opaque(hover ? text : textDim), !light);
    }

    @Override
    public void paintSlider(WHontunSlider s, GuiRenderer renderer, double mouseX, double mouseY) {
        int p = unit();
        int x = snap(s.x), y = snap(s.y);
        int w = snap(s.x + s.width) - x;
        boolean dragging = s.isDragging();

        int ty = y + 3 * p;
        PixelCanvas.fill(x, ty, w, 5 * p, opaque(light ? surface0 : crust));
        PixelCanvas.frame(x, ty, w, 5 * p, s.mouseOver || dragging ? argb(0xAA, accent) : opaque(light ? overlay2 : overlay0));

        double range = s.max() - s.min();
        double t = range == 0 ? 0 : Math.clamp((s.value() - s.min()) / range, 0.0, 1.0);
        int kw = 5 * p;
        int kx = x + snap((w - kw) * t);

        int fw = kx - x - p;
        if (fw > 0) {
            PixelCanvas.fill(x + p, ty + p, fw, 3 * p, opaque(accentLo));
            PixelCanvas.hline(x + p, ty + p, fw, opaque(barLit));
        }

        int fill = s.handleHovered() || dragging ? hot : accent;
        PixelCanvas.fill(kx, y, kw, 11 * p, opaque(fill));
        if (dragging) PixelCanvas.sunk(kx, y, kw, 11 * p, opaque(accentEdge), opaque(bevel));
        else PixelCanvas.raised(kx, y, kw, 11 * p, opaque(bevel), opaque(accentEdge));
    }

    @Override
    public void paintDropdown(WHontunDropdown<?> d, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme t = d.theme();
        int p = unit();
        int x = snap(d.x), y = snap(d.y);
        int w = snap(d.x + d.width) - x, h = snap(d.y + d.height) - y;

        boolean open = d.isExpanded();
        boolean pressed = d.isPressed();
        buttonChrome(t, x, y, w, h, d.mouseOver, pressed, open ? opaque(light ? rim : accentHi) : 0);

        int dy = pressed ? p : 0;
        int ink = opaque(buttonInk());
        int cx = x + 4 * p;

        double title = t.textWidth(d.titleText());
        if (title > 0) {
            PixelCanvas.text(d.titleText(), cx, y + 3 * p + dy, ink, !light);
            cx += (int) Math.round(title) + 4 * p;
        }

        PixelCanvas.fill(cx, y + 6 * p + dy, 2 * p, 2 * p, opaque(accent));
        cx += 5 * p;

        PixelCanvas.text(d.valueText(), cx, y + 3 * p + dy, opaque(accentHi), !light);

        PixelGlyph tri = open ? HVanillaGlyphs.TRI_UP : HVanillaGlyphs.TRI_DOWN;
        PixelCanvas.glyph(tri, x + w - 9 * p, y + 5 * p + dy, p, ink, !light);
    }

    @Override
    public void paintDropdownPopup(WHontunDropdown<?> d, WHontunDropdown.WRoot root, GuiRenderer renderer, double mouseX, double mouseY) {
        int p = unit();
        int x = snap(root.x), y = snap(root.y);
        int w = snap(root.x + root.width) - x;
        int full = snap(root.y + root.height) - y;

        int rows = visibleRows(d, root);
        int n = root.cells.size();
        int h;

        if (rows >= n) h = full;
        else if (rows <= 0) return;
        else {
            WWidget last = root.cells.get(rows - 1).widget();
            h = Math.min(full, snap(last.y + last.height) - y + 2 * p);
        }

        PixelCanvas.fill(x + p, y + p, w - 2 * p, h - 2 * p, light ? opaque(base) : argb(0xF6, base));
        PixelCanvas.frame(x, y, w, h, opaque(popupEdge));
    }

    @Override
    public void paintDropdownValue(WHontunDropdown<?>.WValue v, GuiRenderer renderer, double mouseX, double mouseY) {
        WHontunDropdown<?> d = v.owner();
        WHontunDropdown.WRoot root = d.popup();
        if (indexOf(root, v) >= visibleRows(d, root)) return;

        int p = unit();
        int x = snap(v.x), y = snap(v.y);
        int w = snap(v.x + v.width) - x, h = snap(v.y + v.height) - y;

        boolean selected = v.isSelectedValue();
        boolean hover = v.mouseOver;

        int ink;
        boolean shadow;

        if (hover) {
            PixelCanvas.fill(x, y, w, h, argb(0xCC, accentLo));
            int c = onColor(accentLo);
            ink = opaque(c);
            shadow = c == ON_LIGHT;
        } else {
            ink = opaque(selected ? accentHi : subtext1);
            shadow = !light;
        }

        if (selected) PixelCanvas.fill(x + 3 * p, y + snap((h - 2 * p) / 2.0), 2 * p, 2 * p, opaque(accentHi));

        PixelCanvas.text(v.valueName(), x + 7 * p, y + snap((h - 9 * p) / 2.0), ink, shadow);
    }

    @Override
    public void paintTextBox(WHontunTextBox b, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme t = b.theme();
        int p = unit();
        int x = snap(b.x), y = snap(b.y);
        int w = snap(b.x + b.width) - x, h = snap(b.y + b.height) - y;

        boolean framed = b.rendersBackground();
        boolean focused = b.isFocused();
        boolean hover = b.mouseOver;
        Color custom = b.customColor();

        if (framed) {
            int frame = focused ? opaque(light ? rim : accentHi) : opaque(hover ? (light ? textDim : overlay2) : stroke);

            if (b.hasTitle()) {
                WHontunLabel title = titleLabel(b);
                int left = title != null
                        ? snap(title.x) - 3 * p
                        : x - snap(b.pad() + t.textWidth(b.title()) + t.scale(3));

                PixelCanvas.fill(left + p, y + p, x - left - p, h - 2 * p, light ? opaque(surface1) : argb(0xE6, surface1));
                PixelCanvas.fill(x + p, y + p, w - 2 * p, h - 2 * p, BOX_FILL);
                PixelCanvas.frame(left, y, x - left + w, h, frame);
                PixelCanvas.vline(x, y + p, h - 2 * p, frame);

                if (title != null) {
                    title.hidden = true;
                    int ink = title.color != null ? title.color.getPacked() : opaque(subtext1);
                    PixelCanvas.text(title.richText(), snap(title.x), y + 3 * p, ink, !light);
                }
            } else {
                PixelCanvas.fill(x + p, y + p, w - 2 * p, h - 2 * p, BOX_FILL);
                PixelCanvas.frame(x, y, w, h, frame);
            }
        } else if (custom != null && hover) {
            PixelCanvas.hline(x, y + 11 * p, w, argb(0x66, accent));
        }

        int ink;
        if (framed) ink = custom != null ? custom.getPacked() : focused ? BOX_TEXT : BOX_TEXT_IDLE;
        else ink = custom != null ? opaque(accentHi) : opaque(ON_LIGHT);

        double textX = b.x + b.padding() - b.overflow();
        int textY = y + 3 * p;
        String value = b.textValue();

        if (framed) renderer.scissorStart(x + 2 * p, y + p, w - 4 * p, h - 2 * p);
        else renderer.scissorStart(x, y, w, h);

        if (focused && b.hasSelection()) {
            int s0 = snap(textX + b.textWidthAt(b.selectionStartIndex()));
            int s1 = snap(textX + b.textWidthAt(b.selectionEndIndex()));
            int select = argb(light && !framed && custom != null ? 0x60 : 0x80, selectTone);
            PixelCanvas.fill(Math.min(s0, s1), y + 2 * p, Math.abs(s1 - s0), 10 * p, select);
        }

        if (!value.isEmpty()) {
            b.drawText(renderer, textX, textY, value, color(ink));
        } else if (b.placeholderValue() != null && !b.placeholderValue().isEmpty()) {
            int hint = framed || custom == null ? BOX_HINT : opaque(textDim);
            b.drawText(renderer, textX, textY, b.placeholderValue(), color(hint));
        }

        if (focused) paintBlinkCaret(b, textX, textY, y, ink, framed || !light);
        else carets.remove(b);

        renderer.scissorEnd();
    }

    private void paintBlinkCaret(WHontunTextBox b, double textX, int textY, int y, int ink, boolean shadow) {
        long now = System.currentTimeMillis();
        String value = b.textValue();
        int cursor = b.cursorIndex();

        long[] state = carets.computeIfAbsent(b, k -> new long[]{now, -1, 0});
        if (state[1] != cursor || state[2] != value.hashCode()) {
            state[0] = now;
            state[1] = cursor;
            state[2] = value.hashCode();
        }

        if (((now - state[0]) / CARET_BLINK) % 2 != 0) return;

        int p = unit();
        double cx = textX + b.textWidthAt(cursor);

        if (cursor >= value.length()) PixelCanvas.text("_", cx, textY, ink, shadow);
        else PixelCanvas.vline(snap(cx), y + 2 * p, 10 * p, b.rendersBackground() ? BOX_CARET : ink);
    }

    @Override
    public void paintCompletions(WHontunTextBox b, GuiRenderer renderer, double x, double y, double w, double h) {
        int p = unit();
        int px = snap(x), py = snap(y);
        int pw = snap(x + w) - px, ph = snap(y + h) - py;

        hardShadow(px, py, pw, ph, 2 * p, light ? 0x40 : 0x70);
        PixelCanvas.fill(px + p, py + p, pw - 2 * p, ph - 2 * p, light ? opaque(base) : argb(0xF6, base));
        PixelCanvas.frame(px, py, pw, ph, opaque(popupEdge));
    }

    @Override
    public void paintCompletionItem(WHontunTextBox.CompletionItem item, GuiRenderer renderer, double mouseX, double mouseY) {
        int x = snap(item.x), y = snap(item.y);
        int w = snap(item.x + item.width) - x, h = snap(item.y + item.height) - y;

        int ink;
        boolean shadow;

        if (item.isSelected()) {
            PixelCanvas.fill(x, y, w, h, argb(0xCC, accentLo));
            int c = onColor(accentLo);
            ink = opaque(c);
            shadow = c == ON_LIGHT;
        } else {
            ink = opaque(subtext1);
            shadow = !light;
        }

        PixelCanvas.text(item.richText(), x, y, ink, shadow);
    }

    @Override
    public void paintSearchPanel(WHontunSearch s, GuiRenderer renderer, double mouseX, double mouseY) {
        int p = unit();
        int x = snap(s.x), y = snap(s.y);
        int w = snap(s.x + s.width) - x, h = snap(s.y + s.height) - y;

        if (windowShadow(s.theme())) hardShadow(x, y, w, h, 2 * p);
    }

    @Override
    public void paintSearchHeader(WHontunSearch.WHontunHeader h, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme t = h.theme();
        int p = unit();
        int x = snap(h.x), y = snap(h.y);
        int w = snap(h.x + h.width) - x, hh = snap(h.y + h.height) - y;

        PixelCanvas.fill(x + p, y + p, w - 2 * p, hh - 2 * p, BOX_FILL);
        PixelCanvas.hline(x + p, y + hh - p, w - 2 * p, argb(0xCC, light ? bevel : accent));

        if (h.cells.isEmpty() || !(h.cells.getFirst().widget() instanceof WContainer row)) return;

        boolean focused = false;
        for (Cell<?> cell : row.cells) {
            if (cell.widget() instanceof WHontunTextBox box && box.isFocused()) focused = true;
        }

        for (Cell<?> cell : row.cells) {
            WWidget widget = cell.widget();
            if (widget instanceof WGuiTexture icon) icon.color = focused ? (light ? searchHotColor : t.accentHiColor()) : islandDimColor;
            else if (widget instanceof WLabel label) label.color = islandDimColor;
        }
    }

    @Override
    public void paintSearchBody(WHontunSearch.WHontunResultsContainer c, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme t = c.theme();
        int p = unit();
        int x = snap(c.x), y = snap(c.y);
        int w = snap(c.x + c.width) - x, h = snap(c.y + c.height) - y;

        PixelCanvas.fill(x + p, y, w - 2 * p, h - p, alpha(t.windowOpacity(), base));

        if (c.parent instanceof WHontunSearch s) {
            int sx = snap(s.x), sy = snap(s.y);
            PixelCanvas.frame(sx, sy, snap(s.x + s.width) - sx, snap(s.y + s.height) - sy, outline());
        }

        if (c.cells.size() < 2 || !(c.cells.get(1).widget() instanceof WHontunLabel help)) return;

        boolean empty = c.cells.getFirst().widget() instanceof WContainer view
                && view.cells.isEmpty()
                && !searchQuery(c).isEmpty();

        help.hidden = empty;
        if (empty) {
            double tw = t.textWidth(NO_RESULTS);
            PixelCanvas.text(NO_RESULTS, snap(help.x + help.width / 2 - tw / 2), snap(help.y), opaque(textDim), !light);
        }
    }

    private static WHontunLabel titleLabel(WHontunTextBox b) {
        if (!(b.parent instanceof WContainer list)) return null;

        List<Cell<?>> cells = list.cells;
        for (int i = 1; i < cells.size(); i++) {
            if (cells.get(i).widget() != b) continue;
            if (cells.get(i - 1).widget() instanceof WHontunLabel label
                    && label.richText().getPlainText().strip().equals(b.title().strip())) return label;
            return null;
        }
        return null;
    }

    private static String searchQuery(WWidget container) {
        if (!(container.parent instanceof WContainer search)) return "";
        WHontunTextBox box = findTextBox(search, 4);
        return box != null ? box.textValue() : "";
    }

    private static WHontunTextBox findTextBox(WWidget widget, int depth) {
        if (widget instanceof WHontunTextBox box) return box;
        if (depth <= 0 || !(widget instanceof WContainer container)) return null;
        for (Cell<?> cell : container.cells) {
            if (cell.widget() instanceof WHontunSearch.WHontunResultsContainer) continue;
            WHontunTextBox found = findTextBox(cell.widget(), depth - 1);
            if (found != null) return found;
        }
        return null;
    }

    @Override
    public void paintSearchRow(WHontunSearch.WHontunResult row, GuiRenderer renderer, double mouseX, double mouseY) {
        if (!row.mouseOver) return;

        int x = snap(row.x), y = snap(row.y);
        int w = snap(row.x + row.width) - x, h = snap(row.y + row.height) - y;

        if (light) {
            PixelCanvas.fill(x, y, w, h, opaque(tint));
            PixelCanvas.frame(x, y, w, h, opaque(rim));
        } else {
            PixelCanvas.fill(x, y, w, h, argb(0xCC, accentLo));
            PixelCanvas.frame(x, y, w, h, opaque(accentHi));
        }
        if (row.isPressed()) PixelCanvas.fill(x, y, w, h, argb(light ? 0x30 : 0x60, 0));
    }

    @Override
    public void paintChip(GuiRenderer renderer, double x, double y, double w, double h, Color c, ChipKind kind) {
        int p = unit();
        int size = 12 * p;
        int cx = snap(x + (w - size) / 2);
        int cy = snap(y + (h - size) / 2);
        int rgb = packed(c);

        PixelCanvas.fill(cx + p, cy + p, size - 2 * p, size - 2 * p, argb(0x30, rgb));
        PixelCanvas.frame(cx, cy, size, size, opaque(rgb));
    }

    @Override
    public void paintMultiSelectHeader(WHontunMultiSelect<?>.WHontunHeader h, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme t = h.owner().theme();
        int p = unit();
        int x = snap(h.x), y = snap(h.y);
        int w = snap(h.x + h.width) - x, hh = snap(h.y + h.height) - y;

        strip(x, y, w, hh, h.mouseOver);

        int labels = 0;
        for (Cell<?> cell : h.cells) {
            if (!(cell.widget() instanceof WLabel label)) continue;

            if (labels == 0) {
                label.color = light ? t.accentColor() : t.accentHiColor();
            } else {
                int lx = snap(label.x) - 2 * p, ly = snap(label.y) - p;
                int lw = snap(label.x + label.width) - snap(label.x) + 4 * p;
                PixelCanvas.fill(lx + p, ly + p, lw - 2 * p, 9 * p, opaque(surface1));
                PixelCanvas.frame(lx, ly, lw, 11 * p, opaque(chipEdge));
                label.color = t.accentHiColor();
            }

            labels++;
        }
    }

    @Override
    public void paintMultiSelectBody(WHontunMultiSelect<?> m, GuiRenderer renderer, double mouseX, double mouseY) {
        if (!m.isExpanded() && !m.animating()) return;

        HontunGuiTheme t = m.theme();
        int p = unit();
        int x = snap(m.x);
        int w = snap(m.x + m.width) - x;
        int y = snap(m.y + m.headerWidget().height);
        int h = snap(m.y + m.height) - y;

        if (m.animating()) h = floorTo(h, 4 * p);
        if (h <= 0) return;

        well(x, y, w, h, alpha(t.backgroundOpacity(), mantle));
    }

    @Override
    public void paintMultiSelectItem(WHontunMultiSelect<?>.WHontunItem i, GuiRenderer renderer, double mouseX, double mouseY) {
        if (!i.mouseOver || i.checkboxHovered()) return;

        int x = snap(i.x), y = snap(i.y);
        int w = snap(i.x + i.width) - x, h = snap(i.y + i.height) - y;
        PixelCanvas.fill(x, y, w, h, light ? argb(0x48, selectTone) : argb(0x1A, text));
    }

    @Override
    public void paintTooltip(WHontunTooltip tt, GuiRenderer renderer, double mouseX, double mouseY) {
        if (tt.cells.isEmpty() || !(tt.cells.getFirst().widget() instanceof WHontunLabel label)) return;
        label.hidden = true;

        int p = unit();
        int tx = snap(label.x), ty = snap(label.y);
        int w = snap(label.x + label.width) - tx;
        int h = 8 * p;
        int fill = argb(0xF0, TIP_FILL);

        PixelCanvas.fill(tx - 3 * p, ty - 4 * p, w + 6 * p, p, fill);
        PixelCanvas.fill(tx - 3 * p, ty + h + 3 * p, w + 6 * p, p, fill);
        PixelCanvas.fill(tx - 3 * p, ty - 3 * p, w + 6 * p, h + 6 * p, fill);
        PixelCanvas.fill(tx - 4 * p, ty - 3 * p, p, h + 6 * p, fill);
        PixelCanvas.fill(tx + w + 3 * p, ty - 3 * p, p, h + 6 * p, fill);

        int top = argb(0x80, tipTop);
        int bottom = argb(0x50, tipBottom);
        PixelCanvas.fill(tx - 3 * p, ty - 3 * p, w + 6 * p, p, top);
        PixelCanvas.fill(tx - 3 * p, ty + h + 2 * p, w + 6 * p, p, bottom);
        PixelCanvas.vgrad(tx - 3 * p, ty - 2 * p, p, h + 4 * p, top, bottom);
        PixelCanvas.vgrad(tx + w + 2 * p, ty - 2 * p, p, h + 4 * p, top, bottom);

        PixelCanvas.text(tt.textValue(), tx, ty, opaque(ON_LIGHT), true);
    }

    @Override
    public void paintSwatchChip(WHontunSwatchLabel l, GuiRenderer renderer, double mouseX, double mouseY) {
        Color color = l.color;
        if (color == null || l.get().isEmpty()) return;

        int rgb = packed(color);
        if (HontunLightPalette.contrast(rgb, base) >= WHontunSwatchLabel.MIN_CONTRAST) return;

        int p = unit();
        int x = snap(l.x), y = snap(l.y);
        int w = snap(l.x + l.width) - x, h = snap(l.y + l.height) - y;
        int chip = HontunLightPalette.luminance(rgb) > 0.4 ? argb(0xC0, 0) : argb(0xC0, WHITE);
        PixelCanvas.fill(x - p, y - p, w + 2 * p, h + 2 * p, chip);
    }

    @Override
    public void paintCountChip(HontunSettingsWidgetFactory.WSelectedCountLabel l, GuiRenderer renderer, double mouseX, double mouseY) {
        int p = unit();
        int x = snap(l.x), y = snap(l.y) - p;
        int w = snap(l.x + l.width) - x;
        int h = 11 * p;

        PixelCanvas.fill(x + p, y + p, w - 2 * p, h - 2 * p, opaque(surface1));
        PixelCanvas.frame(x, y, w, h, opaque(light ? overlay1 : overlay0));
        PixelCanvas.text(l.label(), x + snap(l.offsetX()), snap(l.y + l.offsetY()), opaque(accentHi), !light);
    }

    @Override
    public void paintMeteorBackground(WWidget w, GuiRenderer renderer, boolean pressed, boolean hovered) {
        int x = snap(w.x), y = snap(w.y);
        int ww = snap(w.x + w.width) - x, h = snap(w.y + w.height) - y;
        HontunGuiTheme t = w.theme instanceof HontunGuiTheme ht ? ht : theme();
        buttonChrome(t, x, y, ww, h, hovered, pressed, 0);
    }

    @Override
    public void paintMeteorBackground(WWidget w, GuiRenderer renderer, Color outlineColor, Color backgroundColor) {
        int p = unit();
        int x = snap(w.x), y = snap(w.y);
        int ww = snap(w.x + w.width) - x, h = snap(w.y + w.height) - y;

        if (backgroundColor != null) PixelCanvas.fill(x + p, y + p, ww - 2 * p, h - 2 * p, backgroundColor.getPacked());
        if (outlineColor != null) PixelCanvas.frame(x, y, ww, h, outlineColor.getPacked());
    }

    @Override
    public void paintSnapGrid(GuiGraphicsExtractor g, int gridSize) {
        if (gridSize <= 0 || g == null) return;

        int p = u(theme());
        int step = lcm(gridSize, p);
        int windowWidth = Utils.getWindowWidth();
        int windowHeight = Utils.getWindowHeight();
        float guiScale = (float) mc.getWindow().getGuiScale();

        Matrix3x2fStack pose = g.pose();
        pose.pushMatrix();
        pose.scale(1f / guiScale, 1f / guiScale);

        long dots = (long) (windowWidth / step + 1) * (windowHeight / step + 1);

        cz.honzasik.hontun.gui.render.pixel.QuadBatchState.Builder batch = cz.honzasik.hontun.gui.render.pixel.QuadBatchState.builder();
        if (dots <= GRID_DOT_LIMIT) {
            int color = argb(light ? 0x90 : 0x60, overlay0);
            int shade = argb(0x60, 0);
            for (int x = 0; x <= windowWidth; x += step) {
                for (int y = 0; y <= windowHeight; y += step) {
                    if (light) batch.add(x + p, y + p, x + 2 * p, y + 2 * p, shade);
                    batch.add(x, y, x + p, y + p, color);
                }
            }
        } else {
            int color = argb(light ? 0x48 : 0x30, overlay0);
            if (light) {
                int shade = argb(0x30, 0);
                for (int x = 0; x <= windowWidth; x += step) batch.add(x + p, 0, x + 2 * p, windowHeight, shade);
                for (int y = 0; y <= windowHeight; y += step) batch.add(0, y + p, windowWidth, y + 2 * p, shade);
            }
            for (int x = 0; x <= windowWidth; x += step) batch.add(x, 0, x + p, windowHeight, color);
            for (int y = 0; y <= windowHeight; y += step) batch.add(0, y, windowWidth, y + p, color);
        }
        batch.submit(g);

        pose.popMatrix();
    }

    private void buttonChrome(HontunGuiTheme t, int x, int y, int w, int h, boolean hover, boolean pressed, int frameOverride) {
        int p = unit();
        int fill;
        int frame;

        if (light) {
            fill = opaque(pressed ? surface2 : hover ? tint : surface1);
            frame = opaque(pressed ? accentCorr : hover ? rim : accentEdge);
        } else if (pressed) {
            fill = opaque(accentEdge);
            frame = opaque(accentHi);
        } else if (hover) {
            fill = argb(0xCC, accentLo);
            frame = opaque(accentHi);
        } else {
            fill = alpha(0x99 / 255.0 * t.backgroundOpacity(), surface1);
            frame = argb(0xAA, accent);
        }

        if (frameOverride != 0) frame = frameOverride;

        PixelCanvas.fill(x + p, y + p, w - 2 * p, h - 2 * p, fill);
        PixelCanvas.frame(x, y, w, h, frame);
    }

    private void armedChrome(int x, int y, int w, int h) {
        int p = unit();
        boolean phase = (System.currentTimeMillis() / ARMED_BLINK) % 2 == 0;
        PixelCanvas.fill(x + p, y + p, w - 2 * p, h - 2 * p, argb(0xCC, red));
        PixelCanvas.frame(x, y, w, h, opaque(phase ? HontunTheme.lerp(red, WHITE, 0.4f) : WHITE));
    }

    private int buttonInk() {
        return light ? subtext1 : text;
    }

    private void strip(int x, int y, int w, int h, boolean hover) {
        PixelCanvas.fill(x, y, w, h, opaque(hover ? stripHover : stripFill));
        PixelCanvas.raised(x, y, w, h, opaque(stripLit), opaque(stripShade));
    }

    private void well(int x, int y, int w, int h, int fill) {
        PixelCanvas.fill(x, y, w, h, fill);
        PixelCanvas.sunk(x, y, w, h, opaque(wellShade), opaque(wellLit));
    }

    private void hardShadow(int x, int y, int w, int h, int offset) {
        hardShadow(x, y, w, h, offset, 0x70);
    }

    private void hardShadow(int x, int y, int w, int h, int offset, int alpha) {
        if (offset <= 0) return;
        int color = argb(alpha, 0);
        PixelCanvas.fill(x + w, y + offset, offset, h, color);
        PixelCanvas.fill(x + offset, y + h, w - offset, offset, color);
    }

    private void icon(GuiRenderer renderer, GuiTexture texture, int x, int y, int w, int h, int argb, boolean shadow) {
        if (texture == null) return;

        PixelGlyph glyph = glyphFor(texture);
        if (glyph != null) {
            glyphIn(glyph, x, y, w, h, argb, shadow);
            return;
        }

        int p = unit();
        int size = Math.min(8 * p, Math.min(w, h));
        int ix = x + snap((w - size) / 2.0);
        int iy = y + snap((h - size) / 2.0);
        renderer.quad(ix, iy, size, size, texture, color(argb));
    }

    private static void glyphIn(PixelGlyph glyph, int x, int y, int w, int h, int argb, boolean shadow) {
        if (glyph == null) return;

        int p = unit();
        int gx = x + Math.max(0, (w / p - PixelCanvas.glyphWidth(glyph)) / 2) * p;
        int gy = y + Math.max(0, (h / p - PixelCanvas.glyphHeight(glyph)) / 2) * p;
        PixelCanvas.glyph(glyph, gx, gy, p, argb, shadow);
    }

    private int[] computeBox(WHontunWindow w, int p) {
        int x = snap(w.x), y = snap(w.y);
        int width = snap(w.x + w.width) - x;
        int full = snap(w.y + w.height) - y;
        int collapsed = 16 * p;
        int height;

        if (!w.isExpanded() && !w.expandAnimating()) height = collapsed;
        else if (!w.expandAnimating()) height = Math.max(collapsed, full);
        else {
            double body = Math.max(0, full - collapsed) * Math.clamp(w.expandProgress(), 0.0, 1.0);
            height = collapsed + floorTo((int) Math.floor(body), 4 * p);
        }

        return new int[]{x, y, width, height};
    }

    private int[] box(WHontunWindow w, int p) {
        int[] b = boxes.get(w);
        return b != null ? b : computeBox(w, p);
    }

    private boolean dragActive() {
        return System.currentTimeMillis() - dragStamp < DRAG_HOLD;
    }

    private static boolean inTitleBar(WWidget widget) {
        WWidget w = widget.parent;
        for (int i = 0; i < 4 && w != null; i++, w = w.parent) {
            if (w instanceof WHontunWindow.WHontunHeader) return true;
        }
        return false;
    }

    private static int visibleRows(WHontunDropdown<?> d, WHontunDropdown.WRoot root) {
        int n = root.cells.size();
        double progress = d.indicatorProgress();
        if (progress >= 1) return n;
        return (int) Math.floor(Math.clamp(progress, 0.0, 1.0) * n);
    }

    private static int indexOf(WContainer container, WWidget widget) {
        List<Cell<?>> cells = container.cells;
        for (int i = 0; i < cells.size(); i++) {
            if (cells.get(i).widget() == widget) return i;
        }
        return Integer.MAX_VALUE;
    }

    private static String fit(String s, int maxWidth) {
        if (PixelCanvas.width(s) <= maxWidth) return s;
        if (PixelCanvas.width(ELLIPSIS) > maxWidth) return "";

        int end = s.length();
        while (end > 0 && PixelCanvas.width(s.substring(0, end) + ELLIPSIS) > maxWidth) end--;
        return s.substring(0, end).stripTrailing() + ELLIPSIS;
    }

    private static int onColor(int fill) {
        return HontunLightPalette.contrast(ON_LIGHT, fill) >= 3 ? ON_LIGHT : ON_DARK;
    }

    private static int checkInk(int fill) {
        return HontunLightPalette.luminance(fill) > 0.35 ? 0xE0000000 : 0xFFFFFFFF;
    }

    private static int readable(int rgb, int bg, double min) {
        int c = rgb & 0xFFFFFF;
        for (int i = 0; i < 48 && HontunLightPalette.contrast(c, bg) < min; i++) c = HontunTheme.darken(c, 0.92f);
        return c;
    }

    private static int brighten(int rgb, int against, double min) {
        int c = rgb & 0xFFFFFF;
        for (int i = 0; i < 24 && HontunLightPalette.contrast(c, against) < min; i++) c = HontunTheme.lerp(c, WHITE, 0.1f);
        return c;
    }

    private static int rimTone(int rgb) {
        int c = rgb & 0xFFFFFF;
        for (int i = 0; i < 48 && HontunLightPalette.luminance(c) > 0.17; i++) c = HontunTheme.darken(c, 0.95f);
        for (int i = 0; i < 24 && HontunLightPalette.luminance(c) < 0.11; i++) c = HontunTheme.lerp(c, WHITE, 0.06f);
        return c;
    }

    private static int hotFill(int fill, int... targets) {
        int best = fill;
        for (int target : targets) {
            for (int step = 20; step > 0; step--) {
                int c = HontunTheme.lerp(fill, target, step / 20f);
                if (HontunLightPalette.contrast(ON_LIGHT, c) < 4.5) continue;
                if (HontunLightPalette.contrast(c, fill) >= 1.35) return c;
                best = c;
                break;
            }
        }
        return best;
    }

    private static int clampBrightness(int rgb, float max) {
        float[] hsb = java.awt.Color.RGBtoHSB((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF, null);
        if (hsb[2] <= max) return rgb & 0xFFFFFF;
        return java.awt.Color.HSBtoRGB(hsb[0], hsb[1], max) & 0xFFFFFF;
    }

    private static GuiTexture builtin(HontunBuiltinIcons icon) {
        try {
            return icon.texture();
        } catch (IllegalStateException e) {
            return null;
        }
    }

    private static int lcm(int a, int b) {
        int x = Math.max(1, a), y = Math.max(1, b);
        int g = x, h = y;
        while (h != 0) {
            int r = g % h;
            g = h;
            h = r;
        }
        return x / g * y;
    }

    private static int floorTo(int v, int step) {
        if (step <= 0) return v;
        return Math.floorDiv(v, step) * step;
    }

    private static int unit() {
        return PixelCanvas.unit();
    }

    private static int u(HontunGuiTheme t) {
        return t != null ? PixelCanvas.unit(t) : PixelCanvas.unit();
    }

    private static int snap(double v) {
        return PixelCanvas.snap(v);
    }

    private static int opaque(int rgb) {
        return 0xFF000000 | (rgb & 0xFFFFFF);
    }

    private static int argb(int a, int rgb) {
        return ((a & 0xFF) << 24) | (rgb & 0xFFFFFF);
    }

    private static int alpha(double a, int rgb) {
        return argb((int) Math.round(Math.clamp(a, 0.0, 1.0) * 255), rgb);
    }

    private Color color(int argb) {
        scratch.set((argb >> 16) & 0xFF, (argb >> 8) & 0xFF, argb & 0xFF, (argb >>> 24) & 0xFF);
        return scratch;
    }

    private static boolean isSign(RichText label) {
        String plain = label.getPlainText();
        return "+".equals(plain) || "-".equals(plain);
    }
}
