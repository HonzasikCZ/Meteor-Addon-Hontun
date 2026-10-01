package cz.honzasik.hontun.gui.theme.style.chamfer;

import cz.honzasik.hontun.gui.api.animation.Easing;
import cz.honzasik.hontun.gui.api.text.RichText;
import cz.honzasik.hontun.gui.api.text.TextScale;
import cz.honzasik.hontun.gui.render.HontunRenderer;
import cz.honzasik.hontun.gui.theme.HontunGuiTheme;
import cz.honzasik.hontun.gui.theme.HontunLightPalette;
import cz.honzasik.hontun.gui.theme.HontunSettingsWidgetFactory;
import cz.honzasik.hontun.gui.theme.icons.HontunBuiltinIcons;
import cz.honzasik.hontun.gui.theme.style.AnimRole;
import cz.honzasik.hontun.gui.theme.style.AnimSpec;
import cz.honzasik.hontun.gui.theme.style.ChipKind;
import cz.honzasik.hontun.gui.theme.style.ClickStyle;
import cz.honzasik.hontun.gui.theme.style.Knob;
import cz.honzasik.hontun.gui.theme.style.Metrics;
import cz.honzasik.hontun.gui.theme.style.Pipeline;
import cz.honzasik.hontun.gui.theme.style.StyleAnimation;
import cz.honzasik.hontun.gui.theme.style.StylePalette;
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
import cz.honzasik.hontun.gui.util.ColorUtils;
import cz.honzasik.hontun.gui.widget.HontunIcons;
import cz.honzasik.hontun.gui.widget.HontunShapes;
import cz.honzasik.hontun.gui.widget.WGuiTexture;
import cz.honzasik.hontun.utils.HontunTheme;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.renderer.packer.GuiTexture;
import meteordevelopment.meteorclient.gui.tabs.Tab;
import meteordevelopment.meteorclient.gui.utils.Cell;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.joml.Matrix3x2fStack;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.WeakHashMap;

import static cz.honzasik.hontun.gui.theme.style.chamfer.ChamferPaint.alpha;
import static cz.honzasik.hontun.gui.theme.style.chamfer.ChamferPaint.argb;
import static cz.honzasik.hontun.gui.theme.style.chamfer.ChamferPaint.backdropGlow;
import static cz.honzasik.hontun.gui.theme.style.chamfer.ChamferPaint.brackets;
import static cz.honzasik.hontun.gui.theme.style.chamfer.ChamferPaint.cutFor;
import static cz.honzasik.hontun.gui.theme.style.chamfer.ChamferPaint.divider;
import static cz.honzasik.hontun.gui.theme.style.chamfer.ChamferPaint.edgeText;
import static cz.honzasik.hontun.gui.theme.style.chamfer.ChamferPaint.frame;
import static cz.honzasik.hontun.gui.theme.style.chamfer.ChamferPaint.glow;
import static cz.honzasik.hontun.gui.theme.style.chamfer.ChamferPaint.ink;
import static cz.honzasik.hontun.gui.theme.style.chamfer.ChamferPaint.lerpArgb;
import static cz.honzasik.hontun.gui.theme.style.chamfer.ChamferPaint.lerpColor;
import static cz.honzasik.hontun.gui.theme.style.chamfer.ChamferPaint.mix;
import static cz.honzasik.hontun.gui.theme.style.chamfer.ChamferPaint.mul;
import static cz.honzasik.hontun.gui.theme.style.chamfer.ChamferPaint.plate;
import static cz.honzasik.hontun.gui.theme.style.chamfer.ChamferPaint.px;
import static cz.honzasik.hontun.gui.theme.style.chamfer.ChamferPaint.q4;
import static cz.honzasik.hontun.gui.theme.style.chamfer.ChamferPaint.rect;
import static cz.honzasik.hontun.gui.theme.style.chamfer.ChamferPaint.rim;
import static cz.honzasik.hontun.gui.theme.style.chamfer.ChamferPaint.s;
import static cz.honzasik.hontun.gui.theme.style.chamfer.ChamferPaint.scan;
import static cz.honzasik.hontun.gui.theme.style.chamfer.ChamferPaint.subtext1;
import static cz.honzasik.hontun.gui.theme.style.chamfer.ChamferPaint.drawText;
import static cz.honzasik.hontun.gui.theme.style.chamfer.ChamferPaint.u;
import static cz.honzasik.hontun.gui.theme.style.chamfer.ChamferPaint.upper;
import static meteordevelopment.meteorclient.MeteorClient.mc;

public class ChamferClickStyle extends ClickStyle {
    private static final Color TEXT_SHADOW = new Color(0, 0, 0, 0xC0);
    private static final AnimSpec WINDOW_LOCK = AnimSpec.of(Easing.BACK_OUT, 180, 180);

    private static final int ACTIVE_IN = 280;
    private static final int ACTIVE_OUT = 160;
    private static final int BLINK = 530;
    private static final double LIGHT_LO_CONTRAST = 3.5;
    private static final long HELP_SHADOW_NS = 5_000_000L;

    private static final String ESC_HINT = "ESC to close";
    private static final RichText HINT = RichText.of("[ ESC ] CLOSE");
    private static final RichText HINT_OPEN = RichText.of("[ ");
    private static final RichText HINT_KEY = RichText.of("ESC");
    private static final RichText HINT_CLOSE = RichText.of(" ]");
    private static final RichText HINT_TAIL = RichText.of(" CLOSE");
    private static final RichText NO_MATCHES = RichText.of("NO MATCHES");

    private static final String LISTEN = "PRESS A KEY";
    private static final RichText LISTEN_TEXT = RichText.of(LISTEN);
    private static final RichText LISTEN_GAP = RichText.of(LISTEN + " ");
    private static final RichText CURSOR = RichText.of("_");
    private static final RichText SPACE = RichText.of(" ");
    private static final RichText OPEN = RichText.of("[ ");
    private static final RichText CLOSE = RichText.of(" ]");

    private final Map<WHontunWindow, WindowState> windows = new WeakHashMap<>();
    private final Map<WHontunTextBox, long[]> carets = new WeakHashMap<>();
    private final Map<Tab, long[]> tabLock = new HashMap<>();
    private final Map<Tab, long[]> tabHover = new HashMap<>();
    private final Map<String, RichText> smallTitles = new HashMap<>();

    private Boolean legibility;
    private boolean listenerAdded;
    private int helpShadows;
    private long helpShadowUntil;

    @Override
    public HontunTheme.UiMode id() {
        return HontunTheme.UiMode.HModern2;
    }

    @Override
    protected Metrics createMetrics() {
        Metrics m = new Metrics();
        m.windowMinWidth = 200;
        m.windowPad = 4;
        m.headerPadH = 10;
        m.headerPadV = 7;
        m.headerSpacing = 6;
        m.topBarMarginTop = 8;
        m.topBarItemPad = 4;
        m.tabPadH = 10;
        m.tabPadV = 6;
        m.tabIconGap = 4;
        m.rowPadH = 8;
        m.rowPadV = 4;
        m.sectionHeaderPadH = 6;
        m.sectionHeaderPadV = 4;
        m.tooltipPadH = 8;
        m.tooltipPadV = 4;
        m.moduleScreenPad = 8;
        m.moduleInfoSpacing = 6;
        return m;
    }

    @Override
    public Pipeline pipeline() {
        return Pipeline.QUADS;
    }

    @Override
    public double radiusFactor() {
        return 0;
    }

    @Override
    public boolean uses(Knob knob) {
        return knob != Knob.CORNER_RADIUS;
    }

    @Override
    public double windowAlpha(double raw, boolean light) {
        return light ? 0.7 + 0.3 * raw : Math.max(0.72, raw);
    }

    @Override
    public double controlAlpha(double raw, boolean light) {
        return light ? 0.7 + 0.3 * raw : Math.max(0.6, raw);
    }

    @Override
    public double chromeMargin(HontunGuiTheme t) {
        int u = u(t);
        int window = 3 * u + (windowShadow(t) ? 2 * u : 0);
        int chip = (int) Math.ceil((t.textHeight() + 4 * u) / 2.0) + 2 * u;
        return Math.max(window, chip);
    }

    @Override
    public boolean legibilityShadow(HontunGuiTheme t) {
        ensureListener();
        if (t.light()) return consumeHelpShadow();

        double o = t.windowOpacity.get();
        if (legibility == null) legibility = o < 0.625;
        else if (o < 0.60) legibility = true;
        else if (o > 0.65) legibility = false;

        return legibility;
    }

    @Override
    public Color textShadowColor() {
        return TEXT_SHADOW;
    }

    @Override
    public double textShadowOffset(double fontScale, double renderScale) {
        return Math.max(1, Math.round(theme().scale(1)));
    }

    @Override
    public AnimSpec anim(AnimRole role) {
        return switch (role) {
            case MODULE_ACTIVE -> AnimSpec.of(Easing.LINEAR, ACTIVE_IN, ACTIVE_OUT);
            case MODULE_HOVER -> AnimSpec.stepped(Easing.LINEAR, 120, 80, 4);
            case TAB_SELECT -> AnimSpec.stepped(Easing.LINEAR, 60, 60, 3);
            case BUTTON_HOVER, DROPDOWN_HOVER, HEADER_HOVER -> AnimSpec.stepped(Easing.QUAD_OUT, 150, 100, 4);
            case CHECKBOX -> AnimSpec.of(Easing.QUAD_OUT, 140, 100);
            case WINDOW_CORNER, SECTION_CORNER -> AnimSpec.stepped(Easing.LINEAR, 120, 120, 4);
            default -> super.anim(role);
        };
    }

    @Override
    public void palette(StylePalette out, boolean light, HontunGuiTheme t) {
        super.palette(out, light, t);

        if (light) {
            out.accentHi = HontunTheme.darken(out.accent, 0.78f) & 0xFFFFFF;
            out.accentLo = lightAccentLo(out, t);
            return;
        }

        float[] hsb = java.awt.Color.RGBtoHSB((out.accentLo >> 16) & 0xFF, (out.accentLo >> 8) & 0xFF, out.accentLo & 0xFF, null);
        if (hsb[2] < 0.35f) out.accentLo = java.awt.Color.HSBtoRGB(hsb[0], hsb[1], 0.35f) & 0xFFFFFF;
    }

    private int lightAccentLo(StylePalette out, HontunGuiTheme t) {
        int body = HontunLightPalette.body(HontunTheme.ramp(id()), HontunLightPalette.ramp(id()), windowAlpha(t.windowOpacity.get(), true));
        for (int i = 9; i > 0; i--) {
            int c = HontunTheme.lerp(out.accent, out.surface0, i * 0.05f) & 0xFFFFFF;
            if (HontunLightPalette.contrast(c, body) >= LIGHT_LO_CONTRAST) return c;
        }
        return out.accent & 0xFFFFFF;
    }

    @Override
    public WWidget headerLabel(HontunGuiTheme t, WHontunWindow w, String title) {
        WWidget label = super.headerLabel(t, w, title);
        if (w.isDialog() && label instanceof WHontunLabel l) l.hidden = true;
        return label;
    }

    @Override
    public WWidget headerTrailing(HontunGuiTheme t, WHontunWindow w) {
        if (w.isDialog()) return null;

        Category category = category(w.titleText());
        if (category == null) return null;

        WHontunReadout readout = new WHontunReadout(category);
        readout.theme = t;
        return readout;
    }

    @Override
    public String windowTitle(String s) {
        return s == null ? null : s.toUpperCase(Locale.ROOT);
    }

    @Override
    public WWidget sectionTitle(HontunGuiTheme t, WHontunSection s, String title) {
        WChamferLegend legend = new WChamferLegend(title);
        legend.theme = t;
        return legend;
    }

    @Override
    public RichText keybindLabel(HontunGuiTheme t, WHontunKeybind k, boolean listening, String key) {
        if (listening) return RichText.of(LISTEN + " _");

        return RichText.of(k.title().toUpperCase(Locale.ROOT)).scale(TextScale.SMALL.get())
                .append(" ")
                .append("[ ")
                .append(keyText(k, key))
                .append(" ]");
    }

    @Override
    public Color helpKeyColor(HontunGuiTheme t) {
        if (t.light()) return backdropKey(HontunTheme.userAccentHi());
        return t.accentHiColor();
    }

    @Override
    public Color helpTextColor(HontunGuiTheme t) {
        if (t.light()) {
            int text = HontunTheme.ramp(id()).textDim();
            armHelpShadow(t, (HontunTheme.userAccentHi() & 0xFFFFFF) == (text & 0xFFFFFF) ? 1 : 2);
            return backdropText(text);
        }
        return t.textSecondaryColor();
    }

    private void armHelpShadow(HontunGuiTheme t, int draws) {
        helpShadows = t.richText() ? draws : 0;
        helpShadowUntil = System.nanoTime() + HELP_SHADOW_NS;
    }

    private boolean consumeHelpShadow() {
        if (helpShadows <= 0) return false;
        helpShadows--;
        return System.nanoTime() <= helpShadowUntil;
    }

    @Override
    public Color favoriteColor(WHontunFavorite f) {
        HontunGuiTheme t = f.theme();
        if (f.checked) return t.accentHiColor();
        return f.mouseOver ? t.accentColor() : t.textSecondaryColor();
    }

    @Override
    public void tabSize(WHontunTopBar.WTopBarButton b, double[] out) {
        HontunGuiTheme t = b.bar().theme();
        Metrics mt = metrics();
        double padH = t.scale(mt.tabPadH);
        double padV = t.scale(mt.tabPadV);
        double iconWidth = b.hasIcon() ? b.iconSize() + t.scale(mt.tabIconGap) : 0;
        out[0] = padH + iconWidth + t.textWidth(upper(b.text().getPlainText(), false)) + padH;
        out[1] = padV + t.textHeight() + padV;
    }

    @Override
    public void checkboxSize(WHontunCheckbox c, double[] out) {
        HontunGuiTheme t = c.theme();
        int side = 7 * checkCell(c, t, u(t));
        out[0] = side;
        out[1] = side;
    }

    @Override
    public void dropdownSize(WHontunDropdown<?> d, double[] out) {
        HontunGuiTheme t = d.theme();
        int u = u(t);
        double pad = d.pad();
        double th = t.textHeight();
        double titleWidth = t.textWidth(d.titleText());
        double titleCol = titleWidth > 0 ? pad + titleWidth + pad : pad;
        double valueCol = 5 * u + d.maxValueWidth() + pad;
        double arrowCol = pad + th + pad;
        out[0] = titleCol + valueCol + arrowCol;
        out[1] = pad + th + pad;
    }

    @Override
    public double sliderHeight(WHontunSlider s) {
        return 12 * u(s.theme());
    }

    @Override
    public double scrollbarWidth(HontunGuiTheme t) {
        return 3 * u(t);
    }

    @Override
    public double popupOffset(HontunGuiTheme t) {
        return s(t, 3);
    }

    @Override
    public void paintWindowBack(WHontunWindow w, GuiRenderer renderer, double mouseX, double mouseY) {
        ensureListener();

        HontunGuiTheme t = w.theme();
        GuiFill g = GuiFill.of(renderer);
        int u = u(t);
        Geo geo = geo(w, u);
        WindowState st = state(w);
        boolean dialog = w.isDialog();
        int cut = (dialog ? 6 : 5) * u;

        boolean shadow = t.effWindowShadow();
        int rings = shadow ? 4 : 3;
        int base = dialog ? 0x50 : w.isDragging() ? 0x80 : mix(0x38, 0x50, st.hover.getProgress());

        backdropGlow(g, t, geo.x0, geo.y0, geo.x1, geo.by, cut, cut, u, rings, base);
        if (shadow) ChamferPaint.shadowRings(g, t, geo.x0, geo.y0, geo.x1, geo.by, cut, cut, u, rings);

        int wa = alpha(t.windowOpacity());
        if (dialog) {
            plate(g, geo.x0, geo.y0, geo.x1, geo.by, cut, cut, u, argb(wa, t.baseColor()));
            tintActiveLabel(w, t);
        } else if (geo.body) {
            plate(g, geo.x0, geo.hy, geo.x1, geo.by, 0, cut, u, argb(wa, t.mantleColor()));
        }
    }

    @Override
    public void paintWindowHeader(WHontunWindow w, WHontunWindow.WHontunHeader h, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme t = w.theme();
        GuiFill g = GuiFill.of(renderer);
        int u = u(t);
        Geo geo = geo(w, u);
        WindowState st = state(w);
        boolean dialog = w.isDialog();
        int cut = (dialog ? 6 : 5) * u;

        if (!dialog) {
            int headerA = Math.max(0xC8, mul(0xF4, t.windowOpacity()));
            plate(g, geo.x0, geo.y0, geo.x1, geo.hy, cut, geo.body ? 0 : cut, u, argb(headerA, t.crustColor()));
        }

        if (geo.body) divider(g, t, geo.x0 + u, geo.x1 - u, geo.hy - u, u);

        int frame;
        if (dialog) frame = argb(0xC0, t.accentColor());
        else if (w.isDragging()) frame = argb(0xFF, t.accentHiColor());
        else frame = lerpArgb(ink(t, 0xC0), t.accentLoColor(), 0xFF, t.accentColor(), st.hover.getProgress());
        frame(g, geo.x0, geo.y0, geo.x1, geo.by, cut, cut, u, frame);

        int len = dialog ? 5 * u : u * (int) Math.round(4 + 2 * Math.max(0, st.lock.getProgress()));
        brackets(g, geo.x0, geo.y0, geo.x1, geo.by, len, u, ChamferPaint.backdropBracket(t));

        if (w.expandAnimating()) scan(g, t, geo.x0 + u, geo.x1 - cut - u, geo.by - 2 * u, u, w.expandProgress());

        if (!dialog && w.icon instanceof WGuiTexture tex) tex.color = geo.body ? t.accentHiColor() : subtext1(t);

        if (dialog) paintTitleChip(w, h, t, g, u, geo, st);
    }

    private void paintTitleChip(WHontunWindow w, WHontunWindow.WHontunHeader h, HontunGuiTheme t, GuiFill g, int u, Geo geo, WindowState st) {
        RichText title = h.titleWidget() instanceof WHontunLabel l ? l.richText() : upper(w.titleText(), true);
        if (title.getPlainText().isEmpty()) return;

        int chipH = px(t.textHeight()) + 4 * u;
        WWidget icon = w.icon;
        WWidget triangle = h.openIndicator();

        double left = (icon != null && icon.width > 0 ? icon.x + icon.width : geo.x0 + s(t, 10)) + 2 * u;
        double right = (triangle != null ? triangle.x : geo.x1 - s(t, 10)) - 2 * u;

        RichText shown = fit(st, t, title, (int) Math.floor(right - left) - 16 * u);
        int chipW = px(t.textWidth(shown)) + 16 * u;

        double chipX = (geo.x0 + geo.x1) / 2.0 - chipW / 2.0;
        if (chipX + chipW > right) chipX = right - chipW;
        if (chipX < left) chipX = left;

        int x0 = px(chipX);
        int y0 = geo.y0 - chipH / 2;
        int x1 = x0 + chipW;
        int y1 = y0 + chipH;
        int cut = 4 * u;

        glow(g, t, x0, y0, x1, y1, cut, cut, u, 2, 0x50);
        plate(g, x0, y0, x1, y1, cut, cut, u, argb(0xFF, t.crustColor()));
        frame(g, x0, y0, x1, y1, cut, cut, u, argb(0xFF, t.accentColor()));

        int mid = y0 + chipH / 2 - u / 2;
        int dash = argb(0xFF, t.accentHiColor());
        rect(g, x0 + 3 * u, mid, x0 + 6 * u, mid + u, dash);
        rect(g, x1 - 6 * u, mid, x1 - 3 * u, mid + u, dash);

        drawText(shown, x0 + 8 * u, y0 + 2 * u, t.textColor());
    }

    private static RichText fit(WindowState st, HontunGuiTheme t, RichText title, int avail) {
        String source = title.getPlainText();
        if (st.chipText != null && avail == st.chipAvail && source.equals(st.chipSource)) return st.chipText;

        st.chipAvail = avail;
        st.chipSource = source;

        RichText result = title;
        if (avail > 0 && t.textWidth(title) > avail) {
            String cut = source;
            RichText candidate = RichText.bold("...");
            while (!cut.isEmpty()) {
                cut = cut.substring(0, cut.length() - 1);
                candidate = RichText.bold(cut.stripTrailing() + "...");
                if (t.textWidth(candidate) <= avail) break;
            }
            result = candidate;
        }

        st.chipText = result;
        return result;
    }

    private static void tintActiveLabel(WHontunWindow w, HontunGuiTheme t) {
        for (Cell<?> cell : w.cells) {
            WWidget widget = cell.widget();
            if (widget == w.view || widget == w.headerWidget()) continue;
            if (!(widget instanceof WContainer row) || row.cells.size() < 2) continue;
            if (!(row.cells.get(0).widget() instanceof WHontunCheckbox box)) continue;
            if (!(row.cells.get(1).widget() instanceof WHontunLabel label)) continue;

            String plain = label.richText().getPlainText();
            String up = plain.toUpperCase(Locale.ROOT);
            if (!plain.equals(up)) label.set(RichText.of(up));

            label.color = box.checked ? t.accentHiColor() : subtext1(t);
        }
    }

    @Override
    public void paintTopBar(WHontunTopBar b, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme t = b.theme();
        GuiFill g = GuiFill.of(renderer);
        int u = u(t);
        int x0 = px(b.x), y0 = px(b.y), x1 = px(b.x + b.width), y1 = px(b.y + b.height);
        int cut = 4 * u;

        backdropGlow(g, t, x0, y0, x1, y1, cut, cut, u, 2, 0x40);
        plate(g, x0, y0, x1, y1, cut, cut, u, argb(0xF0, t.crustColor()));
        frame(g, x0, y0, x1, y1, cut, cut, u, argb(ink(t, 0xA0), t.accentLoColor()));
        brackets(g, x0, y0, x1, y1, 3 * u, u, ChamferPaint.backdropBracket(t));

        int line = argb(ink(t, 0x60), t.overlay0Color());
        for (int i = 1; i < b.cells.size(); i++) {
            WWidget prev = b.cells.get(i - 1).widget();
            WWidget next = b.cells.get(i).widget();
            int dx = px((prev.x + prev.width + next.x) / 2) - u / 2;
            rect(g, dx, px(next.y) + 2 * u, dx + u, px(next.y + next.height) - 2 * u, line);
        }
    }

    @Override
    public void paintTab(WHontunTopBar.WTopBarButton b, GuiRenderer renderer, double mouseX, double mouseY) {
        ensureListener();

        HontunGuiTheme t = b.bar().theme();
        GuiFill g = GuiFill.of(renderer);
        Metrics mt = metrics();
        int u = u(t);
        int x0 = px(b.x), y0 = px(b.y), x1 = px(b.x + b.width), y1 = px(b.y + b.height);
        int cut = 3 * u;
        long now = System.currentTimeMillis();

        Tab tab = b.tab();
        boolean selected = b.isSelected();
        boolean hover = b.mouseOver;
        boolean pressed = b.isPressed();

        long lock = now;
        if (selected) lock = stamp(tabLock, tab, now);
        else tabLock.remove(tab);

        long hoverStart = now;
        if (hover) hoverStart = stamp(tabHover, tab, now);
        else tabHover.remove(tab);

        if (selected) {
            plate(g, x0, y0, x1, y1, cut, cut, u, argb(pressed ? 0x40 : 0x2A, t.accentColor()));
            frame(g, x0, y0, x1, y1, cut, cut, u, argb(0xFF, t.accentColor()));

            int steps = (int) Math.min(3, (now - lock) / 20);
            int off = (3 - steps) * u;
            brackets(g, x0 - off, y0 - off, x1 + off, y1 + off, 3 * u, u, argb(0xFF, t.accentHiColor()));
        } else {
            if (pressed) plate(g, x0, y0, x1, y1, cut, cut, u, argb(0x40, t.accentColor()));

            if (hover) {
                double hp = q4(Math.min(1, (now - hoverStart) / 120.0));
                if (hp > 0) brackets(g, x0, y0, x1, y1, 2 * u, u, argb(alpha(hp), t.accentHiColor()));
            }
        }

        Color color = selected ? t.accentHiColor() : hover ? t.textColor() : subtext1(t);
        RichText label = upper(b.text().getPlainText(), false);

        double textWidth = t.textWidth(label);
        double gap = t.scale(mt.tabIconGap);
        double iconSize = b.iconSize();
        double contentWidth = textWidth + (b.hasIcon() ? iconSize + gap : 0);

        double offsetX = b.width / 2 - contentWidth / 2;
        double offsetY = b.height / 2 - t.textHeight() / 2;

        if (b.hasIcon()) {
            double iconY = b.height / 2 - iconSize / 2;
            renderer.quad(Math.round(b.x + offsetX), Math.round(b.y + iconY), iconSize, iconSize, b.icon(), color);
            offsetX += iconSize + gap;
        }

        drawText(label, b.x + offsetX, b.y + offsetY, color);
    }

    @Override
    public void paintModule(WHontunModule m, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme t = m.theme();
        GuiFill g = GuiFill.of(renderer);
        Metrics mt = metrics();
        int u = u(t);
        boolean light = t.light();
        int x0 = px(m.x), y0 = px(m.y), x1 = px(m.x + m.width), y1 = px(m.y + m.height);
        int lim = Math.min(x1 - x0, y1 - y0) / 2;
        int c3 = Math.max(u, Math.min(6 * u, lim / u * u));

        boolean active = m.module().isActive();
        boolean prev = m.isPrevActive();
        boolean next = m.isNextActive();
        boolean pressed = m.isPressed();
        double hq = m.hoverProgress();
        double p = m.highlightProgress();

        double ap;
        double chrome;
        if (active) {
            double ms = p * ACTIVE_IN;
            if (p >= 1) {
                ap = 1;
                chrome = 1;
            } else if (ms < 30) {
                ap = 0;
                chrome = 1;
            } else if (ms < 60) {
                ap = 0;
                chrome = 0.35;
            } else {
                double k = Math.clamp((ms - 60) / (ACTIVE_IN - 60.0), 0, 1);
                ap = 1 - Math.pow(1 - k, 4);
                chrome = 1;
            }
        } else {
            ap = p * p;
            chrome = q4(p);
        }

        boolean on = active || p > 0;

        if (!active && hq > 0 && !pressed) {
            plate(g, x0, y0, x1, y1, c3, c3, u, argb(mul(0x50, hq), t.surface0Color()));
            frame(g, x0, y0, x1, y1, c3, c3, u, argb(mul(ink(t, 0xC0), hq), t.accentLoColor()));
        }

        int cutTL = on && prev ? 0 : c3;
        int cutBR = on && next ? 0 : c3;

        if (pressed) {
            int inset = Math.max(0, c3 - u);
            plate(g, x0 + u, y0 + u, x1 - u, y1 - u, cutTL == 0 ? 0 : inset, cutBR == 0 ? 0 : inset, u,
                    light ? argb(0xB0, t.overlay1Color()) : argb(0xF0, t.surface2Color()));
        }

        if (on && chrome > 0) {
            Color accent = t.accentColor();
            plate(g, x0, y0, x1, y1, cutTL, cutBR, u, argb(mul(light ? 0x18 : 0x10, chrome), accent));
            HontunShapes.outlineRun(g, x0, y0, x1 - x0, y1 - y0, cutTL, cutBR, u, !prev, !next, argb(mul(0xFF, chrome), accent));
            if (prev) rect(g, x0 + 6 * u, y0, x1 - u, y0 + u, argb(mul(0x30, chrome), accent));

            int cx0 = x0 + 2 * u;
            boolean slant = cutTL > 0;
            int top = slant ? y0 + 3 * u : y0 + u;
            int straightTop = slant ? y0 + cutTL + u : top;
            int bottom = next ? y1 : y1 - 3 * u;
            int cellH = bottom - top;
            if (cellH > 0 && ap > 0) {
                int visible = ap >= 1 ? cellH : (int) Math.floor(ap * cellH / u) * u;
                int visibleTop = bottom - visible;
                int hi = argb(mul(0xFF, chrome), t.accentHiColor());
                int lo = argb(mul(0xFF, chrome), accent);
                if (slant) {
                    for (int i = 2 * u; i < cutTL; i += u) {
                        int cy = y0 + i + u;
                        if (cy < visibleTop) continue;
                        int lx = x0 + (cutTL - i) + 2 * u;
                        rect(g, lx, cy, lx + 3 * u, cy + u, hi);
                    }
                }
                int from = Math.max(straightTop, visibleTop);
                if (bottom > from) g.gradient(cx0, from, cx0 + 3 * u, bottom, hi, lo);
            }

            if (active && m.mouseOver) brackets(g, x0 + u, y0 + u, x1 - u, y1 - u, 2 * u, u, argb(0xFF, t.accentHiColor()));
        }

        Color off = lerpColor(subtext1(t), t.textColor(), active ? 0 : hq);
        Color onColor = t.accentHiColor();
        Color color = active ? onColor : lerpColor(off, onColor, ap);

        double titleWidth = m.titleWidth();
        double pad = t.scale(mt.rowPadH);
        double x = switch (t.moduleAlignment.get()) {
            case Center -> m.x + m.width / 2 - titleWidth / 2;
            case Right -> m.x + m.width - titleWidth - pad * 2;
            default -> m.x + 5 * u + s(t, 8);
        };

        drawText(m.title(), x, m.y + t.scale(mt.rowPadV) + (pressed ? 1 : 0), color);
    }

    @Override
    public void paintSectionBody(WHontunSection s, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme t = s.theme();
        GuiFill g = GuiFill.of(renderer);
        int u = u(t);
        int x0 = px(s.x), x1 = px(s.x + s.width);
        int yc = px(s.y + s.headerWidget().height / 2) - u / 2;
        int by = px(s.y + s.height);

        int a = mul(t.light() ? 0x80 : 0x3C, t.backgroundOpacity());
        plate(g, x0, yc, x1, by, 3 * u, 3 * u, u, argb(a, t.surface0Color()));
    }

    @Override
    public void paintSectionHeader(WHontunSection s, WHontunSection.WHontunHeader h, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme t = s.theme();
        GuiFill g = GuiFill.of(renderer);
        int u = u(t);
        int x0 = px(s.x), x1 = px(s.x + s.width);
        int yc = px(s.y + h.height / 2) - u / 2;
        int by = px(s.y + s.height);

        int[] gaps = new int[4];
        int count = 0;

        if (h.titleWidget() instanceof WChamferLegend legend && !legend.text().getPlainText().isEmpty()) {
            gaps[count++] = legend.gapLeft();
            gaps[count++] = legend.gapRight();
        }

        WWidget indicator = h.openIndicator() != null ? h.openIndicator() : s.customHeaderWidget();
        if (indicator != null) {
            int a = px(indicator.x) - 2 * u;
            int b = px(indicator.x + indicator.width) + 2 * u;
            if (count == 2 && a < gaps[0]) {
                gaps[2] = gaps[0];
                gaps[3] = gaps[1];
                gaps[0] = a;
                gaps[1] = b;
            } else {
                gaps[count] = a;
                gaps[count + 1] = b;
            }
            count += 2;
        }

        int[] used = count == 4 ? gaps : java.util.Arrays.copyOf(gaps, count);
        int argb = h.mouseOver ? argb(ink(t, 0xC0), t.accentLoColor()) : argb(ink(t, 0x70), t.overlay0Color());
        HontunShapes.groupBox(g, x0, yc, x1 - x0, by - yc, 3 * u, u, used, argb);

        if (s.expandAnimating()) scan(g, t, x0 + u, x1 - 4 * u, by - 2 * u, u, s.expandProgress());
    }

    @Override
    public void paintSeparatorH(WHontunHorizontalSeparator s, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme t = s.theme();
        GuiFill g = GuiFill.of(renderer);
        int u = u(t);
        int x0 = px(s.x), x1 = px(s.x + s.width);
        int rule = argb(ink(t, 0x60), t.overlay0Color());

        if (!s.hasText()) {
            int ly = px(s.y) + s(t, 2);
            int head = Math.min(x1, x0 + 4 * u);
            rect(g, x0, ly, head, ly + u, argb(ink(t, 0xC0), t.accentLoColor()));
            rect(g, head, ly, x1, ly + u, rule);
            return;
        }

        RichText title = upper(s.titleString(), true);
        double tw = t.textWidth(title);
        int tx0 = px(s.x + s.width / 2 - tw / 2);
        int tx1 = tx0 + px(tw);
        int mid = px(s.y + s.height / 2) - u / 2;
        int dash = argb(0xFF, t.accentHiColor());

        rect(g, tx0 - 5 * u, mid, tx0 - 2 * u, mid + u, dash);
        rect(g, tx1 + 2 * u, mid, tx1 + 5 * u, mid + u, dash);
        rect(g, x0, mid, tx0 - 7 * u, mid + u, rule);
        rect(g, tx1 + 7 * u, mid, x1, mid + u, rule);

        drawText(title, tx0, s.y + s.height / 2 - t.textHeight(title) / 2, t.accentHiColor());
    }

    @Override
    public void paintSeparatorV(WHontunVerticalSeparator s, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme t = s.theme();
        int u = u(t);
        int x = px(s.x + s.width / 2) - u / 2;
        rect(GuiFill.of(renderer), x, px(s.y), x + u, px(s.y + s.height), argb(ink(t, 0x50), t.overlay0Color()));
    }

    @Override
    public void paintScrollbar(WHontunView v, GuiRenderer renderer, double mouseX, double mouseY) {
        if (!v.scrollable() || !v.hasScrollBar) return;

        HontunGuiTheme t = v.theme();
        GuiFill g = GuiFill.of(renderer);
        int u = u(t);
        int bx0 = px(v.barX());
        int bx1 = px(v.barX() + v.barWidth());
        int vy0 = px(v.y), vy1 = px(v.y + v.height);

        int lane = bx0 + (bx1 - bx0 - u) / 2;
        rect(g, lane, vy0, lane + u, vy1, argb(ink(t, 0x40), t.overlay0Color()));

        double bar = v.barHeight();
        double thumb = Math.max(8 * u, bar);
        double ty = v.barY() - (thumb - bar) / 2;
        ty = Math.max(v.y, Math.min(ty, v.y + v.height - thumb));

        boolean drag = v.focused;
        boolean hover = v.barHovered();
        int y0 = px(ty), y1 = px(ty + thumb);

        plate(g, bx0, y0, bx1, y1, u, u, u, grip(t, hover, drag));
        frame(g, bx0, y0, bx1, y1, u, u, u, argb(hover || drag ? 0xFF : ink(t, 0xC0), t.accentHiColor()));
    }

    private static int grip(HontunGuiTheme t, boolean hover, boolean drag) {
        if (drag) return argb(0xFF, t.light() ? t.accentHiColor() : t.accentColor());
        if (hover && t.light()) return argb(0xFF, t.accentColor());
        return argb(0xE0, t.accentLoColor());
    }

    @Override
    public void paintButton(WHontunButton b, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme t = b.theme();
        GuiFill g = GuiFill.of(renderer);
        int u = u(t);
        int x0 = px(b.x), y0 = px(b.y), x1 = px(b.x + b.width), y1 = px(b.y + b.height);
        boolean pressed = b.isPressed();
        double hq = b.hoverProgress();

        if (b.parent instanceof WHontunKeybind k && k.button() == b) {
            paintKeybind(k, b, t, g, u, x0, y0, x1, y1, pressed, hq);
            return;
        }

        int dy = pressed ? 1 : 0;
        RichText label = b.displayText();

        if (label != null) {
            chrome(g, t, x0, y0, x1, y1, cutFor(y1 - y0, u), u, hq, pressed, true);
            drawText(label, b.x + b.width / 2 - b.labelWidth() / 2, b.y + b.pad() + dy, t.textColor());
            return;
        }

        chrome(g, t, x0, y0, x1, y1, 2 * u, u, hq, pressed, false);

        GuiTexture icon = b.icon();
        if (icon == null) return;

        double ts = t.textHeight();
        renderer.quad(Math.round(b.x + b.width / 2 - ts / 2), Math.round(b.y + b.pad()) + dy, ts, ts, icon,
                lerpColor(subtext1(t), t.accentHiColor(), hq));
    }

    private void paintKeybind(WHontunKeybind k, WHontunButton b, HontunGuiTheme t, GuiFill g, int u,
                              int x0, int y0, int x1, int y1, boolean pressed, double hq) {
        int cut = cutFor(y1 - y0, u);
        boolean listening = k.isListening();
        long now = System.currentTimeMillis();

        if (listening) {
            double f = 0.5 + 0.5 * Math.sin(now * 2 * Math.PI / 1200.0);
            double bo = t.backgroundOpacity();
            plate(g, x0, y0, x1, y1, cut, cut, u, argb(mul(0xF0, bo), t.surface2Color()));
            frame(g, x0, y0, x1, y1, cut, cut, u, argb(mix(0x60, 0xFF, f), t.accentColor()));
            rect(g, x0 + 2 * u, y1 - 2 * u, x1 - cut - u, y1 - u, argb(0xFF, t.accentColor()));
        } else {
            chrome(g, t, x0, y0, x1, y1, cut, u, hq, pressed, true);
        }

        double ty = b.y + b.pad() + (pressed ? 1 : 0);
        double x = b.x + b.width / 2 - b.labelWidth() / 2;

        if (listening) {
            drawText(LISTEN_TEXT, x, ty, subtext1(t));
            if ((now / BLINK) % 2 == 0) drawText(CURSOR, x + t.textWidth(LISTEN_GAP), ty, t.accentHiColor());
            return;
        }

        RichText bind = smallTitle(k.title());
        double th = t.textHeight();
        drawText(bind, x, ty + (th - t.textHeight(bind)) / 2, t.textSecondaryColor());
        x += t.textWidth(bind) + t.textWidth(SPACE);

        int bracket = argb(0xFF, t.accentLoColor());
        int by0 = px(ty);
        int bh = px(th);
        ChamferPaint.bracketGlyph(g, px(x), by0, bh, u, true, bracket);
        x += t.textWidth(OPEN);

        boolean none = !k.keybind().isSet();
        RichText key = RichText.of(keyText(k, k.keybind().toString()));
        drawText(key, x, ty, none ? t.textSecondaryColor() : t.accentHiColor());
        x += t.textWidth(key) + t.textWidth(CLOSE);

        ChamferPaint.bracketGlyph(g, px(x), by0, bh, u, false, bracket);
    }

    private RichText smallTitle(String title) {
        return smallTitles.computeIfAbsent(title, k -> RichText.of(k.toUpperCase(Locale.ROOT)).scale(TextScale.SMALL.get()));
    }

    private static String keyText(WHontunKeybind k, String key) {
        if (!k.keybind().isSet() || key == null) return "NONE";
        return key.toUpperCase(Locale.ROOT);
    }

    private static void chrome(GuiFill g, HontunGuiTheme t, int x0, int y0, int x1, int y1, int cut, int u,
                               double hq, boolean pressed, boolean decorated) {
        double bo = t.backgroundOpacity();
        int plateArgb;
        int frameArgb;
        int lineArgb;

        if (pressed) {
            plateArgb = ChamferPaint.pressedPlate(t);
            frameArgb = argb(0xFF, t.accentColor());
            lineArgb = argb(0xFF, t.accentColor());
        } else {
            plateArgb = lerpArgb(mul(0xD0, bo), t.surface0Color(), mul(0xF0, bo), t.surface2Color(), hq);
            frameArgb = lerpArgb(ink(t, 0x90), t.accentLoColor(), 0xFF, t.accentHiColor(), hq);
            lineArgb = argb(ink(t, mix(0xA0, 0xFF, hq)), t.accentColor());
            if (decorated && hq > 0) glow(g, t, x0, y0, x1, y1, cut, cut, u, 2, mul(0x60, hq));
        }

        plate(g, x0, y0, x1, y1, cut, cut, u, plateArgb);
        frame(g, x0, y0, x1, y1, cut, cut, u, frameArgb);
        if (decorated) rect(g, x0 + 2 * u, y1 - 2 * u, x1 - cut - u, y1 - u, lineArgb);
    }

    private static void tintedChrome(GuiFill g, HontunGuiTheme t, int x0, int y0, int x1, int y1, int u,
                                     boolean hover, boolean pressed, Color tint) {
        int cut = 2 * u;
        double bo = t.backgroundOpacity();

        glow(g, t, x0, y0, x1, y1, cut, cut, u, 1, 0x50, tint);

        int plateArgb = pressed
                ? ChamferPaint.pressedPlate(t)
                : hover ? argb(mul(0xF0, bo), t.surface2Color()) : argb(mul(0xD0, bo), t.surface0Color());
        plate(g, x0, y0, x1, y1, cut, cut, u, plateArgb);
        int edge = t.light() && (hover || pressed) ? HontunTheme.darken(ChamferPaint.rgb(tint), 0.75f) : ChamferPaint.rgb(tint);
        frame(g, x0, y0, x1, y1, cut, cut, u, argb(hover || pressed ? 0xFF : ink(t, 0x90), edge));
    }

    private static void armedChrome(GuiFill g, HontunGuiTheme t, int x0, int y0, int x1, int y1, int cut, int u, boolean line) {
        Color red = t.redColor();
        plate(g, x0, y0, x1, y1, cut, cut, u, argb(0x40, red));
        frame(g, x0, y0, x1, y1, cut, cut, u, argb(0xFF, red));
        if (line) rect(g, x0 + 2 * u, y1 - 2 * u, x1 - cut - u, y1 - u, argb(0xFF, red));
        brackets(g, x0, y0, x1, y1, 2 * u, u, argb(0xFF, red));
    }

    @Override
    public void paintConfirmedButton(WHontunConfirmedButton b, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme t = b.theme();
        GuiFill g = GuiFill.of(renderer);
        int u = u(t);
        int x0 = px(b.x), y0 = px(b.y), x1 = px(b.x + b.width), y1 = px(b.y + b.height);
        boolean pressed = b.isPressed();
        boolean armed = b.armed();
        double hq = b.mouseOver ? 1 : 0;
        int dy = pressed ? 1 : 0;

        String label = b.getText();
        if (label != null) {
            int cut = cutFor(y1 - y0, u);
            if (armed) armedChrome(g, t, x0, y0, x1, y1, cut, u, true);
            else chrome(g, t, x0, y0, x1, y1, cut, u, hq, pressed, true);
            drawText(RichText.of(label), b.x + b.width / 2 - b.labelWidth() / 2, b.y + b.pad() + dy, t.textColor());
            return;
        }

        if (armed) armedChrome(g, t, x0, y0, x1, y1, 2 * u, u, false);
        else chrome(g, t, x0, y0, x1, y1, 2 * u, u, hq, pressed, false);

        GuiTexture icon = b.icon();
        if (icon == null) return;

        double ts = t.textHeight();
        Color tint = armed ? t.textColor() : lerpColor(subtext1(t), t.accentHiColor(), hq);
        renderer.quad(Math.round(b.x + b.width / 2 - ts / 2), Math.round(b.y + b.pad()) + dy, ts, ts, icon, tint);
    }

    @Override
    public void paintConfirmedMinus(WHontunConfirmedMinus m, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme t = m.theme();
        GuiFill g = GuiFill.of(renderer);
        int u = u(t);
        int x0 = px(m.x), y0 = px(m.y), x1 = px(m.x + m.width), y1 = px(m.y + m.height);
        Color red = t.redColor();

        if (m.armed()) armedChrome(g, t, x0, y0, x1, y1, 2 * u, u, false);
        else tintedChrome(g, t, x0, y0, x1, y1, u, m.mouseOver, m.isPressed(), red);

        double pad = m.pad();
        int bar = Math.max(u, s(t, 3));
        int by = px(m.y + m.height / 2) - bar / 2;
        rect(g, px(m.x + pad), by, px(m.x + m.width - pad), by + bar, argb(0xFF, m.armed() ? red : glyphTint(t, red, m.isPressed())));
    }

    @Override
    public void paintMinus(WHontunMinus m, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme t = m.theme();
        GuiFill g = GuiFill.of(renderer);
        int u = u(t);
        tintedChrome(g, t, px(m.x), px(m.y), px(m.x + m.width), px(m.y + m.height), u, m.mouseOver, m.isPressed(), t.redColor());

        double pad = m.pad();
        double s = t.textHeight();
        renderer.quad(Math.round(m.x + pad), Math.round(m.y + pad), s, s, HontunBuiltinIcons.MINUS.texture(), glyphTint(t, t.redColor(), m.isPressed()));
    }

    @Override
    public void paintPlus(WHontunPlus p, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme t = p.theme();
        GuiFill g = GuiFill.of(renderer);
        int u = u(t);
        tintedChrome(g, t, px(p.x), px(p.y), px(p.x + p.width), px(p.y + p.height), u, p.mouseOver, p.isPressed(), t.greenColor());

        double pad = p.pad();
        double s = t.textHeight();
        renderer.quad(Math.round(p.x + pad), Math.round(p.y + pad), s, s, HontunBuiltinIcons.PLUS.texture(), glyphTint(t, t.greenColor(), p.isPressed()));
    }

    private static Color glyphTint(HontunGuiTheme t, Color tint, boolean pressed) {
        if (!t.light() || !pressed) return tint;
        return ChamferPaint.color(HontunTheme.darken(ChamferPaint.rgb(tint), 0.75f));
    }

    @Override
    public void paintCheckbox(WHontunCheckbox c, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme t = c.theme();
        GuiFill g = GuiFill.of(renderer);
        int u = u(t);
        int cell = checkCell(c, t, u);
        int side = 7 * cell;
        int x0 = px(c.x), y0 = px(c.y);
        int x1 = x0 + side, y1 = y0 + side;
        int cut = 3 * u;
        double bo = t.backgroundOpacity();

        boolean hover = c.mouseOver;
        boolean pressed = c.isPressed();
        double p = Math.clamp(c.progress(), 0, 1);

        boolean light = t.light();
        if (hover && !pressed && (p < 1 || light)) glow(g, t, x0, y0, x1, y1, cut, cut, u, 1, 0x30);

        if (p < 1) {
            plate(g, x0, y0, x1, y1, cut, cut, u, light && pressed
                    ? ChamferPaint.pressedPlate(t)
                    : argb(mul(0xF0, bo), pressed ? t.surface2Color() : t.crustColor()));
            int edge;
            if (light) edge = argb(0xFF, hover ? t.accentColor() : rim(t));
            else edge = hover ? argb(0xFF, t.accentLoColor()) : argb(0x90, t.overlay0Color());
            frame(g, x0, y0, x1, y1, cut, cut, u, edge);
        }

        if (p <= 0) return;

        int inset = u * (int) Math.round((1 - p) * side / (2.0 * u));
        if (x1 - x0 - 2 * inset > 0) {
            plate(g, x0 + inset, y0 + inset, x1 - inset, y1 - inset, cut, cut, u, argb(0xFF, t.accentColor()));
        }

        if (p >= 1) frame(g, x0, y0, x1, y1, cut, cut, u, argb(hover ? 0xFF : ink(t, 0xC0), t.accentHiColor()));

        int strokes = (int) Math.ceil(p * 5);
        HontunIcons.pixelCheck(g, x0, y0, cell, strokes, argb(0xFF, ChamferPaint.onAccent(t)));
    }

    private static int checkCell(WHontunCheckbox c, HontunGuiTheme t, int u) {
        return u * (int) Math.max(1, Math.round(0.9 * (2 * c.pad() + t.textHeight()) / (7.0 * u)));
    }

    @Override
    public void paintColorPicker(WHontunColorPicker p, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme t = p.theme();
        GuiFill g = GuiFill.of(renderer);
        int u = u(t);
        int x0 = px(p.x), y0 = px(p.y), x1 = px(p.x + p.width), y1 = px(p.y + p.height);
        int cut = 2 * u;
        Color color = p.color();

        if (color.a < 255) {
            HontunShapes.checker(g, x0, y0, x1 - x0, y1 - y0, cut, cut, u, 4 * u,
                    argb(0xFF, t.surface1Color()), argb(0xFF, t.overlay1Color()));
        }

        plate(g, x0, y0, x1, y1, cut, cut, u, argb(color.a, color));
        frame(g, x0 + u, y0 + u, x1 - u, y1 - u, u, u, u, argb(0xFF, t.crustColor()));
        int edge;
        if (p.mouseOver) edge = argb(0xFF, t.accentHiColor());
        else edge = t.light() ? argb(0xFF, rim(t)) : argb(0xC0, t.overlay1Color());
        frame(g, x0, y0, x1, y1, cut, cut, u, edge);

        if (p.mouseOver) {
            double s = t.textHeight();
            renderer.quad(Math.round(p.x + p.width / 2 - s / 2), Math.round(p.y + p.height / 2 - s / 2), s, s,
                    p.overlay(), pickerIconTint(color));
        }
    }

    @Override
    public void paintTriangle(WHontunTriangle tr, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme t = tr.theme();
        double s = t.textHeight();
        renderer.rotatedQuad(tr.x, tr.y, s, s, tr.rotation, HontunBuiltinIcons.ARROW.texture(), triangleColor(tr, t));
    }

    @SuppressWarnings("rawtypes")
    private Color triangleColor(WHontunTriangle tr, HontunGuiTheme t) {
        WWidget p = tr.parent;
        for (int depth = 0; p != null && depth < 3; depth++, p = p.parent) {
            if (p instanceof WHontunWindow.WHontunHeader h) {
                WindowState st = state(h.window());
                Color rest = t.light() ? subtext1(t) : t.textSecondaryColor();
                return lerpColor(rest, t.accentHiColor(), st.hover.getProgress());
            }
            if (p instanceof WHontunSection.WHontunHeader h) {
                return h.section().isExpanded() ? edgeText(t) : t.textSecondaryColor();
            }
            if (p instanceof WHontunMultiSelect.WHontunHeader h) {
                return h.mouseOver ? t.accentHiColor() : t.textSecondaryColor();
            }
        }
        return tr.mouseOver ? t.accentHiColor() : t.textSecondaryColor();
    }

    @Override
    public void paintSlider(WHontunSlider s, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme t = s.theme();
        GuiFill g = GuiFill.of(renderer);
        int u = u(t);
        int x0 = px(s.x), x1 = px(s.x + s.width), y0 = px(s.y);
        double bo = t.backgroundOpacity();
        double hs = s.handleSize();

        boolean light = t.light();
        int ty0 = y0 + 3 * u;
        int ty1 = ty0 + 4 * u;
        plate(g, x0, ty0, x1, ty1, u, u, u, argb(mul(0xE0, bo), light ? t.surface2Color() : t.crustColor()));
        int edge;
        if (light) edge = argb(0xFF, s.mouseOver ? t.accentColor() : rim(t));
        else edge = s.mouseOver ? argb(0xC0, t.accentLoColor()) : argb(0x70, t.overlay0Color());
        frame(g, x0, ty0, x1, ty1, u, u, u, edge);

        int cx = px(s.x + s.valueOffset() + hs / 2);
        if (cx > x0 + u) {
            g.gradient(x0 + u, ty0 + u, Math.min(cx, x1 - u), ty0 + 3 * u,
                    argb(0xFF, t.accentHiColor()), argb(0xFF, t.accentColor()));
        }

        int tickOn = argb(ink(t, 0xC0), t.accentLoColor());
        int tickOff = light ? argb(0xFF, t.overlay1Color()) : argb(0x90, t.overlay0Color());
        for (int i = 0; i <= 4; i++) {
            int tx = px(s.x + hs / 2 + (s.width - hs) * i / 4.0) - u / 2;
            rect(g, tx, ty0 + 5 * u, tx + u, ty0 + 6 * u, tx + u <= cx ? tickOn : tickOff);
        }

        boolean knobHover = s.handleHovered();
        boolean drag = s.isDragging();
        int kx0 = cx - 2 * u;
        int kx1 = kx0 + 4 * u;
        int ky0 = ty0 + 2 * u - 5 * u;
        int ky1 = ky0 + 10 * u;

        if (drag) glow(g, t, kx0, ky0, kx1, ky1, u, u, u, 2, 0x80);
        else if (knobHover) glow(g, t, kx0, ky0, kx1, ky1, u, u, u, 1, 0x60);

        plate(g, kx0, ky0, kx1, ky1, u, u, u, grip(t, knobHover, drag));
        frame(g, kx0, ky0, kx1, ky1, u, u, u, argb(knobHover || drag ? 0xFF : ink(t, 0xC0), t.accentHiColor()));

        int gx = kx0 + (3 * u) / 2;
        rect(g, gx, ky0 + 3 * u, gx + u, ky0 + 7 * u, light ? argb(0xFF, t.baseColor()) : argb(0x80, t.accentHiColor()));
    }

    @Override
    public void paintDropdown(WHontunDropdown<?> d, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme t = d.theme();
        GuiFill g = GuiFill.of(renderer);
        int u = u(t);
        int x0 = px(d.x), y0 = px(d.y), x1 = px(d.x + d.width), y1 = px(d.y + d.height);
        boolean pressed = d.isPressed();
        double open = d.indicatorProgress();
        int dy = pressed ? 1 : 0;

        chrome(g, t, x0, y0, x1, y1, cutFor(y1 - y0, u), u, d.hoverProgress(), pressed, true);

        double pad = d.pad();
        double th = t.textHeight();
        double titleWidth = t.textWidth(d.titleText());

        if (titleWidth > 0) drawText(d.titleText(), d.x + pad, d.y + pad + dy, t.textColor());

        int ledX = px(d.x + (titleWidth > 0 ? pad + titleWidth + pad : pad));
        int ledY = px(d.y + d.height / 2) - u + dy;
        rect(g, ledX, ledY, ledX + 2 * u, ledY + 2 * u, argb(0xFF, t.accentColor()));

        drawText(d.valueText(), ledX + 5 * u, d.y + pad + dy, t.accentColor());

        renderer.rotatedQuad(
                Math.round(d.x + d.width - pad - th),
                Math.round(d.y + d.height / 2 - th / 2),
                th,
                th,
                180 * (1 - open),
                HontunBuiltinIcons.ARROW.texture(),
                d.isExpanded() || open > 0 ? t.accentHiColor() : t.textSecondaryColor()
        );
    }

    @Override
    public void paintDropdownPopup(WHontunDropdown<?> d, WHontunDropdown.WRoot root, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme t = root.theme();
        GuiFill g = GuiFill.of(renderer);
        int u = u(t);
        int in = 2 * u;
        int x0 = px(root.x) + in, y0 = px(root.y) + in;
        int x1 = px(root.x + root.width) - in, y1 = px(root.y + root.height) - in;
        int cut = 3 * u;

        glow(g, t, x0, y0, x1, y1, cut, cut, u, 2, 0x50);
        plate(g, x0, y0, x1, y1, cut, cut, u, argb(Math.max(0xFC, mul(0xF4, 0.8 + 0.2 * t.backgroundOpacity())), t.crustColor()));
        frame(g, x0, y0, x1, y1, cut, cut, u, argb(0xC0, t.accentColor()));
        brackets(g, x0, y0, x1, y1, 3 * u, u, argb(0xFF, t.accentHiColor()));

        double p = d.indicatorProgress();
        if (p > 0 && p < 1) scan(g, t, x0 + u, x1 - cut - u, px(root.y + root.height * p) - 2 * u, u, p);
    }

    @Override
    public void paintDropdownValue(WHontunDropdown<?>.WValue v, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme t = v.theme();
        GuiFill g = GuiFill.of(renderer);
        int u = u(t);
        int x0 = px(v.x), y0 = px(v.y), x1 = px(v.x + v.width), y1 = px(v.y + v.height);
        int cut = 2 * u;

        if (v.mouseOver) {
            plate(g, x0, y0, x1, y1, cut, cut, u, argb(0x26, t.accentColor()));
            frame(g, x0, y0, x1, y1, cut, cut, u, argb(ink(t, 0xA0), t.accentLoColor()));
        }

        boolean selected = v.isSelectedValue();
        if (selected) rect(g, x0 + 2 * u, y0 + 2 * u, x0 + 3 * u, y1 - 2 * u, argb(0xFF, t.accentColor()));

        RichText label = v.valueName().boldIf(selected);
        drawText(label, v.x + v.width / 2 - t.textWidth(label) / 2, v.y + v.pad(), selected ? t.accentHiColor() : t.textColor());
    }

    @Override
    public void paintTextBox(WHontunTextBox b, GuiRenderer renderer, double mouseX, double mouseY) {
        ensureListener();

        HontunGuiTheme t = b.theme();
        GuiFill g = GuiFill.of(renderer);
        int u = u(t);
        int x0 = px(b.x), y0 = px(b.y), x1 = px(b.x + b.width), y1 = px(b.y + b.height);
        int cut = 3 * u;
        double bo = t.backgroundOpacity();
        boolean focused = b.isFocused();
        boolean hover = b.mouseOver;
        boolean borderless = !b.rendersBackground();
        boolean light = t.light();

        if (!borderless) {
            int fill = argb(mul(0xF0, bo), focused ? t.surface1Color() : t.crustColor());
            int frame;
            if (focused) frame = argb(0xFF, t.accentColor());
            else if (light) frame = argb(0xFF, hover ? t.textSecondaryColor() : rim(t));
            else frame = argb(hover ? 0xA0 : 0x70, t.overlay0Color());

            if (b.hasTitle()) {
                WHontunLabel label = titleLabel(b);
                if (label != null) label.color = subtext1(t);

                int tx0 = label != null
                        ? px(label.x - b.pad())
                        : px(b.x - (b.pad() + t.textWidth(b.title()) + t.scale(3)));

                if (focused) glow(g, t, tx0, y0, x1, y1, cut, cut, u, 2, 0x50);
                plate(g, tx0, y0, x0, y1, cut, 0, u, argb(mul(0xF0, bo), t.surface0Color()));
                plate(g, x0, y0, x1, y1, 0, cut, u, fill);
                rect(g, x0 - u, y0 + u, x0, y1 - u, argb(ink(t, 0xA0), t.accentLoColor()));
                frame(g, tx0, y0, x1, y1, cut, cut, u, frame);
            } else {
                if (focused) glow(g, t, x0, y0, x1, y1, cut, cut, u, 2, 0x50);
                plate(g, x0, y0, x1, y1, cut, cut, u, fill);
                frame(g, x0, y0, x1, y1, cut, cut, u, frame);
            }

            if (focused) rect(g, x0 + 2 * u, y1 - 2 * u, x1 - 4 * u, y1 - u, argb(0xFF, t.accentColor()));
        } else if (focused) {
            rect(g, x0, y1 - u, x1, y1, argb(0xFF, t.accentColor()));
        } else {
            HontunShapes.dashH(g, x0, x1, y1 - u, u, 2 * u, u, light ? argb(0xFF, t.overlay1Color()) : argb(0x80, t.overlay0Color()));
        }

        double padding = b.padding();
        renderer.scissorStart(b.x + padding, b.y, b.width - padding * 2, b.height);

        double origin = Math.round(b.x + padding - b.overflow());
        double ty = Math.round(b.y + padding);
        double th = t.textHeight();

        String value = b.textValue();
        if (!value.isEmpty()) {
            Color custom = b.customColor();
            Color color;
            if (custom != null) color = custom;
            else if (borderless) color = t.accentColor();
            else if (focused) color = t.textColor();
            else color = light ? t.textSecondaryColor() : dimmedText(t);
            b.drawText(renderer, origin, ty, value, color);
        } else if (b.placeholderValue() != null) {
            b.drawText(renderer, origin, ty, b.placeholderValue(), ColorUtils.withAlpha(t.textSecondaryColor(), light ? 0xD0 : 0xB0));
        }

        if (focused && b.hasSelection()) {
            paintTextSelection(b, renderer, origin + b.textWidthAt(b.selectionStartIndex()), origin + b.textWidthAt(b.selectionEndIndex()), ty, th);
        }

        if (focused && caretOn(b)) paintCaret(b, renderer, origin + b.textWidthAt(b.cursorIndex()), ty, th, 1);

        renderer.scissorEnd();
    }

    private boolean caretOn(WHontunTextBox b) {
        long now = System.currentTimeMillis();
        long[] state = carets.get(b);
        long cursor = b.cursorIndex();
        long hash = b.textValue().hashCode();

        if (state == null) {
            state = new long[]{now, cursor, hash};
            carets.put(b, state);
        } else if (state[1] != cursor || state[2] != hash) {
            state[0] = now;
            state[1] = cursor;
            state[2] = hash;
        }

        return ((now - state[0]) / BLINK) % 2 == 0;
    }

    private static WHontunLabel titleLabel(WHontunTextBox b) {
        if (!(b.parent instanceof WContainer container)) return null;

        WWidget previous = null;
        for (Cell<?> cell : container.cells) {
            if (cell.widget() == b) return previous instanceof WHontunLabel label ? label : null;
            previous = cell.widget();
        }

        return null;
    }

    @Override
    public void paintTextSelection(WHontunTextBox b, GuiRenderer renderer, double x0, double x1, double y, double h) {
        rect(GuiFill.of(renderer), px(Math.min(x0, x1)), px(y), px(Math.max(x0, x1)), px(y + h), argb(0x60, b.theme().accentColor()));
    }

    @Override
    public void paintCaret(WHontunTextBox b, GuiRenderer renderer, double x, double y, double h, double alpha) {
        HontunGuiTheme t = b.theme();
        int u = u(t);
        int cx = px(x), cy = px(y);
        rect(GuiFill.of(renderer), cx, cy, cx + u, cy + px(h), argb(0xFF, t.accentHiColor()));
    }

    @Override
    public void paintCompletions(WHontunTextBox b, GuiRenderer renderer, double x, double y, double w, double h) {
        HontunGuiTheme t = b.theme();
        GuiFill g = GuiFill.of(renderer);
        int u = u(t);
        int x0 = px(x), y0 = px(y), x1 = px(x + w), y1 = px(y + h);
        int cut = 3 * u;

        glow(g, t, x0, y0, x1, y1, cut, cut, u, 2, 0x50);
        plate(g, x0, y0, x1, y1, cut, cut, u, argb(Math.max(0xFC, mul(0xF4, 0.8 + 0.2 * t.backgroundOpacity())), t.crustColor()));
        frame(g, x0, y0, x1, y1, cut, cut, u, argb(0xC0, t.accentColor()));
        brackets(g, x0, y0, x1, y1, 3 * u, u, argb(0xFF, t.accentHiColor()));
    }

    @Override
    public void paintCompletionItem(WHontunTextBox.CompletionItem item, GuiRenderer renderer, double mouseX, double mouseY) {
        if (item.isSelected()) {
            HontunGuiTheme t = item.theme();
            GuiFill g = GuiFill.of(renderer);
            int u = u(t);
            int x0 = px(item.x), y0 = px(item.y), x1 = px(item.x + item.width), y1 = px(item.y + item.height);
            rect(g, x0, y0, x1, y1, argb(0x26, t.accentColor()));
            rect(g, x0, y0, x0 + u, y1, argb(0xFF, t.accentHiColor()));
        }

        item.drawLabel(renderer, mouseX, mouseY);
    }

    @Override
    public void paintTooltip(WHontunTooltip tt, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme t = tt.theme();
        GuiFill g = GuiFill.of(renderer);
        int u = u(t);
        int x0 = px(tt.x), y0 = px(tt.y), x1 = px(tt.x + tt.width), y1 = px(tt.y + tt.height);
        int cut = 2 * u;

        plate(g, x0, y0, x1, y1, cut, cut, u, argb(0xF4, t.crustColor()));
        frame(g, x0, y0, x1, y1, cut, cut, u, argb(ink(t, 0xC0), t.accentLoColor()));
        rect(g, x0 + u, y0 + 2 * u, x0 + 2 * u, y1 - 2 * u, argb(0xFF, t.accentColor()));
        brackets(g, x0, y0, x1, y1, 2 * u, u, argb(0xFF, t.accentHiColor()));
    }

    @Override
    public void paintSearchPanel(WHontunSearch s, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme t = s.theme();
        GuiFill g = GuiFill.of(renderer);
        int u = u(t);
        int x0 = px(s.x), y0 = px(s.y), x1 = px(s.x + s.width), y1 = px(s.y + s.height);
        int cut = 6 * u;
        boolean shadow = t.effWindowShadow();
        int rings = shadow ? 4 : 3;

        backdropGlow(g, t, x0, y0, x1, y1, cut, cut, u, rings, 0x50);
        if (shadow) ChamferPaint.shadowRings(g, t, x0, y0, x1, y1, cut, cut, u, rings);
    }

    @Override
    public void paintSearchHeader(WHontunSearch.WHontunHeader h, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme t = h.theme();
        GuiFill g = GuiFill.of(renderer);
        int u = u(t);
        int x0 = px(h.x), y0 = px(h.y), x1 = px(h.x + h.width), y1 = px(h.y + h.height);

        plate(g, x0, y0, x1, y1, 6 * u, 0, u, argb(0xF4, t.crustColor()));
        divider(g, t, x0 + u, x1 - u, y1 - u, u);

        if (h.cells.isEmpty() || !(h.cells.getFirst().widget() instanceof WContainer row)) return;

        GuiTexture searchIcon = HontunBuiltinIcons.SEARCH.texture();
        for (Cell<?> cell : row.cells) {
            WWidget widget = cell.widget();

            if (widget instanceof WGuiTexture tex && tex.texture == searchIcon) {
                tex.color = ChamferPaint.CLEAR;
                int size = HontunIcons.SIZE * u;
                int gx = px(tex.x + tex.width / 2) - size / 2;
                int gy = px(tex.y + tex.height / 2) - size / 2;
                HontunIcons.draw(g, HontunIcons.SEARCH, gx, gy, u, argb(0xFF, t.accentHiColor()));
            } else if (widget instanceof WHontunLabel label) {
                String plain = label.richText().getPlainText();
                if (ESC_HINT.equals(plain)) {
                    label.set(HINT);
                    label.hidden = true;
                } else if (label.hidden && plain.equals(HINT.getPlainText())) {
                    paintHint(g, t, u, label);
                }
            }
        }
    }

    private static void paintHint(GuiFill g, HontunGuiTheme t, int u, WHontunLabel label) {
        double x = label.x;
        double y = label.y;
        int bracket = argb(0xFF, t.accentLoColor());
        int by0 = px(y);
        int bh = px(t.textHeight());

        ChamferPaint.bracketGlyph(g, px(x), by0, bh, u, true, bracket);
        x += t.textWidth(HINT_OPEN);
        drawText(HINT_KEY, x, y, t.accentHiColor());
        x += t.textWidth(HINT_KEY) + t.textWidth(HINT_CLOSE);
        ChamferPaint.bracketGlyph(g, px(x), by0, bh, u, false, bracket);
        drawText(HINT_TAIL, x, y, t.textSecondaryColor());
    }

    @Override
    public void paintSearchBody(WHontunSearch.WHontunResultsContainer c, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme t = c.theme();
        GuiFill g = GuiFill.of(renderer);
        int u = u(t);
        int x0 = px(c.x), y0 = px(c.y), x1 = px(c.x + c.width), y1 = px(c.y + c.height);
        int cut = 6 * u;

        plate(g, x0, y0, x1, y1, 0, cut, u, argb(alpha(t.windowOpacity()), t.baseColor()));

        if (c.parent instanceof WHontunSearch s) {
            int sx0 = px(s.x), sy0 = px(s.y), sx1 = px(s.x + s.width), sy1 = px(s.y + s.height);
            frame(g, sx0, sy0, sx1, sy1, cut, cut, u, argb(0xC0, t.accentColor()));
            brackets(g, sx0, sy0, sx1, sy1, 5 * u, u, ChamferPaint.backdropBracket(t));
        }

        if (c.cells.size() < 2 || !(c.cells.get(1).widget() instanceof WHontunLabel help)) return;

        boolean empty = c.cells.getFirst().widget() instanceof WContainer view
                && view.cells.isEmpty()
                && !searchQuery(c).isEmpty();

        help.hidden = empty;
        if (empty) {
            drawText(NO_MATCHES, help.x + help.width / 2 - t.textWidth(NO_MATCHES) / 2, help.y, t.textSecondaryColor());
        }
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

        HontunGuiTheme t = row.theme();
        GuiFill g = GuiFill.of(renderer);
        int u = u(t);
        int x0 = px(row.x), y0 = px(row.y), x1 = px(row.x + row.width), y1 = px(row.y + row.height);
        int cut = 3 * u;

        if (row.isPressed()) {
            plate(g, x0 + u, y0 + u, x1 - u, y1 - u, cut - u, cut - u, u, t.light() ? argb(0xB0, t.overlay1Color()) : argb(0xF0, t.surface2Color()));
            return;
        }

        plate(g, x0, y0, x1, y1, cut, cut, u, argb(0x50, t.surface0Color()));
        frame(g, x0, y0, x1, y1, cut, cut, u, argb(ink(t, 0xC0), t.accentLoColor()));
    }

    @Override
    public void paintChip(GuiRenderer renderer, double x, double y, double w, double h, Color c, ChipKind kind) {
        HontunGuiTheme t = theme();
        GuiFill g = GuiFill.of(renderer);
        int u = u(t);
        int x0 = px(x), y0 = px(y), x1 = px(x + w), y1 = px(y + h);
        int cut = 2 * u;

        plate(g, x0, y0, x1, y1, cut, cut, u, argb(0x26, c));
        frame(g, x0, y0, x1, y1, cut, cut, u, t.light() ? argb(0xFF, HontunTheme.darken(ChamferPaint.rgb(c), 0.85f)) : argb(0xC0, c));
    }

    @Override
    public void paintMultiSelectHeader(WHontunMultiSelect<?>.WHontunHeader h, GuiRenderer renderer, double mouseX, double mouseY) {
        WHontunMultiSelect<?> m = h.owner();
        HontunGuiTheme t = m.theme();
        GuiFill g = GuiFill.of(renderer);
        int u = u(t);
        int c3 = 3 * u;
        double bo = t.backgroundOpacity();
        boolean open = m.isExpanded() || m.animating();

        int hx0 = px(h.x), hy0 = px(h.y), hx1 = px(h.x + h.width), hy1 = px(h.y + h.height);
        plate(g, hx0, hy0, hx1, hy1, c3, open ? 0 : c3, u, argb(mul(0xE0, bo), h.mouseOver ? t.surface1Color() : t.surface0Color()));

        int by = open ? px(m.y + m.height) : hy1;
        int edge;
        if (t.light()) edge = argb(0xFF, h.mouseOver ? t.accentColor() : rim(t));
        else edge = h.mouseOver ? argb(0xC0, t.accentLoColor()) : argb(0x70, t.overlay0Color());
        frame(g, px(m.x), px(m.y), px(m.x + m.width), by, c3, c3, u, edge);

        if (!h.cells.isEmpty() && h.cells.getFirst().widget() instanceof WHontunLabel title) {
            String plain = title.richText().getPlainText();
            String up = plain.toUpperCase(Locale.ROOT);
            if (!plain.equals(up)) title.set(RichText.bold(up));
        }

        if (h.cells.size() > 1 && h.cells.get(1).widget() instanceof WHontunLabel count) {
            count.color = t.accentColor();
            int cx0 = px(count.x) - 2 * u, cx1 = px(count.x + count.width) + 2 * u;
            int cy0 = px(count.y) - u, cy1 = px(count.y + count.height) + u;
            plate(g, cx0, cy0, cx1, cy1, 2 * u, 2 * u, u, argb(0x26, t.accentColor()));
            frame(g, cx0, cy0, cx1, cy1, 2 * u, 2 * u, u, argb(ink(t, 0x90), t.accentColor()));
        }
    }

    @Override
    public void paintMultiSelectBody(WHontunMultiSelect<?> m, GuiRenderer renderer, double mouseX, double mouseY) {
        if (!m.isExpanded() && !m.animating()) return;

        HontunGuiTheme t = m.theme();
        GuiFill g = GuiFill.of(renderer);
        int u = u(t);
        int y0 = px(m.y + m.headerWidget().height);
        plate(g, px(m.x), y0, px(m.x + m.width), px(m.y + m.height), 0, 3 * u, u,
                argb(mul(0xC0, t.backgroundOpacity()), t.baseColor()));
    }

    @Override
    public void paintMultiSelectItem(WHontunMultiSelect<?>.WHontunItem i, GuiRenderer renderer, double mouseX, double mouseY) {
        if (!i.mouseOver || i.checkboxHovered()) return;

        HontunGuiTheme t = i.owner().theme();
        GuiFill g = GuiFill.of(renderer);
        int u = u(t);
        int x0 = px(i.x), y0 = px(i.y), x1 = px(i.x + i.width), y1 = px(i.y + i.height);
        int cut = 2 * u;

        plate(g, x0, y0, x1, y1, cut, cut, u, argb(0xE0, t.surface0Color()));
        frame(g, x0, y0, x1, y1, cut, cut, u, argb(ink(t, 0x80), t.accentLoColor()));
    }

    @Override
    public void paintSwatchChip(WHontunSwatchLabel l, GuiRenderer renderer, double mouseX, double mouseY) {
        Color color = l.color;
        if (color == null || l.get().isEmpty()) return;

        HontunGuiTheme t = l.theme();
        int rgb = packed(color);
        if (HontunLightPalette.contrast(rgb, packed(t.mantleColor())) >= WHontunSwatchLabel.MIN_CONTRAST) return;

        int u = u(t);
        int chip = HontunLightPalette.luminance(rgb) > 0.4 ? 0x16181C : 0xF4F5F7;
        double pad = t.scale(2);
        plate(GuiFill.of(renderer), px(l.x - pad), px(l.y - pad / 2), px(l.x + l.width + pad), px(l.y + l.height + pad / 2),
                u, u, u, argb(0xFF, chip));
    }

    @Override
    public void paintCountChip(HontunSettingsWidgetFactory.WSelectedCountLabel l, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme t = l.theme();
        GuiFill g = GuiFill.of(renderer);
        int u = u(t);
        int x0 = px(l.x), y0 = px(l.y), x1 = px(l.x + l.width), y1 = px(l.y + l.height);
        int cut = 2 * u;

        plate(g, x0, y0, x1, y1, cut, cut, u, argb(0x26, t.accentColor()));
        frame(g, x0, y0, x1, y1, cut, cut, u, argb(ink(t, 0x90), t.accentColor()));

        drawText(l.label(), l.x + l.offsetX(), l.y + l.offsetY(), t.accentColor());
    }

    @Override
    public void paintMeteorBackground(WWidget w, GuiRenderer renderer, boolean pressed, boolean hovered) {
        HontunGuiTheme t = theme();
        GuiFill g = GuiFill.of(renderer);
        int u = u(t);
        int x0 = px(w.x), y0 = px(w.y), x1 = px(w.x + w.width), y1 = px(w.y + w.height);
        int cut = Math.min(2 * u, cutFor(y1 - y0, u));
        double bo = t.backgroundOpacity();

        int plateArgb = pressed
                ? ChamferPaint.pressedPlate(t)
                : hovered ? argb(mul(0xF0, bo), t.surface2Color()) : argb(mul(0xD0, bo), t.surface0Color());
        int frameArgb = pressed
                ? argb(0xFF, t.accentColor())
                : hovered ? argb(0xFF, t.accentHiColor()) : argb(ink(t, 0x90), t.accentLoColor());

        plate(g, x0, y0, x1, y1, cut, cut, u, plateArgb);
        frame(g, x0, y0, x1, y1, cut, cut, u, frameArgb);
    }

    @Override
    public void paintMeteorBackground(WWidget w, GuiRenderer renderer, Color outlineColor, Color backgroundColor) {
        HontunGuiTheme t = theme();
        GuiFill g = GuiFill.of(renderer);
        int u = u(t);
        int x0 = px(w.x), y0 = px(w.y), x1 = px(w.x + w.width), y1 = px(w.y + w.height);
        int cut = Math.min(2 * u, cutFor(y1 - y0, u));

        plate(g, x0, y0, x1, y1, cut, cut, u, argb(backgroundColor.a, backgroundColor));
        frame(g, x0, y0, x1, y1, cut, cut, u, argb(outlineColor.a, outlineColor));
    }

    @Override
    public void paintSnapGrid(GuiGraphicsExtractor g, int gridSize) {
        if (gridSize <= 0) return;

        HontunGuiTheme t = theme();
        int color = t.light() ? argb(0x40, HontunTheme.userAccent()) : argb(0x40, t.accentColor());
        int windowWidth = Utils.getWindowWidth();
        int windowHeight = Utils.getWindowHeight();
        float guiScale = (float) mc.getWindow().getGuiScale();

        int cols = windowWidth / gridSize + 1;
        int rows = windowHeight / gridSize + 1;
        int step = 1;
        while ((long) (cols / step + 1) * (rows / step + 1) > 2000) step++;
        int stride = gridSize * step;
        int arm = 3;

        Matrix3x2fStack pose = g.pose();
        pose.pushMatrix();
        pose.scale(1f / guiScale, 1f / guiScale);

        cz.honzasik.hontun.gui.render.pixel.QuadBatchState.Builder batch = cz.honzasik.hontun.gui.render.pixel.QuadBatchState.builder();
        for (int x = 0; x <= windowWidth; x += stride) {
            for (int y = 0; y <= windowHeight; y += stride) {
                batch.add(x - arm, y, x + arm + 1, y + 1, color);
                batch.add(x, y - arm, x + 1, y, color);
                batch.add(x, y + 1, x + 1, y + arm + 1, color);
            }
        }
        batch.submit(g);

        pose.popMatrix();
    }

    private static long stamp(Map<Tab, long[]> map, Tab tab, long now) {
        long[] state = map.get(tab);
        if (state == null || now - state[1] > 250) {
            state = new long[]{now, now};
            map.put(tab, state);
        }
        state[1] = now;
        return state[0];
    }

    private static Category category(String name) {
        if (name == null) return null;
        for (Category category : Modules.loopCategories()) {
            if (name.equals(category.name)) return category;
        }
        return null;
    }

    private void ensureListener() {
        if (listenerAdded) return;
        listenerAdded = true;
        HontunRenderer.addStyleListener(this::resetState);
    }

    private void resetState() {
        windows.clear();
        carets.clear();
        tabLock.clear();
        tabHover.clear();
        legibility = null;
        helpShadows = 0;
    }

    private WindowState state(WHontunWindow w) {
        WindowState st = windows.get(w);
        if (st == null) {
            HontunGuiTheme t = w.theme();
            st = new WindowState(StyleAnimation.of(anim(AnimRole.HEADER_HOVER), t), StyleAnimation.of(WINDOW_LOCK, t));
            windows.put(w, st);
        }

        boolean hovered = w.mouseOver;
        if (hovered != st.hovered) {
            st.hovered = hovered;
            if (hovered) st.hover.forward();
            else st.hover.backward();
        }

        boolean locked = hovered || w.isDragging();
        if (locked != st.locked) {
            st.locked = locked;
            if (locked) st.lock.forward();
            else st.lock.backward();
        }

        return st;
    }

    private static Geo geo(WHontunWindow w, int u) {
        int x0 = px(w.x), y0 = px(w.y), x1 = px(w.x + w.width);
        double headerHeight = w.headerWidget().height;
        int hy = px(w.y + headerHeight);
        double bodyHeight = Math.max((w.height - headerHeight) * w.expandProgress(), 0);
        boolean body = (w.isExpanded() || w.expandAnimating()) && bodyHeight >= u;
        int by = body ? px(w.y + headerHeight + bodyHeight) : hy;
        return new Geo(x0, y0, x1, hy, by, body);
    }

    private record Geo(int x0, int y0, int x1, int hy, int by, boolean body) {}

    private static final class WindowState {
        final StyleAnimation hover;
        final StyleAnimation lock;
        boolean hovered;
        boolean locked;
        String chipSource;
        int chipAvail = Integer.MIN_VALUE;
        RichText chipText;

        WindowState(StyleAnimation hover, StyleAnimation lock) {
            this.hover = hover;
            this.lock = lock;
        }
    }
}
