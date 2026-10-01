package cz.honzasik.hontun.gui.theme.style.flat;

import cz.honzasik.hontun.gui.api.animation.Easing;
import cz.honzasik.hontun.gui.api.render.Corners;
import cz.honzasik.hontun.gui.api.render.RoundedRect;
import cz.honzasik.hontun.gui.api.text.RichText;
import cz.honzasik.hontun.gui.api.text.TextScale;
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
import cz.honzasik.hontun.gui.theme.style.Knob;
import cz.honzasik.hontun.gui.theme.style.Metrics;
import cz.honzasik.hontun.gui.theme.style.Pipeline;
import cz.honzasik.hontun.gui.theme.style.StylePalette;
import cz.honzasik.hontun.gui.theme.style.TabGlide;
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
import cz.honzasik.hontun.gui.widget.input.WMultiSelect;
import cz.honzasik.hontun.utils.HontunTheme;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.renderer.packer.GuiTexture;
import meteordevelopment.meteorclient.gui.utils.Cell;
import meteordevelopment.meteorclient.gui.widgets.WLabel;
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

import static cz.honzasik.hontun.gui.theme.style.flat.FlatAnim.DRAG;
import static cz.honzasik.hontun.gui.theme.style.flat.FlatAnim.FOCUS;
import static cz.honzasik.hontun.gui.theme.style.flat.FlatAnim.HOVER;
import static cz.honzasik.hontun.gui.theme.style.flat.FlatPaint.CLEAR;
import static cz.honzasik.hontun.gui.theme.style.flat.FlatPaint.alpha;
import static cz.honzasik.hontun.gui.theme.style.flat.FlatPaint.box;
import static cz.honzasik.hontun.gui.theme.style.flat.FlatPaint.clamp01;
import static cz.honzasik.hontun.gui.theme.style.flat.FlatPaint.col;
import static cz.honzasik.hontun.gui.theme.style.flat.FlatPaint.edges;
import static cz.honzasik.hontun.gui.theme.style.flat.FlatPaint.lerp;
import static cz.honzasik.hontun.gui.theme.style.flat.FlatPaint.mix;
import static cz.honzasik.hontun.gui.theme.style.flat.FlatPaint.rd;
import static meteordevelopment.meteorclient.MeteorClient.mc;

public class FlatClickStyle extends ClickStyle {
    private static final String LISTENING = "...";
    private static final String NO_RESULTS = "No results";
    private static final String ESC = "Esc";
    private static final double SMALL = TextScale.SMALL.get();

    private final Color shadow = new Color(0, 0, 0, 0xC0);
    private final Color sub1 = new Color(0xCA, 0xCD, 0xD4);
    private final Color sub0 = new Color(0x9C, 0xA0, 0xA8);

    private final Map<Object, double[]> glideFrom = new WeakHashMap<>();
    private final Map<Object, String> keyLayout = new WeakHashMap<>();

    private boolean legibility;
    private boolean legibilityKnown;

    private boolean light;
    private int cCrust = 0x08090B, cMantle = 0x0C0D10, cBase = 0x101114;
    private int cS0 = 0x16181C, cS1 = 0x1E2024, cS2 = 0x282A30;
    private int cO0 = 0x3A3C42, cO1 = 0x4E5058, cO2 = 0x646670;
    private int cSub1 = 0xCACDD4, cSub0 = 0x9CA0A8;
    private int cA = 0x3E8CFF, cAH = 0x5AC8FF, cAL = 0x2E63B8;
    private int cRawA = 0x3E8CFF;
    private int cAt = 0x3E8CFF, cAtHi = 0x5AC8FF, cAtLo = 0x2E63B8;
    private int cTint, cTintHot, cOnA = 0x08090B;
    private int cEdge = 0x646670;
    private int cGreen = 0x5EE0A0, cRed = 0xFF6B7A;

    private static final double LIGHT_STROKE = 3.0;
    private static final double LIGHT_SUBTEXT = 4.5;
    private static final double LIGHT_BODY_TEXT = 7.0;
    private static final double LIGHT_STATUS = 4.5;
    private static final double LIGHT_TINT = 0.28;
    private static final double LIGHT_TINT_HOT = 0.36;
    private static final double LIGHT_CHIP = 0.14;
    private static final int GRID_DOT_LIMIT = 12000;
    private static final long HELP_SHADOW_NS = 5_000_000L;

    private int helpShadows;
    private long helpShadowUntil;

    public FlatClickStyle() {
        cTint = mix(cS1, cA, 0.18);
        cTintHot = mix(cS1, cA, 0.26);
    }

    @Override
    public HontunTheme.UiMode id() {
        return HontunTheme.UiMode.HModern1;
    }

    @Override
    protected Metrics createMetrics() {
        Metrics m = new Metrics();

        m.windowPad = 4;

        m.headerPadH = 10;
        m.headerPadV = 7;
        m.headerSpacing = 6;

        m.topBarMarginTop = 8;
        m.topBarItemPad = 2;
        m.tabPadH = 12;
        m.tabPadV = 7;
        m.tabIconGap = 6;

        m.rowPadH = 10;
        m.rowPadV = 5;
        m.activeBarWidth = 3;
        m.activeBarInset = 0;

        m.viewSpacing = 0;

        m.sectionHeaderPadH = 10;
        m.sectionHeaderPadV = 5;
        m.sectionPad = 4;
        m.tablePad = 10;
        m.tableVSpacing = 6;

        m.tooltipPadH = 9;
        m.tooltipPadV = 5;

        m.moduleScreenPad = 10;
        m.moduleInfoSpacing = 8;

        m.separatorThickness = 1;

        m.searchHeaderPad = 10;
        m.searchViewPad = 6;
        m.searchRowPad = 6;
        m.searchResultPadH = 4;

        m.multiSelectHeaderPadV = 5;
        m.multiSelectHeaderPadL = 10;

        m.gap = 6;
        m.dropdownPad = 4;

        return m;
    }

    @Override
    public Pipeline pipeline() {
        return Pipeline.ROUNDED;
    }

    @Override
    public FontChoice font() {
        return FontChoice.THEME;
    }

    @Override
    public boolean uses(Knob knob) {
        return true;
    }

    @Override
    public double chromeMargin(HontunGuiTheme t) {
        return px(t, 3);
    }

    @Override
    public double windowAlpha(double raw, boolean light) {
        return light ? 0.85 + 0.15 * clamp01(raw) : Math.max(0.75, raw);
    }

    @Override
    public double controlAlpha(double raw, boolean light) {
        return Math.max(0.6, raw);
    }

    @Override
    public boolean legibilityShadow(HontunGuiTheme t) {
        if (t.light()) return consumeHelpShadow();

        double o = t.windowOpacity.get();

        if (!legibilityKnown) {
            legibility = o < 0.575;
            legibilityKnown = true;
        }
        else if (o < 0.55) legibility = true;
        else if (o > 0.60) legibility = false;

        return legibility;
    }

    @Override
    public Color textShadowColor() {
        return shadow;
    }

    @Override
    public double textShadowOffset(double fontScale, double renderScale) {
        return h1(theme());
    }

    @Override
    public AnimSpec anim(AnimRole role) {
        return switch (role) {
            case WINDOW_EXPAND, SECTION_EXPAND, MULTISELECT_EXPAND -> AnimSpec.USER;
            case WINDOW_CORNER, SECTION_CORNER -> AnimSpec.of(Easing.QUART_OUT, 200, 200);
            case MODULE_ACTIVE -> AnimSpec.of(Easing.QUART_OUT, 180, 180);
            case MODULE_HOVER -> AnimSpec.of(Easing.QUAD_OUT, 100, 140);
            case TAB_SELECT -> AnimSpec.of(Easing.QUART_OUT, 220, 220);
            case BUTTON_HOVER, DROPDOWN_HOVER, TEXTBOX_FOCUS, SLIDER_HOVER, FAVORITE -> AnimSpec.of(Easing.QUAD_OUT, 120, 120);
            case CHECKBOX -> AnimSpec.of(Easing.QUAD_OUT, 140, 140);
            case DROPDOWN_OPEN -> AnimSpec.of(Easing.QUART_OUT, 160, 160);
            case HEADER_HOVER -> AnimSpec.of(Easing.QUAD_OUT, 100, 100);
            case SCROLLBAR -> AnimSpec.of(Easing.QUAD_OUT, 150, 150);
            default -> AnimSpec.NONE;
        };
    }

    @Override
    public void palette(StylePalette out, boolean light, HontunGuiTheme t) {
        super.palette(out, light, t);

        this.light = light;

        int body = light ? lightBody(out, t) : out.base;

        if (light) {
            out.subtext1 = atLeast(out.subtext1, body, LIGHT_BODY_TEXT);
            out.subtext0 = atLeast(out.subtext0, body, LIGHT_SUBTEXT);
        }

        cCrust = out.crust;
        cMantle = out.mantle;
        cBase = out.base;
        cS0 = out.surface0;
        cS1 = out.surface1;
        cS2 = out.surface2;
        cO0 = out.overlay0;
        cO1 = out.overlay1;
        cO2 = out.overlay2;
        cSub1 = out.subtext1;
        cSub0 = out.subtext0;

        cRawA = out.userAccent;
        cA = out.userAccent;
        cAH = out.userAccentHi;
        cAL = out.userAccentLo;
        cAt = out.accent;
        cAtHi = out.accentHi;
        cAtLo = out.accentLo;
        cEdge = cO2;

        cTint = mix(cS1, cRawA, light ? LIGHT_TINT : 0.18);
        cTintHot = mix(cS1, cRawA, light ? LIGHT_TINT_HOT : 0.26);

        if (light) {
            int stroke = darker(body, cTint);
            cA = atLeast(cA, stroke, LIGHT_STROKE);
            cAH = atLeast(cAH, stroke, LIGHT_STROKE);
            cAL = atLeast(cAL, stroke, LIGHT_STROKE);
            cEdge = atLeast(cO2, body, LIGHT_STROKE);

            out.green = atLeast(out.green, body, LIGHT_STATUS);
            out.yellow = atLeast(out.yellow, body, LIGHT_STATUS);
            out.red = atLeast(out.red, body, LIGHT_STATUS);
        }

        cGreen = out.green;
        cRed = out.red;

        int onFill = light ? cAt : cA;
        cOnA = HontunLightPalette.contrast(cCrust, onFill) >= HontunLightPalette.contrast(cBase, onFill) ? cCrust : cBase;

        FlatPaint.set(sub1, cSub1);
        FlatPaint.set(sub0, cSub0);
    }

    private int lightBody(StylePalette out, HontunGuiTheme t) {
        double wa = t != null ? windowAlpha(t.windowOpacity.get(), true) : 1;
        return HontunTheme.lerp(HontunTheme.ramp(id()).base(), out.surface0, (float) clamp01(0xF4 / 255.0 * wa));
    }

    private static int atLeast(int rgb, int bg, double ratio) {
        int c = rgb & 0xFFFFFF;
        for (int i = 0; i < 64 && HontunLightPalette.contrast(c, bg) < ratio; i++) c = HontunTheme.darken(c, 0.95f);
        return c;
    }

    private static int darker(int a, int b) {
        return HontunLightPalette.luminance(a) <= HontunLightPalette.luminance(b) ? a : b;
    }

    private int lightHover(int darkRgb) {
        return light ? cO0 : darkRgb;
    }

    private void armHelpShadow(HontunGuiTheme t) {
        helpShadows = t.richText() ? 2 : 0;
        helpShadowUntil = System.nanoTime() + HELP_SHADOW_NS;
    }

    private boolean consumeHelpShadow() {
        if (helpShadows <= 0) return false;
        helpShadows--;
        return System.nanoTime() <= helpShadowUntil;
    }

    private Color hair(int a) {
        return light ? col(cO1, a * 1.6) : col(cO0, a);
    }

    private int underlineAlpha(double hp) {
        return light ? 0xFF : alpha(lerp(0xB0, 0xFF, hp));
    }

    @Override
    public WWidget windowIcon(HontunGuiTheme t, WindowKind kind, Category category, WWidget fallback) {
        if (!(fallback instanceof WGuiTexture icon)) return fallback;

        return t.texture(icon.texture, Math.round(0.9 * t.textHeight())).color(t.accentColor());
    }

    @Override
    public WWidget sectionTitle(HontunGuiTheme t, WHontunSection s, String title) {
        FlatSectionTitle widget = new FlatSectionTitle(title);
        widget.theme = t;
        return widget;
    }

    @Override
    public RichText keybindLabel(HontunGuiTheme t, WHontunKeybind k, boolean listening, String key) {
        return RichText.of(k.title());
    }

    @Override
    public Color helpKeyColor(HontunGuiTheme t) {
        if (t.light()) return backdropKey(HontunTheme.ramp(id()).subtext1());
        return sub1;
    }

    @Override
    public Color helpTextColor(HontunGuiTheme t) {
        if (t.light()) {
            armHelpShadow(t);
            return backdropText(HontunTheme.ramp(id()).textDim());
        }
        return t.textSecondaryColor();
    }

    @Override
    public Color favoriteColor(WHontunFavorite f) {
        HontunGuiTheme t = f.theme();
        if (f.checked) return f.mouseOver ? col(light ? cAtHi : cAH, 255) : t.accentColor();
        return f.mouseOver ? t.textColor() : t.textSecondaryColor();
    }

    private static double s(HontunGuiTheme t) {
        return t.scale(1);
    }

    private static double px(HontunGuiTheme t, double v) {
        return Math.round(v * s(t));
    }

    private static double h1(HontunGuiTheme t) {
        return Math.max(1, Math.round(s(t)));
    }

    private static double h2(HontunGuiTheme t) {
        return Math.max(2, Math.round(2 * s(t)));
    }

    private static double h3(HontunGuiTheme t) {
        return Math.max(3, Math.round(3 * s(t)));
    }

    private static double u(HontunGuiTheme t) {
        return Math.max(1, Math.round(2 * s(t)));
    }

    private static float rSmall(HontunGuiTheme t) {
        return (float) (u(t) * Math.clamp(Math.round(t.smallCornerRadius.get() / 6.0), 1, 3));
    }

    private static float rLarge(HontunGuiTheme t) {
        return (float) (u(t) * Math.clamp(Math.round(t.cornerRadius.get() / 5.0), 1, 3));
    }

    private static double rowH(HontunGuiTheme t) {
        return Math.round(t.textHeight()) + 2 * px(t, 5);
    }

    private static double halo(HontunGuiTheme t) {
        return px(t, 3);
    }

    @Override
    public float radius(HontunWidget w) {
        return rLarge(w.theme());
    }

    @Override
    public float smallRadius(HontunWidget w) {
        return rSmall(w.theme());
    }

    @Override
    public float outlineWidth(HontunWidget w) {
        return (float) h1(w.theme());
    }

    @Override
    public void moduleSize(WHontunModule m, double[] out) {
        HontunGuiTheme t = m.theme();
        out[0] = h3(t) + 2 * px(t, 10) + m.titleWidth();
        out[1] = rowH(t);
    }

    @Override
    public void tabSize(WHontunTopBar.WTopBarButton b, double[] out) {
        HontunGuiTheme t = b.bar().theme();
        double icon = b.hasIcon() ? Math.round(0.9 * t.textHeight()) + px(t, 6) : 0;
        out[0] = 2 * px(t, 12) + icon + t.textWidth(b.text());
        out[1] = Math.round(t.textHeight()) + 2 * px(t, 7);
    }

    @Override
    public void checkboxSize(WHontunCheckbox c, double[] out) {
        double size = Math.round(0.9 * c.theme().textHeight());
        out[0] = size;
        out[1] = size;
    }

    @Override
    public void dropdownSize(WHontunDropdown<?> d, double[] out) {
        HontunGuiTheme t = d.theme();
        double width = px(t, 10) + d.maxValueWidth() + px(t, 8) + Math.round(0.7 * t.textHeight()) + px(t, 10);
        if (hasTitle(d)) width += t.textWidth(d.titleText()) + px(t, 8) + h1(t) + px(t, 8);
        out[0] = width;
        out[1] = rowH(t);
    }

    @Override
    public void buttonSize(WHontunButton b, double[] out) {
        HontunGuiTheme t = b.theme();
        double h = rowH(t);

        if (b.parent instanceof WHontunKeybind k) {
            out[0] = px(t, 10) + t.textWidth(k.title()) + px(t, 6) + keycapWidth(t, k) + px(t, 10);
            out[1] = h;
            return;
        }

        RichText text = b.displayText();
        out[0] = text != null ? 2 * px(t, 10) + t.textWidth(text) : h;
        out[1] = h;
    }

    @Override
    public void pressableSize(WWidget w, double[] out) {
        HontunGuiTheme t = (HontunGuiTheme) w.getTheme();
        double h = rowH(t);

        if (w instanceof WHontunConfirmedButton b) {
            String text = b.getText();
            out[0] = text != null ? 2 * px(t, 10) + t.textWidth(text) : h;
            out[1] = h;
            return;
        }

        if (w instanceof WHontunPlus || w instanceof WHontunMinus || w instanceof WHontunConfirmedMinus) {
            out[0] = h;
            out[1] = h;
        }
    }

    @Override
    public void textBoxSize(WHontunTextBox b, double[] out) {
        HontunGuiTheme t = b.theme();
        double s = t.textHeight();
        out[0] = b.padding() + s + b.padding();
        out[1] = b.rendersBackground() ? rowH(t) : b.padding() + s + b.padding();
    }

    @Override
    public void colorPickerSize(WHontunColorPicker p, double[] out) {
        HontunGuiTheme t = p.theme();
        out[0] = Math.round(t.textHeight() * 3);
        out[1] = rowH(t);
    }

    @Override
    public double sliderHeight(WHontunSlider s) {
        HontunGuiTheme t = s.theme();
        return knobSize(t) + px(t, 4);
    }

    @Override
    public double scrollbarWidth(HontunGuiTheme t) {
        return px(t, 6) + px(t, 1);
    }

    @Override
    public double popupOffset(HontunGuiTheme t) {
        return px(t, 4);
    }

    private static double knobSize(HontunGuiTheme t) {
        long d = Math.round(0.8 * t.textHeight());
        if (d % 2 == 0) d++;
        return d;
    }

    private static boolean hasTitle(WHontunDropdown<?> d) {
        return d.titleText() != null && !d.titleText().getPlainText().isEmpty();
    }

    private static double keycapWidth(HontunGuiTheme t, WHontunKeybind k) {
        double key = Math.max(t.textWidth(k.keybind().toString()), t.textWidth(LISTENING));
        return 2 * px(t, 7) + key;
    }

    private void ring(double x0, double y0, double x1, double y1, float radius, double width, int a) {
        if (a <= 0 || width <= 0) return;

        edges(x0 - width, y0 - width, x1 + width, y1 + width)
                .radius(radius + width)
                .color(CLEAR)
                .outline(col(0x000000, a), (float) width)
                .render();
    }

    private void underline(double x0, double y0, double x1, double y1, float radius, double thickness, int left, int right, int a) {
        if (thickness <= 0 || a <= 0) return;

        double t = Math.round(thickness);
        edges(x0, y0, x1, y1)
                .radius(radius)
                .hgradient(col(left, a), col(right, a))
                .clip(x0, y1 - t, x1 - x0, t)
                .render();
    }

    private void leftBar(double x0, double y0, double x1, double y1, float radius, Corners corners, double width, int top, int bottom, double progress) {
        if (progress <= 0) return;

        double h = y1 - y0;
        double bh = Math.round(h * clamp01(progress));
        if (bh <= 0) return;

        double by = y0 + Math.round((h - bh) / 2);

        edges(x0, y0, x1, y1)
                .radius(radius, corners)
                .vgradient(col(top, 255), col(bottom, 255))
                .clip(x0, by, width, bh)
                .render();
    }

    private void face(HontunGuiTheme t, double x0, double y0, double x1, double y1, double hp, boolean pressed) {
        float r = rSmall(t);
        double h1 = h1(t);

        int top, bottom, topA, bottomA, outline, outlineA, lightC, lightA;

        if (pressed) {
            top = cS0;
            bottom = cS0;
            topA = 0xF0;
            bottomA = 0xF0;
            outline = light ? cAt : cA;
            outlineA = 0xC0;
            lightC = light ? 0xFFFFFF : cO2;
            lightA = light ? 0x80 : 0x2E;
        }
        else if (light) {
            top = mix(cBase, mix(cBase, cRawA, 0.12), hp);
            bottom = mix(cS0, mix(cS0, cRawA, 0.18), hp);
            topA = 0xFF;
            bottomA = 0xFF;
            outline = mix(cO1, cAt, hp);
            outlineA = alpha(lerp(0xC0, 0xFF, hp));
            lightC = 0xFFFFFF;
            lightA = 0x80;
        }
        else {
            top = mix(HontunTheme.lighten(cS2, 1.04f), cAL, hp);
            bottom = mix(cS0, HontunTheme.darken(cAL, 0.6f), hp);
            topA = alpha(lerp(0x55, 0x9A, hp));
            bottomA = alpha(lerp(0x4A, 0x66, hp));
            outline = mix(cO0, cAH, hp);
            outlineA = alpha(lerp(0x80, 0xE0, hp));
            lightC = cO2;
            lightA = alpha(lerp(0x2E, 0x70, hp));
        }

        edges(x0, y0, x1, y1)
                .radius(r)
                .vgradient(col(top, topA), col(bottom, bottomA))
                .outline(col(outline, outlineA), (float) h1)
                .render();

        if (x1 - x0 > 2 * h1 && y1 - y0 > 2 * h1) {
            edges(x0 + h1, y0 + h1, x1 - h1, y1 - h1)
                    .radius(Math.max(0, r - h1))
                    .color(col(lightC, lightA))
                    .clip(x0 + h1, y0 + h1, x1 - x0 - 2 * h1, h1)
                    .render();
        }
    }

    private void popup(HontunGuiTheme t, double x0, double y0, double x1, double y1, boolean halo) {
        float r = rSmall(t);
        if (halo) ring(x0, y0, x1, y1, r, halo(t), light ? 0x1C : 0x40);

        edges(x0, y0, x1, y1)
                .radius(r)
                .color(col(cBase, 0xF8))
                .outline(col(cO1, light ? 0xC0 : 0x90), (float) h1(t))
                .render();
    }

    private void keycap(HontunGuiTheme t, double x0, double y0, double x1, double y1, RichText label, Color fg, Color outline) {
        float rc = (float) u(t);
        double h1 = h1(t);

        edges(x0, y0, x1, y1)
                .radius(rc)
                .color(col(light ? cS0 : cS2, 255))
                .outline(outline, (float) h1)
                .render();

        edges(x0, y0, x1, y1)
                .radius(rc)
                .color(light ? col(cO1, 0xC0) : col(cCrust, 0x90))
                .clip(x0, y1 - h1, x1 - x0, h1)
                .render();

        double tw = t.textWidth(label);
        double th = t.textHeight(label);
        text().text(label, rd(x0 + (x1 - x0 - tw) / 2), rd(y0 + (y1 - y0 - th) / 2), fg);
    }

    private void icon(GuiRenderer renderer, double x0, double y0, double x1, double y1, double size, GuiTexture texture, Color color) {
        if (texture == null || size <= 0) return;
        renderer.quad(rd(x0 + (x1 - x0 - size) / 2), rd(y0 + (y1 - y0 - size) / 2), size, size, texture, color);
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

    void paintTick(FlatTick tick) {
        double x0 = rd(tick.x), y0 = rd(tick.y);
        double w = rd(tick.width), h = rd(tick.height);
        if (w <= 0 || h <= 0) return;

        edges(x0, y0, x0 + w, y0 + h)
                .radius(w / 2)
                .vgradient(col(cAH, 255), col(cAL, 255))
                .render();
    }

    @Override
    public void paintWindowBack(WHontunWindow w, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme t = w.theme();
        double wa = t.windowOpacity();
        float R = rLarge(t);

        double hh = w.headerWidget().height;
        double x0 = rd(w.x), y0 = rd(w.y), x1 = rd(w.x + w.width);
        double y1 = rd(w.y + hh + Math.max((w.height - hh) * w.expandProgress(), 0));

        if (t.effWindowShadow()) ring(x0, y0, x1, y1, R, halo(t), alpha((light ? 0x1C : 0x40) * wa));

        int fa = alpha(0xF4 * wa);

        edges(x0, y0, x1, y1)
                .radius(R)
                .vgradient(col(cS0, fa), col(cBase, fa))
                .outline(col(cO0, light ? 0xC0 : 0x80), (float) h1(t))
                .render();

        tintDescription(w);
    }

    private void tintDescription(WHontunWindow w) {
        if (!(w.icon instanceof WHontunFavorite) || w.view == null || w.view.cells.isEmpty()) return;
        if (!(w.view.cells.getFirst().widget() instanceof WContainer info) || info.cells.isEmpty()) return;
        if (info.cells.getFirst().widget() instanceof WLabel description && description.color == null) description.color = sub1;
    }

    @Override
    public void paintWindowHeader(WHontunWindow w, WHontunWindow.WHontunHeader h, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme t = w.theme();
        double h1 = h1(t);
        float R = rLarge(t);
        double wa = t.windowOpacity();
        double hp = FlatAnim.tween(h, HOVER, h.mouseOver, anim(AnimRole.HEADER_HOVER));

        double x0 = rd(h.x), y0 = rd(h.y), x1 = rd(h.x + h.width), y1 = rd(h.y + h.height);
        double hh = w.headerWidget().height;
        double fy1 = rd(w.y + hh + Math.max((w.height - hh) * w.expandProgress(), 0));

        float ri = (float) Math.max(0, R - h1);
        float rb = (float) (ri * (1 - clamp01(w.cornerProgress())));
        int fa = alpha(255 * Math.max(0.85, wa));

        edges(x0 + h1, y0 + h1, x1 - h1, y1 - h1)
                .radii(ri, ri, rb, rb)
                .vgradient(col(mix(cS1, cS2, hp), fa), col(mix(cS0, cS1, hp), fa))
                .render();

        edges(x0, y0, x1, Math.max(fy1, y1))
                .radius(R)
                .hgradient(col(cAL, 255), col(cAH, 255))
                .clip(x0, y0, x1 - x0, h2(t))
                .render();

        double e = clamp01(w.expandProgress());
        if (e > 0) {
            edges(x0 + h1, y1 - h1, x1 - h1, y1)
                    .color(light ? col(cO1, 0xC0 * e) : col(cO0, 0x70 * e))
                    .render();
        }
    }

    @Override
    public void paintTopBar(WHontunTopBar b, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme t = b.theme();
        double wa = t.windowOpacity();
        float R = rLarge(t);
        double x0 = rd(b.x), y0 = rd(b.y), x1 = rd(b.x + b.width), y1 = rd(b.y + b.height);

        if (t.effWindowShadow()) ring(x0, y0, x1, y1, R, halo(t), alpha((light ? 0x1C : 0x40) * wa));

        int fa = alpha(255 * Math.max(0.9, wa));

        edges(x0, y0, x1, y1)
                .radius(R)
                .vgradient(col(cS0, fa), col(cBase, fa))
                .outline(col(cO0, light ? 0xC0 : 0x80), (float) h1(t))
                .render();
    }

    @Override
    public void paintTab(WHontunTopBar.WTopBarButton b, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme t = b.bar().theme();
        double T = t.textHeight();
        float r = rSmall(t);
        double h2 = h2(t);

        boolean selected = b.isSelected();
        double hp = FlatAnim.tween(b, HOVER, b.mouseOver && !selected, anim(AnimRole.BUTTON_HOVER));

        double x0 = rd(b.x), y0 = rd(b.y), x1 = rd(b.x + b.width), y1 = rd(b.y + b.height);

        if (!selected && b.isPressed()) {
            edges(x0, y0, x1, y1).radius(r).color(col(lightHover(cS2), light ? 0xA0 : 0xB0)).render();
        }
        else if (!selected && hp > 0) {
            edges(x0, y0, x1, y1).radius(r).color(col(lightHover(cS1), (light ? 0x70 : 0x90) * hp)).render();
        }

        double iconSize = b.hasIcon() ? Math.round(0.9 * T) : 0;
        double gap = b.hasIcon() ? px(t, 6) : 0;
        double textWidth = t.textWidth(b.text());
        double contentWidth = iconSize + gap + textWidth;
        double cx = (x0 + x1) / 2;

        double ux = rd(cx - contentWidth / 2 - px(t, 2));
        double uw = rd(contentWidth + px(t, 4));
        double uy = y1 - h2 - px(t, 3);

        if (!selected && hp > 0) {
            box(ux, uy, uw, h2).radius(h2 / 2).color(light ? col(cO2, 0xA0 * hp) : col(cO1, 0x70 * hp)).render();
        }

        if (selected) {
            double p = clamp01(b.selectedProgress());
            double[] from = glideFrom.get(b);

            if (from == null) {
                from = TabGlide.fresh(1000) && TabGlide.w > 0
                        ? new double[]{TabGlide.x, TabGlide.w}
                        : new double[]{Double.NaN, 0};
                glideFrom.put(b, from);
            }

            double gx = ux;
            double gw = uw;

            if (p < 1) {
                if (!Double.isNaN(from[0])) {
                    gx = lerp(from[0], ux, p);
                    gw = lerp(from[1], uw, p);
                }
                else {
                    gw = uw * p;
                    gx = ux + (uw - gw) / 2;
                }
            }

            if (gw >= 1) {
                box(gx, uy, gw, h2)
                        .radius(h2 / 2)
                        .hgradient(col(cAL, 255), col(cAH, 255))
                        .render();
            }

            TabGlide.store(gx, uy, gw, h2);
        }

        Color fg = selected ? t.textColor() : t.textSecondaryColor();
        Color iconColor = selected ? t.accentColor() : t.textSecondaryColor();

        double x = rd(cx - contentWidth / 2);

        if (b.hasIcon()) {
            renderer.quad(x, rd(y0 + (y1 - y0 - iconSize) / 2), iconSize, iconSize, b.icon(), iconColor);
            x += iconSize + gap;
        }

        text().text(b.text(), rd(x), rd(y0 + (y1 - y0 - T) / 2), fg);
    }

    @Override
    public void paintModule(WHontunModule m, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme t = m.theme();
        float r = rSmall(t);
        double T = t.textHeight();

        double hp = clamp01(m.hoverProgress());
        double ap = clamp01(m.highlightProgress());
        boolean active = m.module().isActive();
        Corners corners = m.corners();

        double x0 = rd(m.x), y0 = rd(m.y), x1 = rd(m.x + m.width), y1 = rd(m.y + m.height);

        if (m.isPressed()) {
            if (active) {
                edges(x0, y0, x1, y1)
                        .radius(r, corners)
                        .color(col(mix(cTint, cCrust, 0.15), light ? 0xFF : 0xE6))
                        .render();
            }
            else {
                edges(x0, y0, x1, y1).radius(r).color(col(lightHover(cS2), light ? 0xB0 : 0xC0)).render();
            }
        }
        else {
            if (hp > 0 && ap < 1) {
                edges(x0, y0, x1, y1).radius(r).color(col(lightHover(cS1), (light ? 0x70 : 0xA0) * hp * (1 - ap))).render();
            }

            if (ap > 0) {
                edges(x0, y0, x1, y1)
                        .radius(r, corners)
                        .color(col(mix(cTint, cTintHot, hp), (light ? 0xFF : 0xE6) * ap))
                        .render();
            }
        }

        if (ap > 0) {
            int n = Math.max(1, m.runLength());
            int i = Math.clamp(m.runIndex(), 0, n - 1);
            int top = mix(cAH, cAL, (double) i / n);
            int bottom = mix(cAH, cAL, (double) (i + 1) / n);
            leftBar(x0, y0, x1, y1, r, corners, h3(t), top, bottom, ap);
        }

        double titleWidth = m.titleWidth();
        double x = switch (t.moduleAlignment.get()) {
            case Center -> x0 + (x1 - x0) / 2 - titleWidth / 2;
            case Right -> x1 - titleWidth - px(t, 10);
            default -> x0 + h3(t) + px(t, 10);
        };

        Color color = mix(sub1, t.textColor(), Math.max(hp, ap));
        text().text(m.title(), rd(x), rd(y0 + (y1 - y0 - T) / 2), color);
    }

    @Override
    public void paintSectionBody(WHontunSection s, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme t = s.theme();
        double bo = t.backgroundOpacity();
        double x0 = rd(s.x), y0 = rd(s.y), x1 = rd(s.x + s.width), y1 = rd(s.y + s.height);

        edges(x0, y0, x1, y1)
                .radius(rSmall(t))
                .color(light ? col(cS0, 0x80 * bo) : col(cMantle, 0x70 * bo))
                .outline(light ? col(cO1, 0xA0) : col(cO0, 0x60), (float) h1(t))
                .render();
    }

    @Override
    public void paintSectionHeader(WHontunSection s, WHontunSection.WHontunHeader h, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme t = s.theme();
        double h1 = h1(t);
        double bo = t.backgroundOpacity();
        double hp = FlatAnim.tween(h, HOVER, h.mouseOver, anim(AnimRole.HEADER_HOVER));

        double x0 = rd(h.x), y0 = rd(h.y), x1 = rd(h.x + h.width), y1 = rd(h.y + h.height);
        float ri = (float) Math.max(0, rSmall(t) - h1);
        float rb = (float) (ri * (1 - clamp01(s.cornerProgress())));

        edges(x0 + h1, y0 + h1, x1 - h1, y1 - h1)
                .radii(ri, ri, rb, rb)
                .color(col(mix(cS0, cS1, hp), 0xE0 * bo))
                .render();

        double p = clamp01(s.expandProgress());
        if (p > 0) {
            edges(x0 + h1, y1 - h1, x1 - h1, y1).color(hair(alpha(0x50 * p))).render();
        }
    }

    @Override
    public void paintSeparatorH(WHontunHorizontalSeparator s, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme t = s.theme();
        double h1 = h1(t);
        int lineAlpha = s.parent instanceof WHontunWindow ? 0x70 : 0x60;
        double ly = rd(s.y + (s.height - h1) / 2);

        if (!s.hasText()) {
            edges(s.x, ly, s.x + s.width, ly + h1).color(hair(lineAlpha)).render();
            return;
        }

        RichText title = RichText.bold(s.titleString()).scale(SMALL);
        double tw = t.textWidth(title);
        double th = t.textHeight(title);

        text().text(title, rd(s.x), rd(s.y + (s.height - th) / 2), t.textSecondaryColor());

        double lx = rd(s.x + tw + px(t, 8));
        double lx1 = rd(s.x + s.width);
        if (lx1 > lx) edges(lx, ly, lx1, ly + h1).color(hair(lineAlpha)).render();
    }

    @Override
    public void paintSeparatorV(WHontunVerticalSeparator s, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme t = s.theme();
        double h1 = h1(t);
        double x = rd(s.x + (s.width - h1) / 2);

        edges(x, s.y, x + h1, s.y + s.height).color(hair(0x50)).render();
    }

    @Override
    public void paintScrollbar(WHontunView v, GuiRenderer renderer, double mouseX, double mouseY) {
        if (!v.scrollable() || !v.hasScrollBar) return;

        HontunGuiTheme t = v.theme();
        boolean drag = v.isFocused();
        double hv = FlatAnim.tween(v, HOVER, v.barHovered() || drag, anim(AnimRole.SCROLLBAR));
        double dg = FlatAnim.tween(v, DRAG, drag, anim(AnimRole.SCROLLBAR));

        double width = rd(lerp(px(t, 4), px(t, 6), hv));
        double right = rd(v.barX() + v.barWidth() - px(t, 1));
        double x = right - width;

        if (hv > 0) {
            edges(x, v.y, right, v.y + v.height).radius(width / 2).color(col(light ? cS2 : cS0, 0x80 * hv)).render();
        }

        Color thumb = light
                ? mix(mix(col(cO2, 0xE0), col(cSub0, 0xE0), hv), col(cAt, 0xF0), dg)
                : mix(mix(col(cO1, 0x90), col(cO2, 0xC0), hv), col(cAt, 0xE0), dg);

        edges(x, v.barY(), right, v.barY() + v.barHeight())
                .radius(width / 2)
                .color(thumb)
                .render();
    }

    @Override
    public void paintButton(WHontunButton b, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme t = b.theme();
        double hp = FlatAnim.tween(b, HOVER, b.mouseOver, anim(AnimRole.BUTTON_HOVER));
        boolean pressed = b.isPressed();
        double x0 = rd(b.x), y0 = rd(b.y), x1 = rd(b.x + b.width), y1 = rd(b.y + b.height);
        double T = t.textHeight();

        if (b.parent instanceof WHontunKeybind k) {
            face(t, x0, y0, x1, y1, hp, pressed);
            underline(x0, y0, x1, y1, rSmall(t), h2(t), cA, cAH, k.isListening() ? 0xFF : underlineAlpha(hp));

            RichText label = b.displayText();
            if (label != null) {
                double ty = rd(y0 + (y1 - y0 - T) / 2) + (pressed ? h1(t) : 0);
                text().text(label, x0 + px(t, 10), ty, sub1);
            }
            return;
        }

        RichText label = b.displayText();

        if (label == null) {
            ghost(t, renderer, x0, y0, x1, y1, hp, pressed, b.icon(), mix(sub1, t.textColor(), hp), light ? cBase : cS1, 0xB0 * t.backgroundOpacity());
            return;
        }

        face(t, x0, y0, x1, y1, hp, pressed);
        underline(x0, y0, x1, y1, rSmall(t), h2(t), cA, cAH, underlineAlpha(hp));

        double ty = rd(y0 + (y1 - y0 - T) / 2) + (pressed ? h1(t) : 0);
        text().text(label, rd(x0 + (x1 - x0 - t.textWidth(label)) / 2), ty, t.textColor());
    }

    private void ghost(HontunGuiTheme t, GuiRenderer renderer, double x0, double y0, double x1, double y1,
                       double hp, boolean pressed, GuiTexture texture, Color iconColor, int hoverRgb, double hoverAlpha) {
        float r = rSmall(t);

        if (pressed) edges(x0, y0, x1, y1).radius(r).color(col(lightHover(cS2), light ? 0xB0 : 0xD0)).render();
        else if (hp > 0) edges(x0, y0, x1, y1).radius(r).color(col(hoverRgb, hoverAlpha * hp)).render();

        icon(renderer, x0, y0, x1, y1, Math.round(0.8 * t.textHeight()), texture, iconColor);
    }

    @Override
    public void paintCheckbox(WHontunCheckbox c, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme t = c.theme();
        float r = rSmall(t);
        double p = clamp01(c.progress());
        boolean hover = c.mouseOver;
        boolean pressed = c.isPressed();

        double x0 = rd(c.x), y0 = rd(c.y), x1 = rd(c.x + c.width), y1 = rd(c.y + c.height);

        if (p < 1) {
            int fill = pressed ? cS2 : hover ? cS1 : light ? cBase : cS0;
            int outline = hover ? cAH : light ? cEdge : cO1;

            edges(x0, y0, x1, y1)
                    .radius(r)
                    .color(col(fill, 255 * t.backgroundOpacity()))
                    .outline(col(outline, light ? 0xFF : 0xB0), (float) h1(t))
                    .render();
        }

        if (p <= 0) return;

        int base = light ? cAt : cA;
        int hi = light ? cAtHi : cAH;
        int lo = light ? cAtLo : cAL;
        int top, bottom;

        if (pressed) {
            top = lo;
            bottom = lo;
        }
        else if (hover) {
            top = hi;
            bottom = base;
        }
        else {
            top = base;
            bottom = mix(base, lo, 0.35);
        }

        int a = alpha(255 * p);

        edges(x0, y0, x1, y1)
                .radius(r)
                .vgradient(col(top, a), col(bottom, a))
                .render();

        double tick = Math.round(0.7 * (x1 - x0)) * (0.8 + 0.2 * p);
        icon(renderer, x0, y0, x1, y1, tick, HontunBuiltinIcons.TICK.texture(), col(cOnA, a));
    }

    @Override
    public void paintColorPicker(WHontunColorPicker p, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme t = p.theme();
        float r = rSmall(t);
        Color color = p.color();

        double x0 = rd(p.x), y0 = rd(p.y), x1 = rd(p.x + p.width), y1 = rd(p.y + p.height);
        double mid = rd((x0 + x1) / 2);

        edges(x0, y0, x1, y1).radius(r).color(col(cS2, 255)).render();
        edges(x0, y0, mid, y1).radius(r, Corners.LEFT).color(color).render();
        edges(mid, y0, x1, y1).radius(r, Corners.RIGHT).color(new Color(color.r, color.g, color.b, 255)).render();

        edges(x0, y0, x1, y1)
                .radius(r)
                .color(CLEAR)
                .outline(p.mouseOver ? col(cAH, light ? 0xFF : 0xC0) : light ? col(cEdge, 0xB0) : col(cO1, 0xA0), (float) h1(t))
                .render();

        if (p.mouseOver) icon(renderer, x0, y0, x1, y1, t.textHeight(), p.overlay(), pickerIconTint(color));
    }

    @Override
    public void paintConfirmedButton(WHontunConfirmedButton b, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme t = b.theme();
        double hp = FlatAnim.tween(b, HOVER, b.mouseOver, anim(AnimRole.BUTTON_HOVER));
        boolean pressed = b.isPressed();
        double x0 = rd(b.x), y0 = rd(b.y), x1 = rd(b.x + b.width), y1 = rd(b.y + b.height);
        double T = t.textHeight();
        float r = rSmall(t);
        String label = b.getText();

        if (!b.armed()) {
            if (label == null) {
                ghost(t, renderer, x0, y0, x1, y1, hp, pressed, b.icon(), mix(sub1, t.textColor(), hp), light ? cBase : cS1, 0xB0 * t.backgroundOpacity());
                return;
            }

            face(t, x0, y0, x1, y1, hp, pressed);
            underline(x0, y0, x1, y1, r, h2(t), cA, cAH, underlineAlpha(hp));

            double ty = rd(y0 + (y1 - y0 - T) / 2) + (pressed ? h1(t) : 0);
            text().text(RichText.of(label), rd(x0 + (x1 - x0 - t.textWidth(label)) / 2), ty, t.textColor());
            return;
        }

        edges(x0, y0, x1, y1)
                .radius(r)
                .color(col(mix(cS1, cRed, lerp(0.16, 0.24, hp)), 0xF0))
                .outline(col(cRed, 0xC0), (float) h1(t))
                .render();
        underline(x0, y0, x1, y1, r, h2(t), cRed, cRed, 0xFF);

        Color fg = t.redColor();

        if (label == null) {
            icon(renderer, x0, y0, x1, y1, Math.round(0.8 * T), b.icon(), fg);
            return;
        }

        double ty = rd(y0 + (y1 - y0 - T) / 2) + (pressed ? h1(t) : 0);
        text().text(RichText.of(label), rd(x0 + (x1 - x0 - t.textWidth(label)) / 2), ty, fg);
    }

    @Override
    public void paintConfirmedMinus(WHontunConfirmedMinus m, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme t = m.theme();
        double hp = FlatAnim.tween(m, HOVER, m.mouseOver, anim(AnimRole.BUTTON_HOVER));
        double x0 = rd(m.x), y0 = rd(m.y), x1 = rd(m.x + m.width), y1 = rd(m.y + m.height);

        if (!m.armed()) {
            ghost(t, renderer, x0, y0, x1, y1, hp, m.isPressed(), HontunBuiltinIcons.MINUS.texture(), t.redColor(), cRed, 0x26);
            return;
        }

        edges(x0, y0, x1, y1).radius(rSmall(t)).color(col(cRed, 0xD0)).render();
        icon(renderer, x0, y0, x1, y1, Math.round(0.8 * t.textHeight()), HontunBuiltinIcons.MINUS.texture(), col(cCrust, 255));
    }

    @Override
    public void paintMinus(WHontunMinus m, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme t = m.theme();
        double hp = FlatAnim.tween(m, HOVER, m.mouseOver, anim(AnimRole.BUTTON_HOVER));
        double x0 = rd(m.x), y0 = rd(m.y), x1 = rd(m.x + m.width), y1 = rd(m.y + m.height);

        ghost(t, renderer, x0, y0, x1, y1, hp, m.isPressed(), HontunBuiltinIcons.MINUS.texture(), t.redColor(), cRed, 0x26);
    }

    @Override
    public void paintPlus(WHontunPlus p, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme t = p.theme();
        double hp = FlatAnim.tween(p, HOVER, p.mouseOver, anim(AnimRole.BUTTON_HOVER));
        double x0 = rd(p.x), y0 = rd(p.y), x1 = rd(p.x + p.width), y1 = rd(p.y + p.height);

        ghost(t, renderer, x0, y0, x1, y1, hp, p.isPressed(), HontunBuiltinIcons.PLUS.texture(), t.greenColor(), cGreen, 0x26);
    }

    @Override
    public void paintFavorite(WHontunFavorite f, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme t = f.theme();
        double hp = FlatAnim.tween(f, HOVER, f.mouseOver, anim(AnimRole.FAVORITE));

        Color rest = f.checked ? t.accentColor() : t.textSecondaryColor();
        Color hover = f.checked ? col(light ? cAtHi : cAH, 255) : t.textColor();

        renderer.quad(
                rd(f.x),
                rd(f.y),
                f.size(),
                f.size(),
                f.checked ? HontunBuiltinIcons.BOOKMARK_YES.texture() : HontunBuiltinIcons.BOOKMARK_NO.texture(),
                mix(rest, hover, hp)
        );
    }

    @Override
    public void paintTriangle(WHontunTriangle tri, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme t = tri.theme();
        double hp = FlatAnim.tween(tri, HOVER, headerHovered(tri), anim(AnimRole.HEADER_HOVER));
        double size = Math.round(0.75 * t.textHeight());

        renderer.rotatedQuad(
                rd(tri.x + (tri.width - size) / 2),
                rd(tri.y + (tri.height - size) / 2),
                size,
                size,
                tri.rotation,
                HontunBuiltinIcons.ARROW.texture(),
                mix(t.textSecondaryColor(), t.textColor(), hp)
        );
    }

    @Override
    public void paintKeybindExtra(WHontunKeybind k, GuiRenderer renderer, double mouseX, double mouseY) {
        WHontunButton b = k.button();
        if (b == null) return;

        HontunGuiTheme t = k.theme();
        String key = k.keybind().toString();
        String last = keyLayout.put(k, key);
        if (last != null && !last.equals(key)) k.invalidate();

        boolean listening = k.isListening();
        double pulse = listening ? 0.8 + 0.2 * Math.sin((System.currentTimeMillis() % 1200) / 1200.0 * Math.PI * 2) : 1;

        double h = Math.round(t.textHeight()) + px(t, 4);
        double x1 = rd(b.x + b.width) - px(t, 10);
        double x0 = rd(x1 - keycapWidth(t, k));
        double y0 = rd(b.y + (b.height - h) / 2);

        Color outline = listening ? col(cAt, 255 * pulse) : col(cO1, light ? 0xD0 : 0x90);
        Color fg = listening
                ? col(cAt, 255 * pulse)
                : k.keybind().isSet() ? t.textColor() : t.textSecondaryColor();

        keycap(t, x0, y0, x1, y0 + h, RichText.of(listening ? LISTENING : key), fg, outline);
    }

    @Override
    public void paintSlider(WHontunSlider s, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme t = s.theme();
        double d = knobSize(t);
        double cy = s.y + s.height / 2;

        double track = Math.max(4, px(t, 4));
        double tx0 = rd(s.x + d / 2);
        double tx1 = rd(s.x + s.width - d / 2);
        double ty0 = rd(cy - track / 2);
        double ty1 = ty0 + track;

        edges(tx0, ty0, tx1, ty1)
                .radius(track / 2)
                .color(light ? col(mix(cO0, cO1, 0.6), 255) : col(cS2, 255 * t.backgroundOpacity()))
                .render();

        double knobX = s.x + s.handleSize() / 2 + s.valueOffset();
        double fillEnd = Math.min(rd(knobX), tx1);

        if (fillEnd > tx0) {
            edges(tx0, ty0, fillEnd, ty1)
                    .radius(track / 2)
                    .hgradient(col(cAL, 255), col(cA, 255))
                    .render();
        }

        boolean dragging = s.isDragging();
        double hv = FlatAnim.tween(s, HOVER, s.handleHovered() || dragging, anim(AnimRole.SLIDER_HOVER));
        double dg = FlatAnim.tween(s, DRAG, dragging, anim(AnimRole.SLIDER_HOVER));

        double kx0 = rd(knobX - d / 2);
        double ky0 = rd(cy - d / 2);

        RoundedRect knob = edges(kx0, ky0, kx0 + d, ky0 + d).radius(d / 2);
        if (light) knob.color(col(cBase, 255));
        else knob.vgradient(col(cO0, 255), col(cS2, 255));
        knob.outline(light ? mix(col(cEdge, 255), col(cAH, 255), hv) : mix(col(cO1, 0xA0), col(cAH, 0xC0), hv), (float) h1(t)).render();

        double dot = rd(lerp(lerp(px(t, 7), px(t, 9), hv), px(t, 5), dg));
        if (dot > 0) {
            double dx0 = rd(kx0 + (d - dot) / 2);
            double dy0 = rd(ky0 + (d - dot) / 2);
            edges(dx0, dy0, dx0 + dot, dy0 + dot)
                    .radius(dot / 2)
                    .color(col(mix(cA, cAH, hv), 255))
                    .render();
        }
    }

    @Override
    public void paintDropdown(WHontunDropdown<?> d, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme t = d.theme();
        double T = t.textHeight();
        double h1 = h1(t);
        double hp = FlatAnim.tween(d, HOVER, d.mouseOver, anim(AnimRole.DROPDOWN_HOVER));
        double open = clamp01(d.indicatorProgress());
        double act = Math.max(hp, open);

        double x0 = rd(d.x), y0 = rd(d.y), x1 = rd(d.x + d.width), y1 = rd(d.y + d.height);

        face(t, x0, y0, x1, y1, hp, d.isPressed());
        underline(x0, y0, x1, y1, rSmall(t), lerp(h1, h2(t), act), cA, cAH, alpha(lerp(light ? 0xF0 : 0x60, 0xFF, act)));

        double ty = rd(y0 + (y1 - y0 - T) / 2);
        double x = x0 + px(t, 10);

        if (hasTitle(d)) {
            text().text(d.titleText(), rd(x), ty, sub1);
            x = rd(x + t.textWidth(d.titleText()) + px(t, 8));

            double dh = Math.round(0.8 * T);
            double dy = rd(y0 + (y1 - y0 - dh) / 2);
            edges(x, dy, x + h1, dy + dh).color(hair(0x60)).render();

            x += h1 + px(t, 8);
        }

        text().text(d.valueText(), rd(x), ty, t.textColor());

        double cs = Math.round(0.7 * T);
        renderer.rotatedQuad(
                x1 - px(t, 10) - cs,
                rd(y0 + (y1 - y0 - cs) / 2),
                cs,
                cs,
                180 * (1 - open),
                HontunBuiltinIcons.ARROW.texture(),
                mix(t.textSecondaryColor(), t.textColor(), hp)
        );
    }

    @Override
    public void paintDropdownPopup(WHontunDropdown<?> d, WHontunDropdown.WRoot root, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme t = root.theme();
        double open = clamp01(d.indicatorProgress());

        if (open < 1) {
            renderer.setAlpha(open);
            renderer.post(() -> renderer.setAlpha(1));
        }

        popup(t, rd(root.x), rd(root.y), rd(root.x + root.width), rd(root.y + root.height), false);
    }

    @Override
    public void paintDropdownValue(WHontunDropdown<?>.WValue v, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme t = v.theme();
        float rc = (float) u(t);
        double hp = FlatAnim.tween(v, HOVER, v.mouseOver, anim(AnimRole.BUTTON_HOVER));
        boolean selected = v.isSelectedValue();

        double x0 = rd(v.x), y0 = rd(v.y), x1 = rd(v.x + v.width), y1 = rd(v.y + v.height);

        if (selected) {
            edges(x0, y0, x1, y1).radius(rc).color(col(mix(cTint, cTintHot, hp), 255)).render();
            leftBar(x0, y0, x1, y1, rc, Corners.ALL, h3(t), cAH, cAL, 1);
        }
        else if (hp > 0) {
            edges(x0, y0, x1, y1).radius(rc).color(col(light ? cS2 : cS1, 0xC0 * hp)).render();
        }

        double left = v.owner().popup().x + px(t, 10);
        Color color = selected ? t.textColor() : mix(sub1, t.textColor(), hp);
        text().text(v.valueName(), rd(Math.max(left, x0)), rd(y0 + (y1 - y0 - t.textHeight()) / 2), color);
    }

    @Override
    public void paintTextBox(WHontunTextBox b, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme t = b.theme();
        double T = t.textHeight();
        double h1 = h1(t);
        double h2 = h2(t);
        float r = rSmall(t);
        boolean focused = b.isFocused();
        double fp = FlatAnim.tween(b, FOCUS, focused, anim(AnimRole.TEXTBOX_FOCUS));

        double x0 = rd(b.x), y0 = rd(b.y), x1 = rd(b.x + b.width), y1 = rd(b.y + b.height);

        if (b.rendersBackground()) {
            int fa = alpha(0xF0 * t.backgroundOpacity());
            boolean titled = b.hasTitle();
            double ux = x0;

            if (titled) {
                WHontunLabel label = titleLabel(b);
                if (label != null) {
                    label.color = sub1;
                    ux = rd(label.x - px(t, 10));
                }
                else {
                    ux = rd(x0 - (2 * px(t, 10) + t.textWidth(b.title())));
                }

                edges(ux, y0, x0, y1).radius(r, Corners.LEFT).color(col(cS1, fa)).render();
            }

            edges(x0, y0, x1, y1)
                    .radius(r, titled ? Corners.RIGHT : Corners.ALL)
                    .color(col(focused || b.mouseOver ? cS1 : cS0, fa))
                    .render();

            if (titled) edges(x0, y0, x0 + h1, y1).color(hair(0x60)).render();

            Color idle = light
                    ? b.mouseOver ? col(cO2, 0xD0) : col(cO1, 0xB0)
                    : b.mouseOver ? col(cO1, 0x80) : col(cO0, 0x60);

            edges(ux, y0, x1, y1)
                    .radius(r)
                    .color(CLEAR)
                    .outline(mix(idle, col(cAt, 255), fp), (float) h1)
                    .render();

            underline(ux, y0, x1, y1, r, lerp(h1, h2, fp), cAL, cAH, alpha(lerp(light ? 0xB0 : 0x50, 0xFF, fp)));
        }
        else if (fp > 0) {
            edges(x0, y0, x1, y1)
                    .radius(r)
                    .color(col(cAt, 255 * fp))
                    .clip(x0, y1 - h2, x1 - x0, h2)
                    .render();
        }

        double padding = b.padding();
        double overflow = b.overflow();
        double ty = rd(b.y + (b.height - T) / 2);

        renderer.scissorStart(b.x + padding, b.y, b.width - padding * 2, b.height);

        String value = b.textValue();
        if (!value.isEmpty()) {
            Color custom = b.customColor();
            b.drawText(renderer, b.x + padding - overflow, ty, value, custom != null ? custom : focused ? t.textColor() : sub1);
        }
        else if (b.placeholderValue() != null) {
            b.drawText(renderer, b.x + padding - overflow, ty, b.placeholderValue(), col(cSub0, light ? 0xD8 : 0xA0));
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
        edges(x0, y, x1, y + Math.round(h)).color(col(cAt, light ? 0x40 : 0x55)).render();
    }

    @Override
    public void paintCaret(WHontunTextBox b, GuiRenderer renderer, double x, double y, double h, double alpha) {
        HontunGuiTheme t = b.theme();
        double cx = rd(x);
        edges(cx, y, cx + h1(t), y + Math.round(h)).color(col(cAt, 255 * clamp01(alpha))).render();
    }

    @Override
    public void paintCompletions(WHontunTextBox b, GuiRenderer renderer, double x, double y, double w, double h) {
        popup(b.theme(), rd(x), rd(y), rd(x + w), rd(y + h), true);
    }

    @Override
    public void paintCompletionItem(WHontunTextBox.CompletionItem item, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme t = item.theme();

        if (item.isSelected() || item.mouseOver) {
            edges(item.x, item.y, item.x + item.width, item.y + item.height)
                    .radius(u(t))
                    .color(col(light ? cS2 : cS1, 0xC0))
                    .render();
        }

        item.drawLabel(renderer, mouseX, mouseY);
    }

    @Override
    public void paintSearchPanel(WHontunSearch s, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme t = s.theme();
        float R = rLarge(t);
        double x0 = rd(s.x), y0 = rd(s.y), x1 = rd(s.x + s.width), y1 = rd(s.y + s.height);

        if (t.effWindowShadow()) ring(x0, y0, x1, y1, R, halo(t), alpha((light ? 0x1C : 0x40) * t.windowOpacity()));

        edges(x0, y0, x1, y1)
                .radius(R)
                .color(CLEAR)
                .outline(col(cO0, light ? 0xC0 : 0x80), (float) h1(t))
                .render();
    }

    @Override
    public void paintSearchHeader(WHontunSearch.WHontunHeader h, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme t = h.theme();
        double h1 = h1(t);
        float R = rLarge(t);
        double T = t.textHeight();

        double x0 = rd(h.x), y0 = rd(h.y), x1 = rd(h.x + h.width), y1 = rd(h.y + h.height);
        float ri = (float) Math.max(0, R - h1);
        int fa = alpha(255 * Math.max(0.85, t.windowOpacity()));

        edges(x0 + h1, y0 + h1, x1 - h1, y1 - h1)
                .radii(ri, ri, 0, 0)
                .vgradient(col(cS1, fa), col(cS0, fa))
                .render();

        WWidget panel = h.parent != null ? h.parent : h;
        double px0 = rd(panel.x), py0 = rd(panel.y), px1 = rd(panel.x + panel.width), py1 = rd(panel.y + panel.height);

        edges(px0, py0, px1, Math.max(py1, y1))
                .radius(R)
                .hgradient(col(cAL, 255), col(cAH, 255))
                .clip(px0, py0, px1 - px0, h2(t))
                .render();

        edges(x0 + h1, y1 - h1, x1 - h1, y1).color(hair(0x70)).render();

        WGuiTexture searchIcon = null;
        WTextBox box = null;
        WHontunLabel esc = null;

        for (Cell<?> cell : h.cells) {
            if (!(cell.widget() instanceof WContainer row)) continue;

            for (Cell<?> inner : row.cells) {
                WWidget w = inner.widget();
                if (w instanceof WGuiTexture g) searchIcon = g;
                else if (w instanceof WTextBox tb) box = tb;
                else if (w instanceof WHontunLabel l) esc = l;
            }
        }

        boolean query = box != null && !box.get().isEmpty();
        if (searchIcon != null) searchIcon.color = query ? t.accentColor() : t.textSecondaryColor();

        if (esc != null) {
            esc.hidden = true;

            RichText label = RichText.of(ESC);
            double kh = Math.round(T) + px(t, 4);
            double kx1 = rd(esc.x + esc.width);
            double kx0 = rd(kx1 - (2 * px(t, 7) + t.textWidth(label)));
            double ky0 = rd(esc.y + (esc.height - kh) / 2);

            keycap(t, kx0, ky0, kx1, ky0 + kh, label, t.textColor(), col(cO1, light ? 0xD0 : 0x90));
        }
    }

    @Override
    public void paintSearchBody(WHontunSearch.WHontunResultsContainer c, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme t = c.theme();
        double h1 = h1(t);
        float ri = (float) Math.max(0, rLarge(t) - h1);
        int fa = alpha(0xF4 * t.windowOpacity());

        double x0 = rd(c.x), y0 = rd(c.y), x1 = rd(c.x + c.width), y1 = rd(c.y + c.height);

        edges(x0 + h1, y0, x1 - h1, y1 - h1)
                .radii(0, 0, ri, ri)
                .vgradient(col(cS0, fa), col(cBase, fa))
                .render();

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
        double cy = help != null ? help.y + help.height / 2 : (y0 + y1) / 2;

        text().text(message, rd(x0 + (x1 - x0 - mw) / 2), rd(cy - mh / 2), t.textSecondaryColor());
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
        float r = rSmall(t);
        double hp = FlatAnim.tween(row, HOVER, row.mouseOver, anim(AnimRole.MODULE_HOVER));
        double x0 = rd(row.x), y0 = rd(row.y), x1 = rd(row.x + row.width), y1 = rd(row.y + row.height);

        if (row.isPressed()) edges(x0, y0, x1, y1).radius(r).color(col(lightHover(cS2), light ? 0xB0 : 0xC0)).render();
        else if (hp > 0) edges(x0, y0, x1, y1).radius(r).color(col(lightHover(cS1), (light ? 0x70 : 0xA0) * hp)).render();

        leftBar(x0, y0, x1, y1, r, Corners.ALL, h3(t), cAH, cAL, row.isPressed() ? 1 : hp);
    }

    @Override
    public void paintChip(GuiRenderer renderer, double x, double y, double w, double h, Color c, ChipKind kind) {
        HontunGuiTheme t = theme();
        double inset = px(t, 4);

        edges(x + inset, y + inset, x + w - inset, y + h - inset)
                .radius(u(t))
                .color(light ? col(mix(cBase, packed(c), LIGHT_CHIP), 0xFF) : new Color(c.r, c.g, c.b, 0x30))
                .render();
    }

    @Override
    public void paintMultiSelectHeader(WHontunMultiSelect<?>.WHontunHeader h, GuiRenderer renderer, double mouseX, double mouseY) {
        WHontunMultiSelect<?> m = h.owner();
        HontunGuiTheme t = m.theme();
        double h1 = h1(t);
        double hp = FlatAnim.tween(h, HOVER, h.mouseOver, anim(AnimRole.HEADER_HOVER));
        boolean open = m.isExpanded() || m.animating();

        double x0 = rd(h.x), y0 = rd(h.y), x1 = rd(h.x + h.width), y1 = rd(h.y + h.height);
        float ri = (float) Math.max(0, rSmall(t) - h1);
        float rb = open ? 0 : ri;

        edges(x0 + h1, y0 + h1, x1 - h1, y1 - h1)
                .radii(ri, ri, rb, rb)
                .color(col(mix(cS0, cS1, hp), 0xE0 * t.backgroundOpacity()))
                .render();

        double p = clamp01(m.expandProgress());
        if (p > 0) edges(x0 + h1, y1 - h1, x1 - h1, y1).color(hair(alpha(0x50 * p))).render();
    }

    @Override
    public void paintMultiSelectBody(WHontunMultiSelect<?> m, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme t = m.theme();
        double bo = t.backgroundOpacity();
        double x0 = rd(m.x), y0 = rd(m.y), x1 = rd(m.x + m.width), y1 = rd(m.y + m.height);

        edges(x0, y0, x1, y1)
                .radius(rSmall(t))
                .color(light ? col(cS0, 0x80 * bo) : col(cMantle, 0x70 * bo))
                .outline(light ? col(cO1, 0xA0) : col(cO0, 0x60), (float) h1(t))
                .render();
    }

    @Override
    public void paintMultiSelectItem(WHontunMultiSelect<?>.WHontunItem i, GuiRenderer renderer, double mouseX, double mouseY) {
        if (!i.mouseOver || i.checkboxHovered()) return;

        HontunGuiTheme t = i.owner().theme();
        edges(i.x, i.y, i.x + i.width, i.y + i.height)
                .radius(u(t))
                .color(col(light ? cBase : cS1, 0xA0))
                .render();
    }

    @Override
    public void paintTooltip(WHontunTooltip tt, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme t = tt.theme();
        float r = rSmall(t);
        double x0 = rd(tt.x), y0 = rd(tt.y), x1 = rd(tt.x + tt.width), y1 = rd(tt.y + tt.height);

        ring(x0, y0, x1, y1, r, px(t, 2), light ? 0x18 : 0x40);

        edges(x0, y0, x1, y1)
                .radius(r)
                .color(col(light ? cBase : cS1, 0xF8))
                .outline(col(cO1, light ? 0xC0 : 0x90), (float) h1(t))
                .render();
    }

    @Override
    public void paintSwatchChip(WHontunSwatchLabel l, GuiRenderer renderer, double mouseX, double mouseY) {
        Color color = l.color;
        if (color == null || l.get().isEmpty()) return;

        HontunGuiTheme t = l.theme();
        int rgb = packed(color);
        if (HontunLightPalette.contrast(rgb, packed(t.mantleColor())) >= WHontunSwatchLabel.MIN_CONTRAST) return;

        Color chip = HontunLightPalette.luminance(rgb) > 0.4 ? new Color(22, 24, 28) : new Color(244, 245, 247);
        double pad = px(t, 2);

        edges(l.x - pad, l.y - pad / 2, l.x + l.width + pad, l.y + l.height + pad / 2)
                .radius(u(t))
                .color(chip)
                .render();
    }

    @Override
    public void paintCountChip(HontunSettingsWidgetFactory.WSelectedCountLabel l, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme t = l.theme();
        double ph = Math.round(0.85 * t.textHeight());
        double x0 = rd(l.x), x1 = rd(l.x + l.width);
        double y0 = rd(l.y + (l.height - ph) / 2);

        edges(x0, y0, x1, y0 + ph)
                .radius(ph / 2)
                .color(col(cRawA, light ? 0x22 : 0x2A))
                .render();

        RichText label = RichText.of(l.label().getPlainText()).scale(SMALL);
        double tw = t.textWidth(label);
        double th = t.textHeight(label);

        text().text(label, rd(x0 + (x1 - x0 - tw) / 2), rd(y0 + (ph - th) / 2), t.accentColor());
    }

    @Override
    public void paintMeteorBackground(WWidget w, GuiRenderer renderer, boolean pressed, boolean hovered) {
        face(theme(), rd(w.x), rd(w.y), rd(w.x + w.width), rd(w.y + w.height), hovered ? 1 : 0, pressed);
    }

    @Override
    public void paintMeteorBackground(WWidget w, GuiRenderer renderer, Color outlineColor, Color backgroundColor) {
        face(theme(), rd(w.x), rd(w.y), rd(w.x + w.width), rd(w.y + w.height), w.mouseOver ? 1 : 0, false);
    }

    @Override
    public void paintSnapGrid(GuiGraphicsExtractor g, int gridSize) {
        if (gridSize <= 0) return;

        int windowWidth = Utils.getWindowWidth();
        int windowHeight = Utils.getWindowHeight();
        float guiScale = (float) mc.getWindow().getGuiScale();
        int dot = Math.max(2, Math.round(guiScale));
        int rim = Math.max(1, Math.round(guiScale / 2f));
        int half = dot / 2;

        int step = gridSize;
        while ((long) (windowWidth / step + 1) * (windowHeight / step + 1) > GRID_DOT_LIMIT) step += gridSize;

        int core = light ? col(cA, 0xA8).getPacked() : col(mix(cO2, cAH, 0.3), 0xA0).getPacked();
        int halo = light ? col(0xFFFFFF, 0x70).getPacked() : col(cCrust, 0x60).getPacked();

        Matrix3x2fStack pose = g.pose();
        pose.pushMatrix();
        pose.scale(1f / guiScale, 1f / guiScale);

        cz.honzasik.hontun.gui.render.pixel.QuadBatchState.Builder batch = cz.honzasik.hontun.gui.render.pixel.QuadBatchState.builder();
        for (int x = 0; x <= windowWidth; x += step) {
            for (int y = 0; y <= windowHeight; y += step) {
                int x0 = x - half, y0 = y - half;
                batch.add(x0 - rim, y0 - rim, x0 + dot + rim, y0 + dot + rim, halo);
                batch.add(x0, y0, x0 + dot, y0 + dot, core);
            }
        }
        batch.submit(g);

        pose.popMatrix();
    }
}
