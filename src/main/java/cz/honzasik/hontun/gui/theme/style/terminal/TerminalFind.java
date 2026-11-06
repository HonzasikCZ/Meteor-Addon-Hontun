package cz.honzasik.hontun.gui.theme.style.terminal;

import com.mojang.blaze3d.platform.InputConstants;
import cz.honzasik.hontun.gui.theme.HontunGuiTheme;
import cz.honzasik.hontun.gui.theme.HontunWidget;
import cz.honzasik.hontun.gui.theme.style.FooterKind;
import cz.honzasik.hontun.gui.theme.widgets.container.WHontunWindow;
import cz.honzasik.hontun.gui.util.search.SearchUtils;
import cz.honzasik.hontun.gui.util.search.results.ModuleSearchResult;
import cz.honzasik.hontun.gui.util.search.results.SettingSearchResult;
import meteordevelopment.meteorclient.gui.WidgetScreen;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.utils.Cell;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.KeybindSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static cz.honzasik.hontun.gui.theme.style.terminal.TerminalPalette.opaque;
import static meteordevelopment.meteorclient.MeteorClient.mc;

public class TerminalFind extends WWidget implements HontunWidget {
    private static final int COLUMNS = 76;
    private static final int MAX_ROWS = 18;

    private record Row(String note, int noteColor, Module module, Setting<?> setting, String alias, String display, int[] marks) {
        boolean result() {
            return module != null;
        }
    }

    private final StringBuilder query = new StringBuilder();
    private final List<Row> rows = new ArrayList<>();
    private final List<Integer> results = new ArrayList<>();
    private int modules;
    private int settings;
    private int selected;
    private int scroll;
    private int visibleRows = 3;
    private double windowTop;

    public TerminalFind(String initial) {
        if (initial != null) query.append(initial);
    }

    public static void build(HontunGuiTheme t, WidgetScreen screen, String query) {
        TerminalGrid g = TerminalGrid.of(t);
        if (t.style() instanceof TerminalClickStyle style) style.syncMetrics(t);

        screen.add(t.topBar()).top().centerX();

        WHontunWindow window = (WHontunWindow) t.window(null, "find");
        window.subtitle = "  ~/.hontun/modules";
        window.padding = 0;

        double top = g.tb + Math.round(Utils.getWindowHeight() * 0.09);
        screen.add(window).top().centerX().marginTop(g.unscaled(top));
        window.view.maxHeight = Double.MAX_VALUE / 4;

        TerminalFind find = new TerminalFind(query);
        find.windowTop = top;
        window.add(find).expandX();

        for (Cell<?> cell : window.cells) {
            if (cell.widget() == window.view) cell.padTop(0).padBottom(0);
        }

        WWidget footer = t.style().footer(t, FooterKind.SEARCH);
        if (footer != null) screen.add(footer).bottom().expandX();
    }

    @Override
    public void init() {
        focused = true;
        refresh();
    }

    @Override
    public boolean isFocused() {
        return true;
    }

    private String names(Module m) {
        return theme().terminalNames.get() == TerminalNames.Title ? m.title : m.name;
    }

    private void refresh() {
        rows.clear();
        results.clear();
        modules = 0;
        settings = 0;

        String q = query.toString().trim().toLowerCase(Locale.ROOT);
        boolean ids = theme().terminalNames.get() == TerminalNames.Id;

        if (q.isEmpty()) {
            List<Module> active = new ArrayList<>(Modules.get().getActive());
            active.sort((a, b) -> String.CASE_INSENSITIVE_ORDER.compare(a.name, b.name));
            rows.add(new Row("# no pattern yet, listing the " + active.size() + " enabled modules", TerminalPalette.dim, null, null, null, null, null));
            for (Module m : active) addResult(new Row(null, 0, m, null, null, names(m), new int[0]));
        }
        else {
            String norm = ids ? q.replace(' ', '-') : q.replace('-', ' ');

            for (ModuleSearchResult r : SearchUtils.searchModules(query.toString().trim(), 40)) {
                if (rows.size() >= 80) break;
                String display = names(r.module());
                String alias = r.hasAlias() ? r.alias().toLowerCase(Locale.ROOT).replace(' ', '-') : null;
                addResult(new Row(null, 0, r.module(), null, alias, display, marks(norm, display.toLowerCase(Locale.ROOT))));
                modules++;
            }

            if (q.length() >= 2) {
                boolean header = false;
                for (SettingSearchResult r : SearchUtils.searchSettings(query.toString().trim(), 40)) {
                    if (settings >= 40) break;
                    Setting<?> s = r.setting();
                    if (s.module == null || !s.isVisible()) continue;
                    if (!header) {
                        header = true;
                        if (!rows.isEmpty()) rows.add(new Row("# settings", TerminalPalette.faint, null, null, null, null, null));
                    }
                    addResult(new Row(null, 0, s.module, s, null, s.name, marks(q.replace(' ', '-'), s.name.toLowerCase(Locale.ROOT))));
                    settings++;
                }
            }

            if (results.isEmpty()) rows.add(new Row("find: '" + query + "': no such module or setting", TerminalPalette.dim, null, null, null, null, null));
        }

        selected = Math.max(0, Math.min(selected, results.size() - 1));
        scroll = 0;
        reveal();
        invalidate();
    }

    private void addResult(Row row) {
        results.add(rows.size());
        rows.add(row);
    }

    static int[] marks(String q, String s) {
        if (q.isEmpty()) return new int[0];

        int i = s.indexOf(q);
        if (i >= 0) {
            int[] out = new int[q.length()];
            for (int k = 0; k < q.length(); k++) out[k] = i + k;
            return out;
        }

        if (q.length() < 3) return new int[0];

        int[] out = new int[q.length()];
        int j = 0, last = -1, gaps = 0;
        for (int k = 0; k < s.length() && j < q.length(); k++) {
            if (s.charAt(k) == q.charAt(j)) {
                out[j] = k;
                if (last >= 0) gaps += k - last - 1;
                last = k;
                j++;
            }
        }

        if (j < q.length() || gaps > q.length()) return new int[0];
        return out;
    }

    private double maxResultsHeight() {
        TerminalGrid g = TerminalGrid.of(theme());
        double screenH = Utils.getWindowHeight();
        double maxWindow = Math.min(screenH - g.tb - g.px(14) - windowTop, g.tb + g.qh + 1 + g.ml + MAX_ROWS * g.lh + g.px(8));
        return maxWindow - g.tb - g.qh - 1 - g.ml - 2 * g.px(4);
    }

    @Override
    protected void onCalculateSize() {
        TerminalGrid g = TerminalGrid.of(theme());
        int fit = Math.max(3, (int) Math.floor(maxResultsHeight() / g.lh));
        visibleRows = Math.max(1, Math.min(fit, rows.size()));
        width = COLUMNS * g.cw;
        height = g.qh + 1 + visibleRows * g.lh + 2 * g.px(4) + g.ml;
    }

    private double resultsTop() {
        TerminalGrid g = TerminalGrid.of(theme());
        return y + g.qh + 1 + g.px(4);
    }

    private void reveal() {
        if (results.isEmpty()) return;
        int row = results.get(selected);
        if (row < scroll) scroll = row;
        if (row >= scroll + visibleRows) scroll = row - visibleRows + 1;
        if (selected == 0) scroll = 0;
        scroll = Math.max(0, Math.min(scroll, Math.max(0, rows.size() - visibleRows)));
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        HontunGuiTheme t = theme();
        TerminalGrid g = TerminalGrid.of(t);

        double ty = g.ty(y, g.qh);
        String prompt = t.terminalPromptText();
        double cx = x + g.cw;
        TerminalPaint.text(prompt, cx, ty, opaque(TerminalPalette.acc));
        cx += prompt.length() * g.cw;
        TerminalPaint.text(" find ", cx, ty, opaque(TerminalPalette.fg));
        cx += 6 * g.cw;
        String q = query.toString();
        TerminalPaint.text(q, cx, ty, opaque(TerminalPalette.fg));
        cx += q.length() * g.cw;
        TerminalClickStyle.cursor(t, cx, y, g.qh, true);

        String hint = "esc to close";
        TerminalPaint.text(hint, x + width - g.cw - hint.length() * g.cw, ty, opaque(TerminalPalette.dim));
        TerminalPaint.hline(x, y + g.qh, width, opaque(TerminalPalette.line));

        double top = resultsTop();
        double areaH = visibleRows * g.lh;
        renderer.scissorStart(x, top - g.px(4), width, areaH + 2 * g.px(4));

        boolean brackets = t.terminalMarker.get() == TerminalMarker.Brackets;
        int selectedRow = results.isEmpty() ? -1 : results.get(selected);

        for (int i = scroll; i < Math.min(rows.size(), scroll + visibleRows); i++) {
            Row row = rows.get(i);
            double ry = top + (i - scroll) * g.lh;

            if (!TerminalState.printIn(t, i - scroll)) {
                TerminalPaint.reset();
                continue;
            }

            if (!row.result()) {
                TerminalPaint.text(WTermConfig.trunc(row.note, COLUMNS - 2), x + g.cw, g.ty(ry, g.lh), opaque(row.noteColor));
                TerminalPaint.reset();
                continue;
            }

            if (i == selectedRow) TerminalPaint.rect(x, ry, width, g.lh, TerminalPalette.hl());
            paintRow(t, g, row, ry, brackets);
            TerminalPaint.reset();
        }

        renderer.scissorEnd();

        double my = top + areaH + g.px(4);
        TerminalPaint.hline(x, my, width, opaque(TerminalPalette.line));
        String message = q.trim().isEmpty()
                ? "# type to search " + Modules.get().getAll().size() + " modules and their settings"
                : "# " + modules + (modules == 1 ? " module, " : " modules, ") + settings + (settings == 1 ? " setting" : " settings") + "  ·  lmb toggle  ·  rmb settings";
        TerminalPaint.text(WTermConfig.trunc(message, COLUMNS - 2), x + g.cw, g.ty(my + 1, g.ml - 1), opaque(TerminalPalette.dim));
    }

    private void paintRow(HontunGuiTheme t, TerminalGrid g, Row row, double ry, boolean brackets) {
        double ty = g.ty(ry, g.lh);
        boolean moduleRow = row.setting == null;
        boolean on = moduleRow && row.module.isActive();

        if (moduleRow && brackets) {
            TerminalPaint.text("[", x + g.cw, ty, opaque(TerminalPalette.dim));
            TerminalPaint.text(on ? "x" : " ", x + 2 * g.cw, ty, opaque(TerminalPalette.acc));
            TerminalPaint.text("]", x + 3 * g.cw, ty, opaque(TerminalPalette.dim));
        }
        else if (on) {
            TerminalPaint.exact(Math.round(x + g.cw), Math.round(ry + g.px(2)), Math.round(g.cw), Math.round(g.px(14)), opaque(TerminalPalette.fill));
        }

        int col = brackets ? 5 : 3;
        String cat = WTermConfig.key(row.module.category.name) + "/";
        TerminalPaint.text(cat, x + col * g.cw, ty, opaque(TerminalPalette.dim));
        col += cat.length();

        if (!moduleRow) {
            String owner = names(row.module) + ".";
            TerminalPaint.text(owner, x + col * g.cw, ty, opaque(TerminalPalette.dim));
            col += owner.length();
        }

        int base = moduleRow ? (on ? TerminalPalette.acc : TerminalPalette.fg) : TerminalPalette.fg;
        highlighted(g, row.display, row.marks, x + col * g.cw, ty, base);
        col += row.display.length();

        if (moduleRow) {
            if (row.alias != null) {
                String alias = " (" + row.alias + ")";
                TerminalPaint.text(alias, x + col * g.cw, ty, opaque(TerminalPalette.faint));
                col += alias.length();
            }

            int rem = COLUMNS - col - 3;
            if (rem > 10 && row.module.description != null) {
                TerminalPaint.text("  " + WTermConfig.trunc(row.module.description, rem), x + col * g.cw, ty, opaque(TerminalPalette.faint));
            }
        }
        else {
            TerminalPaint.text(" = ", x + col * g.cw, ty, opaque(TerminalPalette.dim));
            col += 3;
            String value = shortValue(row.setting);
            TerminalPaint.text(WTermConfig.trunc(value, Math.max(3, COLUMNS - col - 1)), x + col * g.cw, ty, opaque(TerminalPalette.acc));
        }
    }

    private void highlighted(TerminalGrid g, String text, int[] marks, double x0, double ty, int base) {
        if (marks == null || marks.length == 0) {
            TerminalPaint.text(text, x0, ty, opaque(base));
            return;
        }

        boolean[] hit = new boolean[text.length()];
        for (int m : marks) if (m >= 0 && m < hit.length) hit[m] = true;

        double underline = Math.round(Math.min(ty + Math.round(g.th * 0.78) + g.px(3), ty + g.th + g.px(2)));
        int start = 0;
        while (start < text.length()) {
            int end = start;
            while (end < text.length() && hit[end] == hit[start]) end++;

            double sx = x0 + start * g.cw;
            TerminalPaint.text(text.substring(start, end), sx, ty, opaque(hit[start] ? TerminalPalette.acc : base));
            if (hit[start]) TerminalPaint.hline(sx, underline, (end - start) * g.cw, opaque(TerminalPalette.acc));

            start = end;
        }
    }

    static String shortValue(Setting<?> s) {
        Object v = s.get();
        if (s instanceof BoolSetting b) return b.get() ? "true" : "false";
        if (s instanceof IntSetting i) return String.valueOf(i.get());
        if (s instanceof DoubleSetting d) return WTermConfig.formatDouble(d.get(), d.decimalPlaces);
        if (s instanceof StringSetting str) return "\"" + (str.get() == null ? "" : str.get()) + "\"";
        if (s instanceof KeybindSetting k) return TerminalClickStyle.keyText(k.get().toString());
        if (s instanceof ColorSetting c) {
            SettingColor col = c.get();
            return String.format(Locale.ROOT, "#%02X%02X%02X", col.r, col.g, col.b);
        }
        if (v instanceof Collection<?> collection) return "[" + collection.size() + "]";
        if (v instanceof Map<?, ?> map) return "[" + map.size() + "]";
        if (v instanceof Enum<?>) return v.toString();
        return WTermConfig.shortName(v);
    }

    private int rowAt(double mx, double my) {
        TerminalGrid g = TerminalGrid.of(theme());
        double top = resultsTop();
        if (mx < x || mx > x + width || my < top || my >= top + visibleRows * g.lh) return -1;
        int i = scroll + (int) Math.floor((my - top) / g.lh);
        return i >= 0 && i < rows.size() ? i : -1;
    }

    @Override
    public void onMouseMoved(double mouseX, double mouseY, double lastMouseX, double lastMouseY) {
        int row = rowAt(mouseX, mouseY);
        if (row < 0) return;
        int index = results.indexOf(row);
        if (index >= 0) selected = index;
    }

    @Override
    public boolean onMouseClicked(MouseButtonEvent click, boolean doubled) {
        int row = rowAt(click.x(), click.y());
        if (row < 0) return false;

        int index = results.indexOf(row);
        if (index < 0) return true;
        selected = index;

        if (click.button() == InputConstants.MOUSE_BUTTON_LEFT) act(rows.get(row), false);
        else if (click.button() == InputConstants.MOUSE_BUTTON_RIGHT) act(rows.get(row), true);
        return true;
    }

    @Override
    public boolean onMouseScrolled(double amount) {
        if (rows.size() <= visibleRows) return false;
        scroll = Math.max(0, Math.min(rows.size() - visibleRows, scroll - (int) Math.signum(amount) * 3));
        return true;
    }

    private void act(Row row, boolean settingsView) {
        if (row == null || !row.result()) return;

        if (row.setting != null) {
            mc.gui.setScreen(new TerminalModuleScreen(theme(), row.module, row.setting));
            return;
        }

        if (settingsView) {
            mc.gui.setScreen(theme().moduleScreen(row.module));
            return;
        }

        row.module.toggle();

        if (query.toString().trim().isEmpty()) {
            int keep = selected;
            refresh();
            selected = Math.max(0, Math.min(keep, results.size() - 1));
            reveal();
        }
    }

    @Override
    public boolean onKeyPressed(KeyEvent input) {
        return key(input);
    }

    @Override
    public boolean onKeyRepeated(KeyEvent input) {
        return key(input);
    }

    private boolean key(KeyEvent input) {
        int k = input.key();
        int mods = input.modifiers();

        if (k == InputConstants.KEY_DOWN) {
            if (!results.isEmpty()) selected = Math.min(results.size() - 1, selected + 1);
            reveal();
            return true;
        }
        if (k == InputConstants.KEY_UP) {
            if (!results.isEmpty()) selected = Math.max(0, selected - 1);
            reveal();
            return true;
        }
        if (k == InputConstants.KEY_RETURN || k == InputConstants.KEY_NUMPADENTER) {
            if (!results.isEmpty()) act(rows.get(results.get(selected)), false);
            return true;
        }
        if (k == InputConstants.KEY_TAB) {
            if (!results.isEmpty()) act(rows.get(results.get(selected)), true);
            return true;
        }
        if (k == InputConstants.KEY_BACKSPACE) {
            if (query.isEmpty()) return true;
            if ((mods & InputConstants.MOD_CONTROL) != 0) {
                int end = query.length();
                int i = end;
                while (i > 0 && (query.charAt(i - 1) == ' ' || query.charAt(i - 1) == '-')) i--;
                while (i > 0 && query.charAt(i - 1) != ' ' && query.charAt(i - 1) != '-') i--;
                query.setLength(i);
            }
            else query.setLength(query.length() - 1);
            selected = 0;
            refresh();
            return true;
        }
        return false;
    }

    @Override
    public boolean onCharTyped(CharacterEvent input) {
        int cp = input.codepoint();
        if (cp < ' ' || query.length() >= 40) return true;
        query.appendCodePoint(cp);
        selected = 0;
        refresh();
        return true;
    }
}
