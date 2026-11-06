package cz.honzasik.hontun.gui.theme.style.terminal;

import cz.honzasik.hontun.gui.api.animation.Easing;
import cz.honzasik.hontun.gui.api.render.Corners;
import cz.honzasik.hontun.gui.api.render.RoundedRect;
import cz.honzasik.hontun.gui.api.text.RichText;
import cz.honzasik.hontun.gui.theme.HontunGuiTheme;
import cz.honzasik.hontun.gui.theme.HontunSettingsWidgetFactory;
import cz.honzasik.hontun.gui.theme.HontunWidget;
import cz.honzasik.hontun.gui.theme.icons.HontunBuiltinIcons;
import cz.honzasik.hontun.gui.theme.style.AnimRole;
import cz.honzasik.hontun.gui.theme.style.AnimSpec;
import cz.honzasik.hontun.gui.theme.style.ChipKind;
import cz.honzasik.hontun.gui.theme.style.ClickStyle;
import cz.honzasik.hontun.gui.theme.style.FooterKind;
import cz.honzasik.hontun.gui.theme.style.Knob;
import cz.honzasik.hontun.gui.theme.style.Metrics;
import cz.honzasik.hontun.gui.theme.style.Pipeline;
import cz.honzasik.hontun.gui.theme.style.StylePalette;
import cz.honzasik.hontun.gui.theme.style.WindowKind;
import cz.honzasik.hontun.gui.theme.widgets.WHontunHorizontalSeparator;
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
import cz.honzasik.hontun.utils.HontunTheme;
import meteordevelopment.meteorclient.gui.WidgetScreen;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.renderer.packer.GuiTexture;
import meteordevelopment.meteorclient.gui.tabs.Tab;
import meteordevelopment.meteorclient.gui.tabs.Tabs;
import meteordevelopment.meteorclient.gui.utils.Cell;
import meteordevelopment.meteorclient.gui.utils.WindowConfig;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;
import meteordevelopment.meteorclient.gui.widgets.containers.WWindow;
import meteordevelopment.meteorclient.settings.Settings;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.joml.Matrix3x2fStack;

import java.util.List;
import java.util.Locale;

import static cz.honzasik.hontun.gui.theme.style.terminal.TerminalPaint.frame;
import static cz.honzasik.hontun.gui.theme.style.terminal.TerminalPaint.hline;
import static cz.honzasik.hontun.gui.theme.style.terminal.TerminalPaint.rect;
import static cz.honzasik.hontun.gui.theme.style.terminal.TerminalPaint.round;
import static cz.honzasik.hontun.gui.theme.style.terminal.TerminalPaint.vline;
import static cz.honzasik.hontun.gui.theme.style.terminal.TerminalPalette.argb;
import static cz.honzasik.hontun.gui.theme.style.terminal.TerminalPalette.opaque;
import static meteordevelopment.meteorclient.MeteorClient.mc;

public class TerminalClickStyle extends ClickStyle {
    static final String HOME = "  ~/.hontun/";

    private static final String[] MONTHS = { "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };

    @Override
    public HontunTheme.UiMode id() {
        return HontunTheme.UiMode.Terminal;
    }

    @Override
    protected Metrics createMetrics() {
        Metrics m = new Metrics();

        m.windowPad = 0;
        m.windowPadL = -1;
        m.windowPadR = -1;
        m.windowPadT = 4;
        m.windowPadB = 4;

        m.headerPadH = 0;
        m.headerPadV = 0;
        m.headerSpacing = 0;

        m.topBarMarginTop = 0;
        m.topBarItemPad = 0;
        m.tabPadH = 0;
        m.tabPadV = 0;
        m.tabIconGap = 0;

        m.rowPadH = 0;
        m.rowPadV = 0;
        m.activeBarWidth = 0;
        m.activeBarInset = 0;

        m.viewSpacing = 0;
        m.categoryViewInset = 0;
        m.categoryStartX = 0;
        m.categoryStartY = 1;
        m.categoryGapX = 16;

        m.sectionHeaderPadH = 0;
        m.sectionHeaderPadV = 0;
        m.sectionPad = 2;
        m.tablePad = 8;
        m.tableVSpacing = 4;

        m.tooltipPadH = 8;
        m.tooltipPadV = 3;

        m.moduleScreenPad = 8;
        m.moduleInfoSpacing = 6;

        m.separatorThickness = 1;

        m.searchHeaderPad = 6;
        m.searchViewPad = 0;
        m.searchRowPad = 0;
        m.searchResultPadH = 0;

        m.multiSelectItemPad = 2;
        m.multiSelectItemSpacing = 0;
        m.multiSelectCheckPad = 4;
        m.multiSelectHeaderPadV = 4;
        m.multiSelectHeaderPadL = 8;

        m.gap = 6;
        m.dropdownPad = 0;

        return m;
    }

    void syncMetrics(HontunGuiTheme t) {
        TerminalGrid g = TerminalGrid.of(t);
        Metrics m = metrics();

        m.windowPadT = g.unscaled(g.px(4));
        m.windowPadB = g.unscaled(g.px(4));
        m.categoryGapX = g.unscaled(Math.round(2 * g.cw));
        m.tooltipPadH = g.unscaled(g.cw);
        m.tooltipPadV = g.unscaled(g.px(3));
    }

    @Override
    public Pipeline pipeline() {
        return Pipeline.ROUNDED;
    }

    @Override
    public boolean uses(Knob knob) {
        return switch (knob) {
            case PALETTE_COLOR -> true;
            default -> false;
        };
    }

    @Override
    public boolean supportsLightMode() {
        return false;
    }

    @Override
    public double radiusFactor() {
        return 0;
    }

    @Override
    public double effectiveScale(double raw) {
        return raw * 1.2;
    }

    @Override
    public boolean windowShadow(HontunGuiTheme t) {
        return false;
    }

    @Override
    public double chromeMargin(HontunGuiTheme t) {
        return TerminalGrid.of(t).px(52);
    }

    @Override
    public double windowAlpha(double raw, boolean light) {
        return Math.max(0.5, Math.min(1, raw));
    }

    @Override
    public double controlAlpha(double raw, boolean light) {
        return Math.max(0, Math.min(1, raw));
    }

    @Override
    public int snapScrim() {
        return TerminalPalette.bg;
    }

    @Override
    public AnimSpec anim(AnimRole role) {
        return switch (role) {
            case MODULE_ACTIVE -> AnimSpec.of(Easing.QUAD_OUT, 120, 120);
            case WINDOW_EXPAND -> AnimSpec.of(Easing.QUAD_OUT, 160, 160);
            default -> AnimSpec.NONE;
        };
    }

    @Override
    public void palette(StylePalette out, boolean light, HontunGuiTheme t) {
        TerminalPalette.update(HontunTheme.userAccent(), t.windowOpacity.get());

        out.userAccent = HontunTheme.userAccent();
        out.userAccentHi = HontunTheme.userAccentHi();
        out.userAccentLo = HontunTheme.userAccentLo();

        out.crust = TerminalPalette.bar;
        out.mantle = TerminalPalette.bg;
        out.base = TerminalPalette.bg;
        out.surface0 = TerminalPalette.line;
        out.surface1 = TerminalPalette.lineF;
        out.surface2 = TerminalPalette.empty;
        out.overlay0 = TerminalPalette.faint;
        out.overlay1 = TerminalPalette.dim;
        out.overlay2 = TerminalPalette.dim;
        out.text = TerminalPalette.fg;
        out.subtext1 = TerminalPalette.dim;
        out.subtext0 = TerminalPalette.dim;
        out.accent = TerminalPalette.acc;
        out.accentHi = TerminalPalette.head;
        out.accentLo = TerminalPalette.fill;
        out.green = TerminalPalette.green;
        out.yellow = TerminalPalette.yellow;
        out.red = TerminalPalette.red;

        out.copyRoles();
        out.accentText = TerminalPalette.acc;
        out.accentHiText = TerminalPalette.acc;
        out.accentLine = TerminalPalette.lineF;
        out.accentHiLine = TerminalPalette.lineF;
        out.accentLoLine = TerminalPalette.lineF;
        out.accentBar = TerminalPalette.fill;
        out.accentHiBar = TerminalPalette.fill;
        out.accentLoBar = TerminalPalette.fill;
        out.onAccent = TerminalPalette.ink;
        out.accentFaint = false;
    }

    @Override
    public double windowMinWidth(HontunGuiTheme t) {
        return TerminalGrid.of(t).windowWidth(24);
    }

    @Override
    public String moduleTitle(Module module) {
        return theme().terminalNames.get() == TerminalNames.Title ? module.title : module.name;
    }

    @Override
    public WWidget windowIcon(HontunGuiTheme t, WindowKind kind, Category category, WWidget fallback) {
        syncMetrics(t);
        return null;
    }

    @Override
    public WWidget headerLabel(HontunGuiTheme t, WHontunWindow w, String title) {
        syncMetrics(t);
        WTermTitle label = new WTermTitle(w);
        label.theme = t;
        return label;
    }

    @Override
    public WWidget headerTrailing(HontunGuiTheme t, WHontunWindow w) {
        return null;
    }

    @Override
    public WWidget sectionTitle(HontunGuiTheme t, WHontunSection s, String title) {
        WTermSectionTitle widget = new WTermSectionTitle(s, title);
        widget.theme = t;
        return widget;
    }

    @Override
    public WWidget categoryPrologue(HontunGuiTheme t, Category category) {
        WTermPromptLine line = new WTermPromptLine("ls", false);
        line.theme = t;
        return line;
    }

    @Override
    public WWidget categoryEpilogue(HontunGuiTheme t, Category category) {
        WTermPromptLine line = new WTermPromptLine("", true);
        line.theme = t;
        return line;
    }

    @Override
    public WWidget footer(HontunGuiTheme t, FooterKind kind) {
        WTermFooter footer = new WTermFooter(kind);
        footer.theme = t;
        return footer;
    }

    @Override
    public boolean topBarFullWidth() {
        return true;
    }

    @Override
    public WWidget topBarLead(HontunGuiTheme t) {
        WTermSession session = new WTermSession();
        session.theme = t;
        return session;
    }

    @Override
    public WWidget topBarTrail(HontunGuiTheme t) {
        WTermStatus status = new WTermStatus();
        status.theme = t;
        return status;
    }

    @Override
    public boolean typeToFind() {
        return true;
    }

    @Override
    public boolean searchView(HontunGuiTheme t, WidgetScreen screen, String query) {
        TerminalFind.build(t, screen, query);
        return true;
    }

    @Override
    public WidgetScreen moduleScreen(HontunGuiTheme t, Module module) {
        return new TerminalModuleScreen(t, module, null);
    }

    @Override
    public WWidget settingsView(HontunGuiTheme t, Settings settings, String filter) {
        WTermConfig config = new WTermConfig(settings, filter, null);
        config.theme = t;
        return config;
    }

    @Override
    public double popupOffset(HontunGuiTheme t) {
        return TerminalGrid.of(t).px(3);
    }

    @Override
    public double popupOffsetX(HontunGuiTheme t) {
        return -TerminalGrid.of(t).cw;
    }

    @Override
    public String iconButtonText(GuiTexture icon) {
        if (icon == null) return null;
        if (icon == HontunBuiltinIcons.COPY.texture() || icon == GuiRenderer.COPY) return ":w";
        if (icon == HontunBuiltinIcons.IMPORT.texture() || icon == GuiRenderer.PASTE) return ":r";
        if (icon == HontunBuiltinIcons.EDIT.texture() || icon == GuiRenderer.EDIT) return "edit";
        if (icon == HontunBuiltinIcons.RESET.texture() || icon == GuiRenderer.RESET) return "rst";
        if (icon == HontunBuiltinIcons.PLUS.texture()) return "+";
        if (icon == HontunBuiltinIcons.MINUS.texture()) return "-";
        if (icon == HontunBuiltinIcons.BOOKMARK_YES.texture() || icon == HontunBuiltinIcons.BOOKMARK_NO.texture()) return "*";
        return null;
    }

    @Override
    public RichText keybindLabel(HontunGuiTheme t, WHontunKeybind k, boolean listening, String key) {
        if (listening) return RichText.of("<press a key>");
        String title = k.title() == null ? "bind" : k.title().toLowerCase(Locale.ROOT).replace(' ', '-');
        return RichText.of(title + " " + keyText(key));
    }

    static String keyText(String key) {
        if (key == null || key.isEmpty() || key.equalsIgnoreCase("none")) return "<none>";
        return "<" + key + ">";
    }

    @Override
    public Color labelColor(HontunGuiTheme t, boolean title) {
        return t.textColor();
    }

    @Override
    public Color helpKeyColor(HontunGuiTheme t) {
        return t.accentColor();
    }

    @Override
    public Color helpTextColor(HontunGuiTheme t) {
        return t.textSecondaryColor();
    }

    @Override
    public Color favoriteColor(WHontunFavorite f) {
        return TerminalPaint.color(opaque(f.checked ? TerminalPalette.acc : TerminalPalette.faint));
    }

    @Override
    public float radius(HontunWidget w) {
        return 0;
    }

    @Override
    public float smallRadius(HontunWidget w) {
        return 0;
    }

    @Override
    public float outlineWidth(HontunWidget w) {
        return 1f;
    }

    @Override
    public Corners corners(HontunWidget w) {
        return Corners.ALL;
    }

    static boolean focused(WHontunWindow w) {
        if (w == null) return true;
        return w.isDialog() || w.mouseOver || w.isDragging();
    }

    static WHontunWindow windowOf(WWidget w) {
        WWidget p = w;
        while (p != null) {
            if (p instanceof WHontunWindow win) return win;
            p = p.parent;
        }
        return null;
    }

    static int indexIn(WWidget w) {
        if (!(w.parent instanceof WContainer c)) return 0;
        List<Cell<?>> cells = c.cells;
        for (int i = 0; i < cells.size(); i++) {
            if (cells.get(i).widget() == w) return i;
        }
        return 0;
    }

    static final int MODULE_COLUMNS = 24;

    private static int moduleLead(HontunGuiTheme t) {
        return t.terminalMarker.get() == TerminalMarker.Brackets ? 5 : 3;
    }

    @Override
    public void moduleSize(WHontunModule m, double[] out) {
        HontunGuiTheme t = m.theme();
        TerminalGrid g = TerminalGrid.of(t);
        int lead = moduleLead(t);
        out[0] = Math.min(MODULE_COLUMNS * g.cw, lead * g.cw + m.titleWidth() + g.cw);
        out[1] = g.lh;
    }

    @Override
    public void paintModule(WHontunModule m, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme t = m.theme();
        TerminalGrid g = TerminalGrid.of(t);

        if (!TerminalState.printIn(t, indexIn(m))) {
            TerminalPaint.reset();
            return;
        }

        double x = m.x, y = m.y, w = m.width, h = m.height;

        if (m.isPressed()) rect(x, y, w, h, TerminalPalette.sel());
        else if (m.mouseOver) rect(x, y, w, h, TerminalPalette.hl());

        double hp = Math.max(0, Math.min(1, m.highlightProgress()));
        boolean brackets = t.terminalMarker.get() == TerminalMarker.Brackets;
        double ty = g.ty(y, h);

        if (brackets) {
            boolean on = m.module().isActive();
            TerminalPaint.text("[", x + g.cw, ty, opaque(TerminalPalette.dim));
            TerminalPaint.text(on ? "x" : " ", x + 2 * g.cw, ty, opaque(TerminalPalette.acc));
            TerminalPaint.text("]", x + 3 * g.cw, ty, opaque(TerminalPalette.dim));
        }
        else if (hp > 0) {
            double bx = Math.round(x + g.cw);
            TerminalPaint.exact(bx, Math.round(y + g.px(2)), Math.max(1, Math.round(g.cw * hp)), Math.round(g.px(14)), opaque(TerminalPalette.fill));
        }

        int color = hp >= 0.5 ? TerminalPalette.acc : TerminalPalette.fg;
        int lead = moduleLead(t);
        int room = Math.max(1, (int) Math.floor(w / g.cw + 1e-6) - lead - 1);
        TerminalPaint.text(WTermConfig.trunc(m.title().getPlainText(), room), x + lead * g.cw, ty, opaque(color));

        TerminalPaint.reset();
    }

    @Override
    public void tabSize(WHontunTopBar.WTopBarButton b, double[] out) {
        TerminalGrid g = TerminalGrid.of(b.bar().theme());
        out[0] = (tabLabel(b.tab(), false, false).length() + 2) * g.cw;
        out[1] = g.tb;
    }

    private static String tabLabel(Tab tab, boolean selected, boolean last) {
        int index = Math.max(0, Tabs.get().indexOf(tab));
        String name = tab.name.toLowerCase(Locale.ROOT).replace(' ', '-');
        return index + ":" + name + (selected ? "*" : last ? "-" : " ");
    }

    @Override
    public void paintTopBar(WHontunTopBar b, GuiRenderer renderer, double mouseX, double mouseY) {
        TerminalState.observeTab();
        rect(b.x, b.y, b.width, b.height, TerminalPalette.barA());
        hline(b.x, b.y + b.height - 1, b.width, opaque(TerminalPalette.line));
    }

    @Override
    public void paintTab(WHontunTopBar.WTopBarButton b, GuiRenderer renderer, double mouseX, double mouseY) {
        TerminalGrid g = TerminalGrid.of(b.bar().theme());
        Tab tab = b.tab();
        boolean selected = tab == TerminalState.currentTab();
        boolean last = !selected && tab == TerminalState.lastTab();

        double h = b.height - 1;
        if (selected) rect(b.x, b.y, b.width, h, opaque(TerminalPalette.fill));
        else if (b.isPressed()) rect(b.x, b.y, b.width, h, TerminalPalette.sel());
        else if (b.mouseOver) rect(b.x, b.y, b.width, h, TerminalPalette.hl());

        int color = selected ? TerminalPalette.ink : last ? TerminalPalette.dim : TerminalPalette.fg;
        TerminalPaint.text(tabLabel(tab, selected, last), b.x + g.cw, g.ty(b.y, h), opaque(color));
    }

    void paintSession(WTermSession s) {
        TerminalGrid g = TerminalGrid.of(s.theme());
        TerminalPaint.text("[hontun]", s.x + g.cw, g.ty(s.y, s.height - 1), opaque(TerminalPalette.acc));
    }

    static String statusText() {
        int on = 0, all = 0;
        meteordevelopment.meteorclient.systems.modules.Modules modules = meteordevelopment.meteorclient.systems.modules.Modules.get();
        if (modules != null) {
            for (Module module : modules.getAll()) {
                all++;
                if (module.isActive()) on++;
            }
        }

        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        return String.format(Locale.ROOT, "%d/%d on  \"hontun\" %02d:%02d %02d-%s-%02d",
                on, all, now.getHour(), now.getMinute(), now.getDayOfMonth(), MONTHS[now.getMonthValue() - 1], now.getYear() % 100);
    }

    void paintStatus(WTermStatus s, String status) {
        TerminalGrid g = TerminalGrid.of(s.theme());
        double w = status.length() * g.cw;
        TerminalPaint.text(status, s.x + s.width - g.cw - w, g.ty(s.y, s.height - 1), opaque(TerminalPalette.dim));
    }

    void paintTitle(WTermTitle tt) {
        WHontunWindow w = tt.window();
        HontunGuiTheme t = tt.theme();
        TerminalGrid g = TerminalGrid.of(t);
        boolean focus = focused(w);

        double hx = w.x, hy = w.y, ww = w.width;
        double hh = w.headerWidget().height - 1;

        double d = g.px(8), gap = g.px(6);
        double dx0 = hx + g.px(9), dy0 = hy + Math.round((hh - d) / 2);
        for (int i = 0; i < 3; i++) {
            int base = i == 0 && focus ? TerminalPalette.fill : TerminalPalette.faint;
            int alpha = focus ? 0xFF : 0x80;
            round(dx0 + i * (d + gap), dy0, d, d, d / 2, argb(alpha, base));
        }

        String prefix, name, suffix;
        Category category = tt.category();
        if (category != null || !w.isDialog()) {
            prefix = "~/";
            name = w.titleText().toLowerCase(Locale.ROOT).replace(' ', '-');
            suffix = "";
        }
        else if (w.subtitle == null) {
            prefix = "~/";
            name = WTermConfig.key(w.titleText());
            suffix = "";
        }
        else {
            prefix = "";
            name = w.titleText();
            suffix = w.subtitle != null ? w.subtitle : "";
            if (HOME.equals(w.subtitle) && !name.contains(".")) name = WTermConfig.key(name) + ".conf";
        }

        int chars = prefix.length() + name.length() + suffix.length();
        double tw = chars * g.cw;
        double minX = dx0 + 3 * d + 2 * gap + g.cw;
        double tx = Math.max(minX, Math.round(hx + (ww - tw) / 2));
        double ty = g.ty(hy, hh);

        TerminalPaint.text(prefix, tx, ty, opaque(TerminalPalette.dim));
        tx += prefix.length() * g.cw;
        TerminalPaint.text(name, tx, ty, opaque(focus ? TerminalPalette.acc : TerminalPalette.fg));
        tx += name.length() * g.cw;
        TerminalPaint.text(suffix, tx, ty, opaque(TerminalPalette.dim));

        if (category != null) {
            int[] counts = tt.counts();
            String total = "/" + counts[1];
            String on = String.valueOf(counts[0]);
            double rx = hx + ww - g.cw - total.length() * g.cw;
            TerminalPaint.text(total, rx, ty, opaque(TerminalPalette.dim));
            TerminalPaint.text(on, rx - on.length() * g.cw, ty, opaque(counts[0] > 0 ? TerminalPalette.acc : TerminalPalette.dim));
        }
    }

    void paintPrompt(WTermPromptLine line) {
        HontunGuiTheme t = line.theme();
        TerminalGrid g = TerminalGrid.of(t);

        if (!TerminalState.printIn(t, indexIn(line))) {
            TerminalPaint.reset();
            return;
        }

        String prompt = t.terminalPromptText();
        double x = line.x + g.cw;
        double ty = g.ty(line.y, line.height);

        TerminalPaint.text(prompt, x, ty, opaque(TerminalPalette.acc));
        x += (prompt.length() + 1) * g.cw;

        if (!line.command().isEmpty()) {
            TerminalPaint.text(line.command(), x, ty, opaque(TerminalPalette.fg));
            x += (line.command().length() + 1) * g.cw;
        }

        if (line.cursor()) cursor(t, x, line.y, line.height, focused(windowOf(line)));

        TerminalPaint.reset();
    }

    static void cursor(HontunGuiTheme t, double x, double rowY, double rowH, boolean focus) {
        TerminalGrid g = TerminalGrid.of(t);
        double h = Math.min(rowH - 2, g.px(16));
        double y = Math.round(rowY + (rowH - h) / 2);
        double w = Math.round(g.cw);

        if (!focus) {
            TerminalPaint.box(Math.round(x), y, w, Math.round(h), opaque(TerminalPalette.dim));
            return;
        }

        if (TerminalState.blinkOn(t)) TerminalPaint.exact(Math.round(x), y, w, Math.round(h), opaque(TerminalPalette.fill));
    }

    void paintFooter(WTermFooter f) {
        HontunGuiTheme t = f.theme();
        TerminalGrid g = TerminalGrid.of(t);

        TerminalLog.watchModules(t.terminalNames.get() == TerminalNames.Id);

        double x = f.x, y = f.y, w = f.width, h = f.height;
        rect(x, y, w, h, TerminalPalette.barA());
        hline(x, y, w, opaque(TerminalPalette.line));

        double cx = x + g.cw;
        double chipY = Math.round(y + g.px(4));
        double chipH = Math.round(g.px(16));
        double ty = g.ty(y + 1, h - 1);

        String[][] keys = f.keys();
        for (String[] pair : keys) {
            boolean arrows = WTermFooter.ARROWS.equals(pair[0]);
            double kw = pair[0].length() * g.cw;
            TerminalPaint.exact(Math.round(cx), chipY, Math.round(kw), chipH, opaque(TerminalPalette.fill));
            if (arrows) {
                TerminalPaint.arrow(cx, chipY, g.cw, chipH, g.u, true, opaque(TerminalPalette.ink));
                TerminalPaint.arrow(cx + g.cw, chipY, g.cw, chipH, g.u, false, opaque(TerminalPalette.ink));
            }
            else TerminalPaint.text(pair[0], cx, g.ty(chipY, chipH), opaque(TerminalPalette.ink));
            cx += kw;
            TerminalPaint.text(" " + pair[1], cx, ty, opaque(TerminalPalette.fg));
            cx += (pair[1].length() + 1) * g.cw + 2 * g.cw;
        }

        String message = TerminalLog.message();
        if (message.isEmpty()) return;

        double right = x + w - g.cw;
        int avail = (int) Math.floor((right - (cx + g.cw)) / g.cw);
        if (avail < 4) return;

        int color = argb((int) Math.round(255 * TerminalLog.alpha()), TerminalPalette.dim);
        if (message.length() <= avail) {
            TerminalPaint.text(message, right - message.length() * g.cw, ty, color);
            return;
        }

        double start = right - avail * g.cw;
        TerminalPaint.text(message.substring(0, avail - 1), start, ty, color);
        TerminalPaint.ellipsis(start + (avail - 1) * g.cw, ty, g.cw, color);
    }

    @Override
    public boolean layoutCategories(HontunGuiTheme t, List<Cell<?>> cells) {
        TerminalGrid g = TerminalGrid.of(t);
        double screenW = Utils.getWindowWidth();
        double screenH = Utils.getWindowHeight();

        double ww = g.windowWidth(24);
        double gap = Math.round(2 * g.cw);
        double top = g.tb + g.px(18);
        double bottom = screenH - g.tb - g.px(12);

        int k = (int) Math.floor((screenW - g.px(32) + gap) / (ww + gap));
        k = Math.max(1, Math.min(6, k));
        double total = k * ww + (k - 1) * gap;
        double x0 = Math.max(g.px(16), Math.round((screenW - total) / 2));

        double[] colY = new double[k];
        java.util.Arrays.fill(colY, top);
        int placed = 0;

        for (Cell<?> cell : cells) {
            WWidget widget = cell.widget();
            WindowConfig config = widget instanceof WWindow win && win.id != null ? t.getWindowConfig(win.id) : null;
            boolean moved = config != null && (config.x != -1 || config.y != -1);

            double x, y;
            if (moved) {
                x = config.x != -1 ? config.x : x0;
                y = config.y != -1 ? config.y : top;
                fitWindow(g, widget, bottom - y);
            }
            else {
                int col = placed;
                if (placed >= k) {
                    col = 0;
                    for (int i = 1; i < k; i++) if (colY[i] < colY[col]) col = i;
                }
                placed++;

                x = x0 + col * (ww + gap);
                y = colY[col];
                fitWindow(g, widget, bottom - y);

                double visible = widget instanceof WHontunWindow hw && !hw.isExpanded() ? hw.headerWidget().height : widget.height;
                colY[col] += visible + gap;
            }

            cell.x = x;
            cell.y = y;
            cell.width = widget.width;
            cell.height = widget.height;
            cell.alignWidget();
        }

        return true;
    }

    private static void fitWindow(TerminalGrid g, WWidget widget, double maxWindowHeight) {
        if (!(widget instanceof WHontunWindow w) || w.view == null || w.headerWidget() == null) return;

        double chrome = w.headerWidget().height + 2 * g.px(4);
        double viewMax = Math.max(3 * g.lh, maxWindowHeight - chrome);
        if (Math.abs(w.view.maxHeight - viewMax) < 0.5) return;

        w.view.maxHeight = viewMax;
        w.calculateSize();
    }

    @Override
    public void paintWindowBack(WHontunWindow w, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme t = w.theme();
        TerminalGrid g = TerminalGrid.of(t);

        double hh = w.headerWidget().height;
        double body = Math.max(0, (w.height - hh) * Math.max(0, Math.min(1, w.expandProgress())));
        double x = Math.round(w.x), y = Math.round(w.y), ww = Math.round(w.width), wh = Math.round(hh + body);
        double r = g.px(6);
        double spread = g.px(4);

        RoundedRect.get()
                .pos(x - spread, y + g.px(14) - spread)
                .size(ww + 2 * spread, wh + 2 * spread)
                .radius(r + spread)
                .color(new Color(0, 0, 0, 0x78))
                .feather((float) g.px(30))
                .dither(true)
                .render();

        if (body > 0) {
            RoundedRect.get()
                    .pos(x, y + hh)
                    .size(ww, Math.round(body))
                    .radii(0, 0, (float) r, (float) r)
                    .color(TerminalPaint.color(TerminalPalette.win()))
                    .render();
        }
    }

    @Override
    public void paintWindowHeader(WHontunWindow w, WHontunWindow.WHontunHeader h, GuiRenderer renderer, double mouseX, double mouseY) {
        TerminalGrid g = TerminalGrid.of(w.theme());
        boolean collapsed = w.expandProgress() <= 0 && !w.isExpanded();
        float r = (float) g.px(6);

        double x = Math.round(w.x), y = Math.round(h.y), ww = Math.round(w.width), hh = Math.round(h.height);

        RoundedRect.get()
                .pos(x, y)
                .size(ww, hh)
                .radii(r, r, collapsed ? r : 0, collapsed ? r : 0)
                .color(TerminalPaint.color(TerminalPalette.barA()))
                .render();

        if (!collapsed) hline(x, y + hh - 1, ww, opaque(TerminalPalette.line));
    }

    @Override
    public void paintWindowFront(WHontunWindow w, GuiRenderer renderer, double mouseX, double mouseY) {
        TerminalGrid g = TerminalGrid.of(w.theme());

        double hh = w.headerWidget().height;
        double body = Math.max(0, (w.height - hh) * Math.max(0, Math.min(1, w.expandProgress())));
        int outline = focused(w) ? TerminalPalette.lineF : TerminalPalette.line;

        frame(w.x, w.y, w.width, hh + body, g.px(6), 0, opaque(outline));
    }

    @Override
    public double collapsedHeight(WHontunWindow w) {
        return w.headerWidget().height;
    }

    @Override
    public void paintViewUnderlay(WHontunView v, GuiRenderer renderer, double mouseX, double mouseY) {
    }

    @Override
    public double scrollbarWidth(HontunGuiTheme t) {
        return TerminalGrid.of(t).px(2);
    }

    @Override
    public void paintScrollbar(WHontunView v, GuiRenderer renderer, double mouseX, double mouseY) {
        if (!v.scrollable()) return;
        if (!focused(windowOf(v)) && !v.mouseOver) return;

        TerminalGrid g = TerminalGrid.of(v.theme());
        double bw = g.px(2);
        double bar = v.barHeight();
        double th = Math.max(g.px(16), bar);
        double range = v.height - bar;
        double frac = range > 0 ? Math.max(0, Math.min(1, (v.barY() - v.y) / range)) : 0;
        double y = v.y + (v.height - th) * frac;
        double x = v.x + v.width - bw - g.px(2);

        round(x, y, bw, th, bw / 2, argb(0x8C, TerminalPalette.acc));
    }

    @Override
    public void paintSectionBody(WHontunSection s, GuiRenderer renderer, double mouseX, double mouseY) {
    }

    @Override
    public void paintSectionHeader(WHontunSection s, WHontunSection.WHontunHeader h, GuiRenderer renderer, double mouseX, double mouseY) {
        if (!s.isExpanded()) rect(h.x, h.y, h.width, h.height, TerminalPalette.fold());
        else if (h.mouseOver) rect(h.x, h.y, h.width, h.height, TerminalPalette.hl());
    }

    void paintSectionTitle(WTermSectionTitle title) {
        WHontunSection s = title.section();
        WHontunSection.WHontunHeader h = s.headerWidget();
        if (h == null) return;

        TerminalGrid g = TerminalGrid.of(title.theme());
        double ty = g.ty(h.y, h.height);
        String name = "[" + title.label() + "]";
        TerminalPaint.text(name, h.x + g.cw, ty, opaque(TerminalPalette.acc));

        String tail = s.isExpanded() ? "[-]" : "[+]";
        TerminalPaint.text(tail, h.x + h.width - g.cw - tail.length() * g.cw, ty, opaque(TerminalPalette.faint));
    }

    @Override
    public void paintSeparatorH(WHontunHorizontalSeparator s, GuiRenderer renderer, double mouseX, double mouseY) {
        TerminalGrid g = TerminalGrid.of(s.theme());

        if (!s.hasText()) {
            hline(s.x, Math.round(s.y + s.height / 2), s.width, opaque(TerminalPalette.line));
            return;
        }

        TerminalPaint.text("# " + s.titleString(), s.x, g.ty(s.y, s.height), opaque(TerminalPalette.dim));
    }

    @Override
    public void paintSeparatorV(WHontunVerticalSeparator s, GuiRenderer renderer, double mouseX, double mouseY) {
        vline(Math.round(s.x + s.width / 2), s.y, s.height, opaque(TerminalPalette.line));
    }

    @Override
    public void buttonSize(WHontunButton b, double[] out) {
        TerminalGrid g = TerminalGrid.of(b.theme());
        RichText text = b.displayText();

        if (text != null) {
            out[0] = (text.getPlainText().length() + 4) * g.cw;
            out[1] = g.lh;
            return;
        }

        out[0] = Math.round(g.th + 2 * g.cw);
        out[1] = g.lh;
    }

    @Override
    public void paintButton(WHontunButton b, GuiRenderer renderer, double mouseX, double mouseY) {
        TerminalGrid g = TerminalGrid.of(b.theme());

        if (b.isPressed()) rect(b.x, b.y, b.width, b.height, TerminalPalette.sel());
        else if (b.mouseOver) rect(b.x, b.y, b.width, b.height, TerminalPalette.hl());

        RichText label = b.displayText();
        double ty = g.ty(b.y, b.height);

        if (label != null) {
            String s = label.getPlainText();
            double x = Math.round(b.x + (b.width - (s.length() + 4) * g.cw) / 2);
            TerminalPaint.text("[", x, ty, opaque(TerminalPalette.dim));
            TerminalPaint.text(s, x + 2 * g.cw, ty, opaque(TerminalPalette.acc));
            TerminalPaint.text("]", x + (s.length() + 3) * g.cw, ty, opaque(TerminalPalette.dim));
            return;
        }

        if (b.icon() != null) {
            double s = Math.round(g.th);
            renderer.quad(Math.round(b.x + (b.width - s) / 2), Math.round(b.y + (b.height - s) / 2), s, s, b.icon(), TerminalPaint.color(opaque(TerminalPalette.acc)));
        }
    }

    @Override
    public void pressableSize(WWidget w, double[] out) {
        TerminalGrid g = TerminalGrid.of((HontunGuiTheme) w.getTheme());

        if (w instanceof WHontunConfirmedButton b) {
            String s = b.getText();
            out[0] = s != null ? (s.length() + 4) * g.cw : Math.round(g.th + 2 * g.cw);
            out[1] = g.lh;
            return;
        }

        if (w instanceof WHontunPlus || w instanceof WHontunMinus || w instanceof WHontunConfirmedMinus) {
            out[0] = 3 * g.cw;
            out[1] = g.lh;
        }
    }

    @Override
    public void paintConfirmedButton(WHontunConfirmedButton b, GuiRenderer renderer, double mouseX, double mouseY) {
        TerminalGrid g = TerminalGrid.of(b.theme());
        boolean armed = b.armed();

        if (armed) rect(b.x, b.y, b.width, b.height, opaque(TerminalPalette.fill));
        else if (b.isPressed()) rect(b.x, b.y, b.width, b.height, TerminalPalette.sel());
        else if (b.mouseOver) rect(b.x, b.y, b.width, b.height, TerminalPalette.hl());

        String s = b.getText();
        double ty = g.ty(b.y, b.height);

        if (s != null) {
            double x = Math.round(b.x + (b.width - (s.length() + 4) * g.cw) / 2);
            int bracket = armed ? TerminalPalette.ink : TerminalPalette.dim;
            int label = armed ? TerminalPalette.ink : TerminalPalette.acc;
            TerminalPaint.text("[", x, ty, opaque(bracket));
            TerminalPaint.text(s, x + 2 * g.cw, ty, opaque(label));
            TerminalPaint.text("]", x + (s.length() + 3) * g.cw, ty, opaque(bracket));
            return;
        }

        if (b.icon() != null) {
            double size = Math.round(g.th);
            renderer.quad(Math.round(b.x + (b.width - size) / 2), Math.round(b.y + (b.height - size) / 2), size, size, b.icon(),
                    TerminalPaint.color(opaque(armed ? TerminalPalette.ink : TerminalPalette.acc)));
        }
    }

    @Override
    public void paintConfirmedMinus(WHontunConfirmedMinus m, GuiRenderer renderer, double mouseX, double mouseY) {
        TerminalGrid g = TerminalGrid.of(m.theme());
        boolean armed = m.armed();

        if (armed) rect(m.x, m.y, m.width, m.height, opaque(TerminalPalette.red));
        else if (m.mouseOver) rect(m.x, m.y, m.width, m.height, TerminalPalette.hl());

        TerminalPaint.text("-", Math.round(m.x + (m.width - g.cw) / 2), g.ty(m.y, m.height), opaque(armed ? TerminalPalette.INK_D : TerminalPalette.red));
    }

    @Override
    public void paintMinus(WHontunMinus m, GuiRenderer renderer, double mouseX, double mouseY) {
        TerminalGrid g = TerminalGrid.of(m.theme());
        if (m.mouseOver) rect(m.x, m.y, m.width, m.height, TerminalPalette.hl());
        TerminalPaint.text("-", Math.round(m.x + (m.width - g.cw) / 2), g.ty(m.y, m.height), opaque(TerminalPalette.red));
    }

    @Override
    public void paintPlus(WHontunPlus p, GuiRenderer renderer, double mouseX, double mouseY) {
        TerminalGrid g = TerminalGrid.of(p.theme());
        if (p.mouseOver) rect(p.x, p.y, p.width, p.height, TerminalPalette.hl());
        TerminalPaint.text("+", Math.round(p.x + (p.width - g.cw) / 2), g.ty(p.y, p.height), opaque(TerminalPalette.acc));
    }

    @Override
    public void checkboxSize(WHontunCheckbox c, double[] out) {
        TerminalGrid g = TerminalGrid.of(c.theme());
        out[0] = 3 * g.cw;
        out[1] = g.lh;
    }

    @Override
    public void paintCheckbox(WHontunCheckbox c, GuiRenderer renderer, double mouseX, double mouseY) {
        TerminalGrid g = TerminalGrid.of(c.theme());
        if (c.isPressed()) rect(c.x, c.y, c.width, c.height, TerminalPalette.sel());
        else if (c.mouseOver) rect(c.x, c.y, c.width, c.height, TerminalPalette.hl());

        double x = Math.round(c.x + (c.width - 3 * g.cw) / 2);
        double ty = g.ty(c.y, c.height);
        TerminalPaint.text("[", x, ty, opaque(TerminalPalette.dim));
        TerminalPaint.text(c.checked ? "x" : " ", x + g.cw, ty, opaque(TerminalPalette.acc));
        TerminalPaint.text("]", x + 2 * g.cw, ty, opaque(TerminalPalette.dim));
    }

    @Override
    public void colorPickerSize(WHontunColorPicker p, double[] out) {
        TerminalGrid g = TerminalGrid.of(p.theme());
        out[0] = Math.round(2 * g.cw) + 2;
        out[1] = g.lh;
    }

    @Override
    public void paintColorPicker(WHontunColorPicker p, GuiRenderer renderer, double mouseX, double mouseY) {
        TerminalGrid g = TerminalGrid.of(p.theme());
        if (p.mouseOver) rect(p.x, p.y, p.width, p.height, TerminalPalette.hl());
        swatch(g, p.color(), p.x + 1, p.y + (p.height - g.px(12)) / 2, true);
    }

    static void swatch(TerminalGrid g, Color c, double x, double y, boolean alpha) {
        double w = Math.round(2 * g.cw), h = Math.round(g.px(12));
        int a = alpha ? (int) Math.round(255 * Math.max(0.2, c.a / 255.0)) : 255;
        int rgb = (c.r << 16) | (c.g << 8) | c.b;
        double r = g.px(2);

        RoundedRect.get().pos(Math.round(x + TerminalPaint.dx), Math.round(y)).size(w, h).radius(r)
                .color(TerminalPaint.color(argb(a, rgb)))
                .outline(TerminalPaint.color(argb(0x2E, 0xFFFFFF)), 1f)
                .render();
    }

    @Override
    public double sliderHeight(WHontunSlider s) {
        return TerminalGrid.of(s.theme()).lh;
    }

    @Override
    public void paintSlider(WHontunSlider s, GuiRenderer renderer, double mouseX, double mouseY) {
        TerminalGrid g = TerminalGrid.of(s.theme());
        double range = s.max() - s.min();
        double fr = range == 0 ? 0 : Math.max(0, Math.min(1, (s.value() - s.min()) / range));
        int n = Math.max(1, (int) Math.floor(s.width / g.cw) - 2);
        TerminalBar.paint(g, s.x, s.y, s.height, n, fr);
    }

    @Override
    public void dropdownSize(WHontunDropdown<?> d, double[] out) {
        TerminalGrid g = TerminalGrid.of(d.theme());
        String title = d.titleText() != null ? d.titleText().getPlainText() : "";
        int valueCols = (int) Math.ceil(d.maxValueWidth() / g.cw);
        int cols = title.isEmpty() ? valueCols : title.length() + 3 + valueCols;
        out[0] = (cols + 2) * g.cw;
        out[1] = g.lh;
    }

    @Override
    public void paintDropdown(WHontunDropdown<?> d, GuiRenderer renderer, double mouseX, double mouseY) {
        TerminalGrid g = TerminalGrid.of(d.theme());
        if (d.isExpanded() || d.isPressed()) rect(d.x, d.y, d.width, d.height, TerminalPalette.sel());
        else if (d.mouseOver) rect(d.x, d.y, d.width, d.height, TerminalPalette.hl());

        String title = d.titleText() != null ? d.titleText().getPlainText() : "";
        double x = d.x + g.cw;
        double ty = g.ty(d.y, d.height);

        if (!title.isEmpty()) {
            TerminalPaint.text(title, x, ty, opaque(TerminalPalette.fg));
            x += title.length() * g.cw;
            TerminalPaint.text(" = ", x, ty, opaque(TerminalPalette.dim));
            x += 3 * g.cw;
        }

        TerminalPaint.text(d.valueText().getPlainText(), x, ty, opaque(TerminalPalette.acc));
    }

    static void popupFrame(TerminalGrid g, double x, double y, double w, double h) {
        double r = g.px(4);
        double spread = g.px(3);
        RoundedRect.get()
                .pos(Math.round(x) - spread, Math.round(y) + g.px(10) - spread)
                .size(Math.round(w) + 2 * spread, Math.round(h) + 2 * spread)
                .radius(r + spread)
                .color(new Color(0, 0, 0, 0x80))
                .feather((float) g.px(24))
                .dither(true)
                .render();

        frame(x, y, w, h, r, TerminalPalette.sbar(), opaque(TerminalPalette.line));
    }

    @Override
    public void paintDropdownPopup(WHontunDropdown<?> d, WHontunDropdown.WRoot root, GuiRenderer renderer, double mouseX, double mouseY) {
        popupFrame(TerminalGrid.of(root.theme()), root.x, root.y, root.width, root.height);
    }

    @Override
    public void paintDropdownValue(WHontunDropdown<?>.WValue v, GuiRenderer renderer, double mouseX, double mouseY) {
        TerminalGrid g = TerminalGrid.of(v.theme());
        boolean current = v.isSelectedValue();

        if (current) rect(v.x, v.y, v.width, v.height, opaque(TerminalPalette.fill));
        else if (v.mouseOver) rect(v.x, v.y, v.width, v.height, TerminalPalette.hl());

        TerminalPaint.text(v.valueName().getPlainText(), v.x + g.cw, g.ty(v.y, v.height), opaque(current ? TerminalPalette.ink : TerminalPalette.fg));
    }

    @Override
    public void textBoxSize(WHontunTextBox b, double[] out) {
        TerminalGrid g = TerminalGrid.of(b.theme());
        out[0] = b.padding() + g.th + b.padding();
        out[1] = Math.max(g.lh, b.padding() + g.th + b.padding());
    }

    @Override
    public void paintTextBox(WHontunTextBox b, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme t = b.theme();
        TerminalGrid g = TerminalGrid.of(t);
        double padding = b.padding();
        double ty = g.ty(b.y, b.height);

        if (b.rendersBackground()) hline(b.x, b.y + b.height - 1, b.width, opaque(b.isFocused() ? TerminalPalette.lineF : TerminalPalette.line));

        double overflow = b.overflow();
        renderer.scissorStart(b.x + padding, b.y, b.width - padding * 2, b.height);

        String value = b.textValue();
        double x0 = b.x + padding - overflow;

        if (b.isFocused() && b.hasSelection()) {
            double s0 = x0 + b.textWidthAt(b.selectionStartIndex());
            double s1 = x0 + b.textWidthAt(b.selectionEndIndex());
            rect(Math.min(s0, s1), ty, Math.abs(s1 - s0), g.th, TerminalPalette.sel());
        }

        if (!value.isEmpty()) {
            Color custom = b.customColor();
            b.drawText(renderer, x0, ty, value, custom != null ? custom : TerminalPaint.color(opaque(TerminalPalette.fg)));
        }
        else if (b.placeholderValue() != null) {
            b.drawText(renderer, x0, ty, b.placeholderValue(), TerminalPaint.color(opaque(TerminalPalette.faint)));
        }

        if (b.isFocused() && TerminalState.blinkOn(t)) {
            int cursor = b.cursorIndex();
            double cx = Math.round(x0 + b.textWidthAt(cursor));
            TerminalPaint.exact(cx, ty, Math.round(g.cw), Math.round(g.th), opaque(TerminalPalette.fill));
            if (cursor < value.length()) TerminalPaint.text(String.valueOf(value.charAt(cursor)), cx, ty, opaque(TerminalPalette.ink));
        }

        renderer.scissorEnd();
    }

    @Override
    public void paintTextSelection(WHontunTextBox b, GuiRenderer renderer, double x0, double x1, double y, double h) {
        rect(Math.min(x0, x1), y, Math.abs(x1 - x0), h, TerminalPalette.sel());
    }

    @Override
    public void paintCaret(WHontunTextBox b, GuiRenderer renderer, double x, double y, double h, double alpha) {
        if (!TerminalState.blinkOn(b.theme())) return;
        TerminalGrid g = TerminalGrid.of(b.theme());
        TerminalPaint.exact(Math.round(x), Math.round(y), Math.round(g.cw), Math.round(h), opaque(TerminalPalette.fill));
    }

    @Override
    public void paintCompletions(WHontunTextBox b, GuiRenderer renderer, double x, double y, double w, double h) {
        popupFrame(TerminalGrid.of(b.theme()), x, y, w, h);
    }

    @Override
    public void paintCompletionItem(WHontunTextBox.CompletionItem item, GuiRenderer renderer, double mouseX, double mouseY) {
        if (item.isSelected()) rect(item.x, item.y, item.width, item.height, TerminalPalette.hl());
        item.drawLabel(renderer, mouseX, mouseY);
    }

    @Override
    public void paintTooltip(WHontunTooltip tt, GuiRenderer renderer, double mouseX, double mouseY) {
        popupFrame(TerminalGrid.of(tt.theme()), tt.x, tt.y, tt.width, tt.height);
    }

    @Override
    public void paintTriangle(WHontunTriangle tr, GuiRenderer renderer, double mouseX, double mouseY) {
        WWidget list = tr.parent;
        WWidget owner = list != null ? list.parent : null;

        if (owner instanceof WHontunWindow.WHontunHeader h && h.openIndicator() == tr) return;
        if (owner instanceof WHontunSection.WHontunHeader h && h.openIndicator() == tr) return;

        TerminalGrid g = TerminalGrid.of(tr.theme());
        String glyph = tr.rotation >= 45 ? "v" : ">";
        TerminalPaint.text(glyph, Math.round(tr.x + (tr.width - g.cw) / 2), g.ty(tr.y, tr.height), opaque(tr.mouseOver ? TerminalPalette.acc : TerminalPalette.dim));
    }

    @Override
    public double favoriteSize(WHontunFavorite f) {
        return TerminalGrid.of(f.theme()).cw;
    }

    @Override
    public void paintFavorite(WHontunFavorite f, GuiRenderer renderer, double mouseX, double mouseY) {
        TerminalGrid g = TerminalGrid.of(f.theme());
        int color = f.checked ? TerminalPalette.acc : f.mouseOver ? TerminalPalette.dim : TerminalPalette.faint;
        TerminalPaint.text("*", f.x, g.ty(f.y, f.height), opaque(color));
    }

    @Override
    public void paintKeybindExtra(WHontunKeybind k, GuiRenderer renderer, double mouseX, double mouseY) {
    }

    @Override
    public void paintSearchPanel(WHontunSearch s, GuiRenderer renderer, double mouseX, double mouseY) {
        frame(s.x, s.y, s.width, s.height, TerminalGrid.of(s.theme()).px(6), TerminalPalette.win(), opaque(TerminalPalette.lineF));
    }

    @Override
    public void paintSearchHeader(WHontunSearch.WHontunHeader h, GuiRenderer renderer, double mouseX, double mouseY) {
        hline(h.x, h.y + h.height - 1, h.width, opaque(TerminalPalette.line));
    }

    @Override
    public void paintSearchBody(WHontunSearch.WHontunResultsContainer c, GuiRenderer renderer, double mouseX, double mouseY) {
    }

    @Override
    public void paintSearchRow(WHontunSearch.WHontunResult row, GuiRenderer renderer, double mouseX, double mouseY) {
        if (row.isPressed()) rect(row.x, row.y, row.width, row.height, TerminalPalette.sel());
        else if (row.mouseOver) rect(row.x, row.y, row.width, row.height, TerminalPalette.hl());
    }

    @Override
    public void paintChip(GuiRenderer renderer, double x, double y, double w, double h, Color c, ChipKind kind) {
    }

    @Override
    public void paintMultiSelectHeader(WHontunMultiSelect<?>.WHontunHeader h, GuiRenderer renderer, double mouseX, double mouseY) {
        WHontunMultiSelect<?> m = h.owner();
        if (!m.isExpanded()) rect(h.x, h.y, h.width, h.height, TerminalPalette.fold());
        else if (h.mouseOver) rect(h.x, h.y, h.width, h.height, TerminalPalette.hl());
    }

    @Override
    public void paintMultiSelectBody(WHontunMultiSelect<?> m, GuiRenderer renderer, double mouseX, double mouseY) {
    }

    @Override
    public void paintMultiSelectItem(WHontunMultiSelect<?>.WHontunItem i, GuiRenderer renderer, double mouseX, double mouseY) {
        if (i.mouseOver) rect(i.x, i.y, i.width, i.height, TerminalPalette.hl());
    }

    @Override
    public void paintSwatchChip(WHontunSwatchLabel l, GuiRenderer renderer, double mouseX, double mouseY) {
    }

    @Override
    public void paintCountChip(HontunSettingsWidgetFactory.WSelectedCountLabel l, GuiRenderer renderer, double mouseX, double mouseY) {
        TerminalPaint.text(l.label().getPlainText(), l.x + l.offsetX(), l.y + l.offsetY(), opaque(TerminalPalette.acc));
    }

    @Override
    public void paintMeteorBackground(WWidget w, GuiRenderer renderer, boolean pressed, boolean hovered) {
        frame(w.x, w.y, w.width, w.height, 0, pressed ? TerminalPalette.sel() : TerminalPalette.win(), opaque(hovered ? TerminalPalette.lineF : TerminalPalette.line));
    }

    @Override
    public void paintMeteorBackground(WWidget w, GuiRenderer renderer, Color outlineColor, Color backgroundColor) {
        frame(w.x, w.y, w.width, w.height, 0, TerminalPalette.win(), opaque(TerminalPalette.line));
    }

    @Override
    public void paintSnapGrid(GuiGraphicsExtractor g, int gridSize) {
        if (gridSize <= 0) return;

        int color = argb(0x60, TerminalPalette.line);
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
}
