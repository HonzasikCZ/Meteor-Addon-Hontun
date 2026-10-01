package cz.honzasik.hontun.gui.theme.style.smog;

import cz.honzasik.hontun.gui.api.animation.Easing;
import cz.honzasik.hontun.gui.api.render.Corners;
import cz.honzasik.hontun.gui.api.render.RoundedRect;
import cz.honzasik.hontun.gui.api.text.RichText;
import cz.honzasik.hontun.gui.api.text.TextScale;
import cz.honzasik.hontun.gui.render.HontunRenderer;
import cz.honzasik.hontun.gui.render.route.Routers;
import cz.honzasik.hontun.gui.screen.HontunModuleScreen;
import cz.honzasik.hontun.gui.theme.HontunGuiTheme;
import cz.honzasik.hontun.gui.theme.HontunLightPalette;
import cz.honzasik.hontun.gui.theme.HontunSettingsWidgetFactory;
import cz.honzasik.hontun.gui.theme.HontunWidget;
import cz.honzasik.hontun.gui.theme.icons.HontunBuiltinIcons;
import cz.honzasik.hontun.gui.theme.style.AnimRole;
import cz.honzasik.hontun.gui.theme.style.AnimSpec;
import cz.honzasik.hontun.gui.theme.style.ChipKind;
import cz.honzasik.hontun.gui.theme.style.ClickStyle;
import cz.honzasik.hontun.gui.theme.style.FontChoice;
import cz.honzasik.hontun.gui.theme.style.HoverTarget;
import cz.honzasik.hontun.gui.theme.style.Knob;
import cz.honzasik.hontun.gui.theme.style.Metrics;
import cz.honzasik.hontun.gui.theme.style.Pipeline;
import cz.honzasik.hontun.gui.theme.style.StylePalette;
import cz.honzasik.hontun.gui.theme.style.TabGlide;
import cz.honzasik.hontun.gui.theme.style.WindowKind;
import cz.honzasik.hontun.gui.theme.style.smog.SmogPaint.Glass;
import cz.honzasik.hontun.gui.theme.widgets.WHontunHorizontalSeparator;
import cz.honzasik.hontun.gui.theme.widgets.WHontunLabel;
import cz.honzasik.hontun.gui.theme.widgets.WHontunModule;
import cz.honzasik.hontun.gui.theme.widgets.WHontunMultiLabel;
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
import cz.honzasik.hontun.gui.theme.widgets.settings.WHontunDoubleEdit;
import cz.honzasik.hontun.gui.theme.widgets.settings.WHontunIntEdit;
import cz.honzasik.hontun.gui.theme.widgets.settings.WHontunKeybind;
import cz.honzasik.hontun.gui.widget.WGuiTexture;
import cz.honzasik.hontun.gui.widget.input.WMultiSelect;
import cz.honzasik.hontun.utils.HontunTheme;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.renderer.packer.GuiTexture;
import meteordevelopment.meteorclient.gui.utils.AlignmentX;
import meteordevelopment.meteorclient.gui.utils.Cell;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;
import meteordevelopment.meteorclient.gui.widgets.containers.WView;
import meteordevelopment.meteorclient.gui.widgets.input.WTextBox;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.joml.Matrix3x2fStack;

import java.util.Map;
import java.util.WeakHashMap;

import static cz.honzasik.hontun.gui.theme.style.smog.SmogAnim.AUX;
import static cz.honzasik.hontun.gui.theme.style.smog.SmogAnim.DRAG;
import static cz.honzasik.hontun.gui.theme.style.smog.SmogAnim.FOCUS;
import static cz.honzasik.hontun.gui.theme.style.smog.SmogAnim.HOVER;
import static cz.honzasik.hontun.gui.theme.style.smog.SmogAnim.ON;
import static cz.honzasik.hontun.gui.theme.style.smog.SmogAnim.PRESS;
import static cz.honzasik.hontun.gui.theme.style.smog.SmogPaint.CLEAR;
import static cz.honzasik.hontun.gui.theme.style.smog.SmogPaint.blend;
import static cz.honzasik.hontun.gui.theme.style.smog.SmogPaint.clamp01;
import static cz.honzasik.hontun.gui.theme.style.smog.SmogPaint.col;
import static cz.honzasik.hontun.gui.theme.style.smog.SmogPaint.edges;
import static cz.honzasik.hontun.gui.theme.style.smog.SmogPaint.exact;
import static cz.honzasik.hontun.gui.theme.style.smog.SmogPaint.lerp;
import static cz.honzasik.hontun.gui.theme.style.smog.SmogPaint.mix;
import static cz.honzasik.hontun.gui.theme.style.smog.SmogPaint.rd;
import static cz.honzasik.hontun.gui.theme.style.smog.SmogPaint.scaleAlpha;
import static meteordevelopment.meteorclient.MeteorClient.mc;

public class SmogClickStyle extends ClickStyle {
    private static final double SMALL = TextScale.SMALL.get();
    private static final double LARGE = TextScale.LARGE.get();

    private static final String LISTENING = "Press a key...";
    private static final String SEARCH_PLACEHOLDER = "Search modules and settings";
    private static final String ESC_HINT = "esc to close";
    private static final String NO_RESULTS = "No results";
    private static final String FROM = "From: ";

    private static final long DRAG_HOLD = 100_000_000L;
    private static final long HELP_SHADOW_NS = 5_000_000L;

    private final Color sub1 = new Color(0xC8, 0xCD, 0xD2);
    private final Color sub0Soft = new Color(0x90, 0x96, 0xA0, 0xB3);
    private final Color sub0Min = new Color(0x90, 0x96, 0xA0, 0x99);
    private final Color helpText = new Color(0x90, 0x96, 0xA0, 0xCC);
    private final Color backdropHelpKey = new Color(0xFF, 0xFF, 0xFF);
    private final Color backdropHelpText = new Color(0xC8, 0xCD, 0xD2, 0xCC);
    private final Color helpShadow = new Color(0x00, 0x00, 0x00, 0xB4);

    private int helpShadows;
    private long helpShadowUntil;

    private int cText = 0xFFFFFF;
    private int cBase = 0x0A0A0C;
    private int cMantle = 0x050506;
    private int cCrust = 0x000000;
    private int cS0 = 0x0E0E10;
    private int cAccent = 0xE6E9EC;
    private int cAccentHi = 0xFFFFFF;
    private int cRed = 0xFF6B7A;

    private boolean boost;
    private boolean boostKnown;

    private long lastDrag;

    private final Map<Object, Puck> pucks = new WeakHashMap<>();
    private final Map<Object, long[]> tooltips = new WeakHashMap<>();
    private final Map<Object, double[]> bars = new WeakHashMap<>();
    private final Map<Object, Boolean> polished = new WeakHashMap<>();
    private final Map<Object, Double> lensInk = new WeakHashMap<>();

    @Override
    public HontunTheme.UiMode id() {
        return HontunTheme.UiMode.SmogClient;
    }

    @Override
    protected Metrics createMetrics() {
        Metrics m = new Metrics();

        m.windowPad = 0;

        m.headerPadH = 16;
        m.headerPadV = 11;
        m.headerSpacing = 8;

        m.topBarMarginTop = 14;
        m.topBarItemPad = 4;
        m.tabPadH = 14;
        m.tabPadV = 7;
        m.tabIconGap = 7;

        m.rowPadH = 14;
        m.rowPadV = 7;
        m.activeBarWidth = 0;
        m.activeBarInset = 0;

        m.viewSpacing = 2;

        m.sectionHeaderPadH = 6;
        m.sectionHeaderPadV = 6;
        m.sectionPad = 6;
        m.tablePad = 12;
        m.tableVSpacing = 10;

        m.tooltipPadH = 10;
        m.tooltipPadV = 6;

        m.moduleScreenPad = 16;
        m.moduleInfoSpacing = 10;

        m.separatorThickness = 1;

        m.searchHeaderPad = 14;
        m.searchViewPad = 8;
        m.searchRowPad = 8;
        m.searchResultPadH = 8;

        m.dropdownPad = 4;

        return m;
    }

    @Override
    public Pipeline pipeline() {
        return Pipeline.ROUNDED;
    }

    @Override
    public FontChoice font() {
        return FontChoice.SF;
    }

    @Override
    public boolean uses(Knob knob) {
        return knob != Knob.PALETTE_COLOR;
    }

    @Override
    public double radiusFactor() {
        return 1.0;
    }

    @Override
    public double chromeMargin(HontunGuiTheme t) {
        return u(t, 44);
    }

    @Override
    public double windowAlpha(double raw, boolean light) {
        double o = clamp01(raw);
        return light ? 0.80 + 0.18 * o : 0.62 + 0.26 * o;
    }

    @Override
    public double controlAlpha(double raw, boolean light) {
        return clamp01(raw);
    }

    @Override
    public boolean legibilityShadow(HontunGuiTheme t) {
        if (!t.light() || helpShadows <= 0) return false;
        helpShadows--;
        return System.nanoTime() <= helpShadowUntil;
    }

    @Override
    public Color textShadowColor() {
        return theme().light() ? helpShadow : super.textShadowColor();
    }

    @Override
    public AnimSpec anim(AnimRole role) {
        return switch (role) {
            case WINDOW_EXPAND, SECTION_EXPAND, MULTISELECT_EXPAND -> expandSpec();
            case WINDOW_CORNER, SECTION_CORNER -> AnimSpec.of(Easing.QUART_OUT, 200, 200);
            case MODULE_ACTIVE -> AnimSpec.of(Easing.QUART_OUT, 240, 240);
            case MODULE_HOVER, BUTTON_HOVER, DROPDOWN_HOVER, HEADER_HOVER -> AnimSpec.of(Easing.QUAD_OUT, 120, 180);
            case TAB_SELECT -> AnimSpec.of(Easing.QUART_OUT, 280, 280);
            case CHECKBOX -> AnimSpec.of(Easing.QUART_OUT, 220, 220);
            case DROPDOWN_OPEN -> AnimSpec.of(Easing.QUART_OUT, 180, 180);
            case TEXTBOX_FOCUS -> AnimSpec.of(Easing.QUAD_OUT, 140, 140);
            case SLIDER_HOVER, SCROLLBAR -> AnimSpec.of(Easing.QUAD_OUT, 120, 120);
            case FAVORITE -> AnimSpec.of(Easing.QUART_OUT, 160, 160);
            default -> AnimSpec.NONE;
        };
    }

    private AnimSpec expandSpec() {
        HontunGuiTheme t = theme();
        Easing easing = t.guiAnimationEasing();
        if (easing == null || easing.name().startsWith("BACK")) easing = Easing.QUART_OUT;

        int duration = t.guiAnimationDuration();
        return AnimSpec.of(easing, duration, duration);
    }

    @Override
    public void palette(StylePalette out, boolean light, HontunGuiTheme t) {
        double o = t.windowOpacity.get();
        if (!boostKnown) {
            boost = o < 0.80;
            boostKnown = true;
        }
        else if (o < 0.78) boost = true;
        else if (o > 0.82) boost = false;

        if (light) {
            out.crust = 0xEBEBED;
            out.mantle = 0xF5F5F6;
            out.base = 0xFFFFFF;
            out.surface0 = 0xEEEEF0;
            out.surface1 = 0xE3E3E6;
            out.surface2 = 0xD7D7DB;
            out.overlay0 = 0xC2C2C7;
            out.overlay1 = 0xA6A6AD;
            out.overlay2 = 0x8A8A92;
            out.text = 0x0A0A0C;
            out.subtext1 = 0x2C2C30;
            out.subtext0 = 0x4A4A50;
            out.accent = 0x26272B;
            out.accentHi = 0x0E0E10;
            out.accentLo = 0x5A5A60;
            out.red = 0xD20F39;
            out.green = 0x1E8E3E;
            out.yellow = 0x9A6700;
        }
        else {
            out.crust = 0x000000;
            out.mantle = 0x050506;
            out.base = 0x0A0A0C;
            out.surface0 = 0x0E0E10;
            out.surface1 = 0x151517;
            out.surface2 = 0x1E1E20;
            out.overlay0 = 0x2A2A2C;
            out.overlay1 = 0x3C3C3E;
            out.overlay2 = 0x555558;
            out.text = 0xFFFFFF;
            out.subtext1 = 0xC8CDD2;
            out.subtext0 = 0x9096A0;
            out.accent = 0xE6E9EC;
            out.accentHi = 0xFFFFFF;
            out.accentLo = 0x8A9196;
            out.red = 0xFF6B7A;
            out.green = 0x5EE0A0;
            out.yellow = 0xF5D28A;
        }

        if (boost) {
            out.subtext0 = out.subtext1;
            out.subtext1 = out.text;
        }

        out.userAccent = HontunTheme.userAccent();
        out.userAccentHi = HontunTheme.userAccentHi();
        out.userAccentLo = HontunTheme.userAccentLo();

        cText = out.text;
        cBase = out.base;
        cMantle = out.mantle;
        cCrust = out.crust;
        cS0 = out.surface0;
        cAccent = out.accent;
        cAccentHi = out.accentHi;
        cRed = out.red;

        SmogPaint.set(sub1, out.subtext1, 255);
        SmogPaint.set(sub0Soft, out.subtext0, light ? 0xE6 : 0xB3);
        SmogPaint.set(sub0Min, out.subtext0, light ? 0xD9 : 0x99);
        SmogPaint.set(helpText, out.subtext0, 0xCC);

        HontunTheme.Ramp backdrop = HontunTheme.ramp(id());
        SmogPaint.set(backdropHelpKey, boost ? backdrop.text() : backdrop.subtext1(), 255);
        SmogPaint.set(backdropHelpText, boost ? backdrop.subtext1() : backdrop.textDim(), 0xCC);
    }

    @Override
    public WWidget windowIcon(HontunGuiTheme t, WindowKind kind, Category category, WWidget fallback) {
        if (!(fallback instanceof WGuiTexture icon)) return fallback;

        return t.texture(icon.texture, Math.round(0.9 * t.textHeight())).color(sub1);
    }

    @Override
    public WWidget headerLabel(HontunGuiTheme t, WHontunWindow w, String title) {
        SmogTitle label = new SmogTitle(w, RichText.of(windowTitle(title)).scale(LARGE));
        label.theme = t;
        return label;
    }

    @Override
    public WWidget sectionTitle(HontunGuiTheme t, WHontunSection s, String title) {
        SmogSectionTitle widget = new SmogSectionTitle(s, title);
        widget.theme = t;
        return widget;
    }

    @Override
    public RichText keybindLabel(HontunGuiTheme t, WHontunKeybind k, boolean listening, String key) {
        if (listening) return RichText.of(LISTENING);
        return RichText.of(k.title()).scale(SMALL).append("  ").append(key);
    }

    @Override
    public Color labelColor(HontunGuiTheme t, boolean title) {
        return t.textColor();
    }

    @Override
    public Color helpKeyColor(HontunGuiTheme t) {
        if (t.light()) return backdropHelpKey;
        return sub1;
    }

    @Override
    public Color helpTextColor(HontunGuiTheme t) {
        if (t.light()) {
            helpShadows = t.richText() ? 2 : 0;
            helpShadowUntil = System.nanoTime() + HELP_SHADOW_NS;
            return backdropHelpText;
        }
        return helpText;
    }

    @Override
    public Color favoriteColor(WHontunFavorite f) {
        HontunGuiTheme t = f.theme();
        if (f.checked) return t.textColor();
        return f.mouseOver ? sub1 : sub0Soft;
    }

    private static double s(HontunGuiTheme t) {
        return t.scale(1);
    }

    private static double u(HontunGuiTheme t, double n) {
        return Math.round(n * s(t));
    }

    private static double hair(HontunGuiTheme t) {
        return Math.max(1, Math.round(s(t)));
    }

    private static double cardRadius(HontunGuiTheme t) {
        double r = Math.round(u(t, 16) * t.cornerRadius.get() / 10.0);
        return Math.clamp(r, u(t, 8), u(t, 24));
    }

    private static double controlHeight(HontunGuiTheme t) {
        return 2 * u(t, 6) + Math.round(t.textHeight());
    }

    private Color ink(double a) {
        return col(cText, a);
    }

    private static Color shade(double a) {
        return col(0x000000, a);
    }

    private static void label(RichText text, double x, double y, Color color) {
        text().text(text, Math.round(x), Math.round(y), color);
    }

    private static void icon(GuiRenderer renderer, GuiTexture texture, double cx, double cy, double size, Color color) {
        if (texture == null || size <= 0 || color.a <= 0) return;
        double s = Math.round(size);
        renderer.quad(Math.round(cx - s / 2), Math.round(cy - s / 2), s, s, texture, color);
    }

    private static void rotatedIcon(GuiRenderer renderer, GuiTexture texture, double cx, double cy, double size, double rotation, Color color) {
        if (texture == null || size <= 0 || color.a <= 0) return;
        double s = Math.round(size);
        renderer.rotatedQuad(Math.round(cx - s / 2), Math.round(cy - s / 2), s, s, rotation, texture, color);
    }

    private static GuiTexture chevronTexture() {
        try {
            return HontunBuiltinIcons.CHEVRON.texture();
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static void chevron(GuiRenderer renderer, double cx, double cy, double size, double rotation, Color color) {
        GuiTexture texture = chevronTexture();
        if (texture != null) rotatedIcon(renderer, texture, cx, cy, size, rotation, color);
        else rotatedIcon(renderer, HontunBuiltinIcons.ARROW.texture(), cx, cy, size, rotation + 90, color);
    }

    private Color glassFill(HontunGuiTheme t, Glass level, double fillAlpha, double fade) {
        int rgb = level == Glass.DOCK && t.light() ? cMantle : cBase;
        return col(rgb, 255 * clamp01(fillAlpha) * fade);
    }

    private void glass(HontunGuiTheme t, Glass level, double x0, double y0, double x1, double y1, double r,
                       double fillAlpha, double ambient, double lift, double fade) {
        boolean dark = !t.light();
        Color top = ink((dark ? 0x24 : 0x20) * fade);
        Color bottom = ink((dark ? 0x0E : 0x20) * fade);

        SmogPaint.glass(level, x0, y0, x1, y1, r, s(t), !dark, t.effWindowShadow(), ambient, lift, fade,
                glassFill(t, level, fillAlpha, fade), top, bottom, hair(t));
    }

    private void glassBody(HontunGuiTheme t, Glass level, double x0, double y0, double x1, double y1, double r, double fillAlpha) {
        boolean dark = !t.light();
        SmogPaint.body(x0, y0, x1, y1, r, glassFill(t, level, fillAlpha, 1), ink(dark ? 0x24 : 0x20), ink(dark ? 0x0E : 0x20), hair(t));
    }

    private static double popupAlpha(HontunGuiTheme t) {
        return Math.min(1, Math.max(0.98, t.windowOpacity() + 0.08));
    }

    private void controlPill(HontunGuiTheme t, double x0, double y0, double x1, double y1, double hover, double active, double press) {
        double bo = t.backgroundOpacity();
        boolean lt = t.light();

        double f = lerp(lerp(lt ? 0x14 : 0x0F, lt ? 0x20 : 0x18, hover), lt ? 0x2E : 0x22, active);
        double o = lerp(lerp(lt ? 0x1C : 0x10, lt ? 0x2A : 0x1C, hover), lt ? 0x38 : 0x24, active);

        double inset = Math.round(u(t, 1) * clamp01(press));
        double r = Math.max(0, Math.round(0.42 * (y1 - y0)) - inset);

        SmogPaint.control(x0 + inset, y0 + inset, x1 - inset, y1 - inset, r, ink(f * bo), ink(o), hair(t));
    }

    private void ghost(HontunGuiTheme t, GuiRenderer renderer, WWidget w, double hover, double press,
                       GuiTexture texture, Color iconColor, Color fill, Color outline) {
        double d = Math.min(Math.round(w.width), Math.round(w.height));
        double cx = rd(w.x + w.width / 2);
        double cy = rd(w.y + w.height / 2);
        double x0 = rd(cx - d / 2);
        double y0 = rd(cy - d / 2);

        Color base = fill != null ? fill : ghostInk(t, hover, press);
        boolean ring = outline != null && outline.a > 0;

        if (base.a > 0 || ring) {
            RoundedRect rect = edges(x0, y0, x0 + d, y0 + d).radius(d / 2).color(base.a > 0 ? base : CLEAR);
            if (ring) rect.outline(outline, (float) hair(t));
            rect.render();
        }

        icon(renderer, texture, x0 + d / 2, y0 + d / 2, Math.round(0.55 * d), iconColor);
    }

    private Color ghostInk(HontunGuiTheme t, double hover, double press) {
        boolean lt = t.light();
        return ink(lerp((lt ? 0x1A : 0x12) * hover, lt ? 0x2C : 0x1E, clamp01(press)));
    }

    private static boolean headerHovered(WWidget w) {
        WWidget p = w;
        for (int i = 0; i < 4 && p != null; i++) {
            if (p instanceof WHontunWindow.WHontunHeader
                    || p instanceof WHontunSection.WHontunHeader
                    || p instanceof WMultiSelect.WHeader) return p.mouseOver;
            p = p.parent;
        }
        return w.mouseOver;
    }

    private static boolean inMultiSelect(WWidget w) {
        WWidget p = w.parent;
        for (int i = 0; i < 3 && p != null; i++) {
            if (p instanceof WMultiSelect<?>) return true;
            p = p.parent;
        }
        return false;
    }

    private static boolean isEditValue(WHontunTextBox b) {
        WWidget p = b.parent != null ? b.parent.parent : null;
        return p instanceof WHontunIntEdit || p instanceof WHontunDoubleEdit;
    }

    private static boolean inSearchHeader(WWidget w) {
        return w.parent != null && w.parent.parent instanceof WHontunSearch.WHontunHeader;
    }

    void layoutWindow(WHontunWindow w, SmogTitle title) {
        Metrics m = metrics();
        boolean hero = w.isDialog();

        WWidget list = title.parent;
        if (list != null && list.parent instanceof WContainer header) {
            for (Cell<?> cell : header.cells) {
                if (cell.widget() != list) continue;
                cell.padHorizontal(hero ? 20 : m.headerPadH);
                cell.padVertical(hero ? 14 : m.headerPadV);
            }
        }

        WView view = w.view;
        if (view == null) return;

        for (Cell<?> cell : w.cells) {
            if (cell.widget() != view) continue;

            if (hero) {
                double pad = w.padding <= 0 ? 0 : Math.max(w.padding, m.moduleScreenPad);
                cell.padLeft(pad).padRight(pad).padTop(pad).padBottom(pad);
            }
            else {
                cell.padLeft(w.padding).padRight(w.padding).padTop(4).padBottom(8);
            }
        }
    }

    void layoutSection(SmogSectionTitle title) {
        HontunGuiTheme t = title.theme();
        WWidget list = title.parent;
        if (!(list instanceof WContainer row) || !(list.parent instanceof WContainer header)) return;

        double tallest = title.height;
        for (Cell<?> cell : row.cells) {
            if (cell.widget() == title) continue;
            tallest = Math.max(tallest, cell.padTop() + cell.widget().height + cell.padBottom());
        }

        double target = 2 * u(t, 6) + Math.round(title.height);
        double pad = Math.max(0, (target - tallest) / 2) / s(t);

        for (Cell<?> cell : header.cells) {
            if (cell.widget() == list) cell.padVertical(pad);
        }
    }

    void paintSectionTitle(SmogSectionTitle title, GuiRenderer renderer) {
        WHontunSection section = title.section();
        WHontunSection.WHontunHeader h = section.headerWidget();
        if (h == null) return;

        HontunGuiTheme t = title.theme();
        double hp = SmogAnim.tween(h, HOVER, h.mouseOver, anim(AnimRole.HEADER_HOVER));

        RichText text = title.label();
        double tw = t.textWidth(text);
        double th = t.textHeight(text);

        double lx = rd(h.x + u(t, 4));
        double ty = rd(h.y + (h.height - th) / 2);

        label(text, lx, ty, mix(t.textSecondaryColor(), sub1, hp));

        double end = h.x + h.width - u(t, 10);
        WWidget indicator = h.openIndicator() != null ? h.openIndicator() : section.customHeaderWidget();
        if (indicator != null) {
            double glyph = indicator instanceof WHontunTriangle ? Math.round(0.62 * t.textHeight()) : indicator.width;
            end = indicator.x + (indicator.width - glyph) / 2 - u(t, 10);
        }

        double start = lx + tw + u(t, 10);
        double hw = hair(t);
        SmogPaint.rule(start, end, rd(h.y + (h.height - hw) / 2), hw, ink(t.light() ? 0x1A : 0x10), 0);
    }

    @Override
    public void moduleSize(WHontunModule m, double[] out) {
        HontunGuiTheme t = m.theme();
        out[0] = m.titleWidth() + 2 * (u(t, 6) + u(t, 24));
        out[1] = u(t, 7) + Math.round(t.textHeight()) + u(t, 7);
    }

    @Override
    public void tabSize(WHontunTopBar.WTopBarButton b, double[] out) {
        HontunGuiTheme t = b.bar().theme();
        double icon = b.hasIcon() ? Math.round(0.85 * t.textHeight()) + u(t, 7) : 0;
        out[0] = u(t, 14) + icon + t.textWidth(b.text()) + u(t, 14);
        out[1] = u(t, 7) + Math.round(t.textHeight()) + u(t, 7);
    }

    @Override
    public void checkboxSize(WHontunCheckbox c, double[] out) {
        HontunGuiTheme t = c.theme();

        if (inMultiSelect(c)) {
            out[0] = u(t, 16);
            out[1] = u(t, 16);
            return;
        }

        out[0] = u(t, 32);
        out[1] = u(t, 18);
    }

    @Override
    public void dropdownSize(WHontunDropdown<?> d, double[] out) {
        HontunGuiTheme t = d.theme();
        double width = u(t, 12) + d.maxValueWidth() + u(t, 8) + Math.round(0.62 * t.textHeight()) + u(t, 12);
        if (hasTitle(d)) width += t.textWidth(d.titleText()) + u(t, 8);

        out[0] = width;
        out[1] = controlHeight(t);
    }

    private static boolean hasTitle(WHontunDropdown<?> d) {
        return d.titleText() != null && !d.titleText().getPlainText().isEmpty();
    }

    @Override
    public void buttonSize(WHontunButton b, double[] out) {
        HontunGuiTheme t = b.theme();
        double h = controlHeight(t);

        if (b.parent instanceof WHontunKeybind k) {
            RichText bind = RichText.of(k.title()).scale(SMALL);
            double idle = t.textWidth(bind) + u(t, 6) + t.textWidth(k.keybind().toString());
            double listen = t.textWidth(LISTENING);
            out[0] = u(t, 12) + Math.max(idle, listen) + u(t, 12);
            out[1] = h;
            return;
        }

        RichText text = b.displayText();

        if (text == null) {
            double d = isSharing(b.icon()) ? u(t, 28) : u(t, 26);
            out[0] = d;
            out[1] = d;
            return;
        }

        if (isStep(text)) {
            out[0] = u(t, 26);
            out[1] = u(t, 26);
            return;
        }

        out[0] = u(t, 14) + t.textWidth(text) + u(t, 14);
        out[1] = h;
    }

    private static boolean isSharing(GuiTexture texture) {
        if (texture == null) return false;
        return texture == HontunBuiltinIcons.COPY.texture() || texture == HontunBuiltinIcons.IMPORT.texture();
    }

    private static boolean isStep(RichText text) {
        String plain = text.getPlainText();
        return "+".equals(plain) || "-".equals(plain);
    }

    @Override
    public void pressableSize(WWidget w, double[] out) {
        HontunGuiTheme t = (HontunGuiTheme) w.getTheme();

        if (w instanceof WHontunConfirmedButton b) {
            String text = b.getText();
            if (text != null) {
                out[0] = u(t, 14) + t.textWidth(text) + u(t, 14);
                out[1] = controlHeight(t);
            }
            else {
                out[0] = u(t, 26);
                out[1] = u(t, 26);
            }
            return;
        }

        if (w instanceof WHontunPlus || w instanceof WHontunMinus || w instanceof WHontunConfirmedMinus) {
            out[0] = u(t, 26);
            out[1] = u(t, 26);
        }
    }

    @Override
    public void textBoxSize(WHontunTextBox b, double[] out) {
        super.textBoxSize(b, out);

        if (!inSearchHeader(b)) return;

        HontunGuiTheme t = b.theme();
        WWidget row = b.parent;
        WWidget header = row.parent;

        if (header instanceof WContainer hc) {
            for (Cell<?> cell : hc.cells) {
                if (cell.widget() == row) cell.padHorizontal(20).padVertical(14);
            }
        }

        double iconSize = Math.round(0.9 * t.textHeight());

        if (row instanceof WContainer rc) {
            for (Cell<?> cell : rc.cells) {
                if (cell.widget() instanceof WGuiTexture icon) {
                    icon.size = iconSize;
                    icon.width = iconSize;
                    icon.height = iconSize;
                    icon.color = t.textSecondaryColor();
                }
            }
        }

        double others = 2 * u(t, 20) + iconSize + t.textWidth("ESC to close") + 2 * t.scale(3);
        out[0] = Math.max(out[0], u(t, 560) - others);
    }

    @Override
    public double sliderHeight(WHontunSlider s) {
        return u(s.theme(), 20);
    }

    @Override
    public double popupOffset(HontunGuiTheme t) {
        return u(t, 4);
    }

    @Override
    public float radius(HontunWidget w) {
        return (float) cardRadius(w.theme());
    }

    @Override
    public float smallRadius(HontunWidget w) {
        return (float) u(w.theme(), 10);
    }

    @Override
    public float outlineWidth(HontunWidget w) {
        return (float) hair(w.theme());
    }

    @Override
    public Corners corners(HontunWidget w) {
        return Corners.ALL;
    }

    @Override
    public void paintWindowBack(WHontunWindow w, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme t = w.theme();
        long now = System.nanoTime();
        if (w.isDragging()) lastDrag = now;
        boolean anyDrag = lastDrag != 0 && now - lastDrag < DRAG_HOLD;

        boolean hero = w.isDialog();
        double hh = w.headerWidget().height;
        double body = Math.max((w.height - hh) * Math.max(0, w.expandProgress()), 0);

        double x0 = rd(w.x), y0 = rd(w.y), x1 = rd(w.x + w.width);
        double y1 = rd(w.y + hh + body);

        double card = hero ? u(t, 18) : cardRadius(t);
        double cap = (y1 - y0) / 2;
        double r = lerp(Math.min(cap, card), card, clamp01(w.cornerProgress()));

        double ambient = anyDrag ? 0 : clamp01(w.isExpanded() ? 1 : w.expandProgress());
        double lift = SmogAnim.tween(w, DRAG, w.isDragging(), Easing.QUAD_OUT, 140, 140);

        glass(t, hero ? Glass.HERO : Glass.WINDOW, x0, y0, x1, y1, r, t.windowOpacity(), ambient, lift, 1);

        if (hero) polishModuleScreen(w, t);
    }

    private void polishModuleScreen(WHontunWindow w, HontunGuiTheme t) {
        if (mc == null || mc.gui == null || !(mc.gui.screen() instanceof HontunModuleScreen)) return;
        if (w.view == null || w.view.cells.isEmpty()) return;
        if (!(w.view.cells.get(0).widget() instanceof WContainer info)) return;
        if (polished.containsKey(info)) return;
        polished.put(info, Boolean.TRUE);

        for (Cell<?> cell : info.cells) {
            WWidget widget = cell.widget();

            if (widget instanceof WHontunMultiLabel description && description.color == null) {
                description.color = sub1;
                continue;
            }

            if (!(widget instanceof WContainer row) || row.cells.isEmpty()) continue;
            if (!(row.cells.get(0).widget() instanceof WHontunLabel from) || !FROM.equals(from.get())) continue;

            for (Cell<?> inner : row.cells) {
                if (!(inner.widget() instanceof WHontunLabel l)) continue;

                l.set(RichText.of(l.get()).scale(SMALL));
                l.color = l == from ? t.textSecondaryColor() : t.textColor();
            }
        }
    }

    @Override
    public void paintWindowHeader(WHontunWindow w, WHontunWindow.WHontunHeader h, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme t = w.theme();
        double p = clamp01(w.expandProgress());
        if (p <= 0) return;

        double inset = w.isDialog() ? u(t, 20) : u(t, 16);
        double hw = hair(t);
        double y = rd(h.y + h.height);

        SmogPaint.rule(w.x + inset, w.x + w.width - inset, y, hw, ink((t.light() ? 0x1C : 0x12) * p), 0);
    }

    @Override
    public void paintWindowFront(WHontunWindow w, GuiRenderer renderer, double mouseX, double mouseY) {
    }

    @Override
    public void paintTopBar(WHontunTopBar b, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme t = b.theme();
        double x0 = rd(b.x), y0 = rd(b.y), x1 = rd(b.x + b.width), y1 = rd(b.y + b.height);

        glass(t, Glass.DOCK, x0, y0, x1, y1, (y1 - y0) / 2, t.windowOpacity(), 1, 0, 1);

        WHontunTopBar.WTopBarButton selected = null;
        for (Cell<?> cell : b.cells) {
            if (cell.widget() instanceof WHontunTopBar.WTopBarButton button && button.isSelected()) {
                selected = button;
                break;
            }
        }

        if (selected == null) return;

        double tx = rd(selected.x), ty = rd(selected.y);
        double tw = rd(selected.x + selected.width) - tx;
        double th = rd(selected.y + selected.height) - ty;

        long now = System.nanoTime();
        Puck puck = pucks.get(b);

        if (puck == null) {
            puck = new Puck();
            puck.start = now;
            puck.selected = selected.tab();

            if (TabGlide.fresh(400) && TabGlide.w > 0 && (TabGlide.x != tx || TabGlide.w != tw)) {
                puck.slide = true;
                puck.fromX = TabGlide.x;
                puck.fromW = TabGlide.w;
            }

            pucks.put(b, puck);
        }
        else if (puck.selected != selected.tab()) {
            puck.fromX = puck.x;
            puck.fromW = puck.w;
            puck.slide = true;
            puck.start = now;
            puck.selected = selected.tab();
        }

        double elapsed = (now - puck.start) / 1_000_000.0;
        double x = tx, w = tw, a = 1;

        if (puck.slide) {
            double e = Easing.QUART_OUT.apply(clamp01(elapsed / 280.0));
            x = lerp(puck.fromX, tx, e);
            w = lerp(puck.fromW, tw, e);
        }
        else {
            a = clamp01(elapsed / 120.0);
        }

        puck.x = x;
        puck.w = w;

        double r = th / 2;

        if (t.light()) {
            SmogPaint.halo(x, ty + u(t, 1), x + w, ty + th + u(t, 1), r, u(t, 2), u(t, 4), shade(0x18 * a));
            edges(x, ty, x + w, ty + th).radius(r).color(col(cBase, 255 * a)).outline(ink(0x18 * a), (float) hair(t)).render();
        }
        else {
            edges(x, ty, x + w, ty + th).radius(r).color(ink(0x16 * a)).outline(ink(0x14 * a), (float) hair(t)).render();
        }

        TabGlide.store(x, ty, w, th);
    }

    @Override
    public void paintTab(WHontunTopBar.WTopBarButton b, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme t = b.bar().theme();
        double T = t.textHeight();
        boolean selected = b.isSelected();
        double hp = SmogAnim.tween(b, HOVER, b.mouseOver && !selected, anim(AnimRole.BUTTON_HOVER));

        double x0 = rd(b.x), y0 = rd(b.y), x1 = rd(b.x + b.width), y1 = rd(b.y + b.height);

        if (!selected && hp > 0) SmogPaint.pill(x0, y0, x1, y1, ink((t.light() ? 0x0F : 0x0A) * hp));

        Color fg = selected ? t.textColor() : mix(t.textSecondaryColor(), sub1, hp);

        double iconSize = b.hasIcon() ? Math.round(0.85 * T) : 0;
        double gap = b.hasIcon() ? u(t, 7) : 0;
        double textWidth = t.textWidth(b.text());
        double x = rd((x0 + x1) / 2 - (iconSize + gap + textWidth) / 2);
        double cy = (y0 + y1) / 2;

        if (b.hasIcon()) {
            icon(renderer, b.icon(), x + iconSize / 2, cy, iconSize, fg);
            x += iconSize + gap;
        }

        label(b.text(), x, cy - T / 2, fg);
    }

    @Override
    public void paintModule(WHontunModule m, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme t = m.theme();
        boolean lt = t.light();
        double T = t.textHeight();

        double hp = clamp01(m.hoverProgress());
        double ap = clamp01(m.highlightProgress());
        double pp = SmogAnim.tween(m, PRESS, m.isPressed(), Easing.QUAD_OUT, 80, 80);

        double x0 = rd(m.x), y0 = rd(m.y), x1 = rd(m.x + m.width), y1 = rd(m.y + m.height);
        double px0 = x0 + u(t, 6), py0 = y0 + u(t, 1), px1 = x1 - u(t, 6), py1 = y1 - u(t, 1);
        double inset = Math.round(u(t, 1) * pp);

        if (ap > 0) {
            double pw = (px1 - px0) * (0.9 + 0.1 * ap);
            double cx = (px0 + px1) / 2;
            double a = (lerp(lt ? 0x1C : 0x13, lt ? 0x2A : 0x1B, hp) + (lt ? 0x0C : 0x08) * pp) * ap;
            SmogPaint.pill(cx - pw / 2 + inset, py0 + inset, cx + pw / 2 - inset, py1 - inset, ink(a));
        }

        if (pp > 0 && ap < 1) {
            SmogPaint.pill(px0 + inset, py0 + inset, px1 - inset, py1 - inset, ink((lt ? 0x0C : 0x08) * pp * (1 - ap)));
        }

        boolean right = t.moduleAlignment.get() == AlignmentX.Right;

        if (ap > 0) {
            double d = u(t, 5) * ap;
            double cx = right ? px0 + u(t, 12) : px1 - u(t, 12);
            double cy = (py0 + py1) / 2;
            exact(cx - d / 2, cy - d / 2, d, d).radius(d / 2).color(col(cAccent, 255 * ap)).render();
        }

        double titleWidth = m.titleWidth();
        double x = switch (t.moduleAlignment.get()) {
            case Center -> (px0 + px1) / 2 - titleWidth / 2;
            case Right -> px1 - u(t, 12) - titleWidth;
            default -> px0 + u(t, 12);
        };

        Color color = mix(mix(t.textSecondaryColor(), sub1, hp), t.textColor(), ap);
        label(m.title(), x, y0 + (y1 - y0 - T) / 2, color);
    }

    @Override
    public void paintSectionBody(WHontunSection s, GuiRenderer renderer, double mouseX, double mouseY) {
        if (!s.isExpanded() && !s.expandAnimating()) return;

        HontunGuiTheme t = s.theme();
        boolean lt = t.light();
        double hh = s.headerWidget().height;

        double x0 = rd(s.x), x1 = rd(s.x + s.width);
        double y0 = rd(s.y + hh + u(t, 4));
        double y1 = rd(s.y + s.height);
        if (y1 - y0 < 1) return;

        edges(x0, y0, x1, y1)
                .radius(u(t, 12))
                .color(ink(lt ? 0x06 : 0x04))
                .outline(ink(lt ? 0x18 : 0x0C), (float) hair(t))
                .render();
    }

    @Override
    public void paintSectionHeader(WHontunSection s, WHontunSection.WHontunHeader h, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme t = s.theme();
        double hp = SmogAnim.tween(h, HOVER, h.mouseOver, anim(AnimRole.HEADER_HOVER));
        if (hp <= 0) return;

        SmogPaint.pill(h.x, h.y, h.x + h.width, h.y + h.height, ink((t.light() ? 0x09 : 0x06) * hp));
    }

    @Override
    public void paintSeparatorH(WHontunHorizontalSeparator s, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme t = s.theme();
        double hw = hair(t);
        double ly = rd(s.y + (s.height - hw) / 2);
        Color line = ink(t.light() ? 0x1C : 0x12);

        if (!s.hasText()) {
            SmogPaint.rule(s.x + u(t, 12), s.x + s.width - u(t, 12), ly, hw, line, u(t, 24));
            return;
        }

        RichText title = RichText.of(s.titleString()).scale(SMALL);
        double tw = t.textWidth(title);
        double th = t.textHeight(title);

        label(title, s.x, s.y + (s.height - th) / 2, t.textSecondaryColor());

        double start = s.x + tw + u(t, 8);
        double end = s.x + s.width;
        SmogPaint.rule(start, end, ly, hw, line, Math.min(u(t, 24), Math.max(0, end - start) / 2));
    }

    @Override
    public void paintSeparatorV(WHontunVerticalSeparator s, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme t = s.theme();
        double hw = hair(t);
        double x = rd(s.x + (s.width - hw) / 2);
        SmogPaint.vrule(x, rd(s.y + u(t, 4)), rd(s.y + s.height - u(t, 4)), hw, ink(t.light() ? 0x1A : 0x10));
    }

    @Override
    public void paintViewUnderlay(WHontunView v, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme t = v.theme();
        boolean lt = t.light();
        SmogGlide glide = SmogGlide.of(v);

        WWidget target = findTarget(v, 0);
        double ink = 0;

        if (target != null) {
            double tx0 = target.x, ty0 = target.y, tx1 = target.x + target.width, ty1 = target.y + target.height;

            if (target instanceof WHontunModule module) {
                tx0 += u(t, 6);
                tx1 -= u(t, 6);
                ty0 += u(t, 1);
                ty1 -= u(t, 1);
                ink = lt ? 0x10 : 0x0C;
                if (module.module().isActive()) ink *= 0.5;
            }
            else if (target instanceof WHontunSearch.WHontunResult) {
                ink = lt ? 0x13 : 0x0E;
            }
            else {
                ink = lt ? 0x10 : 0x0C;
            }

            glide.track(target, rd(tx0), rd(ty0), rd(tx1) - rd(tx0), rd(ty1) - rd(ty0), u(t, 200));
            lensInk.put(v, ink);
        }
        else {
            glide.track(null, 0, 0, 0, 0, u(t, 200));
            Double last = lensInk.get(v);
            ink = last != null ? last : (lt ? 0x10 : 0x0C);
        }

        SmogPaint.lens(glide, ink(ink));
    }

    private static WWidget findTarget(WContainer container, int depth) {
        for (Cell<?> cell : container.cells) {
            WWidget w = cell.widget();
            if (!w.visible) continue;

            if (w instanceof HoverTarget target) {
                if (target.hoverLit()) return w;
                continue;
            }

            if (depth >= 3 || !w.mouseOver || w instanceof WView || !(w instanceof WContainer c)) continue;
            if (w instanceof WHontunSection section && !section.isExpanded()) continue;
            if (w instanceof WHontunMultiSelect<?> multi && !multi.isExpanded()) continue;

            WWidget found = findTarget(c, depth + 1);
            if (found != null) return found;
        }

        return null;
    }

    @Override
    public void paintScrollbar(WHontunView v, GuiRenderer renderer, double mouseX, double mouseY) {
        if (!v.scrollable() || !v.hasScrollBar) return;

        HontunGuiTheme t = v.theme();
        boolean lt = t.light();
        boolean drag = v.focused;
        boolean hover = v.barHovered();

        double wide = SmogAnim.tween(v, HOVER, hover || drag, anim(AnimRole.SCROLLBAR));
        double dg = SmogAnim.tween(v, DRAG, drag, anim(AnimRole.SCROLLBAR));

        long now = System.nanoTime();
        double[] state = bars.computeIfAbsent(v, k -> new double[]{Double.NaN, now});
        if (state[0] != v.barY() || hover || drag) {
            state[0] = v.barY();
            state[1] = now;
        }

        double idle = (now - state[1]) / 1_000_000.0;
        double dim = 1 - 0.65 * clamp01((idle - 800) / 200.0);

        double laneX0 = rd(v.x + v.width - u(t, 8));
        double laneX1 = laneX0 + u(t, 3);
        double laneY0 = rd(v.y + u(t, 6));
        double laneY1 = rd(v.y + v.height - u(t, 6));
        if (laneY1 - laneY0 <= 0) return;

        double lw = laneX1 - laneX0;
        edges(laneX0, laneY0, laneX1, laneY1).radius(lw / 2).color(ink((lt ? 0x14 : 0x0E) * dim)).render();

        double tw = Math.round(lerp(u(t, 3), u(t, 5), wide));
        double th = Math.min(laneY1 - laneY0, Math.max(u(t, 24), v.barHeight() - u(t, 4)));
        double ty = v.barY() + (v.barHeight() - th) / 2;
        ty = Math.clamp(ty, laneY0, laneY1 - th);

        double a = lt ? lerp(lerp(0x8C, 0xC0, wide), 0xE0, dg) * dim : lerp(lerp(0x55, 0x8C, wide), 0xB4, dg) * dim;
        edges(laneX1 - tw, ty, laneX1, ty + th).radius(tw / 2).color(ink(a)).render();
    }

    @Override
    public void paintButton(WHontunButton b, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme t = b.theme();
        double hp = SmogAnim.tween(b, HOVER, b.mouseOver, anim(AnimRole.BUTTON_HOVER));
        double pp = SmogAnim.tween(b, PRESS, b.isPressed(), Easing.QUAD_OUT, 80, 80);

        if (b.parent instanceof WHontunKeybind k) {
            paintKeybindButton(t, b, k, hp, pp);
            return;
        }

        RichText text = b.displayText();

        if (text == null) {
            ghost(t, renderer, b, hp, pp, b.icon(), mix(t.textSecondaryColor(), t.textColor(), Math.max(hp, pp)), null, null);
            return;
        }

        if (isStep(text)) {
            GuiTexture icon = "+".equals(text.getPlainText()) ? HontunBuiltinIcons.PLUS.texture() : HontunBuiltinIcons.MINUS.texture();
            ghost(t, renderer, b, hp, pp, icon, mix(t.textSecondaryColor(), t.textColor(), Math.max(hp, pp)), null, null);
            return;
        }

        double x0 = rd(b.x), y0 = rd(b.y), x1 = rd(b.x + b.width), y1 = rd(b.y + b.height);
        controlPill(t, x0, y0, x1, y1, hp, pp, pp);

        double tw = t.textWidth(text);
        double T = t.textHeight();
        label(text, (x0 + x1) / 2 - tw / 2, (y0 + y1) / 2 - T / 2, mix(sub1, t.textColor(), hp));
    }

    private void paintKeybindButton(HontunGuiTheme t, WHontunButton b, WHontunKeybind k, double hp, double pp) {
        boolean listening = k.isListening();
        double x0 = rd(b.x), y0 = rd(b.y), x1 = rd(b.x + b.width), y1 = rd(b.y + b.height);
        double T = t.textHeight();
        double cy = (y0 + y1) / 2;

        if (listening) {
            double phase = (System.currentTimeMillis() % 1200) / 1200.0;
            double pulse = 0.5 + 0.5 * Math.sin(phase * Math.PI * 2);
            boolean lt = t.light();
            double fill = (lt ? 0x1E : 0x18) * t.backgroundOpacity();
            Color ring = ink(lt ? lerp(0x46, 0xBA, pulse) : lerp(0x30, 0x90, pulse));

            SmogPaint.control(x0, y0, x1, y1, Math.round(0.42 * (y1 - y0)), ink(fill), ring, hair(t));
            label(RichText.of(LISTENING), x0 + u(t, 12), cy - T / 2, sub1);
            return;
        }

        controlPill(t, x0, y0, x1, y1, hp, pp, pp);

        RichText bind = RichText.of(k.title()).scale(SMALL);
        double bh = t.textHeight(bind);
        label(bind, x0 + u(t, 12), cy - bh / 2, t.textSecondaryColor());

        RichText key = RichText.of(k.keybind().toString());
        label(key, x0 + u(t, 12) + t.textWidth(bind) + u(t, 6), cy - T / 2, t.textColor());
    }

    @Override
    public void paintCheckbox(WHontunCheckbox c, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme t = c.theme();

        if (inMultiSelect(c)) {
            paintCheckCircle(t, c, renderer);
            return;
        }

        boolean lt = t.light();
        double p = clamp01(c.progress());
        boolean hoverState = c.mouseOver;
        double hv = SmogAnim.tween(c, HOVER, hoverState, anim(AnimRole.BUTTON_HOVER));
        double pp = SmogAnim.tween(c, PRESS, c.isPressed(), Easing.QUAD_OUT, 90, 90);
        double bo = t.backgroundOpacity();

        double x0 = rd(c.x), y0 = rd(c.y), x1 = rd(c.x + c.width), y1 = rd(c.y + c.height);
        double h = y1 - y0;

        double offA = lt ? lerp(0x2A, 0x3A, hv) * bo : lerp(0x1C, 0x28, hv) * bo;
        int onRgb = SmogPaint.mixRgb(cAccent, cAccentHi, hv);
        Color track = blend(cText, offA, onRgb, 255, p);
        Color trackHair = ink((lt ? 0x1C : 0x10) * (1 - p));

        RoundedRect rect = edges(x0, y0, x1, y1).radius(h / 2).color(track);
        if (trackHair.a > 0) rect.outline(trackHair, (float) hair(t));
        rect.render();

        double d = u(t, 14);
        double inset = u(t, 2);
        double stretch = u(t, 4) * pp;
        double kx = lerp(x0 + inset, x1 - inset - d, p);
        double kw = d + stretch;
        if (c.checked) kx -= stretch;
        double ky = y0 + (h - d) / 2;

        int offKnob = SmogPaint.mixRgb(packed(sub1), cText, hv);
        Color knob = blend(offKnob, 255, cBase, 255, p);

        if (lt && p > 0) {
            SmogPaint.halo(kx, ky + u(t, 1), kx + kw, ky + d + u(t, 1), d / 2, u(t, 2), u(t, 3), shade(0x30 * p));
        }

        exact(kx, ky, kw, d).radius(d / 2).color(knob).render();
    }

    private void paintCheckCircle(HontunGuiTheme t, WHontunCheckbox c, GuiRenderer renderer) {
        boolean rowHover = c.mouseOver || (c.parent != null && c.parent.mouseOver);
        double hv = SmogAnim.tween(c, HOVER, rowHover, anim(AnimRole.BUTTON_HOVER));
        double p = SmogAnim.tween(c, ON, c.checked, Easing.QUART_OUT, 180, 180);

        double d = Math.min(Math.round(c.width), Math.round(c.height));
        double cx = rd(c.x + c.width / 2);
        double cy = rd(c.y + c.height / 2);

        if (p < 1) {
            double ringW = 1.25 * hair(t);
            edges(cx - d / 2, cy - d / 2, cx + d / 2, cy + d / 2)
                    .radius(d / 2)
                    .color(CLEAR)
                    .outline(ink((t.light() ? lerp(0x78, 0xA8, hv) : lerp(0x3C, 0x70, hv)) * (1 - p)), (float) ringW)
                    .render();
        }

        if (p <= 0) return;

        double size = d * (0.5 + 0.5 * p);
        exact(cx - size / 2, cy - size / 2, size, size).radius(size / 2).color(col(cAccent, 255 * p)).render();
        icon(renderer, HontunBuiltinIcons.TICK.texture(), cx, cy, 0.6 * size, col(cBase, 255 * p));
    }

    @Override
    public void paintColorPicker(WHontunColorPicker p, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme t = p.theme();
        boolean lt = t.light();
        double hv = SmogAnim.tween(p, HOVER, p.mouseOver, anim(AnimRole.BUTTON_HOVER));
        double r = u(t, 8);
        Color color = p.color();
        Color opaque = new Color(color.r, color.g, color.b, 255);

        double x0 = rd(p.x), y0 = rd(p.y), x1 = rd(p.x + p.width), y1 = rd(p.y + p.height);
        double mid = rd((x0 + x1) / 2);

        if (color.a < 255) {
            edges(x0, y0, mid, y1).radius((float) r, Corners.LEFT).color(opaque).render();
            edges(mid, y0, x1, y1).radius((float) r, Corners.RIGHT).color(ink(lt ? 0x16 : 0x10)).render();
            edges(mid, y0, x1, y1).radius((float) r, Corners.RIGHT).color(color).render();
        }
        else {
            edges(x0, y0, x1, y1).radius(r).color(opaque).render();
        }

        edges(x0, y0, x1, y1).radius(r).color(CLEAR).outline(ink(lt ? 0x38 : 0x22), (float) hair(t)).render();

        if (hv > 0) {
            SmogPaint.ring(x0, y0, x1, y1, r, u(t, 3), 1.5 * hair(t), ink((lt ? 0xBA : 0x90) * hv));
            icon(renderer, HontunBuiltinIcons.BRUSH.texture(), (x0 + x1) / 2, (y0 + y1) / 2, Math.round(0.8 * t.textHeight()), scaleAlpha(pickerIconTint(color), hv));
        }
    }

    @Override
    public void paintConfirmedButton(WHontunConfirmedButton b, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme t = b.theme();
        boolean lt = t.light();
        double hp = SmogAnim.tween(b, HOVER, b.mouseOver, anim(AnimRole.BUTTON_HOVER));
        double pp = SmogAnim.tween(b, PRESS, b.isPressed(), Easing.QUAD_OUT, 80, 80);
        double ap = SmogAnim.tween(b, ON, b.armed(), Easing.LINEAR, 160, 160);

        String text = b.getText();
        Color armedFill = lt ? col(cAccent, 255 * ap) : ink(0xEB * ap);

        if (text == null) {
            Color iconColor = mix(mix(t.textSecondaryColor(), t.textColor(), Math.max(hp, pp)), col(cBase, 255), ap);
            Color fill = ap > 0 ? mix(ghostInk(t, hp, pp), armedFill, ap) : null;
            ghost(t, renderer, b, hp, pp, b.icon(), iconColor, fill, null);
            return;
        }

        double x0 = rd(b.x), y0 = rd(b.y), x1 = rd(b.x + b.width), y1 = rd(b.y + b.height);
        controlPill(t, x0, y0, x1, y1, hp, pp, pp);

        if (ap > 0) {
            double inset = Math.round(u(t, 1) * pp);
            edges(x0 + inset, y0 + inset, x1 - inset, y1 - inset)
                    .radius(Math.round(0.42 * (y1 - y0)))
                    .color(armedFill)
                    .render();
        }

        RichText rt = RichText.of(text);
        double tw = t.textWidth(rt);
        double T = t.textHeight();
        Color fg = mix(mix(sub1, t.textColor(), hp), col(cBase, 255), ap);
        label(rt, (x0 + x1) / 2 - tw / 2, (y0 + y1) / 2 - T / 2, fg);
    }

    @Override
    public void paintConfirmedMinus(WHontunConfirmedMinus m, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme t = m.theme();
        double hp = SmogAnim.tween(m, HOVER, m.mouseOver, anim(AnimRole.BUTTON_HOVER));
        double pp = SmogAnim.tween(m, PRESS, m.isPressed(), Easing.QUAD_OUT, 80, 80);
        double ap = SmogAnim.tween(m, ON, m.armed(), Easing.LINEAR, 160, 160);

        Color idleIcon = mix(t.textSecondaryColor(), t.textColor(), Math.max(hp, pp));
        Color iconColor = mix(idleIcon, col(cRed, 255), ap);
        Color fill = ap > 0 ? mix(ghostInk(t, hp, pp), col(cRed, 0x26), ap) : null;
        Color outline = ap > 0 ? col(cRed, 0x70 * ap) : null;

        ghost(t, renderer, m, hp, pp, HontunBuiltinIcons.MINUS.texture(), iconColor, fill, outline);
    }

    @Override
    public void paintMinus(WHontunMinus m, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme t = m.theme();
        double hp = SmogAnim.tween(m, HOVER, m.mouseOver, anim(AnimRole.BUTTON_HOVER));
        double pp = SmogAnim.tween(m, PRESS, m.isPressed(), Easing.QUAD_OUT, 80, 80);

        ghost(t, renderer, m, hp, pp, HontunBuiltinIcons.MINUS.texture(), mix(t.textSecondaryColor(), t.textColor(), Math.max(hp, pp)), null, null);
    }

    @Override
    public void paintPlus(WHontunPlus p, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme t = p.theme();
        double hp = SmogAnim.tween(p, HOVER, p.mouseOver, anim(AnimRole.BUTTON_HOVER));
        double pp = SmogAnim.tween(p, PRESS, p.isPressed(), Easing.QUAD_OUT, 80, 80);

        ghost(t, renderer, p, hp, pp, HontunBuiltinIcons.PLUS.texture(), mix(t.textSecondaryColor(), t.textColor(), Math.max(hp, pp)), null, null);
    }

    @Override
    public void paintFavorite(WHontunFavorite f, GuiRenderer renderer, double mouseX, double mouseY) {
        double since = SmogAnim.sinceChange(f, AUX, f.checked);
        double k = since == Double.MAX_VALUE ? 1 : clamp01(since / 160.0);
        double scale = 0.8 + 0.2 * Easing.QUART_OUT.apply(k);

        double size = f.size() * scale;
        GuiTexture texture = f.checked ? HontunBuiltinIcons.BOOKMARK_YES.texture() : HontunBuiltinIcons.BOOKMARK_NO.texture();
        double cx = f.x + f.size() / 2;
        double cy = f.y + f.size() / 2;

        renderer.quad(cx - size / 2, cy - size / 2, size, size, texture, f.tint());
    }

    @Override
    public void paintTriangle(WHontunTriangle tri, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme t = tri.theme();
        double hp = SmogAnim.tween(tri, HOVER, headerHovered(tri), anim(AnimRole.HEADER_HOVER));
        double size = Math.round(0.62 * t.textHeight());

        chevron(
                renderer,
                tri.x + tri.width / 2,
                tri.y + tri.height / 2,
                size,
                tri.rotation - 90,
                mix(t.textSecondaryColor(), t.textColor(), hp)
        );
    }

    @Override
    public void paintKeybindExtra(WHontunKeybind k, GuiRenderer renderer, double mouseX, double mouseY) {
    }

    @Override
    public void paintSlider(WHontunSlider s, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme t = s.theme();
        boolean lt = t.light();
        boolean drag = s.isDragging();
        boolean hover = s.mouseOver || s.handleHovered();

        double hv = SmogAnim.tween(s, HOVER, hover || drag, anim(AnimRole.SLIDER_HOVER));
        double dg = SmogAnim.tween(s, DRAG, drag, anim(AnimRole.SLIDER_HOVER));

        double cy = s.y + s.height / 2;
        double wellH = u(t, 14);
        double wx0 = rd(s.x), wx1 = rd(s.x + s.width);
        double wy0 = rd(cy - wellH / 2), wy1 = wy0 + wellH;

        Color wellFill = lt ? ink(lerp(0x0E, 0x12, hv)) : col(cCrust, 0x80);
        Color wellHair = lt ? ink(lerp(0x1C, 0x28, hv)) : ink(lerp(0x10, 0x1A, hv));
        SmogPaint.well(wx0, wy0, wx1, wy1, wellH / 2, wellFill, wellHair, hair(t));

        double knobX = s.x + s.handleSize() / 2 + s.valueOffset();

        double px0 = wx0 + u(t, 2), py0 = wy0 + u(t, 2), py1 = wy1 - u(t, 2);
        double px1 = Math.max(px0, Math.min(rd(knobX), wx1 - u(t, 2)));
        if (px1 - px0 >= 1) {
            double a = lt ? lerp(lerp(0x40, 0x4C, hv), 0x58, dg) : lerp(lerp(0x2A, 0x36, hv), 0x40, dg);
            edges(px0, py0, px1, py1).radius((py1 - py0) / 2).color(ink(a)).render();
        }

        double nw = Math.round(lerp(u(t, 3), u(t, 4), Math.max(hv, dg)));
        double nh = u(t, 20);
        double nx0 = rd(knobX - nw / 2);
        double ny0 = rd(cy - nh / 2);

        if (dg > 0) SmogPaint.halo(nx0, ny0, nx0 + nw, ny0 + nh, nw / 2, u(t, 4), u(t, 4), ink((lt ? 0x30 : 0x24) * dg));

        edges(nx0, ny0, nx0 + nw, ny0 + nh).radius(nw / 2).color(t.textColor()).render();

        if (s.parent instanceof WContainer row && (row.parent instanceof WHontunIntEdit || row.parent instanceof WHontunDoubleEdit)) {
            for (Cell<?> cell : row.cells) {
                if (cell.widget() instanceof WHontunLabel l) l.color = sub0Min;
            }
        }

        bubble(t, renderer, s, knobX, wy0);
    }

    private void bubble(HontunGuiTheme t, GuiRenderer renderer, WHontunSlider s, double knobX, double wellTop) {
        WWidget edit = s.parent != null ? s.parent.parent : null;
        if (!(edit instanceof WHontunIntEdit) && !(edit instanceof WHontunDoubleEdit)) return;

        double a = SmogAnim.tween(s, AUX, s.isDragging(), Easing.QUAD_OUT, 100, 100);
        if (a <= 0) return;

        WHontunTextBox box = valueBox(edit);
        String value = box != null ? box.get() : String.valueOf(s.value());

        RichText text = RichText.of(value).scale(SMALL);
        double tw = t.textWidth(text);
        double th = t.textHeight(text);
        double bw = Math.round(tw + 2 * u(t, 8));
        double bh = Math.round(th + 2 * u(t, 3));
        double bx = rd(knobX - bw / 2);
        double by = rd(wellTop - u(t, 6) - bh);
        Color fg = col(t.textColor(), 255 * a);

        renderer.absolutePost(() -> {
            Routers.of(pipeline()).windowLayer(renderer);
            renderer.scissorStart(0, 0, Utils.getWindowWidth(), Utils.getWindowHeight());
            glass(t, Glass.TOOLTIP, bx, by, bx + bw, by + bh, bh / 2, popupAlpha(t), 1, 0, a);
            label(text, bx + (bw - tw) / 2, by + (bh - th) / 2, fg);
            renderer.scissorEnd();
        });
    }

    private static WHontunTextBox valueBox(WWidget edit) {
        if (!(edit instanceof WContainer c) || c.cells.isEmpty()) return null;
        if (!(c.cells.get(0).widget() instanceof WContainer row)) return null;

        for (Cell<?> cell : row.cells) {
            if (cell.widget() instanceof WHontunTextBox box) return box;
        }

        return null;
    }

    @Override
    public void paintDropdown(WHontunDropdown<?> d, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme t = d.theme();
        double T = t.textHeight();
        double hp = SmogAnim.tween(d, HOVER, d.mouseOver, anim(AnimRole.DROPDOWN_HOVER));
        double open = clamp01(d.indicatorProgress());
        double active = Math.max(open, d.isPressed() ? 1 : 0);

        double x0 = rd(d.x), y0 = rd(d.y), x1 = rd(d.x + d.width), y1 = rd(d.y + d.height);
        controlPill(t, x0, y0, x1, y1, hp, active, 0);

        double cy = (y0 + y1) / 2;
        double x = x0 + u(t, 12);

        if (hasTitle(d)) {
            label(d.titleText(), x, cy - T / 2, t.textSecondaryColor());
            x += t.textWidth(d.titleText()) + u(t, 8);
        }

        label(d.valueText(), x, cy - T / 2, t.textColor());

        double cs = Math.round(0.62 * T);
        chevron(renderer, x1 - u(t, 12) - cs / 2, cy, cs, 90 - 180 * open, mix(t.textSecondaryColor(), t.textColor(), hp));

        if (open > 0 && t.effWindowShadow()) popupShadow(t, renderer, d.popup(), open);
    }

    private void popupShadow(HontunGuiTheme t, GuiRenderer renderer, WHontunDropdown.WRoot root, double open) {
        boolean lt = t.light();

        renderer.absolutePost(() -> {
            double x0 = rd(root.x), y0 = rd(root.y), x1 = rd(root.x + root.width);
            double y1 = rd(root.y + root.height * open);
            if (y1 - y0 <= 0) return;

            Routers.of(pipeline()).windowLayer(renderer);
            renderer.scissorStart(0, 0, Utils.getWindowWidth(), Utils.getWindowHeight());
            SmogPaint.shadows(Glass.POPUP, x0, y0, x1, y1, u(t, 12), s(t), lt, 1, 0, open);
            renderer.scissorEnd();
        });
    }

    @Override
    public void paintDropdownPopup(WHontunDropdown<?> d, WHontunDropdown.WRoot root, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme t = root.theme();
        boolean lt = t.light();
        double open = clamp01(d.indicatorProgress());

        double natural = d.y + d.height + popupOffset(t);
        double want = natural - Math.round(u(t, 4) * (1 - open));
        if (root.y != want) root.move(0, want - root.y);

        if (open < 1) {
            double prior = HontunRenderer.get().globalAlpha();
            renderer.setAlpha(prior * open);
            renderer.post(() -> renderer.setAlpha(prior));
        }

        double x0 = rd(root.x), y0 = rd(root.y), x1 = rd(root.x + root.width), y1 = rd(root.y + root.height);
        glassBody(t, Glass.POPUP, x0, y0, x1, y1, u(t, 12), popupAlpha(t));

        SmogGlide glide = SmogGlide.of(root);
        WWidget target = null;
        for (Cell<?> cell : root.cells) {
            if (cell.widget().mouseOver) {
                target = cell.widget();
                break;
            }
        }

        if (target != null) glide.track(target, rd(target.x), rd(target.y), rd(target.x + target.width) - rd(target.x), rd(target.y + target.height) - rd(target.y), u(t, 200));
        else glide.track(null, 0, 0, 0, 0, u(t, 200));

        SmogPaint.lens(glide, ink(lt ? 0x16 : 0x10));
    }

    @Override
    public void paintDropdownValue(WHontunDropdown<?>.WValue v, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme t = v.theme();
        double T = t.textHeight();
        boolean selected = v.isSelectedValue();

        double x0 = rd(v.x), y0 = rd(v.y), x1 = rd(v.x + v.width), y1 = rd(v.y + v.height);
        double cy = (y0 + y1) / 2;

        label(v.valueName(), x0 + u(t, 8), cy - T / 2, selected ? t.textColor() : sub1);

        if (selected) {
            double size = Math.round(0.55 * T);
            icon(renderer, HontunBuiltinIcons.TICK.texture(), x1 - u(t, 8) - size / 2, cy, size, t.textColor());
        }
    }

    @Override
    public void paintTextBox(WHontunTextBox b, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme t = b.theme();
        boolean lt = t.light();
        boolean focused = b.isFocused();
        double T = t.textHeight();

        double fp = SmogAnim.tween(b, FOCUS, focused, anim(AnimRole.TEXTBOX_FOCUS));
        double hv = SmogAnim.tween(b, HOVER, b.mouseOver, anim(AnimRole.BUTTON_HOVER));

        double x0 = rd(b.x), y0 = rd(b.y), x1 = rd(b.x + b.width), y1 = rd(b.y + b.height);

        if (b.rendersBackground()) {
            double r = u(t, 10);
            double hw = hair(t);
            boolean titled = b.hasTitle();
            double wx0 = x0;
            RichText title = titled ? RichText.of(b.title()).scale(SMALL) : null;

            if (titled) {
                WHontunLabel titleLabel = titleLabel(b);
                if (titleLabel != null) {
                    titleLabel.hidden = true;
                    wx0 = rd(titleLabel.x - u(t, 10));
                }
                else {
                    wx0 = rd(x0 - (2 * u(t, 10) + t.textWidth(title)));
                }
            }

            if (fp > 0) SmogPaint.halo(wx0, y0, x1, y1, r, u(t, 4), u(t, 4), ink((lt ? 0x1C : 0x14) * fp));

            Color fill = lt ? col(cS0, 255) : col(cCrust, lerp(lerp(0x70, 0x80, hv), 0x96, fp));
            Color idleHair = ink(lt ? lerp(0x24, 0x34, hv) : lerp(0x12, 0x1C, hv));
            Color focusHair = lt ? col(cAccent, 255) : ink(0xB4);
            Color ring = mix(idleHair, focusHair, fp);

            edges(wx0, y0, x1, y1).radius(r).color(fill).outline(ring, (float) hw).render();

            if (titled) {
                double segR = Math.max(0, r - hw);
                edges(wx0 + hw, y0 + hw, x0, y1 - hw)
                        .radii((float) segR, 0, (float) segR, 0)
                        .color(ink(lt ? 0x08 : 0x05))
                        .render();

                SmogPaint.vrule(x0, y0 + u(t, 6), y1 - u(t, 6), hw, ink(lt ? 0x1C : 0x12));

                double th = t.textHeight(title);
                label(title, wx0 + u(t, 10), (y0 + y1) / 2 - th / 2, t.textSecondaryColor());
            }
        }

        double padding = b.padding();
        double overflow = b.overflow();
        double ty = rd(b.y + (b.height - T) / 2);

        renderer.scissorStart(b.x + padding, b.y, b.width - padding * 2, b.height);

        String value = b.textValue();
        if (!value.isEmpty()) {
            Color custom = b.customColor();
            Color color = custom != null && !isEditValue(b)
                    ? custom
                    : (focused || isEditValue(b) ? t.textColor() : sub1);

            b.drawText(renderer, b.x + padding - overflow, ty, value, color);
        }
        else {
            String placeholder = inSearchHeader(b) ? SEARCH_PLACEHOLDER : b.placeholderValue();
            if (placeholder != null) b.drawText(renderer, b.x + padding - overflow, ty, placeholder, sub0Soft);
        }

        if (focused && b.hasSelection()) {
            double from = b.x + padding + b.textWidthAt(b.selectionStartIndex()) - overflow;
            double to = b.x + padding + b.textWidthAt(b.selectionEndIndex()) - overflow;
            paintTextSelection(b, renderer, Math.min(from, to), Math.max(from, to), ty, T);
        }

        double caret = b.caretAlpha();
        if ((focused && b.caretVisible()) || caret > 0) {
            paintCaret(b, renderer, b.x + padding + b.textWidthAt(b.cursorIndex()) - overflow, ty, T, caret);
        }

        renderer.scissorEnd();
    }

    private static WHontunLabel titleLabel(WHontunTextBox b) {
        if (!(b.parent instanceof WContainer container)) return null;

        WHontunLabel found = null;
        for (Cell<?> cell : container.cells) {
            WWidget w = cell.widget();
            if (w == b) break;
            if (w instanceof WHontunLabel label && label.richText().getPlainText().equals(b.title())) found = label;
        }

        return found;
    }

    @Override
    public void paintTextSelection(WHontunTextBox b, GuiRenderer renderer, double x0, double x1, double y, double h) {
        HontunGuiTheme t = b.theme();
        if (x1 - x0 < 1) return;
        edges(x0, y, x1, y + Math.round(h)).radius(u(t, 3)).color(ink(t.light() ? 0x40 : 0x38)).render();
    }

    @Override
    public void paintCaret(WHontunTextBox b, GuiRenderer renderer, double x, double y, double h, double alpha) {
        HontunGuiTheme t = b.theme();
        double a = clamp01(alpha);
        if (a <= 0) return;

        double cx = rd(x);
        edges(cx, y, cx + hair(t), y + Math.round(h)).color(col(t.textColor(), 255 * a)).render();
    }

    @Override
    public void paintCompletions(WHontunTextBox b, GuiRenderer renderer, double x, double y, double w, double h) {
        HontunGuiTheme t = b.theme();
        double pad = u(t, 4);
        glass(t, Glass.POPUP, rd(x - pad), rd(y), rd(x + w + pad), rd(y + h + pad), u(t, 12), popupAlpha(t), 1, 0, 1);
    }

    @Override
    public void paintCompletionItem(WHontunTextBox.CompletionItem item, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme t = item.theme();

        if (item.parent instanceof WContainer list && !list.cells.isEmpty() && list.cells.get(0).widget() == item) {
            WWidget target = null;
            for (Cell<?> cell : list.cells) {
                if (cell.widget() instanceof WHontunTextBox.CompletionItem c && (c.isSelected() || c.mouseOver)) {
                    target = c;
                    if (c.mouseOver) break;
                }
            }

            SmogGlide glide = SmogGlide.of(list);
            double pad = u(t, 2);
            if (target != null) {
                glide.track(target, rd(target.x - pad), rd(target.y), rd(target.x + target.width + pad) - rd(target.x - pad), rd(target.y + target.height) - rd(target.y), u(t, 200));
            }
            else glide.track(null, 0, 0, 0, 0, u(t, 200));

            SmogPaint.lens(glide, ink(t.light() ? 0x16 : 0x10));
        }

        item.color = item.isSelected() || item.mouseOver ? t.textColor() : sub1;
        item.drawLabel(renderer, mouseX, mouseY);
    }

    @Override
    public void paintSearchPanel(WHontunSearch s, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme t = s.theme();
        double x0 = rd(s.x), y0 = rd(s.y), x1 = rd(s.x + s.width), y1 = rd(s.y + s.height);

        glass(t, Glass.HERO, x0, y0, x1, y1, u(t, 18), t.windowOpacity(), 1, 0, 1);
    }

    @Override
    public void paintSearchHeader(WHontunSearch.WHontunHeader h, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme t = h.theme();
        double hw = hair(t);

        WHontunLabel esc = null;
        for (Cell<?> cell : h.cells) {
            if (!(cell.widget() instanceof WContainer row)) continue;

            for (Cell<?> inner : row.cells) {
                WWidget w = inner.widget();
                if (w instanceof WGuiTexture icon) icon.color = t.textSecondaryColor();
                else if (w instanceof WHontunLabel l) esc = l;
            }
        }

        if (esc != null) {
            esc.hidden = true;

            RichText hint = RichText.of(ESC_HINT).scale(SMALL);
            double tw = t.textWidth(hint);
            double th = t.textHeight(hint);
            label(hint, esc.x + esc.width - tw, esc.y + (esc.height - th) / 2, t.textSecondaryColor());
        }

        WWidget panel = h.parent != null ? h.parent : h;
        double inset = u(t, 20);
        SmogPaint.rule(panel.x + inset, panel.x + panel.width - inset, rd(h.y + h.height - hw), hw, ink(t.light() ? 0x1C : 0x12), 0);
    }

    @Override
    public void paintSearchBody(WHontunSearch.WHontunResultsContainer c, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme t = c.theme();

        WView view = null;
        WHontunLabel help = null;

        for (Cell<?> cell : c.cells) {
            WWidget w = cell.widget();
            if (w instanceof WView v) view = v;
            else if (w instanceof WHontunLabel l) help = l;
        }

        String query = searchQuery(c.parent);
        boolean empty = view != null && view.cells.isEmpty() && query != null && !query.isEmpty();

        if (help != null) help.hidden = empty;
        if (!empty) return;

        RichText message = RichText.of(NO_RESULTS).scale(SMALL);
        double mw = t.textWidth(message);
        double mh = t.textHeight(message);
        double cy = help != null ? help.y + help.height / 2 : c.y + c.height / 2;

        label(message, c.x + (c.width - mw) / 2, cy - mh / 2, t.textSecondaryColor());
    }

    private static String searchQuery(WWidget search) {
        if (!(search instanceof WContainer container)) return null;

        for (Cell<?> cell : container.cells) {
            if (!(cell.widget() instanceof WHontunSearch.WHontunHeader header)) continue;

            for (Cell<?> rowCell : header.cells) {
                if (!(rowCell.widget() instanceof WContainer row)) continue;

                for (Cell<?> inner : row.cells) {
                    if (inner.widget() instanceof WTextBox box) return box.get();
                }
            }
        }

        return null;
    }

    @Override
    public void paintSearchRow(WHontunSearch.WHontunResult row, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme t = row.theme();
        double pp = SmogAnim.tween(row, PRESS, row.isPressed(), Easing.QUAD_OUT, 80, 80);

        if (pp > 0) SmogPaint.pill(row.x, row.y, row.x + row.width, row.y + row.height, ink((t.light() ? 0x0C : 0x08) * pp));

        tintChips(row, sub1);
    }

    private static void tintChips(WContainer container, Color color) {
        for (Cell<?> cell : container.cells) {
            WWidget w = cell.widget();

            if (w instanceof WHontunSearch.WHontunResult.WResultType type) {
                for (Cell<?> inner : type.cells) {
                    if (inner.widget() instanceof WGuiTexture icon) icon.color = color;
                }
                continue;
            }

            if (w instanceof WContainer c) tintChips(c, color);
        }
    }

    @Override
    public void paintChip(GuiRenderer renderer, double x, double y, double w, double h, Color c, ChipKind kind) {
        HontunGuiTheme t = theme();
        double size = u(t, 26);
        double cx = x + w / 2;
        double cy = y + h / 2;

        edges(cx - size / 2, cy - size / 2, cx + size / 2, cy + size / 2)
                .radius(u(t, 8))
                .color(ink(t.light() ? 0x12 : 0x0C))
                .render();
    }

    @Override
    public void paintMultiSelectHeader(WHontunMultiSelect<?>.WHontunHeader h, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme t = h.owner().theme();
        double hp = SmogAnim.tween(h, HOVER, h.mouseOver, anim(AnimRole.HEADER_HOVER));

        if (hp > 0) SmogPaint.pill(h.x, h.y, h.x + h.width, h.y + h.height, ink((t.light() ? 0x09 : 0x06) * hp));

        int index = 0;
        for (Cell<?> cell : h.cells) {
            if (!(cell.widget() instanceof WHontunLabel l)) continue;

            if (index == 0) {
                l.color = mix(t.textSecondaryColor(), sub1, hp);
            }
            else {
                double th = t.textHeight(l.richText());
                double ch = Math.round(th + 2 * u(t, 2));
                double x0 = rd(l.x - u(t, 6)), x1 = rd(l.x + l.width + u(t, 6));
                double y0 = rd(l.y + (l.height - ch) / 2);

                SmogPaint.pill(x0, y0, x1, y0 + ch, ink(t.light() ? 0x14 : 0x0E));
                l.color = t.textColor();
            }

            index++;
        }
    }

    @Override
    public void paintMultiSelectBody(WHontunMultiSelect<?> m, GuiRenderer renderer, double mouseX, double mouseY) {
        if (!m.isExpanded() && !m.animating()) return;

        HontunGuiTheme t = m.theme();
        boolean lt = t.light();
        double hh = m.headerWidget().height;

        double x0 = rd(m.x), x1 = rd(m.x + m.width);
        double y0 = rd(m.y + hh + u(t, 4));
        double y1 = rd(m.y + m.height);
        if (y1 - y0 < 1) return;

        edges(x0, y0, x1, y1)
                .radius(u(t, 12))
                .color(ink(lt ? 0x06 : 0x04))
                .outline(ink(lt ? 0x18 : 0x0C), (float) hair(t))
                .render();
    }

    @Override
    public void paintMultiSelectItem(WHontunMultiSelect<?>.WHontunItem i, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme t = i.owner().theme();
        boolean checked = false;

        for (Cell<?> cell : i.cells) {
            WWidget w = cell.widget();
            if (w instanceof WHontunCheckbox box) checked = box.checked;
            else if (w instanceof WHontunLabel l) l.color = checked ? t.textColor() : sub1;
        }
    }

    @Override
    public void paintTooltip(WHontunTooltip tt, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme t = tt.theme();
        long now = System.nanoTime();

        long[] seen = tooltips.computeIfAbsent(tt, k -> new long[]{now, now});
        if (now - seen[1] > 100_000_000L) seen[0] = now;
        seen[1] = now;

        double fade = Easing.QUAD_OUT.apply(clamp01((now - seen[0]) / 120_000_000.0));
        if (fade < 1) renderer.setAlpha(HontunRenderer.get().globalAlpha() * fade);

        for (Cell<?> cell : tt.cells) {
            if (cell.widget() instanceof WHontunLabel l) l.color = sub1;
        }

        double x0 = rd(tt.x), y0 = rd(tt.y), x1 = rd(tt.x + tt.width), y1 = rd(tt.y + tt.height);
        glass(t, Glass.TOOLTIP, x0, y0, x1, y1, u(t, 10), popupAlpha(t), 1, 0, 1);
    }

    @Override
    public void paintSwatchChip(WHontunSwatchLabel l, GuiRenderer renderer, double mouseX, double mouseY) {
        Color color = l.color;
        if (color == null || l.get().isEmpty()) return;

        HontunGuiTheme t = l.theme();
        if (HontunLightPalette.contrast(packed(color), packed(t.mantleColor())) >= WHontunSwatchLabel.MIN_CONTRAST) return;

        double padH = u(t, 4);
        double padV = u(t, 1);

        edges(l.x - padH, l.y - padV, l.x + l.width + padH, l.y + l.height + padV)
                .radius(u(t, 6))
                .color(ink(t.light() ? 0x16 : 0x10))
                .render();
    }

    @Override
    public void paintCountChip(HontunSettingsWidgetFactory.WSelectedCountLabel l, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme t = l.theme();

        RichText text = RichText.of(l.label().getPlainText()).scale(SMALL);
        double tw = t.textWidth(text);
        double th = t.textHeight(text);

        double ch = Math.round(th + 2 * u(t, 2));
        double cw = Math.max(ch, Math.round(tw + 2 * u(t, 6)));
        double x0 = rd(l.x);
        double y0 = rd(l.y + (l.height - ch) / 2);

        SmogPaint.pill(x0, y0, x0 + cw, y0 + ch, ink(t.light() ? 0x14 : 0x0E));
        label(text, x0 + (cw - tw) / 2, y0 + (ch - th) / 2, t.textColor());
    }

    @Override
    public void paintMeteorBackground(WWidget w, GuiRenderer renderer, boolean pressed, boolean hovered) {
        HontunGuiTheme t = theme();
        controlPill(t, rd(w.x), rd(w.y), rd(w.x + w.width), rd(w.y + w.height), hovered ? 1 : 0, pressed ? 1 : 0, pressed ? 1 : 0);
    }

    @Override
    public void paintMeteorBackground(WWidget w, GuiRenderer renderer, Color outlineColor, Color backgroundColor) {
        HontunGuiTheme t = theme();
        controlPill(t, rd(w.x), rd(w.y), rd(w.x + w.width), rd(w.y + w.height), w.mouseOver ? 1 : 0, 0, 0);
    }

    @Override
    public void paintSnapGrid(GuiGraphicsExtractor g, int gridSize) {
        if (gridSize <= 0) return;

        int color = (theme().light() ? col(0x808084, 0x40) : ink(0x08)).getPacked();
        int windowWidth = Utils.getWindowWidth();
        int windowHeight = Utils.getWindowHeight();
        float guiScale = (float) mc.getWindow().getGuiScale();

        Matrix3x2fStack pose = g.pose();
        pose.pushMatrix();
        pose.scale(1f / guiScale, 1f / guiScale);

        cz.honzasik.hontun.gui.render.pixel.QuadBatchState.Builder batch = cz.honzasik.hontun.gui.render.pixel.QuadBatchState.builder();
        for (int x = 0; x <= windowWidth; x += gridSize) batch.add(x, 0, x + 1, windowHeight, color);
        for (int y = 0; y <= windowHeight; y += gridSize) batch.add(0, y, windowWidth, y + 1, color);
        batch.submit(g);

        pose.popMatrix();
    }

    private static final class Puck {
        private double fromX, fromW;
        private double x, w;
        private long start;
        private boolean slide;
        private Object selected;
    }
}
