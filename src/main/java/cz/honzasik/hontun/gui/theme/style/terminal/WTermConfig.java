package cz.honzasik.hontun.gui.theme.style.terminal;

import com.mojang.blaze3d.platform.InputConstants;
import cz.honzasik.hontun.gui.mixin.meteorclient.WViewScrollAccessor;
import cz.honzasik.hontun.gui.screen.settings.HontunEntityTypeListSettingScreen;
import cz.honzasik.hontun.gui.tabs.HontunTab;
import cz.honzasik.hontun.gui.theme.HontunGuiTheme;
import cz.honzasik.hontun.gui.theme.HontunWidget;
import cz.honzasik.hontun.gui.theme.widgets.container.WHontunWindow;
import cz.honzasik.hontun.gui.theme.widgets.settings.WHontunKeybind;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.gui.GuiThemes;
import meteordevelopment.meteorclient.gui.WidgetScreen;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.screens.settings.BlockDataSettingScreen;
import meteordevelopment.meteorclient.gui.screens.settings.BlockListSettingScreen;
import meteordevelopment.meteorclient.gui.screens.settings.BlockSettingScreen;
import meteordevelopment.meteorclient.gui.screens.settings.EnchantmentListSettingScreen;
import meteordevelopment.meteorclient.gui.screens.settings.EntityTypeListSettingScreen;
import meteordevelopment.meteorclient.gui.screens.settings.FontFaceSettingScreen;
import meteordevelopment.meteorclient.gui.screens.settings.ItemListSettingScreen;
import meteordevelopment.meteorclient.gui.screens.settings.ItemSettingScreen;
import meteordevelopment.meteorclient.gui.screens.settings.ModuleListSettingScreen;
import meteordevelopment.meteorclient.gui.screens.settings.PacketBoolSettingScreen;
import meteordevelopment.meteorclient.gui.screens.settings.ParticleTypeListSettingScreen;
import meteordevelopment.meteorclient.gui.screens.settings.PotionSettingScreen;
import meteordevelopment.meteorclient.gui.screens.settings.ScreenHandlerSettingScreen;
import meteordevelopment.meteorclient.gui.screens.settings.SoundEventListSettingScreen;
import meteordevelopment.meteorclient.gui.screens.settings.StatusEffectAmplifierMapSettingScreen;
import meteordevelopment.meteorclient.gui.screens.settings.StatusEffectListSettingScreen;
import meteordevelopment.meteorclient.gui.screens.settings.StorageBlockListSettingScreen;
import meteordevelopment.meteorclient.gui.tabs.Tabs;
import meteordevelopment.meteorclient.gui.tabs.builtin.GuiTab;
import meteordevelopment.meteorclient.gui.utils.Cell;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;
import meteordevelopment.meteorclient.gui.widgets.containers.WView;
import meteordevelopment.meteorclient.gui.widgets.input.WTextBox;
import meteordevelopment.meteorclient.settings.BlockDataSetting;
import meteordevelopment.meteorclient.settings.BlockListSetting;
import meteordevelopment.meteorclient.settings.BlockSetting;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnchantmentListSetting;
import meteordevelopment.meteorclient.settings.EntityTypeListSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.FileSetting;
import meteordevelopment.meteorclient.settings.FontFaceSetting;
import meteordevelopment.meteorclient.settings.GenericSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.ItemListSetting;
import meteordevelopment.meteorclient.settings.ItemSetting;
import meteordevelopment.meteorclient.settings.KeybindSetting;
import meteordevelopment.meteorclient.settings.ModuleListSetting;
import meteordevelopment.meteorclient.settings.PacketListSetting;
import meteordevelopment.meteorclient.settings.ParticleTypeListSetting;
import meteordevelopment.meteorclient.settings.PotionSetting;
import meteordevelopment.meteorclient.settings.ProvidedStringSetting;
import meteordevelopment.meteorclient.settings.ScreenHandlerListSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.Settings;
import meteordevelopment.meteorclient.settings.SoundEventListSetting;
import meteordevelopment.meteorclient.settings.StatusEffectAmplifierMapSetting;
import meteordevelopment.meteorclient.settings.StatusEffectListSetting;
import meteordevelopment.meteorclient.settings.StorageBlockListSetting;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static cz.honzasik.hontun.gui.theme.style.terminal.TerminalPalette.argb;
import static cz.honzasik.hontun.gui.theme.style.terminal.TerminalPalette.opaque;
import static meteordevelopment.meteorclient.MeteorClient.mc;

public class WTermConfig extends WWidget implements HontunWidget {
    public static final int COLUMNS = 78;

    private static final int TEXT = 0;
    private static final int SWATCH = 1;
    private static final int CURSOR = 2;

    enum Kind { COMMENT, BLANK, HEADER, SECTION, FOLD, SETTING }

    enum Header { BIND, BIND_MODE, CHAT, RUN, THEME, RESET_LAYOUT, RESET_COLORS, RESET }

    public enum Mode { NORMAL, INSERT, BIND }

    static final class Line {
        final Kind kind;
        String comment;
        String commentTail;
        SettingGroup group;
        String groupKey;
        int count;
        Setting<?> setting;
        Header header;
        String key;
        int keyWidth;
        int indent;

        Line(Kind kind) {
            this.kind = kind;
        }

        boolean keyable() {
            return kind == Kind.HEADER || kind == Kind.SECTION || kind == Kind.FOLD || kind == Kind.SETTING;
        }

        Object identity() {
            if (setting != null) return setting;
            if (header != null) return header;
            if (group != null) return group;
            return null;
        }
    }

    record Seg(String text, int color, int kind) {}

    private static final class Popup {
        final Line line;
        final boolean color;
        final String[] items;
        final Object[] values;
        int hover;
        int current;
        double x, y, w, h;

        Popup(Line line, boolean color, String[] items, Object[] values, int current) {
            this.line = line;
            this.color = color;
            this.items = items;
            this.values = values;
            this.current = current;
            this.hover = Math.max(0, current);
        }
    }

    private final Settings settings;
    private final String filter;
    private final Module module;
    private final List<Line> lines = new ArrayList<>();
    private final Map<Setting<?>, WHontunKeybind> keybinds = new IdentityHashMap<>();

    private int signature;
    private int cursor = -1;
    private Mode mode = Mode.NORMAL;
    private Line editLine;
    private final StringBuilder buffer = new StringBuilder();
    private boolean swallowChar;
    private Popup popup;
    private Line dragLine;
    private int dragChannel = -1;
    private double pendingValue = Double.NaN;
    private Setting<?> focusSetting;
    private int pendingReveal = -1;
    private boolean hasRun;
    private boolean runChecked;
    private double lastMouseX = -1, lastMouseY = -1;

    public WTermConfig(Settings settings, String filter, Module module) {
        this.settings = settings;
        this.filter = filter == null ? "" : filter;
        this.module = module;
    }

    public void focus(Setting<?> setting) {
        focusSetting = setting;
    }

    public Module module() {
        return module;
    }

    @Override
    public void init() {
        focused = true;

        WHontunWindow window = TerminalClickStyle.windowOf(this);
        if (module == null && window != null && window.isDialog()) {
            if (window.subtitle == null) window.subtitle = TerminalClickStyle.HOME;
            window.cells.removeIf(cell -> cell.widget() instanceof WTermStatusLine || cell.widget() instanceof WTermMessageLine);
            window.addDirect(new WTermStatusLine(this)).expandX();
            window.addDirect(new WTermMessageLine(this)).expandX();
        }

        rebuild();
    }

    public String fileName() {
        if (module != null) return module.name + ".conf";

        WHontunWindow window = TerminalClickStyle.windowOf(this);
        String title = window != null ? window.titleText() : null;
        if (title == null || title.isBlank()) return "config.conf";
        return title.contains(".") ? title : key(title) + ".conf";
    }

    Object dirtyKey() {
        return module != null ? module : settings;
    }

    @Override
    public boolean isFocused() {
        return true;
    }

    private HontunGuiTheme t() {
        return theme();
    }

    private TerminalGrid g() {
        return TerminalGrid.of(theme());
    }

    private int gutter() {
        return t().terminalLineNumbers.get() ? 5 : 2;
    }

    private int content() {
        return COLUMNS - gutter() - 1;
    }

    static String key(String name) {
        return name == null ? "" : name.toLowerCase(Locale.ROOT).trim().replace(' ', '-');
    }

    private boolean matches(Setting<?> s) {
        if (filter.isEmpty()) return true;
        String f = filter.toLowerCase(Locale.ROOT);
        return s.title.toLowerCase(Locale.ROOT).contains(f) || s.name.toLowerCase(Locale.ROOT).contains(f);
    }

    private int computeSignature() {
        int h = 17;
        for (SettingGroup group : settings.groups) {
            h = h * 31 + (group.sectionExpanded ? 1 : 2);
            for (Setting<?> s : group) {
                if (!s.isVisible() || !matches(s)) continue;
                h = h * 31 + System.identityHashCode(s);
            }
        }
        return h;
    }

    private boolean hasRunWidget() {
        if (!runChecked && module != null) {
            runChecked = true;
            try {
                hasRun = module.getWidget(t()) != null;
            } catch (RuntimeException e) {
                hasRun = false;
            }
        }
        return hasRun;
    }

    public void rebuild() {
        Object keep = cursor >= 0 && cursor < lines.size() ? lines.get(cursor).identity() : null;
        int oldSize = lines.size();

        lines.clear();
        int cc = content();

        if (module != null) {
            comment("# " + module.title, null);
            for (String l : wrap(module.description, cc - 2)) comment("# " + l, null);
            if (module.addon != null && module.addon != MeteorClient.ADDON) comment("# from: ", module.addon.name);
            blank();

            int kw = "chat-feedback".length();
            header(Header.BIND, "bind", kw);
            header(Header.BIND_MODE, "bind-mode", kw);
            header(Header.CHAT, "chat-feedback", kw);
            if (hasRunWidget()) header(Header.RUN, "run", kw);
            blank();
        }
        else if (settings == t().settings || settings == t().guiSettings) {
            int kw = "reset-layout".length();
            header(Header.THEME, "theme", kw);
            header(Header.RESET_LAYOUT, "reset-layout", kw);
            header(Header.RESET_COLORS, "reset-colors", kw);
            blank();
        }
        else if (settings == t().hontunSettings) {
            header(Header.RESET, "reset", "reset".length());
            blank();
        }

        for (SettingGroup group : settings.groups) {
            List<Setting<?>> visible = new ArrayList<>();
            for (Setting<?> s : group) {
                boolean v = s.isVisible();
                s.lastWasVisible = v;
                if (v && matches(s)) visible.add(s);
            }
            if (visible.isEmpty()) continue;

            String gk = key(group.name);

            if (!group.sectionExpanded) {
                Line fold = new Line(Kind.FOLD);
                fold.group = group;
                fold.groupKey = gk;
                fold.count = visible.size();
                lines.add(fold);
                blank();
                continue;
            }

            Line section = new Line(Kind.SECTION);
            section.group = group;
            section.groupKey = gk;
            section.count = visible.size();
            lines.add(section);

            int kw = 0;
            for (Setting<?> s : visible) kw = Math.max(kw, s.name.length());
            kw = Math.min(28, kw);

            for (Setting<?> s : visible) {
                Line line = new Line(Kind.SETTING);
                line.setting = s;
                line.group = group;
                line.key = s.name;
                line.keyWidth = kw;
                line.indent = 2;
                lines.add(line);
            }

            blank();
        }

        while (!lines.isEmpty() && lines.getLast().kind == Kind.BLANK) lines.removeLast();

        signature = computeSignature();

        int found = -1;
        if (focusSetting != null) {
            for (int i = 0; i < lines.size(); i++) {
                if (lines.get(i).setting == focusSetting) found = i;
            }
            if (found >= 0) {
                focusSetting = null;
                pendingReveal = found;
            }
        }
        if (found < 0 && keep != null) {
            for (int i = 0; i < lines.size(); i++) {
                if (lines.get(i).identity() == keep) {
                    found = i;
                    break;
                }
            }
        }
        if (found < 0) {
            int saved = TerminalState.cursor(settings)[0];
            if (saved >= 0 && saved < lines.size() && lines.get(saved).keyable()) found = saved;
        }
        if (found < 0) found = defaultCursor();
        setCursor(found, false);

        if (lines.size() != oldSize) invalidate();
    }

    private void comment(String text, String tail) {
        Line l = new Line(Kind.COMMENT);
        l.comment = text;
        l.commentTail = tail;
        lines.add(l);
    }

    private void blank() {
        lines.add(new Line(Kind.BLANK));
    }

    private void header(Header header, String key, int kw) {
        Line l = new Line(Kind.HEADER);
        l.header = header;
        l.key = key;
        l.keyWidth = kw;
        lines.add(l);
    }

    static List<String> wrap(String text, int width) {
        List<String> out = new ArrayList<>();
        if (text == null) return out;
        StringBuilder line = new StringBuilder();
        for (String word : text.trim().split("\\s+")) {
            if (word.isEmpty()) continue;
            if (!line.isEmpty() && line.length() + 1 + word.length() > width) {
                out.add(line.toString());
                line.setLength(0);
            }
            if (!line.isEmpty()) line.append(' ');
            line.append(word);
        }
        if (!line.isEmpty()) out.add(line.toString());
        return out;
    }

    static String trunc(String s, int n) {
        if (n <= 1) return "~";
        return s.length() > n ? s.substring(0, n - 1) + "~" : s;
    }

    private int defaultCursor() {
        int first = firstKeyable();
        if (first < 0 || lines.get(first).header != Header.RESET) return first;
        for (int i = first + 1; i < lines.size(); i++) if (lines.get(i).keyable()) return i;
        return first;
    }

    private int firstKeyable() {
        for (int i = 0; i < lines.size(); i++) if (lines.get(i).keyable()) return i;
        return -1;
    }

    private int lastKeyable() {
        for (int i = lines.size() - 1; i >= 0; i--) if (lines.get(i).keyable()) return i;
        return -1;
    }

    private void setCursor(int index, boolean reveal) {
        cursor = index;
        TerminalState.cursor(settings)[0] = index;
        if (reveal && index >= 0) reveal(index);
    }

    public Mode mode() {
        if (capturing()) return Mode.BIND;
        return mode;
    }

    public int cursorNumber() {
        return cursor + 1;
    }

    public int lineCount() {
        return lines.size();
    }

    public String description() {
        if (cursor < 0 || cursor >= lines.size()) return "";
        Line l = lines.get(cursor);

        return switch (l.kind) {
            case SETTING -> "# " + (l.setting.description == null ? "" : l.setting.description);
            case SECTION -> "# [" + l.groupKey + "] " + keys(l.count) + ". Click or RMB to fold.";
            case FOLD -> "# [" + l.groupKey + "] is folded. Click to open.";
            case HEADER -> switch (l.header) {
                case BIND -> "# Key that toggles the module. Click, then press a key; ESC clears it.";
                case BIND_MODE -> "# Determines whether the module toggles or remains active only while holding the key.";
                case CHAT -> "# Displays a toggle message in chat when enabled.";
                case RUN -> "# Opens the module's own controls.";
                case THEME -> "# The Meteor GUI theme in use. Click to pick another one.";
                case RESET_LAYOUT -> "# Moves every ClickGUI window back to its default position.";
                case RESET_COLORS -> "# Resets every gui.conf key to its default value.";
                case RESET -> "# Resets every hontun.conf key to its default value.";
            };
            default -> "";
        };
    }

    private static String keys(int n) {
        return n + (n == 1 ? " key" : " keys");
    }

    private boolean capturing() {
        if (module != null && Modules.get() != null && Modules.get().isBinding()) return true;
        for (WHontunKeybind k : keybinds.values()) if (k.isListening()) return true;
        return false;
    }

    @Override
    protected void onCalculateSize() {
        TerminalGrid g = g();
        width = COLUMNS * g.cw;
        height = Math.max(g.lh, lines.size() * g.lh);
    }

    private double clipTop() {
        WView v = getView();
        return v != null ? v.y : y;
    }

    private double clipBottom() {
        WView v = getView();
        return v != null ? v.y + v.height : y + height;
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        if (computeSignature() != signature) rebuild();
        revealPending();

        HontunGuiTheme t = t();
        TerminalGrid g = g();
        double top = clipTop(), bottom = clipBottom();

        for (int i = 0; i < lines.size(); i++) {
            double ly = y + i * g.lh;
            if (ly + g.lh < top || ly > bottom) continue;

            if (!TerminalState.printIn(t, i)) {
                TerminalPaint.reset();
                continue;
            }

            paintLine(t, g, lines.get(i), i, ly);
            TerminalPaint.reset();
        }

        if (popup != null) renderer.absolutePost(() -> paintPopup(renderer));
    }

    private void paintLine(HontunGuiTheme t, TerminalGrid g, Line line, int index, double ly) {
        boolean current = index == cursor && line.keyable();
        int gw = gutter();
        int cc = content();
        double x0 = x + gw * g.cw;
        double ty = g.ty(ly, g.lh);

        if (line.kind == Kind.FOLD) TerminalPaint.rect(x, ly, width, g.lh, current ? TerminalPalette.hl() : TerminalPalette.fold());
        else if (current) TerminalPaint.rect(x, ly, width, g.lh, TerminalPalette.hl());

        String gutter = t.terminalLineNumbers.get() ? String.format(Locale.ROOT, "%4d ", index + 1) : "  ";
        TerminalPaint.text(gutter, x, ty, opaque(current ? TerminalPalette.acc : TerminalPalette.faint));

        switch (line.kind) {
            case BLANK -> {}
            case COMMENT -> {
                TerminalPaint.text(line.comment, x0, ty, opaque(TerminalPalette.dim));
                if (line.commentTail != null) TerminalPaint.text(line.commentTail, x0 + line.comment.length() * g.cw, ty, opaque(TerminalPalette.acc));
            }
            case SECTION -> {
                String name = "[" + line.groupKey + "]";
                TerminalPaint.text(name, x0, ty, opaque(TerminalPalette.acc));
                String tail = keys(line.count) + " [-]";
                TerminalPaint.text(tail, x0 + (cc - tail.length()) * g.cw, ty, opaque(TerminalPalette.faint));
            }
            case FOLD -> {
                String head = "+--- [" + line.groupKey + "] " + keys(line.count) + " ";
                TerminalPaint.text(head, x0, ty, opaque(TerminalPalette.dim));
                int dots = Math.max(0, cc - head.length());
                if (dots > 0) TerminalPaint.text("·".repeat(dots), x0 + head.length() * g.cw, ty, opaque(TerminalPalette.faint));
            }
            case HEADER, SETTING -> paintKeyValue(t, g, line, current, ly, ty, x0, cc);
        }
    }

    private boolean hasBar(Line line) {
        if (line.setting instanceof IntSetting s) return !s.noSlider;
        if (line.setting instanceof DoubleSetting s) return !s.noSlider;
        return false;
    }

    private double barX(Line line) {
        TerminalGrid g = g();
        return x + (gutter() + content() - TerminalBar.SEGMENTS - 2) * g.cw;
    }

    private int valueColumn(Line line) {
        return line.indent + line.keyWidth + 3;
    }

    private double valueX(Line line) {
        return x + (gutter() + valueColumn(line)) * g().cw;
    }

    private void paintKeyValue(HontunGuiTheme t, TerminalGrid g, Line line, boolean current, double ly, double ty, double x0, int cc) {
        double cx = x0 + line.indent * g.cw;
        String key = line.key.length() > line.keyWidth ? line.key.substring(0, Math.max(0, line.keyWidth - 1)) + "~" : line.key;
        TerminalPaint.text(key, cx, ty, opaque(TerminalPalette.fg));
        cx = x0 + (line.indent + line.keyWidth) * g.cw;
        TerminalPaint.text(" = ", cx, ty, opaque(TerminalPalette.dim));
        cx += 3 * g.cw;

        boolean bar = hasBar(line);
        int avail = (bar ? cc - TerminalBar.SEGMENTS - 2 - 1 : cc) - valueColumn(line) - 1;

        List<Seg> segs = segments(line, avail);
        boolean editing = mode == Mode.INSERT && editLine == line;
        boolean first = current && !editing && popup == null && !capturingLine(line);
        boolean blink = TerminalState.blinkOn(t);

        for (Seg seg : segs) {
            if (seg.kind == SWATCH) {
                Color c = line.setting instanceof ColorSetting cs ? cs.get() : null;
                if (c != null) TerminalClickStyle.swatch(g, c, cx, ly + (g.lh - g.px(12)) / 2, true);
                cx += 2 * g.cw;
                continue;
            }

            if (seg.kind == CURSOR) {
                TerminalClickStyle.cursor(t, cx, ly, g.lh, true);
                cx += g.cw;
                continue;
            }

            String s = seg.text;
            if (s.isEmpty()) continue;

            if (first) {
                first = false;
                if (blink) {
                    TerminalPaint.exact(Math.round(cx), Math.round(ly + g.px(1)), Math.round(g.cw), g.lh - 2 * Math.round(g.px(1)), opaque(TerminalPalette.fill));
                    TerminalPaint.text(s.substring(0, 1), cx, ty, opaque(TerminalPalette.ink));
                }
                else {
                    TerminalPaint.text(s.substring(0, 1), cx, ty, opaque(seg.color));
                }
                if (s.length() > 1) TerminalPaint.text(s.substring(1), cx + g.cw, ty, opaque(seg.color));
            }
            else {
                TerminalPaint.text(s, cx, ty, opaque(seg.color));
            }

            cx += s.length() * g.cw;
        }

        if (bar) {
            double fr = barFraction(line);
            TerminalBar.paint(g, barX(line), ly, g.lh, TerminalBar.SEGMENTS, fr);
        }
    }

    private boolean capturingLine(Line line) {
        if (line.kind == Kind.HEADER && line.header == Header.BIND) return module != null && Modules.get().isBinding();
        WHontunKeybind k = line.setting != null ? keybinds.get(line.setting) : null;
        return k != null && k.isListening();
    }

    private double[] range(Setting<?> s) {
        if (s instanceof IntSetting i) return new double[] { i.sliderMin, i.sliderMax, i.min, i.max };
        if (s instanceof DoubleSetting d) return new double[] { d.sliderMin, d.sliderMax, d.min, d.max };
        return new double[] { 0, 1, 0, 1 };
    }

    private double numberValue(Line line) {
        if (dragLine == line && !Double.isNaN(pendingValue)) return pendingValue;
        if (line.setting instanceof IntSetting s) return s.get();
        if (line.setting instanceof DoubleSetting s) return s.get();
        return 0;
    }

    private double barFraction(Line line) {
        double[] r = range(line.setting);
        double span = r[1] - r[0];
        if (span == 0) return 0;
        return Math.max(0, Math.min(1, (numberValue(line) - r[0]) / span));
    }

    static String formatDouble(double v, int decimals) {
        int d = Math.max(0, Math.min(2, decimals));
        String s = String.format(Locale.ROOT, "%." + d + "f", v);
        if (s.contains(".")) {
            s = s.replaceAll("0+$", "");
            if (s.endsWith(".")) s += "0";
        }
        return s;
    }

    private String numberText(Line line) {
        if (line.setting instanceof IntSetting) return String.valueOf((int) Math.round(numberValue(line)));
        if (line.setting instanceof DoubleSetting d) return formatDouble(numberValue(line), d.decimalPlaces);
        return "";
    }

    private List<Seg> segments(Line line, int avail) {
        List<Seg> out = new ArrayList<>(6);
        int acc = TerminalPalette.acc, dim = TerminalPalette.dim;

        if (line.kind == Kind.HEADER) {
            switch (line.header) {
                case BIND -> {
                    if (capturingLine(line)) {
                        out.add(new Seg("<press a key>", acc, TEXT));
                        out.add(new Seg("", 0, CURSOR));
                    }
                    else {
                        String k = TerminalClickStyle.keyText(module.keybind.toString());
                        out.add(new Seg(k, k.equals("<none>") ? dim : acc, TEXT));
                    }
                }
                case BIND_MODE -> out.add(new Seg(module.toggleOnBindRelease ? "Hold" : "Toggle", acc, TEXT));
                case CHAT -> out.add(new Seg(module.chatFeedback ? "true" : "false", module.chatFeedback ? acc : dim, TEXT));
                case RUN -> {
                    out.add(new Seg("[ ", dim, TEXT));
                    out.add(new Seg("open", acc, TEXT));
                    out.add(new Seg(" ]", dim, TEXT));
                }
                case THEME -> out.add(new Seg(trunc(GuiThemes.get().name, Math.max(4, avail)), acc, TEXT));
                case RESET_LAYOUT, RESET_COLORS, RESET -> {
                    out.add(new Seg("[ ", dim, TEXT));
                    out.add(new Seg("run", acc, TEXT));
                    out.add(new Seg(" ]", dim, TEXT));
                }
            }
            return out;
        }

        Setting<?> s = line.setting;

        if (mode == Mode.INSERT && editLine == line) {
            boolean quoted = s instanceof StringSetting && !(s instanceof ProvidedStringSetting);
            if (quoted) out.add(new Seg("\"", dim, TEXT));
            out.add(new Seg(buffer.toString(), acc, TEXT));
            out.add(new Seg("", 0, CURSOR));
            if (quoted) out.add(new Seg("\"", dim, TEXT));
            return out;
        }

        if (capturingLine(line)) {
            out.add(new Seg("<press a key>", acc, TEXT));
            out.add(new Seg("", 0, CURSOR));
            return out;
        }

        if (s instanceof BoolSetting b) {
            boolean v = b.get();
            out.add(new Seg(v ? "true" : "false", v ? acc : dim, TEXT));
        }
        else if (s instanceof IntSetting || s instanceof DoubleSetting) {
            out.add(new Seg(numberText(line), acc, TEXT));
        }
        else if (s instanceof PotionSetting p) {
            out.add(new Seg(trunc(String.valueOf(p.get()), Math.max(4, avail)), acc, TEXT));
        }
        else if (s instanceof EnumSetting<?> e) {
            out.add(new Seg(trunc(String.valueOf(e.get()), Math.max(4, avail)), acc, TEXT));
        }
        else if (s instanceof ProvidedStringSetting p) {
            out.add(new Seg(trunc(String.valueOf(p.get()), Math.max(4, avail)), acc, TEXT));
        }
        else if (s instanceof StringSetting str) {
            String v = str.get() == null ? "" : str.get();
            out.add(new Seg("\"", dim, TEXT));
            out.add(new Seg(trunc(v, Math.max(4, avail - 2)), acc, TEXT));
            out.add(new Seg("\"", dim, TEXT));
        }
        else if (s instanceof KeybindSetting k) {
            String text = TerminalClickStyle.keyText(k.get().toString());
            out.add(new Seg(text, text.equals("<none>") ? dim : acc, TEXT));
        }
        else if (s instanceof ColorSetting c) {
            SettingColor v = c.get();
            out.add(new Seg(String.format(Locale.ROOT, "#%02X%02X%02X", v.r, v.g, v.b), acc, TEXT));
            out.add(new Seg(" ", dim, TEXT));
            out.add(new Seg("", 0, SWATCH));
            out.add(new Seg(" a=" + v.a, dim, TEXT));
        }
        else if (s instanceof FileSetting f) {
            File file = f.get();
            if (file == null || !file.exists()) out.add(new Seg("<none>", dim, TEXT));
            else out.add(new Seg(trunc(file.getName(), Math.max(4, avail)), acc, TEXT));
        }
        else if (s instanceof FontFaceSetting f) {
            out.add(new Seg(trunc(f.get() != null ? f.get().info.family() : "default", Math.max(4, avail)), acc, TEXT));
        }
        else if (s instanceof BlockSetting || s instanceof ItemSetting) {
            out.add(new Seg(trunc(shortName(s.get()), Math.max(4, avail)), acc, TEXT));
        }
        else if (s instanceof GenericSetting<?> || s instanceof BlockDataSetting<?>) {
            out.add(new Seg("[ ", dim, TEXT));
            out.add(new Seg("edit", acc, TEXT));
            out.add(new Seg(" ]", dim, TEXT));
        }
        else if (s.get() instanceof Collection<?> collection) {
            listSegments(out, new ArrayList<>(collection), avail);
        }
        else if (s.get() instanceof Map<?, ?> map) {
            listSegments(out, new ArrayList<>(map.keySet()), avail);
        }
        else {
            out.add(new Seg(trunc(vectorText(s.get()), Math.max(4, avail)), acc, TEXT));
        }

        return out;
    }

    private static String vectorText(Object v) {
        if (v == null) return "null";
        String s = v.toString();
        return s.replaceAll("^[A-Za-z]*[\\[{(]", "").replaceAll("[\\]})]$", "");
    }

    static String shortName(Object o) {
        if (o == null) return "none";
        if (o instanceof Module m) return m.name;
        if (o instanceof String s) return s;
        if (o instanceof Class<?> c) return c.getSimpleName();
        if (o instanceof Color c) return String.format(Locale.ROOT, "#%02X%02X%02X", c.r, c.g, c.b);

        String s = o.toString();
        int colon = s.lastIndexOf(':');
        if (colon >= 0 && colon < s.length() - 1) s = s.substring(colon + 1);
        else {
            int dot = s.lastIndexOf('.');
            if (dot >= 0 && dot < s.length() - 1) s = s.substring(dot + 1);
        }

        int end = 0;
        while (end < s.length()) {
            char ch = s.charAt(end);
            if (!(Character.isLetterOrDigit(ch) || ch == '_' || ch == '.' || ch == '-')) break;
            end++;
        }
        s = s.substring(0, end);
        return s.isEmpty() ? o.getClass().getSimpleName().toLowerCase(Locale.ROOT) : s.toLowerCase(Locale.ROOT);
    }

    private void listSegments(List<Seg> out, List<?> items, int avail) {
        int acc = TerminalPalette.acc, dim = TerminalPalette.dim;
        if (items.isEmpty()) {
            out.add(new Seg("[]", dim, TEXT));
            return;
        }

        out.add(new Seg("[", dim, TEXT));
        int used = 1;
        int i = 0;
        for (; i < items.size(); i++) {
            String name = shortName(items.get(i)).replace(' ', '_');
            int add = (i > 0 ? 2 : 0) + name.length();
            int rest = items.size() - i - 1;
            int tail = rest > 0 ? (" .. +" + rest).length() : 0;
            if (i > 0 && used + add + 1 + tail > avail) break;
            if (i > 0) out.add(new Seg(", ", dim, TEXT));
            out.add(new Seg(trunc(name, Math.max(3, avail - used - 2)), acc, TEXT));
            used += add;
        }
        if (i < items.size()) out.add(new Seg(" .. +" + (items.size() - i), dim, TEXT));
        out.add(new Seg("]", dim, TEXT));
    }

    private int lineAt(double mx, double my) {
        if (mx < x || mx > x + width) return -1;
        if (my < clipTop() || my > clipBottom()) return -1;
        int i = (int) Math.floor((my - y) / g().lh);
        return i >= 0 && i < lines.size() ? i : -1;
    }

    @Override
    public void onMouseMoved(double mouseX, double mouseY, double lastMouseX, double lastMouseY) {
        this.lastMouseX = mouseX;
        this.lastMouseY = mouseY;

        if (dragLine != null) {
            dragBar(mouseX);
            return;
        }

        if (popup != null) {
            if (dragChannel >= 0) dragColor(mouseX);
            else if (!popup.color) {
                int item = popupItemAt(mouseX, mouseY);
                if (item >= 0) popup.hover = item;
            }
            return;
        }

        if (mode != Mode.NORMAL || capturing()) return;

        int i = lineAt(mouseX, mouseY);
        if (i >= 0 && lines.get(i).keyable() && i != cursor) setCursor(i, false);
    }

    @Override
    public boolean onMouseClicked(MouseButtonEvent click, boolean doubled) {
        double mx = click.x(), my = click.y();
        int button = click.button();

        if (popup != null) {
            if (insidePopup(mx, my)) {
                if (button == InputConstants.MOUSE_BUTTON_LEFT) {
                    if (popup.color) {
                        int ch = colorChannelAt(mx, my);
                        if (ch >= 0) {
                            dragChannel = ch;
                            dragColor(mx);
                        }
                    }
                    else {
                        int item = popupItemAt(mx, my);
                        if (item >= 0) pick(item);
                    }
                }
                return true;
            }
            popup = null;
            dragChannel = -1;
        }

        if (capturing()) return false;

        int i = lineAt(mx, my);

        if (mode == Mode.INSERT && (i < 0 || lines.get(i) != editLine)) commitEdit();
        if (i < 0) return false;

        Line line = lines.get(i);
        if (!line.keyable()) return true;

        if (button == InputConstants.MOUSE_BUTTON_RIGHT) {
            SettingGroup group = line.group;
            if (group != null) toggleFold(group);
            return true;
        }

        if (button != InputConstants.MOUSE_BUTTON_LEFT) return false;

        setCursor(i, false);

        if (hasBar(line) && mode == Mode.NORMAL) {
            double bx = barX(line);
            if (mx >= bx && mx <= bx + TerminalBar.width(g(), TerminalBar.SEGMENTS)) {
                dragLine = line;
                dragBar(mx);
                return true;
            }
        }

        if (mode == Mode.INSERT && editLine == line) return true;

        activate(line);
        return true;
    }

    @Override
    public boolean onMouseReleased(MouseButtonEvent click) {
        if (dragLine != null) {
            Line line = dragLine;
            dragLine = null;
            if (!Double.isNaN(pendingValue)) {
                applyNumber(line, pendingValue);
                pendingValue = Double.NaN;
            }
            return true;
        }

        if (dragChannel >= 0) {
            dragChannel = -1;
            return true;
        }

        return false;
    }

    @Override
    public boolean onMouseScrolled(double amount) {
        if (popup != null) return true;

        WView view = getView();
        if (!(view instanceof WViewScrollAccessor scroll)) return false;
        if (!view.mouseOver && lastMouseY >= 0 && (lastMouseY < clipTop() || lastMouseY > clipBottom())) return false;

        double max = scroll.hontun$getActualHeight() - view.height;
        if (max <= 0) return false;

        double lh = g().lh;
        double target = scroll.hontun$getTargetScroll() - Math.signum(amount) * 3 * lh;
        target = Math.round(target / lh) * lh;
        target = Math.max(0, Math.min(max, target));
        scroll.hontun$setTargetScroll(target);
        return true;
    }

    private void revealPending() {
        int index = pendingReveal;
        if (index < 0) return;

        WView view = getView();
        if (!(view instanceof WViewScrollAccessor scroll) || index >= lines.size()) {
            pendingReveal = -1;
            return;
        }

        double actual = scroll.hontun$getActualHeight();
        if (view.height <= 0 || actual <= 0) return;
        pendingReveal = -1;

        double max = actual - view.height;
        if (max <= 0) return;

        double lh = g().lh;
        double current = scroll.hontun$getScroll();
        double lineTop = y + index * lh - view.y + current;
        double target = Math.round((lineTop + lh / 2 - view.height / 2) / lh) * lh;
        target = Math.max(0, Math.min(max, target));
        if (target != scroll.hontun$getTargetScroll()) scroll.hontun$setTargetScroll(target);
    }

    private boolean textBoxFocused() {
        WWidget root = this;
        while (root.parent != null) root = root.parent;
        return focusedTextBox(root);
    }

    private static boolean focusedTextBox(WWidget widget) {
        if (widget instanceof WTextBox box) return box.isFocused();
        if (widget instanceof WContainer container) {
            for (Cell<?> cell : container.cells) {
                if (focusedTextBox(cell.widget())) return true;
            }
        }
        return false;
    }

    private void reveal(int index) {
        WView view = getView();
        if (!(view instanceof WViewScrollAccessor scroll)) return;

        double max = scroll.hontun$getActualHeight() - view.height;
        if (max <= 0) return;

        double lh = g().lh;
        double ly = y + index * lh;
        double current = scroll.hontun$getTargetScroll();
        double target = current;

        double pad = Math.round(g().px(4));
        if (ly < view.y) target = current - (view.y - ly) - pad;
        else if (ly + lh > view.y + view.height) target = current + (ly + lh - view.y - view.height) + pad;

        target = Math.max(0, Math.min(max, target));
        if (target != current) scroll.hontun$setTargetScroll(target);
    }

    @Override
    public boolean onKeyPressed(KeyEvent input) {
        return key(input, false);
    }

    @Override
    public boolean onKeyRepeated(KeyEvent input) {
        return key(input, true);
    }

    private boolean key(KeyEvent input, boolean repeat) {
        int k = input.key();
        int mods = input.modifiers();

        if (capturing()) return true;
        if (popup == null && mode == Mode.NORMAL && textBoxFocused()) return false;
        if ((mods & InputConstants.MOD_CONTROL) != 0 && mode == Mode.NORMAL && popup == null) return false;

        if (popup != null) {
            if (k == InputConstants.KEY_ESCAPE) {
                popup = null;
                dragChannel = -1;
                return true;
            }
            if (!popup.color && popup.items.length > 0) {
                int n = popup.items.length;
                if (k == InputConstants.KEY_DOWN || k == InputConstants.KEY_J) popup.hover = (popup.hover + 1) % n;
                else if (k == InputConstants.KEY_UP || k == InputConstants.KEY_K) popup.hover = (popup.hover - 1 + n) % n;
                else if (!repeat && (k == InputConstants.KEY_RETURN || k == InputConstants.KEY_NUMPADENTER || k == InputConstants.KEY_SPACE)) {
                    pick(popup.hover);
                    swallowChar = k == InputConstants.KEY_SPACE;
                }
            }
            return true;
        }

        if (mode == Mode.INSERT) {
            if (k == InputConstants.KEY_ESCAPE) {
                mode = Mode.NORMAL;
                editLine = null;
            }
            else if (k == InputConstants.KEY_RETURN || k == InputConstants.KEY_NUMPADENTER || k == InputConstants.KEY_TAB) commitEdit();
            else if (k == InputConstants.KEY_BACKSPACE && !buffer.isEmpty()) buffer.setLength(buffer.length() - 1);
            return true;
        }

        if (k == InputConstants.KEY_DOWN || k == InputConstants.KEY_J) {
            move(1);
            return true;
        }
        if (k == InputConstants.KEY_UP || k == InputConstants.KEY_K) {
            move(-1);
            return true;
        }
        if (k == InputConstants.KEY_LEFT || k == InputConstants.KEY_H) {
            adjust(-1);
            return true;
        }
        if (k == InputConstants.KEY_RIGHT || k == InputConstants.KEY_L) {
            adjust(1);
            return true;
        }

        if (repeat) return false;

        if (k == InputConstants.KEY_RETURN || k == InputConstants.KEY_NUMPADENTER || k == InputConstants.KEY_SPACE) {
            if (cursor >= 0 && cursor < lines.size()) {
                swallowChar = k == InputConstants.KEY_SPACE;
                activate(lines.get(cursor));
            }
            return true;
        }
        if (k == InputConstants.KEY_Z) {
            if (cursor >= 0 && cursor < lines.size() && lines.get(cursor).group != null) toggleFold(lines.get(cursor).group);
            return true;
        }
        if (k == InputConstants.KEY_G) {
            boolean shift = (mods & InputConstants.MOD_SHIFT) != 0;
            int i = shift ? lastKeyable() : firstKeyable();
            if (i >= 0) setCursor(i, true);
            return true;
        }

        return false;
    }

    @Override
    public boolean onCharTyped(CharacterEvent input) {
        if (popup == null && mode == Mode.NORMAL && textBoxFocused()) {
            swallowChar = false;
            return false;
        }

        if (swallowChar) {
            swallowChar = false;
            if (input.codepoint() == ' ') return true;
        }

        if (mode != Mode.INSERT || editLine == null) return false;

        int cp = input.codepoint();
        if (cp < ' ') return true;

        Setting<?> s = editLine.setting;
        boolean numeric = s instanceof IntSetting || s instanceof DoubleSetting;
        if (numeric && !(Character.isDigit(cp) || cp == '.' || cp == '-')) return true;
        if (buffer.length() >= 48) return true;
        if (s == t().terminalPrompt && buffer.length() >= 3) return true;
        if (s instanceof StringSetting str && str.filter != null && !str.filter.filter(buffer.toString(), (char) cp)) return true;

        buffer.appendCodePoint(cp);
        return true;
    }

    private void move(int dir) {
        int i = cursor;
        do {
            i += dir;
        } while (i >= 0 && i < lines.size() && !lines.get(i).keyable());
        if (i >= 0 && i < lines.size()) setCursor(i, true);
    }

    private void toggleFold(SettingGroup group) {
        group.sectionExpanded = !group.sectionExpanded;
        rebuild();
        for (int i = 0; i < lines.size(); i++) {
            Line l = lines.get(i);
            if (l.group == group && (l.kind == Kind.SECTION || l.kind == Kind.FOLD)) {
                setCursor(i, true);
                break;
            }
        }
    }

    private void changed() {
        TerminalState.dirty(dirtyKey(), true);
    }

    @SuppressWarnings("unchecked")
    private static <T> void setRaw(Setting<?> s, Object value) {
        ((Setting<T>) s).set((T) value);
    }

    private void activate(Line line) {
        switch (line.kind) {
            case SECTION, FOLD -> toggleFold(line.group);
            case HEADER -> activateHeader(line);
            case SETTING -> activateSetting(line);
            default -> {}
        }
    }

    private void activateHeader(Line line) {
        switch (line.header) {
            case BIND -> {
                Modules.get().setModuleToBind(module);
                TerminalLog.post(module.name + ": press a key to bind, ESC clears it");
            }
            case BIND_MODE -> openEnum(line, new String[] { "Toggle", "Hold" }, new Object[] { Boolean.FALSE, Boolean.TRUE }, module.toggleOnBindRelease ? 1 : 0);
            case CHAT -> {
                module.chatFeedback = !module.chatFeedback;
                changed();
            }
            case RUN -> {
                WWidget widget = module.getWidget(t());
                if (widget != null) mc.gui.setScreen(new TerminalSettingScreen(t(), module.name, widget));
            }
            case THEME -> {
                String[] names = GuiThemes.getNames();
                String now = GuiThemes.get().name;
                int current = 0;
                for (int i = 0; i < names.length; i++) if (names[i].equals(now)) current = i;
                openEnum(line, names.clone(), names.clone(), current);
            }
            case RESET_LAYOUT -> {
                t().clearWindowConfigs();
                TerminalLog.post("reset-layout: window positions cleared");
            }
            case RESET_COLORS -> {
                t().settings.reset();
                reopenGuiTab();
            }
            case RESET -> {
                t().hontunSettings.reset();
                HontunTab.reopen();
            }
        }
    }

    private static void reopenGuiTab() {
        mc.gui.setScreen(null);
        Tabs.get(GuiTab.class).openScreen(GuiThemes.get());
    }

    private void activateSetting(Line line) {
        Setting<?> s = line.setting;
        HontunGuiTheme t = t();

        if (s instanceof BoolSetting b) {
            b.set(!b.get());
            changed();
        }
        else if (s instanceof IntSetting || s instanceof DoubleSetting) startEdit(line, numberText(line));
        else if (s instanceof PotionSetting p) mc.gui.setScreen(new PotionSettingScreen(t, p));
        else if (s instanceof EnumSetting<?> e) {
            Object v = e.get();
            Object[] values = ((Enum<?>) v).getDeclaringClass().getEnumConstants();
            String[] names = new String[values.length];
            int current = 0;
            for (int i = 0; i < values.length; i++) {
                names[i] = String.valueOf(values[i]);
                if (values[i] == v) current = i;
            }
            openEnum(line, names, values, current);
        }
        else if (s instanceof ProvidedStringSetting p) {
            String[] values = p.supplier.get();
            if (values == null || values.length == 0) {
                startEdit(line, p.get());
                return;
            }
            int current = 0;
            for (int i = 0; i < values.length; i++) if (values[i].equals(p.get())) current = i;
            openEnum(line, values.clone(), values.clone(), current);
        }
        else if (s instanceof StringSetting str) startEdit(line, str.get() == null ? "" : str.get());
        else if (s instanceof KeybindSetting k) startBind(k);
        else if (s instanceof ColorSetting) openColor(line);
        else if (s instanceof BlockListSetting b) mc.gui.setScreen(new BlockListSettingScreen(t, b));
        else if (s instanceof ItemListSetting i) mc.gui.setScreen(new ItemListSettingScreen(t, i));
        else if (s instanceof EntityTypeListSetting e) mc.gui.setScreen(t.hontunEntityTypeListScreen.get() ? new HontunEntityTypeListSettingScreen(t, e) : new EntityTypeListSettingScreen(t, e));
        else if (s instanceof EnchantmentListSetting e) mc.gui.setScreen(new EnchantmentListSettingScreen(t, e));
        else if (s instanceof ModuleListSetting m) mc.gui.setScreen(new ModuleListSettingScreen(t, m));
        else if (s instanceof PacketListSetting p) mc.gui.setScreen(new PacketBoolSettingScreen(t, p));
        else if (s instanceof ParticleTypeListSetting p) mc.gui.setScreen(new ParticleTypeListSettingScreen(t, p));
        else if (s instanceof SoundEventListSetting p) mc.gui.setScreen(new SoundEventListSettingScreen(t, p));
        else if (s instanceof StatusEffectAmplifierMapSetting p) mc.gui.setScreen(new StatusEffectAmplifierMapSettingScreen(t, p));
        else if (s instanceof StatusEffectListSetting p) mc.gui.setScreen(new StatusEffectListSettingScreen(t, p));
        else if (s instanceof StorageBlockListSetting p) mc.gui.setScreen(new StorageBlockListSettingScreen(t, p));
        else if (s instanceof ScreenHandlerListSetting p) mc.gui.setScreen(new ScreenHandlerSettingScreen(t, p));
        else if (s instanceof BlockDataSetting<?> b) mc.gui.setScreen(new BlockDataSettingScreen<>(t, b));
        else if (s instanceof GenericSetting<?> gs) mc.gui.setScreen(gs.createScreen(t));
        else if (s instanceof BlockSetting b) mc.gui.setScreen(new BlockSettingScreen(t, b));
        else if (s instanceof ItemSetting i) mc.gui.setScreen(new ItemSettingScreen(t, i));
        else if (s instanceof FontFaceSetting f) mc.gui.setScreen(new FontFaceSettingScreen(t, f));
        else {
            Settings single = new Settings();
            single.createGroup(line.group != null ? line.group.name : "General").add(s);
            mc.gui.setScreen(new TerminalSettingScreen(t, s.name, t.classicSettings(single, "")));
        }
    }

    private void startBind(KeybindSetting setting) {
        WHontunKeybind widget = keybinds.computeIfAbsent(setting, s -> {
            WHontunKeybind k = t().hontunKeybind(setting.title, setting.get(), setting.getDefaultValue());
            k.init();
            k.action = setting::onChanged;
            return k;
        });

        setting.widget = widget;
        if (widget.button() != null && widget.button().action != null) widget.button().action.run();
        TerminalLog.post(setting.name + ": press a key to bind, ESC clears it");
    }

    private void startEdit(Line line, String text) {
        popup = null;
        mode = Mode.INSERT;
        editLine = line;
        buffer.setLength(0);
        buffer.append(text == null ? "" : text);
    }

    private void commitEdit() {
        Line line = editLine;
        mode = Mode.NORMAL;
        editLine = null;
        if (line == null || line.setting == null) return;

        Setting<?> s = line.setting;
        String text = buffer.toString();

        if (s instanceof IntSetting || s instanceof DoubleSetting) {
            try {
                applyNumber(line, Double.parseDouble(text.trim()));
            } catch (NumberFormatException ignored) {
            }
            return;
        }

        if (s instanceof StringSetting str) {
            String value = s == t().terminalPrompt && text.length() > 3 ? text.substring(0, 3) : text;
            if (str.set(value)) {
                changed();
                TerminalLog.post(s.name + " = \"" + value + "\"");
            }
        }
    }

    private double roundFor(Line line, double v) {
        double[] r = range(line.setting);
        if (line.setting instanceof IntSetting) return Math.round(v);
        int dec = (r[1] - r[0]) >= 20 ? 1 : 2;
        double p = Math.pow(10, dec);
        return Math.round(v * p) / p;
    }

    private double clampFor(Line line, double v) {
        double[] r = range(line.setting);
        return Math.max(r[2], Math.min(r[3], v));
    }

    private void applyNumber(Line line, double v) {
        v = clampFor(line, v);

        if (line.setting instanceof IntSetting i) {
            if (i.set((int) Math.round(v))) changed();
        }
        else if (line.setting instanceof DoubleSetting d) {
            if (d.set(v)) changed();
        }
    }

    private void dragBar(double mouseX) {
        Line line = dragLine;
        if (line == null) return;

        double fr = TerminalBar.fraction(g(), barX(line), TerminalBar.SEGMENTS, mouseX);
        double[] r = range(line.setting);
        double v = clampFor(line, roundFor(line, r[0] + fr * (r[1] - r[0])));

        if (line.setting instanceof DoubleSetting d && d.onSliderRelease) pendingValue = v;
        else applyNumber(line, v);
    }

    private void adjust(int dir) {
        if (cursor < 0 || cursor >= lines.size()) return;
        Line line = lines.get(cursor);

        if (line.kind == Kind.SECTION || line.kind == Kind.FOLD) {
            toggleFold(line.group);
            return;
        }

        if (line.kind == Kind.HEADER) {
            if (line.header == Header.CHAT) {
                module.chatFeedback = !module.chatFeedback;
                changed();
            }
            else if (line.header == Header.BIND_MODE) {
                module.toggleOnBindRelease = !module.toggleOnBindRelease;
                changed();
            }
            return;
        }

        Setting<?> s = line.setting;
        if (s == null) return;

        if (s instanceof IntSetting) applyNumber(line, numberValue(line) + dir);
        else if (s instanceof DoubleSetting) {
            double[] r = range(s);
            double exponent = Math.floor(Math.log10(Math.max(1e-6, (r[1] - r[0]) / 40)));
            double step = Math.pow(10, exponent);
            double p = Math.pow(10, Math.max(0, -exponent));
            applyNumber(line, Math.round((numberValue(line) + dir * step) * p) / p);
        }
        else if (s instanceof BoolSetting b) {
            b.set(!b.get());
            changed();
        }
        else if (s instanceof EnumSetting<?> e && !(s instanceof PotionSetting)) {
            Object v = e.get();
            Object[] values = ((Enum<?>) v).getDeclaringClass().getEnumConstants();
            int i = 0;
            for (int j = 0; j < values.length; j++) if (values[j] == v) i = j;
            setRaw(s, values[(i + dir + values.length) % values.length]);
            changed();
        }
    }

    private void openEnum(Line line, String[] items, Object[] values, int current) {
        popup = new Popup(line, false, items, values, current);
        layoutPopup();
    }

    private void openColor(Line line) {
        popup = new Popup(line, true, new String[0], new Object[0], -1);
        layoutPopup();
    }

    private void pick(int index) {
        Popup p = popup;
        popup = null;
        if (p == null || index < 0 || index >= p.values.length) return;

        Line line = p.line;
        Object value = p.values[index];

        if (line.kind == Kind.HEADER && line.header == Header.BIND_MODE) {
            module.toggleOnBindRelease = (Boolean) value;
            changed();
            return;
        }

        if (line.kind == Kind.HEADER && line.header == Header.THEME) {
            String name = String.valueOf(value);
            if (name.equals(GuiThemes.get().name)) return;
            GuiThemes.select(name);
            reopenGuiTab();
            return;
        }

        if (line.setting != null) {
            setRaw(line.setting, value);
            changed();
        }
    }

    private void layoutPopup() {
        Popup p = popup;
        if (p == null) return;

        TerminalGrid g = g();
        int index = lines.indexOf(p.line);
        if (index < 0) {
            popup = null;
            return;
        }

        double pad = g.px(3);
        double ly = y + index * g.lh;

        if (p.color) {
            p.w = 24 * g.cw;
            p.h = pad + Math.round(g.px(22)) + pad + channels(p) * Math.round(g.px(20)) + pad;
        }
        else {
            int longest = 0;
            for (String s : p.items) longest = Math.max(longest, s.length());
            p.w = Math.max(g.px(120), (longest + 2) * g.cw);
            p.h = pad + p.items.length * g.lh + pad;
        }

        double sw = meteordevelopment.meteorclient.utils.Utils.getWindowWidth();
        double sh = meteordevelopment.meteorclient.utils.Utils.getWindowHeight();
        double px = valueX(p.line) - g.cw;
        double py = ly + g.lh + pad;
        if (py + p.h > sh - g.tb - g.px(4)) py = ly - p.h - pad;
        px = Math.max(g.px(4), Math.min(sw - p.w - g.px(4), px));
        py = Math.max(g.px(4), py);

        p.x = Math.round(px);
        p.y = Math.round(py);
    }

    private int channels(Popup p) {
        return 4;
    }

    private boolean insidePopup(double mx, double my) {
        Popup p = popup;
        return p != null && mx >= p.x && mx <= p.x + p.w && my >= p.y && my <= p.y + p.h;
    }

    private int popupItemAt(double mx, double my) {
        Popup p = popup;
        if (p == null || p.color || !insidePopup(mx, my)) return -1;
        TerminalGrid g = g();
        int i = (int) Math.floor((my - p.y - g.px(3)) / g.lh);
        return i >= 0 && i < p.items.length ? i : -1;
    }

    private double channelRowY(int channel) {
        TerminalGrid g = g();
        double pad = g.px(3);
        return popup.y + pad + Math.round(g.px(22)) + pad + channel * Math.round(g.px(20));
    }

    private int colorChannelAt(double mx, double my) {
        Popup p = popup;
        if (p == null || !p.color) return -1;
        TerminalGrid g = g();
        double rowH = Math.round(g.px(20));
        for (int c = 0; c < channels(p); c++) {
            double ry = channelRowY(c);
            if (my >= ry && my < ry + rowH) {
                double bx = p.x + 3 * g.cw;
                if (mx >= bx - g.cw && mx <= bx + TerminalBar.width(g, TerminalBar.SEGMENTS) + g.cw) return c;
            }
        }
        return -1;
    }

    private void dragColor(double mx) {
        Popup p = popup;
        if (p == null || !p.color || dragChannel < 0 || !(p.line.setting instanceof ColorSetting cs)) return;

        TerminalGrid g = g();
        double fr = TerminalBar.fraction(g, p.x + 3 * g.cw, TerminalBar.SEGMENTS, mx);
        int v = (int) Math.round(fr * 255);

        SettingColor c = cs.get();
        int r = c.r, gr = c.g, b = c.b, a = c.a;
        switch (dragChannel) {
            case 0 -> r = v;
            case 1 -> gr = v;
            case 2 -> b = v;
            default -> a = v;
        }

        if (r == c.r && gr == c.g && b == c.b && a == c.a) return;

        SettingColor next = new SettingColor(r, gr, b, a);
        next.rainbow(c.rainbow);
        cs.set(next);
        changed();
        TerminalLog.post(cs.name + " = " + String.format(Locale.ROOT, "#%02X%02X%02X", r, gr, b));
    }

    private void paintPopup(GuiRenderer renderer) {
        Popup p = popup;
        if (p == null) return;

        layoutPopup();
        p = popup;
        if (p == null) return;

        TerminalGrid g = g();
        double margin = g.px(40);
        renderer.scissorStart(p.x - margin, p.y - margin, p.w + 2 * margin, p.h + 2 * margin);

        TerminalClickStyle.popupFrame(g, p.x, p.y, p.w, p.h);
        double pad = g.px(3);

        if (!p.color) {
            for (int i = 0; i < p.items.length; i++) {
                double iy = p.y + pad + i * g.lh;
                boolean current = i == p.current;
                if (current) TerminalPaint.rect(p.x, iy, p.w, g.lh, opaque(TerminalPalette.fill));
                if (i == p.hover && !current) TerminalPaint.rect(p.x, iy, p.w, g.lh, TerminalPalette.hl());
                else if (i == p.hover) TerminalPaint.rect(p.x, iy, p.w, g.lh, argb(0x30, TerminalPalette.ink));
                TerminalPaint.text(p.items[i], p.x + g.cw, g.ty(iy, g.lh), opaque(current ? TerminalPalette.ink : TerminalPalette.fg));
            }
        }
        else if (p.line.setting instanceof ColorSetting cs) {
            SettingColor c = cs.get();
            double hy = p.y + pad;
            double hh = Math.round(g.px(22));
            double ty = g.ty(hy, hh);
            String hex = String.format(Locale.ROOT, "#%02X%02X%02X", c.r, c.g, c.b);
            TerminalPaint.text("color ", p.x + g.cw, ty, opaque(TerminalPalette.dim));
            TerminalPaint.text(hex, p.x + 7 * g.cw, ty, opaque(TerminalPalette.acc));
            TerminalClickStyle.swatch(g, c, p.x + 15 * g.cw, hy + (hh - g.px(12)) / 2, true);
            TerminalPaint.hline(p.x, hy + hh - 1, p.w, opaque(TerminalPalette.line));

            String[] names = { "r", "g", "b", "a" };
            int[] values = { c.r, c.g, c.b, c.a };
            double rowH = Math.round(g.px(20));
            for (int ch = 0; ch < 4; ch++) {
                double ry = channelRowY(ch);
                TerminalPaint.text(names[ch] + " ", p.x + g.cw, g.ty(ry, rowH), opaque(TerminalPalette.dim));
                TerminalBar.paint(g, p.x + 3 * g.cw, ry, rowH, TerminalBar.SEGMENTS, values[ch] / 255.0);
                TerminalPaint.text(String.format(Locale.ROOT, "%4d", values[ch]), p.x + 17 * g.cw, g.ty(ry, rowH), opaque(TerminalPalette.acc));
            }
        }

        renderer.scissorEnd();
    }

    public boolean consumesEscape() {
        return popup != null || mode == Mode.INSERT || capturing();
    }

    public void reload() {
        runChecked = false;
        rebuild();
    }

    public WidgetScreen screen() {
        return mc.gui.screen() instanceof WidgetScreen ws ? ws : null;
    }
}
