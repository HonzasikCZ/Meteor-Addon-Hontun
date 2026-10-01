package cz.honzasik.hontun.gui.theme.style.vanilla;

import cz.honzasik.hontun.Hontun;
import cz.honzasik.hontun.gui.api.text.RichText;
import cz.honzasik.hontun.gui.api.text.RichTextSegment;
import cz.honzasik.hontun.gui.render.pixel.PixelCanvas;
import cz.honzasik.hontun.gui.theme.HontunGuiTheme;
import cz.honzasik.hontun.gui.theme.HontunLightPalette;
import cz.honzasik.hontun.gui.theme.HontunSettingsWidgetFactory;
import cz.honzasik.hontun.gui.theme.icons.HontunBuiltinIcons;
import cz.honzasik.hontun.gui.theme.style.AnimRole;
import cz.honzasik.hontun.gui.theme.style.AnimSpec;
import cz.honzasik.hontun.gui.theme.style.Backdrop;
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
import cz.honzasik.hontun.gui.theme.widgets.settings.WHontunKeybind;
import cz.honzasik.hontun.gui.util.search.SearchResult;
import cz.honzasik.hontun.gui.util.search.results.ModuleSearchResult;
import cz.honzasik.hontun.gui.util.search.results.SettingSearchResult;
import cz.honzasik.hontun.gui.widget.WGuiTexture;
import cz.honzasik.hontun.utils.HontunTheme;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.renderer.packer.GuiTexture;
import meteordevelopment.meteorclient.gui.tabs.Tab;
import meteordevelopment.meteorclient.gui.tabs.Tabs;
import meteordevelopment.meteorclient.gui.utils.Cell;
import meteordevelopment.meteorclient.gui.widgets.WLabel;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;
import meteordevelopment.meteorclient.gui.widgets.containers.WView;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.render.DisplayItemUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.joml.Matrix3x2fStack;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class VanillaClickStyle extends ClickStyle {
    private static final int WHITE = 0xFFFFFFFF;
    private static final int FACE_TEXT = 0xFF404040;
    private static final int FACE_TITLE = 0xFF303030;
    private static final int OFF_TEXT = 0xFFAAAAAA;
    private static final int FACE_DIM = 0xFF555555;
    private static final int GREY = 0xFFA0A0A0;
    private static final int EDIT_TEXT = 0xFFE0E0E0;
    private static final int HINT = 0xFF555555;
    private static final int SEARCH_HINT = 0xFFAAAAAA;
    private static final int LIST_GREY = 0xFF808080;
    private static final int YELLOW = 0xFFFFFF55;
    private static final int SUGGEST = 0xFFFFFF00;
    private static final int SUGGEST_IDLE = 0xFFAAAAAA;
    private static final int DANGER = 0xFFFF5555;
    private static final int POPUP = 0xFF000000;
    private static final int PANEL_BORDER = 4;
    private static final int SLOT_HOVER = 0x80FFFFFF;
    private static final int LINK = 0xFF0000AA;
    private static final int OFF_TINT = 0xFF8C8C8C;
    private static final int OFF_HOT_TINT = 0xFFC0C0C0;
    private static final int GOLD = 0xFFFFAA00;
    private static final int STAR_HOVER = 0xFFFFFFA0;
    private static final int WELL = 0x373737;
    private static final int GRID = 0x40000000;
    private static final int CHECKER_LIGHT = 0xFFC6C6C6;
    private static final int CHECKER_DARK = 0xFF8B8B8B;
    private static final int GUIDE_LIGHT = 0x33FFFFFF;
    private static final int GUIDE_DARK = 0xBF000000;
    private static final int SWATCH_DARK = 0xD0000000;
    private static final int LIGHT_TEXT = 0xFF303030;
    private static final int LIGHT_DIM = 0xFF484848;
    private static final int LIGHT_HOT = 0xFF000000;
    private static final int TRACK = 0xFF000000;

    private static final String STAR = "★";
    private static final String STAR_OUTLINE = "☆";
    private static final String GLYPH_DOWN = "▼";
    private static final String GLYPH_RIGHT = "▶";
    private static final String NO_RESULTS = "No results";
    private static final long CARET_BLINK = 300;
    private static final int TOOLTIP_WRAP = 170;
    private static final int DIALOG_DEPTH = 5;
    private static final int ROWS_PER_LAYER = 12;

    private static final Color WHITE_COLOR = new Color(255, 255, 255, 255);
    private static final Color CLEAR = new Color(0, 0, 0, 0);
    private static final Color LIST_GREY_COLOR = new Color(0x80, 0x80, 0x80, 255);
    private static final Color LIGHT_DIM_COLOR = new Color(0x48, 0x48, 0x48, 255);

    private final Map<WHontunTextBox, long[]> carets = new WeakHashMap<>();
    private final Set<WHontunLabel> faceLabels = Collections.newSetFromMap(new WeakHashMap<>());
    private final Map<Category, ItemStack> stacks = new HashMap<>();
    private final Color scratch = new Color(255, 255, 255, 255);

    private Module pendingModule;
    private int layerRows;
    private boolean pendingActive;

    @Override
    public HontunTheme.UiMode id() {
        return HontunTheme.UiMode.Vanilla;
    }

    @Override
    protected Metrics createMetrics() {
        Metrics m = new Metrics();
        m.windowMinWidth = 200;
        m.windowPad = 12;
        m.windowPadL = 16;
        m.windowPadR = 16;
        m.windowPadT = 4;
        m.windowPadB = 16;
        m.headerPadH = 12;
        m.headerPadV = 8;
        m.headerSpacing = 8;
        m.topBarMarginTop = 0;
        m.topBarItemPad = 0;
        m.tabPadH = 16;
        m.tabPadV = 15;
        m.tabIconGap = 8;
        m.rowPadH = 12;
        m.rowPadV = 8;
        m.activeBarWidth = 0;
        m.activeBarInset = 0;
        m.viewSpacing = 2;
        m.sectionHeaderPadH = 8;
        m.sectionHeaderPadV = 0;
        m.sectionPad = 8;
        m.tablePad = 8;
        m.tableVSpacing = 8;
        m.tooltipPadH = 8;
        m.tooltipPadV = 8;
        m.moduleScreenPad = 6;
        m.footerPadT = 2;
        m.footerPadB = 14;
        m.moduleInfoSpacing = 8;
        m.separatorThickness = 4;
        m.searchHeaderPad = 12;
        m.searchViewPad = 16;
        m.searchRowPad = 0;
        m.searchResultPadH = 8;
        m.searchHintPadL = 4;
        m.multiSelectItemPad = 4;
        m.multiSelectItemSpacing = 0;
        m.multiSelectCheckPad = 4;
        m.multiSelectHeaderPadV = 7;
        m.multiSelectHeaderPadL = 8;
        m.multiSelectRowScale = 20.0 / 17.0;
        m.gap = 8;
        m.dropdownPad = 0;
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
    public Backdrop backdrop() {
        return Backdrop.VANILLA;
    }

    @Override
    public boolean uses(Knob knob) {
        return knob == Knob.CATEGORY_ICONS;
    }

    @Override
    public double effectiveScale(double raw) {
        return Math.max(1, Math.round(2 * raw)) / 2.0;
    }

    @Override
    public double pixelAlpha(double a) {
        return 1;
    }

    @Override
    public double radiusFactor() {
        return 0;
    }

    @Override
    public boolean windowShadow(HontunGuiTheme t) {
        return false;
    }

    @Override
    public double chromeMargin(HontunGuiTheme t) {
        return 0;
    }

    @Override
    public double windowAlpha(double raw, boolean light) {
        return light ? Math.max(0.9, 0.8 + 0.2 * raw) : Math.max(0.85, 0.6 + 0.4 * raw);
    }

    @Override
    public double snapWindow(HontunGuiTheme t, double value, int gridSize) {
        int p = u(t);
        int step = gridSize > 0 ? lcm(gridSize, p) : p;
        return Math.floor(value / step + 0.5) * step;
    }

    @Override
    public AnimSpec anim(AnimRole role) {
        return AnimSpec.NONE;
    }

    @Override
    public void palette(StylePalette out, boolean light, HontunGuiTheme t) {
        out.userAccent = HontunTheme.userAccent();
        out.userAccentHi = HontunTheme.userAccentHi();
        out.userAccentLo = HontunTheme.userAccentLo();

        if (!light) {
            out.crust = 0x000000;
            out.mantle = 0x212121;
            out.base = 0x373737;
            out.surface0 = 0x2B2B2B;
            out.surface1 = 0x555555;
            out.surface2 = 0x6F6F6F;
            out.overlay0 = 0x8B8B8B;
            out.overlay1 = 0xA0A0A0;
            out.overlay2 = 0xC6C6C6;
            out.text = 0xFFFFFF;
            out.subtext1 = 0xE0E0E0;
            out.subtext0 = 0xA0A0A0;
            out.accent = 0xFFFF55;
            out.accentHi = 0xFFFFA0;
            out.accentLo = 0xAAAA00;
            out.green = 0x55FF55;
            out.yellow = 0xFFFF55;
            out.red = 0xFF5555;
            return;
        }

        out.crust = 0x000000;
        out.mantle = 0xDBDBDB;
        out.base = 0xC6C6C6;
        out.surface0 = 0x8B8B8B;
        out.surface1 = 0x737373;
        out.surface2 = 0x555555;
        out.overlay0 = 0x555555;
        out.overlay1 = 0x404040;
        out.overlay2 = 0x373737;
        out.text = LIGHT_TEXT & 0xFFFFFF;
        out.subtext1 = 0x3C3C3C;
        out.subtext0 = LIGHT_DIM & 0xFFFFFF;
        out.accent = 0x0000AA;
        out.accentHi = 0x3333DD;
        out.accentLo = 0x000080;
        out.green = 0x006000;
        out.yellow = 0x704A00;
        out.red = 0xAA0000;
    }

    @Override
    public void onPressed(WWidget w) {
        if (w instanceof WHontunModule m) {
            pendingModule = m.module();
            pendingActive = m.module().isActive();
            return;
        }

        click();
    }

    private void resolvePending() {
        Module module = pendingModule;
        if (module == null) return;

        pendingModule = null;
        if (module.isActive() != pendingActive) click();
    }

    private static void click() {
        if (mc == null || mc.getSoundManager() == null) return;
        AbstractWidget.playButtonClickSound(mc.getSoundManager());
    }

    @Override
    public WWidget windowIcon(HontunGuiTheme t, WindowKind kind, Category category, WWidget fallback) {
        WItemIcon icon = switch (kind) {
            case CATEGORY -> fallback == null ? null : new WItemIcon(categoryStack(category));
            case FAVORITES -> new WItemIcon(DisplayItemUtils.toStack(Items.NETHER_STAR));
            case SEARCH -> new WItemIcon(VanillaSprites.SEARCH);
            default -> null;
        };

        if (icon == null) return kind == WindowKind.CATEGORY ? null : fallback;

        icon.theme = t;
        return icon;
    }

    @Override
    public WWidget headerLabel(HontunGuiTheme t, WHontunWindow w, String title) {
        WVanillaTitle label = new WVanillaTitle(RichText.of(windowTitle(title)), 16, w);
        label.theme = t;
        return label;
    }

    @Override
    public WWidget sectionTitle(HontunGuiTheme t, WHontunSection s, String title) {
        WVanillaTitle label = new WVanillaTitle(RichText.of(title), 16, null);
        label.theme = t;
        return label;
    }

    @Override
    public String iconButtonText(GuiTexture icon) {
        if (icon == null) return null;
        if (is(icon, HontunBuiltinIcons.RESET) || icon == GuiRenderer.RESET) return "Reset";
        if (is(icon, HontunBuiltinIcons.COPY) || icon == GuiRenderer.COPY) return "Copy";
        if (is(icon, HontunBuiltinIcons.IMPORT) || icon == GuiRenderer.PASTE) return "Paste";
        if (is(icon, HontunBuiltinIcons.EDIT) || icon == GuiRenderer.EDIT) return "Edit";
        if (is(icon, HontunBuiltinIcons.PLUS)) return "+";
        if (is(icon, HontunBuiltinIcons.MINUS)) return "-";
        return null;
    }

    @Override
    public RichText keybindLabel(HontunGuiTheme t, WHontunKeybind k, boolean listening, String key) {
        String name = keyName(k, key);
        return RichText.of(listening ? "> " + name + " <" : name);
    }

    private static String keyName(WHontunKeybind k, String key) {
        if (k.keybind() != null && k.keybind().isSet() && key != null) return key;
        return Component.translatable("key.keyboard.unknown").getString();
    }

    @Override
    public Color helpKeyColor(HontunGuiTheme t) {
        return WHITE_COLOR;
    }

    @Override
    public Color helpTextColor(HontunGuiTheme t) {
        return WHITE_COLOR;
    }

    @Override
    public Color favoriteColor(WHontunFavorite f) {
        HontunGuiTheme t = f.theme();
        return f.checked ? t.yellowColor() : t.textSecondaryColor();
    }

    @Override
    public void moduleSize(WHontunModule m, double[] out) {
        int p = u(m.theme());
        out[0] = m.titleWidth() + 12 * p;
        out[1] = 16 * p;
    }

    @Override
    public void tabSize(WHontunTopBar.WTopBarButton b, double[] out) {
        HontunGuiTheme t = b.bar().theme();
        int p = u(t);

        double widest = 0;
        int count = 0;
        for (Tab tab : Tabs.get()) {
            widest = Math.max(widest, t.textWidth(tab.name));
            count++;
        }

        double each = Math.max(60 * p, widest + 16 * p);
        if (each * count > Utils.getWindowWidth() - 28 * p) each = t.textWidth(b.text()) + 16 * p;

        out[0] = each;
        out[1] = 24 * p;
    }

    @Override
    public void checkboxSize(WHontunCheckbox c, double[] out) {
        int p = u(c.theme());
        out[0] = 17 * p;
        out[1] = 17 * p;
    }

    @Override
    public void dropdownSize(WHontunDropdown<?> d, double[] out) {
        HontunGuiTheme t = d.theme();
        int p = u(t);
        String title = plain(d.titleText());
        double prefix = title.isEmpty() ? 0 : t.textWidth(title + ": ");
        out[0] = prefix + d.maxValueWidth() + 26 * p;
        out[1] = 20 * p;
    }

    @Override
    public void buttonSize(WHontunButton b, double[] out) {
        HontunGuiTheme t = b.theme();
        int p = u(t);
        RichText label = b.displayText();
        out[1] = 20 * p;

        if (b.parent instanceof WHontunKeybind) {
            double w = label != null ? t.textWidth(label) : 0;
            out[0] = Math.max(75 * p, w + 16 * p);
            return;
        }

        if (label == null || isSign(label.getPlainText())) {
            out[0] = 20 * p;
            return;
        }

        String iconText = b.icon() != null ? iconButtonText(b.icon()) : null;

        if (isReset(b.icon()) && "Reset".equals(label.getPlainText())) {
            out[0] = 40 * p;
            return;
        }

        double min = iconText != null && (iconText.equals("Copy") || iconText.equals("Paste")) ? 44 * p : 20 * p;
        out[0] = Math.max(min, t.textWidth(label) + 16 * p);
    }

    @Override
    public void pressableSize(WWidget w, double[] out) {
        HontunGuiTheme t = w.theme instanceof HontunGuiTheme h ? h : theme();
        int p = u(t);

        if (w instanceof WHontunConfirmedButton b) {
            String mapped = b.getText() == null ? iconButtonText(b.icon()) : null;
            double label = b.getText() != null ? b.labelWidth() : mapped != null ? t.textWidth(mapped) : -1;
            out[0] = label < 0 ? 20 * p : Math.max(20 * p, label + 16 * p);
            out[1] = 20 * p;
            return;
        }

        if (w instanceof WHontunMinus || w instanceof WHontunPlus || w instanceof WHontunConfirmedMinus) {
            out[0] = 20 * p;
            out[1] = 20 * p;
        }
    }

    @Override
    public void textBoxSize(WHontunTextBox b, double[] out) {
        super.textBoxSize(b, out);
        out[1] = 20 * u(b.theme());
    }

    @Override
    public void colorPickerSize(WHontunColorPicker p, double[] out) {
        int u = u(p.theme());
        out[0] = 28 * u;
        out[1] = 16 * u;
    }

    @Override
    public double sliderHeight(WHontunSlider s) {
        return 20 * u(s.theme());
    }

    @Override
    public double favoriteSize(WHontunFavorite f) {
        return 9 * u(f.theme());
    }

    @Override
    public double scrollbarWidth(HontunGuiTheme t) {
        return 6 * u(t);
    }

    @Override
    public double popupOffset(HontunGuiTheme t) {
        return 0;
    }

    @Override
    public void paintWindowBack(WHontunWindow w, GuiRenderer renderer, double mouseX, double mouseY) {
        resolvePending();
        layerRows = 0;

        HontunGuiTheme t = w.theme();
        int p = unit();
        int x = snap(w.x), y = snap(w.y);
        int width = snap(w.x + w.width) - x;
        int header = snap(w.y + w.headerWidget().height) - y;
        boolean expanded = w.isExpanded();
        int height = expanded ? snap(w.y + w.height) - y : header;

        int tint = WHITE;
        int top = y + header;
        int bottom = expanded && w.view != null ? Math.min(y + height - 6 * p, snap(w.view.y + w.view.height) + 2 * p) : top;
        boolean inset = expanded && w.view != null && width > 14 * p && bottom - top >= 6 * p;

        if (inset && !t.light()) {
            panelAround(x, y, width, height, x + 6 * p, top, width - 12 * p, bottom - top, tint);
            well(x + 6 * p, top, width - 12 * p, bottom - top, wellAlpha(t));
        } else {
            PixelCanvas.sprite(VanillaSprites.PANEL, x, y, width / p, height / p, tint);
            if (inset && w.isDialog()) lightWell(x + 6 * p, top, width - 12 * p, bottom - top);
        }

        if (w.isDialog() && expanded) prepareDialog(w, t);
    }

    @Override
    public void paintWindowHeader(WHontunWindow w, WHontunWindow.WHontunHeader h, GuiRenderer renderer, double mouseX, double mouseY) {
        int p = unit();
        int x = snap(w.x), y = snap(w.y);
        int width = snap(w.x + w.width) - x;
        WWidget icon = w.icon;

        int tx;
        if (icon instanceof WHontunFavorite) tx = x + 20 * p;
        else if (icon instanceof WItemIcon) tx = x + 26 * p;
        else if (icon != null && h.titleWidget() != null) tx = snap(h.titleWidget().x);
        else tx = x + 8 * p;

        String title = windowTitle(w.titleText());
        if (title != null && !title.isEmpty()) PixelCanvas.text(title, tx, y + 8 * p, FACE_TITLE, false);

        if (!w.isDialog()) {
            Identifier indicator = w.isExpanded() ? VanillaSprites.SORT_DOWN : VanillaSprites.SORT_UP;
            PixelCanvas.sprite(indicator, x + width - 21 * p, y + 3 * p, 18, 18);
        }
    }

    @Override
    public void paintWindowFront(WHontunWindow w, GuiRenderer renderer, double mouseX, double mouseY) {
        if (!w.isDialog() || !w.isExpanded()) return;

        int ink = faceText(w.theme());
        for (Cell<?> cell : w.cells) {
            WWidget widget = cell.widget();
            if (widget == w.headerWidget() || widget == w.view) continue;
            drawFaceLabels(widget, DIALOG_DEPTH, ink);
        }
    }

    private void prepareDialog(WHontunWindow w, HontunGuiTheme t) {
        for (Cell<?> cell : w.cells) {
            WWidget widget = cell.widget();
            if (widget == w.headerWidget() || widget == w.view) continue;
            hideFaceLabels(widget, DIALOG_DEPTH);
        }

        if (w.icon instanceof WHontunFavorite) tintModuleInfo(w, t);
    }

    private void hideFaceLabels(WWidget widget, int depth) {
        if (widget instanceof WHontunLabel label) {
            if (!label.hidden) {
                label.hidden = true;
                faceLabels.add(label);
            }
            return;
        }

        if (depth <= 0 || !(widget instanceof WContainer container)) return;
        for (Cell<?> cell : container.cells) hideFaceLabels(cell.widget(), depth - 1);
    }

    private void drawFaceLabels(WWidget widget, int depth, int fallback) {
        if (widget instanceof WHontunLabel label) {
            if (!faceLabels.contains(label) || label.get().isEmpty()) return;
            int ink = label.color != null ? label.color.getPacked() : fallback;
            PixelCanvas.text(label.richText(), label.x, label.y, ink, false);
            return;
        }

        if (depth <= 0 || !(widget instanceof WContainer container)) return;
        for (Cell<?> cell : container.cells) drawFaceLabels(cell.widget(), depth - 1, fallback);
    }

    private static void tintModuleInfo(WHontunWindow w, HontunGuiTheme t) {
        if (w.view == null || w.view.cells.isEmpty()) return;
        if (!(w.view.cells.getFirst().widget() instanceof WContainer info)) return;

        boolean description = false;
        for (Cell<?> cell : info.cells) {
            WWidget widget = cell.widget();

            if (!description && widget instanceof WHontunMultiLabel label) {
                description = true;
                if (label.color == null) label.color = t.textSecondaryColor();
                continue;
            }

            if (widget instanceof WContainer row && row.cells.size() == 2
                    && row.cells.get(0).widget() instanceof WLabel from
                    && "From: ".equals(from.get())
                    && row.cells.get(1).widget() instanceof WLabel addon
                    && addon.color != t.textColor()) {
                addon.color = t.textColor();
            }
        }
    }

    @Override
    public void paintTopBar(WHontunTopBar b, GuiRenderer renderer, double mouseX, double mouseY) {
        resolvePending();

        int p = unit();
        Identifier separator = headerSeparator();
        int y = snap(b.y) + 22 * p;
        int left = snap(b.x);
        int right = snap(b.x + b.width);
        int screen = Utils.getWindowWidth();

        if (left > 0) PixelCanvas.tiled(separator, 0, y, ceilDiv(left, p), 2, 32, 2);
        if (screen > right) PixelCanvas.tiled(separator, right, y, ceilDiv(screen - right, p), 2, 32, 2);

        if (b.theme() != null && b.theme().light() && b.parent instanceof WContainer root) shadeBackdropLabels(root);
    }

    private static void shadeBackdropLabels(WContainer root) {
        for (Cell<?> cell : root.cells) {
            if (!(cell.widget() instanceof WContainer list) || list instanceof WHontunWindow) continue;

            for (Cell<?> c : list.cells) {
                if (!(c.widget() instanceof WHontunLabel label) || label.richText() == null) continue;
                for (RichTextSegment segment : label.richText().getSegments()) {
                    if (!segment.hasShadow()) segment.setShadow(true);
                }
            }
        }
    }

    @Override
    public void paintTab(WHontunTopBar.WTopBarButton b, GuiRenderer renderer, double mouseX, double mouseY) {
        int p = unit();
        int x = snap(b.x), y = snap(b.y);
        int w = snap(b.x + b.width) - x, h = snap(b.y + b.height) - y;

        boolean selected = b.isSelected();
        boolean hover = b.mouseOver;

        Identifier sprite = selected
                ? (hover ? VanillaSprites.TAB_SELECTED_HIGHLIGHTED : VanillaSprites.TAB_SELECTED)
                : (hover ? VanillaSprites.TAB_HIGHLIGHTED : VanillaSprites.TAB);

        PixelCanvas.sprite(sprite, x, y, w / p, h / p);

        int tw = PixelCanvas.width(b.text());

        if (selected) {
            PixelCanvas.menuBackground(Screen.MENU_BACKGROUND, x + 2 * p, y + 2 * p, w / p - 4, h / p - 2);
            int uw = Math.min(tw, w - 4 * p);
            PixelCanvas.fill(x + center(w, uw, p), y + h - 2 * p, uw, p, WHITE);
        }

        PixelCanvas.text(b.text(), x + center(w, tw, p), y + (selected ? 8 : 10) * p, WHITE, true);
    }

    @Override
    public void paintModule(WHontunModule m, GuiRenderer renderer, double mouseX, double mouseY) {
        resolvePending();

        HontunGuiTheme t = m.theme();
        int p = unit();
        int x = snap(m.x), y = snap(m.y);
        int w = snap(m.x + m.width) - x, h = snap(m.y + m.height) - y;

        boolean on = m.module().isActive();
        boolean hot = m.mouseOver || m.isPressed();

        if (++layerRows > ROWS_PER_LAYER) {
            PixelCanvas.nextLayer();
            layerRows = 1;
        }

        int tint = on ? WHITE : hot ? OFF_HOT_TINT : OFF_TINT;
        int ink = on ? WHITE : hot ? EDIT_TEXT : OFF_TEXT;

        PixelCanvas.sprite(hot ? VanillaSprites.BUTTON_HIGHLIGHTED : VanillaSprites.BUTTON, x, y, w / p, h / p, tint);

        int tw = PixelCanvas.width(m.title());
        int tx = switch (t.moduleAlignment.get()) {
            case Left -> x + 6 * p;
            case Right -> x + w - 6 * p - tw;
            default -> x + center(w, tw, p);
        };

        PixelCanvas.text(m.title(), tx, y + center(h, 8 * p, p), ink, true);
    }

    @Override
    public void paintSectionBody(WHontunSection s, GuiRenderer renderer, double mouseX, double mouseY) {
    }

    @Override
    public void paintSectionHeader(WHontunSection s, WHontunSection.WHontunHeader h, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme t = s.theme();
        int p = unit();
        int x = snap(h.x), y = snap(h.y);
        int w = snap(h.x + h.width) - x, hh = snap(h.y + h.height) - y;

        boolean face = t.light() || onFace(s);
        boolean hover = h.mouseOver;

        String title = s.titleText() == null ? "" : s.titleText();
        Component text = PixelCanvas.component(RichText.of(title), hover);
        int tw = PixelCanvas.width(text);

        String glyph = s.isExpanded() ? GLYPH_DOWN : GLYPH_RIGHT;
        int gw = h.openIndicator() != null ? PixelCanvas.width(glyph) : 0;
        int gap = gw > 0 ? 4 * p : 0;

        int gx = x + center(w, gw + gap + tw, p);
        int ty = y + center(hh, 8 * p, p);
        int ink = faceText(t);

        if (gw > 0) {
            int glyphInk = hover ? (face ? faceHot(t) : WHITE) : (face ? faceDim(t) : GREY);
            PixelCanvas.text(glyph, gx, ty, glyphInk, !face);
        }

        PixelCanvas.text(text, gx + gw + gap, ty, face ? ink : WHITE, !face);
    }

    @Override
    public void paintSeparatorH(WHontunHorizontalSeparator s, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme t = s.theme();
        int p = unit();
        int x = snap(s.x);
        int w = snap(s.x + s.width) - x;
        boolean face = t.light() || onFace(s);

        if (!s.hasText()) {
            int y = snap(s.y + s.height / 2) - p;

            if (face) {
                PixelCanvas.hline(x, y, w, FACE_DIM);
                PixelCanvas.hline(x, y + p, w, WHITE);
            } else {
                PixelCanvas.tiled(headerSeparator(), x, y, w / p, 2, 32, 2);
            }
            return;
        }

        String title = s.titleString();
        if (title == null || title.isEmpty()) return;

        int y = snap(s.y);
        int h = snap(s.y + s.height) - y;
        int tw = PixelCanvas.width(title);
        PixelCanvas.text(title, x + center(w, tw, p), y + center(h, 8 * p, p), face ? faceText(t) : WHITE, !face);
    }

    @Override
    public void paintSeparatorV(WHontunVerticalSeparator s, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme t = s.theme();
        int p = unit();
        int x = snap(s.x), y = snap(s.y);
        int h = snap(s.y + s.height) - y;
        boolean face = t.light() || onFace(s);

        PixelCanvas.vline(x, y, h, face ? FACE_DIM : GUIDE_LIGHT);
        PixelCanvas.vline(x + p, y, h, face ? WHITE : GUIDE_DARK);
    }

    @Override
    public void paintViewUnderlay(WHontunView v, GuiRenderer renderer, double mouseX, double mouseY) {
    }

    @Override
    public void paintScrollbar(WHontunView v, GuiRenderer renderer, double mouseX, double mouseY) {
        if (!v.scrollable() || !v.hasScrollBar) return;

        int p = unit();
        int bx = snap(v.barX());
        int vy = snap(v.y);
        int vh = snap(v.y + v.height) - vy;
        if (vh <= 0) return;

        double handle = v.barHeight();
        double range = v.height - handle;
        double fraction = range > 0 ? Math.clamp((v.barY() - v.y) / range, 0.0, 1.0) : 0;

        if (v.theme().light()) {
            int inner = vh - 2 * p;
            if (inner < 2 * p) return;

            PixelCanvas.fill(bx - p, vy, 6 * p, vh, TRACK);
            int th = Math.min(inner, Math.max(32 * p, snap(handle)));
            int ty = vy + p + snap((inner - th) * fraction);
            PixelCanvas.sprite(VanillaSprites.SCROLLER, bx, ty, 4, th / p);
            return;
        }

        PixelCanvas.sprite(VanillaSprites.SCROLLER_BACKGROUND, bx, vy, 6, vh / p);

        int th = Math.min(vh, Math.max(32 * p, snap(handle)));
        int ty = vy + snap((vh - th) * fraction);

        PixelCanvas.sprite(VanillaSprites.SCROLLER, bx, ty, 6, th / p);
    }

    @Override
    public void paintButton(WHontunButton b, GuiRenderer renderer, double mouseX, double mouseY) {
        if (b.parent instanceof WHontunKeybind k) {
            paintKeybindButton(k, b);
            return;
        }

        int p = unit();
        int x = snap(b.x), y = snap(b.y);
        int w = snap(b.x + b.width) - x, h = snap(b.y + b.height) - y;

        stone(x, y, w, h, b.mouseOver || b.isPressed());

        RichText label = b.displayText();
        if (label != null) {
            int tw = PixelCanvas.width(label);
            PixelCanvas.text(label, x + center(w, tw, p), y + center(h, 8 * p, p), WHITE, true);
            return;
        }

        icon(renderer, b.icon(), x, y, w, h, WHITE_COLOR);
    }

    private void paintKeybindButton(WHontunKeybind k, WHontunButton b) {
        int p = unit();
        int x = snap(b.x), y = snap(b.y);
        int w = snap(b.x + b.width) - x, h = snap(b.y + b.height) - y;

        stone(x, y, w, h, b.mouseOver || b.isPressed());

        String name = keyName(k, k.keybind() != null ? k.keybind().toString() : null);
        Component text;

        if (k.isListening()) {
            MutableComponent listening = Component.empty();
            listening.append(Component.literal("> ").setStyle(Style.EMPTY.withColor(YELLOW & 0xFFFFFF)));
            listening.append(Component.literal(name).setStyle(Style.EMPTY.withColor(0xFFFFFF).withUnderlined(true)));
            listening.append(Component.literal(" <").setStyle(Style.EMPTY.withColor(YELLOW & 0xFFFFFF)));
            text = listening;
        } else {
            text = Component.literal(name);
        }

        int tw = PixelCanvas.width(text);
        PixelCanvas.text(text, x + center(w, tw, p), y + center(h, 8 * p, p), WHITE, true);
    }

    @Override
    public void paintCheckbox(WHontunCheckbox c, GuiRenderer renderer, double mouseX, double mouseY) {
        int p = unit();
        int x = snap(c.x), y = snap(c.y);

        boolean hot = c.mouseOver || c.isFocused() || rowHovered(c);
        Identifier sprite = c.checked
                ? (hot ? VanillaSprites.CHECKBOX_SELECTED_HIGHLIGHTED : VanillaSprites.CHECKBOX_SELECTED)
                : (hot ? VanillaSprites.CHECKBOX_HIGHLIGHTED : VanillaSprites.CHECKBOX);

        int s = Math.max(1, (snap(c.x + c.width) - x) / p);
        PixelCanvas.sprite(sprite, x, y, s, s);
    }

    private static boolean rowHovered(WHontunCheckbox c) {
        return c.parent instanceof WHontunMultiSelect<?>.WHontunItem item && item.mouseOver;
    }

    @Override
    public void paintColorPicker(WHontunColorPicker pk, GuiRenderer renderer, double mouseX, double mouseY) {
        int p = unit();
        int x = snap(pk.x), y = snap(pk.y);
        int w = snap(pk.x + pk.width) - x, h = snap(pk.y + pk.height) - y;
        Color color = pk.color();

        stone(x, y, w, h, pk.mouseOver || pk.isPressed());

        int ix = x + 3 * p, iy = y + 3 * p, iw = w - 6 * p, ih = h - 6 * p;
        if (iw <= 0 || ih <= 0) return;

        if (color.a < 255) PixelCanvas.checker(ix, iy, iw, ih, 2 * p, CHECKER_LIGHT, CHECKER_DARK);
        PixelCanvas.fill(ix, iy, iw, ih, color.getPacked());
    }

    @Override
    public void paintConfirmedButton(WHontunConfirmedButton b, GuiRenderer renderer, double mouseX, double mouseY) {
        int p = unit();
        int x = snap(b.x), y = snap(b.y);
        int w = snap(b.x + b.width) - x, h = snap(b.y + b.height) - y;
        boolean armed = b.armed();

        stone(x, y, w, h, armed || b.mouseOver || b.isPressed());

        String text = b.getText() != null ? b.getText() : iconButtonText(b.icon());
        int ink = armed ? DANGER : WHITE;

        if (text != null) {
            int tw = PixelCanvas.width(text);
            PixelCanvas.text(text, x + center(w, tw, p), y + center(h, 8 * p, p), ink, true);
            return;
        }

        icon(renderer, b.icon(), x, y, w, h, color(ink));
    }

    @Override
    public void paintConfirmedMinus(WHontunConfirmedMinus m, GuiRenderer renderer, double mouseX, double mouseY) {
        int x = snap(m.x), y = snap(m.y);
        int w = snap(m.x + m.width) - x, h = snap(m.y + m.height) - y;
        boolean armed = m.armed();

        stone(x, y, w, h, armed || m.mouseOver || m.isPressed());
        sign("-", x, y, w, h, armed ? DANGER : WHITE);
    }

    @Override
    public void paintMinus(WHontunMinus m, GuiRenderer renderer, double mouseX, double mouseY) {
        int x = snap(m.x), y = snap(m.y);
        int w = snap(m.x + m.width) - x, h = snap(m.y + m.height) - y;

        stone(x, y, w, h, m.mouseOver || m.isPressed());
        sign("-", x, y, w, h, WHITE);
    }

    @Override
    public void paintPlus(WHontunPlus pl, GuiRenderer renderer, double mouseX, double mouseY) {
        int x = snap(pl.x), y = snap(pl.y);
        int w = snap(pl.x + pl.width) - x, h = snap(pl.y + pl.height) - y;

        stone(x, y, w, h, pl.mouseOver || pl.isPressed());
        sign("+", x, y, w, h, WHITE);
    }

    @Override
    public void paintFavorite(WHontunFavorite f, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme t = f.theme();
        int p = unit();
        WHontunWindow window = headerWindow(f);
        boolean face = t.light() || window != null;
        boolean hover = f.mouseOver;

        String glyph = f.checked ? STAR : STAR_OUTLINE;
        int ink;
        boolean shadow;

        if (f.checked) {
            ink = hover ? STAR_HOVER : face ? GOLD : YELLOW;
            shadow = true;
        } else {
            ink = hover ? (face ? faceHot(t) : WHITE) : (face ? faceDim(t) : GREY);
            shadow = !face;
        }

        int gx, gy;
        if (window != null && window.isDialog()) {
            gx = snap(window.x) + 8 * p;
            gy = snap(window.y) + 8 * p;
        } else {
            int x = snap(f.x), y = snap(f.y);
            int w = snap(f.x + f.width) - x, h = snap(f.y + f.height) - y;
            gx = x + center(w, PixelCanvas.width(glyph), p);
            gy = y + center(h, 8 * p, p);
        }

        PixelCanvas.text(glyph, gx, gy, ink, shadow);
    }

    @Override
    public void paintTriangle(WHontunTriangle tr, GuiRenderer renderer, double mouseX, double mouseY) {
        WWidget list = tr.parent;
        WWidget owner = list != null ? list.parent : null;

        if (owner instanceof WHontunWindow.WHontunHeader h && h.openIndicator() == tr) return;
        if (owner instanceof WHontunSection.WHontunHeader h && h.openIndicator() == tr) return;

        HontunGuiTheme t = tr.theme();
        int p = unit();
        int x = snap(tr.x), y = snap(tr.y);
        int w = snap(tr.x + tr.width) - x, h = snap(tr.y + tr.height) - y;

        boolean face = t.light() || onFace(tr);
        boolean hover = tr.mouseOver || (list != null && list.mouseOver);
        String glyph = tr.rotation >= 135 ? GLYPH_DOWN : GLYPH_RIGHT;
        int ink = hover ? (face ? faceHot(t) : WHITE) : (face ? faceDim(t) : GREY);

        PixelCanvas.text(glyph, x + center(w, PixelCanvas.width(glyph), p), y + center(h, 8 * p, p), ink, !face);
    }

    @Override
    public void paintSlider(WHontunSlider s, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme t = s.theme();
        int p = unit();
        int x = snap(s.x), y = snap(s.y);
        int w = snap(s.x + s.width) - x, h = snap(s.y + s.height) - y;
        boolean dragging = s.isDragging();

        Identifier track = s.mouseOver || s.isFocused() || dragging ? VanillaSprites.SLIDER_HIGHLIGHTED : VanillaSprites.SLIDER;
        PixelCanvas.sprite(track, x, y, w / p, h / p);

        double range = s.max() - s.min();
        double fraction = range == 0 ? 0 : Math.clamp((s.value() - s.min()) / range, 0.0, 1.0);
        int handle = 8 * p;
        int hx = x + snap((w - handle) * fraction);

        Identifier knob = s.handleHovered() || dragging ? VanillaSprites.SLIDER_HANDLE_HIGHLIGHTED : VanillaSprites.SLIDER_HANDLE;
        PixelCanvas.sprite(knob, hx, y, 8, h / p);

        if (s.parent instanceof WContainer row && row.cells.size() >= 3) {
            if (row.cells.getFirst().widget() instanceof WLabel min) min.color = t.textSecondaryColor();
            if (row.cells.getLast().widget() instanceof WLabel max) max.color = t.textSecondaryColor();
        }
    }

    @Override
    public void paintDropdown(WHontunDropdown<?> d, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme t = d.theme();
        int p = unit();
        int x = snap(d.x), y = snap(d.y);
        int w = snap(d.x + d.width) - x, h = snap(d.y + d.height) - y;

        boolean open = d.isExpanded();
        boolean hover = d.mouseOver;
        stone(x, y, w, h, hover || open || d.isPressed());

        String title = plain(d.titleText());
        String value = plain(d.valueText());
        String message = title.isEmpty() ? value : title + ": " + value;

        int tw = PixelCanvas.width(message);
        int ty = y + center(h, 8 * p, p);
        PixelCanvas.text(message, x + center(w, tw, p), ty, WHITE, true);

        int gw = PixelCanvas.width(GLYPH_DOWN);
        PixelCanvas.text(GLYPH_DOWN, x + w - 5 * p - gw, ty, hover ? WHITE : GREY, true);
    }

    @Override
    public void paintDropdownPopup(WHontunDropdown<?> d, WHontunDropdown.WRoot root, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme t = root.theme();
        int p = unit();
        int x = snap(root.x), y = snap(root.y);
        int w = snap(root.x + root.width) - x, h = snap(root.y + root.height) - y;
        if (w <= 0 || h <= 0) return;

        if (t.light()) PixelCanvas.sprite(VanillaSprites.PANEL, x, y, w / p, h / p);
        else PixelCanvas.fill(x, y, w, h, POPUP);
    }

    @Override
    public void paintDropdownValue(WHontunDropdown<?>.WValue v, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme t = v.theme();
        int p = unit();
        int x = snap(v.x), y = snap(v.y);
        int w = snap(v.x + v.width) - x, h = snap(v.y + v.height) - y;

        boolean selected = v.isSelectedValue();
        boolean hover = v.mouseOver;
        int ty = y + center(h, 8 * p, p);

        if (t.light()) {
            if (hover) PixelCanvas.fill(x, y, w, h, SLOT_HOVER);
            PixelCanvas.text(v.valueName(), x + 4 * p, ty, selected || hover ? LINK : LIGHT_TEXT, false);
            return;
        }

        PixelCanvas.text(v.valueName(), x + 2 * p, ty, selected || hover ? SUGGEST : SUGGEST_IDLE, true);
    }

    @Override
    public void paintTextBox(WHontunTextBox b, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme t = b.theme();
        int p = unit();
        int x = snap(b.x), y = snap(b.y);
        int w = snap(b.x + b.width) - x, h = snap(b.y + b.height) - y;

        boolean framed = b.rendersBackground() || inSearchHeader(b);
        boolean focused = b.isFocused();
        boolean face = !framed && (t.light() || onFace(b));
        boolean shadow = framed || !face;

        if (framed) PixelCanvas.sprite(focused ? VanillaSprites.TEXT_FIELD_HIGHLIGHTED : VanillaSprites.TEXT_FIELD, x, y, w / p, h / p);

        double pad = framed ? 4 * p : b.padding();
        double textX = b.x + pad - b.overflow();
        int textY = y + center(h, 8 * p, p);

        Color custom = b.customColor();
        int ink = custom != null ? custom.getPacked() : framed ? EDIT_TEXT : face ? faceText(t) : t.textColor().getPacked();

        if (framed) renderer.scissorStart(x + 4 * p, y, Math.max(0, w - 8 * p), h);
        else renderer.scissorStart(x, y, w, h);

        String value = b.textValue();

        if (!value.isEmpty()) {
            b.drawText(renderer, textX, textY, value, color(ink));
        } else if (b.placeholderValue() != null && !b.placeholderValue().isEmpty()) {
            String hint = b.placeholderValue();
            boolean search = hint.toLowerCase(Locale.ROOT).startsWith("search");
            Component text = search
                    ? Component.literal(hint).setStyle(Style.EMPTY.withItalic(true))
                    : Component.literal(hint);
            boolean pale = search && (framed || !t.light());
            PixelCanvas.text(text, textX, textY, pale ? SEARCH_HINT : HINT, shadow);
        }

        if (focused && b.hasSelection()) {
            double s0 = textX + b.textWidthAt(b.selectionStartIndex());
            double s1 = textX + b.textWidthAt(b.selectionEndIndex());
            PixelCanvas.highlight(Math.min(s0, s1), textY - p, Math.max(s0, s1), textY + 10 * p, true);
        }

        if (focused) paintBlinkCaret(b, textX, textY, ink, shadow);
        else carets.remove(b);

        renderer.scissorEnd();
    }

    private void paintBlinkCaret(WHontunTextBox b, double textX, int textY, int ink, boolean shadow) {
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
        else PixelCanvas.fill(snap(cx), textY - p, p, 10 * p, ink);
    }

    @Override
    public void paintCompletions(WHontunTextBox b, GuiRenderer renderer, double x, double y, double w, double h) {
        int p = unit();
        int px = snap(x), py = snap(y);
        int pw = snap(x + w) - px, ph = snap(y + h) - py;
        if (pw <= 0 || ph <= 0) return;

        if (b.theme().light()) PixelCanvas.sprite(VanillaSprites.PANEL, px, py, pw / p, ph / p);
        else PixelCanvas.fill(px, py, pw, ph, POPUP);
    }

    @Override
    public void paintCompletionItem(WHontunTextBox.CompletionItem item, GuiRenderer renderer, double mouseX, double mouseY) {
        boolean light = item.theme().light();
        boolean selected = item.isSelected();

        if (light) {
            if (selected) {
                int x = snap(item.x), y = snap(item.y);
                PixelCanvas.fill(x, y, snap(item.x + item.width) - x, snap(item.y + item.height) - y, SLOT_HOVER);
            }
            PixelCanvas.text(item.richText(), item.x, item.y, selected ? LINK : LIGHT_TEXT, false);
            return;
        }

        PixelCanvas.text(item.richText(), item.x, item.y, selected ? SUGGEST : SUGGEST_IDLE, true);
    }

    @Override
    public void paintSearchPanel(WHontunSearch s, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme t = s.theme();
        int p = unit();
        int x = snap(s.x), y = snap(s.y);
        int w = snap(s.x + s.width) - x, h = snap(s.y + s.height) - y;
        if (w <= 0 || h <= 0) return;

        int tint = WHITE;

        if (!t.light()) {
            for (Cell<?> cell : s.cells) {
                if (!(cell.widget() instanceof WHontunSearch.WHontunResultsContainer body)) continue;

                int by = snap(body.y);
                int bw = snap(body.x + body.width) - snap(body.x);
                int bh = searchWellBottom(body) - by;
                if (bw - 12 * p < 2 * p || bh < 2 * p) break;

                panelAround(x, y, w, h, snap(body.x) + 6 * p, by, bw - 12 * p, bh, tint);
                return;
            }
        }

        PixelCanvas.sprite(VanillaSprites.PANEL, x, y, w / p, h / p, tint);
    }

    @Override
    public void paintSearchHeader(WHontunSearch.WHontunHeader h, GuiRenderer renderer, double mouseX, double mouseY) {
        if (h.cells.isEmpty() || !(h.cells.getFirst().widget() instanceof WContainer row)) return;

        int p = unit();
        int ry = snap(row.y);
        int rh = snap(row.y + row.height) - ry;
        int ink = faceText(h.theme());

        for (Cell<?> cell : row.cells) {
            WWidget widget = cell.widget();

            if (widget instanceof WGuiTexture icon) {
                icon.color = CLEAR;
                int ix = snap(icon.x + icon.width / 2) - 6 * p;
                int iy = snap(icon.y + icon.height / 2) - 6 * p;
                PixelCanvas.sprite(VanillaSprites.SEARCH, ix, iy, 12, 12);
            } else if (widget instanceof WHontunLabel label) {
                label.hidden = true;
                PixelCanvas.text(label.richText(), label.x, ry + center(rh, 8 * p, p), ink, false);
            }
        }
    }

    @Override
    public void paintSearchBody(WHontunSearch.WHontunResultsContainer c, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme t = c.theme();
        int p = unit();
        int x = snap(c.x), y = snap(c.y);
        int w = snap(c.x + c.width) - x, h = snap(c.y + c.height) - y;

        boolean light = t.light();
        if (!light) well(x + 6 * p, y, w - 12 * p, searchWellBottom(c) - y, wellAlpha(t));
        else lightWell(x + 6 * p, y, w - 12 * p, searchWellBottom(c) - y);

        if (c.cells.size() < 2 || !(c.cells.get(1).widget() instanceof WHontunLabel help)) return;

        help.hidden = true;
        int hy = snap(help.y);

        if (!searchEmpty(c)) {
            PixelCanvas.text(help.richText(), snap(help.x), hy, faceDim(t), false);
            return;
        }

        int tw = PixelCanvas.width(NO_RESULTS);
        int hx = snap(help.x + help.width / 2) - tw / 2;
        PixelCanvas.text(NO_RESULTS, snap(hx), hy, light ? LIGHT_DIM : GREY, !light);
    }

    private static boolean searchEmpty(WHontunSearch.WHontunResultsContainer c) {
        return c.cells.getFirst().widget() instanceof WContainer view
                && view.cells.isEmpty()
                && !searchQuery(c).isEmpty();
    }

    private static int searchWellBottom(WHontunSearch.WHontunResultsContainer c) {
        int p = unit();
        int bottom = snap(c.y + c.height) - 6 * p;
        if (c.cells.size() < 2 || !(c.cells.get(1).widget() instanceof WHontunLabel help)) return bottom;
        if (searchEmpty(c)) return Math.min(snap(c.y + c.height) - 3 * p, snap(help.y + help.height) + 2 * p);
        return Math.min(bottom, snap(help.y) - 3 * p);
    }

    @Override
    public void paintSearchRow(WHontunSearch.WHontunResult row, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme t = row.theme();
        int p = unit();
        int x = snap(row.x), y = snap(row.y);
        int w = snap(row.x + row.width) - x, h = snap(row.y + row.height) - y;
        boolean light = t.light();

        if (row.mouseOver) {
            PixelCanvas.fill(x, y, w, h, light ? SLOT_HOVER : 0xFF000000);
            PixelCanvas.frame(x, y, w, h, light ? FACE_DIM : LIST_GREY);
        }

        if (row.cells.isEmpty() || !(row.cells.getFirst().widget() instanceof WContainer inner)) return;

        int tagY = y + 3 * p;

        for (Cell<?> cell : inner.cells) {
            WWidget widget = cell.widget();

            if (widget instanceof WHontunSearch.WHontunResult.WResultType chip) {
                for (Cell<?> c : chip.cells) {
                    if (c.widget() instanceof WGuiTexture icon) icon.color = CLEAR;
                }

                int cx = snap(chip.x), cy = snap(chip.y);
                int cw = snap(chip.x + chip.width) - cx, ch = snap(chip.y + chip.height) - cy;
                PixelCanvas.item(stackFor(row.result()), cx + center(cw, 16 * p, p), cy + center(ch, 16 * p, p));
            } else if (widget instanceof WContainer info && info.cells.size() >= 2) {
                tagY = snap(info.cells.getFirst().widget().y);
                if (info.cells.get(1).widget() instanceof WLabel description) description.color = light ? LIGHT_DIM_COLOR : LIST_GREY_COLOR;
            }
        }

        SearchResult result = row.result();
        String tag = result instanceof SettingSearchResult ? "Setting" : "Module";
        boolean alias = result instanceof ModuleSearchResult r && r.hasAlias();
        int tagInk = light ? (alias ? LINK : LIGHT_DIM) : (alias ? YELLOW : LIST_GREY);
        PixelCanvas.text(tag, x + w - 4 * p - PixelCanvas.width(tag), tagY, tagInk, !light);
    }

    @Override
    public void paintChip(GuiRenderer renderer, double x, double y, double w, double h, Color c, ChipKind kind) {
    }

    @Override
    public void paintMultiSelectHeader(WHontunMultiSelect<?>.WHontunHeader h, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme t = h.owner().theme();
        int labels = 0;

        for (Cell<?> cell : h.cells) {
            if (!(cell.widget() instanceof WLabel label)) continue;
            if (labels == 1) label.color = t.textSecondaryColor();
            labels++;
        }
    }

    @Override
    public void paintMultiSelectBody(WHontunMultiSelect<?> m, GuiRenderer renderer, double mouseX, double mouseY) {
    }

    @Override
    public void paintMultiSelectItem(WHontunMultiSelect<?>.WHontunItem i, GuiRenderer renderer, double mouseX, double mouseY) {
    }

    @Override
    public void paintTooltip(WHontunTooltip tt, GuiRenderer renderer, double mouseX, double mouseY) {
        if (tt.cells.isEmpty() || !(tt.cells.getFirst().widget() instanceof WHontunLabel label)) return;
        label.hidden = true;

        String text = tt.textValue();
        if (text == null || text.isEmpty()) return;

        List<FormattedText> lines = mc.font.getSplitter().splitLines(text, TOOLTIP_WRAP, Style.EMPTY);
        if (lines.isEmpty()) return;

        int widest = 0;
        for (FormattedText line : lines) widest = Math.max(widest, mc.font.width(line));

        PixelCanvas.nextLayer();

        int p = unit();
        int lx = snap(label.x), ly = snap(label.y);
        PixelCanvas.tooltipBackground(lx, ly, widest, lines.size() * 10 - 2);

        for (int i = 0; i < lines.size(); i++) {
            PixelCanvas.text(lines.get(i).getString(), lx, ly + i * 10 * p, WHITE, true);
        }
    }

    @Override
    public void paintSwatchChip(WHontunSwatchLabel l, GuiRenderer renderer, double mouseX, double mouseY) {
        Color color = l.color;
        if (color == null || l.get().isEmpty()) return;

        boolean light = l.theme().light();
        if (HontunLightPalette.contrast(packed(color), light ? 0xC6C6C6 : WELL) >= WHontunSwatchLabel.MIN_CONTRAST) return;

        int p = unit();
        int x = snap(l.x), y = snap(l.y);
        int w = snap(l.x + l.width) - x, h = snap(l.y + l.height) - y;
        PixelCanvas.fill(x - p, y - p, w + 2 * p, h + 2 * p, SWATCH_DARK);
    }

    @Override
    public void paintCountChip(HontunSettingsWidgetFactory.WSelectedCountLabel l, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme t = l.theme();
        int p = unit();
        int x = snap(l.x), y = snap(l.y);
        int w = snap(l.x + l.width) - x, h = snap(l.y + l.height) - y;
        boolean face = t.light() || onFace(l);

        String text = "(" + plain(l.label()) + ")";
        PixelCanvas.text(text, x + center(w, PixelCanvas.width(text), p), y + center(h, 8 * p, p), face ? faceDim(t) : GREY, !face);
    }

    @Override
    public void paintMeteorBackground(WWidget w, GuiRenderer renderer, boolean pressed, boolean hovered) {
        int x = snap(w.x), y = snap(w.y);
        int ww = snap(w.x + w.width) - x, h = snap(w.y + w.height) - y;
        stone(x, y, ww, h, pressed || hovered);
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

        cz.honzasik.hontun.gui.render.pixel.QuadBatchState.Builder batch = cz.honzasik.hontun.gui.render.pixel.QuadBatchState.builder();
        for (int x = 0; x <= windowWidth; x += step) batch.add(x, 0, x + p, windowHeight, GRID);
        for (int y = 0; y <= windowHeight; y += step) batch.add(0, y, windowWidth, y + p, GRID);
        batch.submit(g);

        pose.popMatrix();
    }

    private void stone(int x, int y, int w, int h, boolean hot) {
        int p = unit();
        if (w < p || h < p) return;
        PixelCanvas.sprite(hot ? VanillaSprites.BUTTON_HIGHLIGHTED : VanillaSprites.BUTTON, x, y, w / p, h / p);
    }

    private void sign(String sign, int x, int y, int w, int h, int ink) {
        int p = unit();
        PixelCanvas.text(sign, x + center(w, PixelCanvas.width(sign), p), y + center(h, 8 * p, p), ink, true);
    }

    private static void icon(GuiRenderer renderer, GuiTexture texture, int x, int y, int w, int h, Color tint) {
        if (texture == null) return;

        int p = unit();
        int size = 9 * p;
        renderer.quad(x + center(w, size, p), y + center(h, size, p), size, size, texture, tint);
    }

    private static void panelAround(int x, int y, int w, int h, int hx, int hy, int hw, int hh, int tint) {
        int p = unit();
        int b = PANEL_BORDER * p;
        int hb = hy + hh;
        int hr = hx + hw;

        clippedPanel(x - p, y - p, w + 2 * p, hy - y + 2 * p, x, y, w, hy - y + b, tint);
        clippedPanel(x - p, hb - p, w + 2 * p, y + h - hb + 2 * p, x, hb - b, w, y + h - hb + b, tint);
        clippedPanel(x - p, hy, hx - x + 2 * p, hh, x, hy - b, hx - x + b, hh + 2 * b, tint);
        clippedPanel(hr - p, hy, x + w - hr + 2 * p, hh, hr - b, hy - b, x + w - hr + b, hh + 2 * b, tint);
    }

    private static void clippedPanel(int cx, int cy, int cw, int ch, int x, int y, int w, int h, int tint) {
        if (cw <= 0 || ch <= 0 || w <= 0 || h <= 0) return;
        int p = unit();
        PixelCanvas.pushClip(cx, cy, cw, ch);
        PixelCanvas.sprite(VanillaSprites.PANEL, x, y, w / p, h / p, tint);
        PixelCanvas.popClip();
    }

    private static void well(int x, int y, int w, int h, double a) {
        int p = unit();
        if (w < 2 * p || h < 2 * p) return;

        int alpha = (int) Math.round(Math.clamp(a, 0.0, 1.0) * 255);
        int body = argb(alpha, WELL);
        int corner = argb(0xFF, WELL);
        int dark = argb(0xFF, 0x000000);
        int bright = argb(0xFF, 0xFFFFFF);

        PixelCanvas.fill(x + p, y + p, w - 2 * p, h - 2 * p, body);
        PixelCanvas.fill(x, y, w - p, p, dark);
        PixelCanvas.fill(x, y + p, p, h - 2 * p, dark);
        PixelCanvas.fill(x + p, y + h - p, w - p, p, bright);
        PixelCanvas.fill(x + w - p, y + p, p, h - 2 * p, bright);
        PixelCanvas.fill(x + w - p, y, p, p, corner);
        PixelCanvas.fill(x, y + h - p, p, p, corner);
    }

    private static void lightWell(int x, int y, int w, int h) {
        int p = unit();
        if (w < 2 * p || h < 2 * p) return;
        PixelCanvas.sunk(x, y, w, h, argb(0xFF, WELL), WHITE);
    }

    private static double wellAlpha(HontunGuiTheme t) {
        return Math.max(0.7, 0.35 + 0.65 * t.windowOpacity.get());
    }

    private static int faceText(HontunGuiTheme t) {
        return t != null && t.light() ? LIGHT_TEXT : FACE_TEXT;
    }

    private static int faceHot(HontunGuiTheme t) {
        return t != null && t.light() ? LIGHT_HOT : FACE_TEXT;
    }

    private static int faceDim(HontunGuiTheme t) {
        return t != null && t.light() ? LIGHT_DIM : FACE_DIM;
    }

    private static Identifier headerSeparator() {
        return mc != null && mc.level != null ? Screen.INWORLD_HEADER_SEPARATOR : Screen.HEADER_SEPARATOR;
    }

    private static boolean onFace(WWidget widget) {
        WWidget w = widget.parent;
        for (int i = 0; i < 64 && w != null; i++, w = w.parent) {
            if (w instanceof WView) return false;
            if (w instanceof WHontunWindow || w instanceof WHontunSearch.WHontunHeader) return true;
        }
        return false;
    }

    private static boolean inSearchHeader(WWidget widget) {
        WWidget w = widget.parent;
        for (int i = 0; i < 4 && w != null; i++, w = w.parent) {
            if (w instanceof WHontunSearch.WHontunHeader) return true;
        }
        return false;
    }

    private static WHontunWindow headerWindow(WWidget widget) {
        WWidget w = widget.parent;
        for (int i = 0; i < 4 && w != null; i++, w = w.parent) {
            if (w instanceof WHontunWindow.WHontunHeader header) return header.window();
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

    private ItemStack stackFor(SearchResult result) {
        Category category = null;

        if (result instanceof ModuleSearchResult r && r.module() != null) category = r.module().category;
        else if (result instanceof SettingSearchResult r && r.setting() != null && r.setting().module != null) category = r.setting().module.category;

        return categoryStack(category);
    }

    private ItemStack categoryStack(Category category) {
        if (category == null) return DisplayItemUtils.toStack(Items.BOOK);
        return stacks.computeIfAbsent(category, VanillaClickStyle::resolveStack);
    }

    private static ItemStack resolveStack(Category category) {
        if (category == Hontun.CATEGORY) return DisplayItemUtils.toStack(Items.ENDER_EYE);

        if (category.icon != null) {
            ItemStack stack = category.icon.get();
            if (stack != null && !stack.isEmpty()) return stack;
        }

        return DisplayItemUtils.toStack(Items.BOOK);
    }

    private static boolean is(GuiTexture texture, HontunBuiltinIcons icon) {
        try {
            return icon.texture() == texture;
        } catch (IllegalStateException e) {
            return false;
        }
    }

    private static boolean isReset(GuiTexture texture) {
        return texture != null && (is(texture, HontunBuiltinIcons.RESET) || texture == GuiRenderer.RESET);
    }

    private static boolean isSign(String text) {
        return "+".equals(text) || "-".equals(text);
    }

    private static String plain(RichText text) {
        return text == null ? "" : text.getPlainText();
    }

    private static int center(int span, int size, int p) {
        return Math.floorDiv(Math.floorDiv(span - size, p), 2) * p;
    }

    private static int ceilDiv(int a, int b) {
        return Math.floorDiv(a + b - 1, b);
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

    private static int unit() {
        return PixelCanvas.unit();
    }

    private static int u(HontunGuiTheme t) {
        return t != null ? PixelCanvas.unit(t) : PixelCanvas.unit();
    }

    private static int snap(double v) {
        return PixelCanvas.snap(v);
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
}
