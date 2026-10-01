package cz.honzasik.hontun.gui.theme.style;

import cz.honzasik.hontun.Hontun;
import cz.honzasik.hontun.gui.api.animation.Easing;
import cz.honzasik.hontun.gui.api.render.Corners;
import cz.honzasik.hontun.gui.api.render.RoundedRect;
import cz.honzasik.hontun.gui.api.text.RichText;
import cz.honzasik.hontun.gui.render.HontunRenderer;
import cz.honzasik.hontun.gui.render.pixel.PixelGlyph;
import cz.honzasik.hontun.gui.render.text.RichTextRenderer;
import cz.honzasik.hontun.gui.theme.HontunGuiTheme;
import cz.honzasik.hontun.gui.theme.HontunLightPalette;
import cz.honzasik.hontun.gui.theme.HontunSettingsWidgetFactory;
import cz.honzasik.hontun.gui.theme.HontunWidget;
import cz.honzasik.hontun.gui.theme.icons.HontunBuiltinIcons;
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
import cz.honzasik.hontun.gui.util.ColorUtils;
import cz.honzasik.hontun.utils.HontunTheme;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.renderer.packer.GuiTexture;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.joml.Matrix3x2fStack;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public abstract class ClickStyle {
    private static final Color SELECTED_COMPLETION = new Color(255, 255, 255, 15);

    private final Metrics metrics = createMetrics();

    public abstract HontunTheme.UiMode id();

    protected Metrics createMetrics() {
        return new Metrics();
    }

    public Metrics metrics() {
        return metrics;
    }

    public Pipeline pipeline() {
        return Pipeline.LEGACY;
    }

    public FontChoice font() {
        return id() == HontunTheme.UiMode.SmogClient ? FontChoice.SF : FontChoice.THEME;
    }

    public Backdrop backdrop() {
        return switch (id()) {
            case Vanilla -> Backdrop.VANILLA;
            case SmogClient -> Backdrop.SMOG;
            default -> Backdrop.MENU;
        };
    }

    public boolean uses(Knob knob) {
        return switch (knob) {
            case CORNER_RADIUS -> id() == HontunTheme.UiMode.HModern1 || id() == HontunTheme.UiMode.HModern2;
            case PALETTE_COLOR -> id() != HontunTheme.UiMode.SmogClient;
            default -> true;
        };
    }

    public double effectiveScale(double raw) {
        return raw;
    }

    public int pixelUnit(HontunGuiTheme t) {
        return (int) Math.max(1, Math.round(2 * t.rawScale()));
    }

    public double pixelAlpha(double a) {
        return a;
    }

    public PixelGlyph glyphFor(GuiTexture texture) {
        return null;
    }

    public double radiusFactor() {
        return id() == HontunTheme.UiMode.HModern1 || id() == HontunTheme.UiMode.HModern2 ? 1.0 : 0.0;
    }

    public boolean windowShadow(HontunGuiTheme t) {
        HontunTheme.UiMode m = id();
        return t.windowShadow.get() && (m == HontunTheme.UiMode.HModern1 || m == HontunTheme.UiMode.HModern2 || m == HontunTheme.UiMode.SmogClient);
    }

    public double chromeMargin(HontunGuiTheme t) {
        return windowShadow(t) ? 2 : 0;
    }

    public double windowAlpha(double raw, boolean light) {
        return light ? 0.7 + 0.3 * raw : raw;
    }

    public double controlAlpha(double raw, boolean light) {
        return light ? 0.7 + 0.3 * raw : raw;
    }

    public boolean legibilityShadow(HontunGuiTheme t) {
        return false;
    }

    public Color textShadowColor() {
        return RichTextRenderer.SHADOW_COLOR;
    }

    public double textShadowOffset(double fontScale, double renderScale) {
        return fontScale * renderScale;
    }

    public String windowId(String base) {
        return base;
    }

    public double snapWindow(HontunGuiTheme t, double value, int gridSize) {
        return gridSize > 0 ? Math.round(value / gridSize) * gridSize : value;
    }

    public AnimSpec anim(AnimRole role) {
        return switch (role) {
            case WINDOW_EXPAND, SECTION_EXPAND, DROPDOWN_OPEN, MULTISELECT_EXPAND -> AnimSpec.USER;
            case WINDOW_CORNER, SECTION_CORNER -> AnimSpec.of(Easing.QUART_OUT, 200, 200);
            case MODULE_ACTIVE -> AnimSpec.of(Easing.QUART_OUT, 300, 300);
            case MODULE_HOVER -> AnimSpec.of(Easing.LINEAR, 200, 0);
            case TAB_SELECT -> AnimSpec.of(Easing.QUART_OUT, 300, 300);
            case BUTTON_HOVER -> AnimSpec.of(Easing.QUAD_OUT, 250, 0);
            case CHECKBOX -> AnimSpec.of(Easing.BACK_IN_OUT, 300, 300);
            case DROPDOWN_HOVER -> AnimSpec.of(Easing.QUAD_OUT, 250, 250);
            default -> AnimSpec.NONE;
        };
    }

    public void palette(StylePalette out, boolean light, HontunGuiTheme t) {
        HontunTheme.UiMode m = id();
        boolean smog = m == HontunTheme.UiMode.SmogClient;
        HontunTheme.Ramp dark = HontunTheme.ramp(m);

        out.userAccent = HontunTheme.userAccent();
        out.userAccentHi = HontunTheme.userAccentHi();
        out.userAccentLo = HontunTheme.userAccentLo();

        if (!light) {
            fillRamp(out, dark);
            out.accent = smog ? HontunTheme.SMOG_ACCENT : out.userAccent;
            out.accentHi = smog ? HontunTheme.SMOG_ACCENT_HI : out.userAccentHi;
            out.accentLo = smog ? HontunTheme.SMOG_ACCENT_LO : out.userAccentLo;
            out.green = HontunTheme.green();
            out.yellow = HontunTheme.yellow();
            out.red = HontunTheme.red();
            return;
        }

        HontunTheme.Ramp lightRamp = HontunLightPalette.ramp(m);
        fillRamp(out, lightRamp);
        int body = HontunLightPalette.body(dark, lightRamp, windowAlpha(t.windowOpacity.get(), true));
        out.accent = smog ? HontunLightPalette.SMOG_ACCENT : HontunLightPalette.accentFor(out.userAccent, body);
        out.accentHi = smog ? HontunLightPalette.SMOG_ACCENT_HI : HontunLightPalette.accentFor(out.userAccentHi, body);
        out.accentLo = smog ? lightRamp.textDim() : HontunLightPalette.accentFor(out.userAccentLo, body);
        out.green = HontunLightPalette.green();
        out.yellow = HontunLightPalette.yellow();
        out.red = HontunLightPalette.red();
    }

    protected static void fillRamp(StylePalette out, HontunTheme.Ramp ramp) {
        out.crust = ramp.crust();
        out.mantle = ramp.mantle();
        out.base = ramp.base();
        out.surface0 = ramp.surface0();
        out.surface1 = ramp.surface1();
        out.surface2 = ramp.surface2();
        out.overlay0 = ramp.overlay0();
        out.overlay1 = ramp.overlay1();
        out.overlay2 = ramp.overlay2();
        out.text = ramp.text();
        out.subtext1 = ramp.subtext1();
        out.subtext0 = ramp.textDim();
    }

    public void onPressed(WWidget w) {
    }

    public WWidget windowIcon(HontunGuiTheme t, WindowKind kind, Category category, WWidget fallback) {
        return fallback;
    }

    public WWidget headerLabel(HontunGuiTheme t, WHontunWindow w, String title) {
        return t.label(windowTitle(title), true);
    }

    public WWidget headerTrailing(HontunGuiTheme t, WHontunWindow w) {
        return null;
    }

    public String windowTitle(String s) {
        return s;
    }

    public WWidget sectionTitle(HontunGuiTheme t, WHontunSection s, String title) {
        return t.horizontalSeparator(title);
    }

    public String iconButtonText(GuiTexture icon) {
        return null;
    }

    public RichText keybindLabel(HontunGuiTheme t, WHontunKeybind k, boolean listening, String key) {
        if (listening) return RichText.of("Press any key");
        return RichText.bold(k.title() + ": ").append(key);
    }

    public Color labelColor(HontunGuiTheme t, boolean title) {
        return t.textColor();
    }

    public Color helpKeyColor(HontunGuiTheme t) {
        return helpTextColor(t);
    }

    public Color helpTextColor(HontunGuiTheme t) {
        if (t.light()) return backdropText(HontunTheme.ramp(id()).text());
        return t.textColor();
    }

    private final Color backdropKey = new Color(255, 255, 255, 255);
    private final Color backdropText = new Color(255, 255, 255, 255);

    protected Color backdropKey(int rgb) {
        return backdropKey.set((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF, 255);
    }

    protected Color backdropText(int rgb) {
        return backdropText.set((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF, 255);
    }

    public void moduleSize(WHontunModule m, double[] out) {
        HontunGuiTheme t = m.theme();
        Metrics mt = metrics();
        double padH = t.scale(mt.rowPadH);
        double padV = t.scale(mt.rowPadV);
        out[0] = padH + padH + m.titleWidth() + padH;
        out[1] = padV + t.textHeight() + padV;
    }

    public void tabSize(WHontunTopBar.WTopBarButton b, double[] out) {
        HontunGuiTheme t = b.bar().theme();
        Metrics mt = metrics();
        double padH = t.scale(mt.tabPadH);
        double padV = t.scale(mt.tabPadV);
        double iconWidth = b.hasIcon() ? b.iconSize() + t.scale(mt.tabIconGap) : 0;
        out[0] = padH + iconWidth + t.textWidth(b.text()) + padH;
        out[1] = padV + t.textHeight() + padV;
    }

    public void checkboxSize(WHontunCheckbox c, double[] out) {
        double pad = c.pad();
        double s = c.theme().textHeight();
        out[0] = (pad + s + pad) * 0.9;
        out[1] = (pad + s + pad) * 0.9;
    }

    public void dropdownSize(WHontunDropdown<?> d, double[] out) {
        HontunGuiTheme t = d.theme();
        double pad = d.pad();
        double titleWidth = pad + t.textWidth(d.titleText()) + pad;
        double valueWidth = pad + d.maxValueWidth() + pad;
        double arrowWidth = pad + t.textHeight() + pad;
        out[0] = titleWidth + valueWidth + arrowWidth;
        out[1] = pad + t.textHeight() + pad;
    }

    public void buttonSize(WHontunButton b, double[] out) {
        HontunGuiTheme t = b.theme();
        double pad = b.pad();
        RichText text = b.displayText();
        if (text != null) {
            out[0] = pad + t.textWidth(text) + pad;
            out[1] = pad + t.textHeight() + pad;
        } else {
            double s = t.textHeight();
            out[0] = pad + s + pad;
            out[1] = pad + s + pad;
        }
    }

    public void pressableSize(WWidget w, double[] out) {
    }

    public void textBoxSize(WHontunTextBox b, double[] out) {
        double s = b.theme().textHeight();
        out[0] = b.padding() + s + b.padding();
        out[1] = b.padding() + s + b.padding();
    }

    public void colorPickerSize(WHontunColorPicker p, double[] out) {
        double s = p.theme().textHeight();
        out[0] = s * 3;
        out[1] = s * 1.5;
    }

    public double sliderHeight(WHontunSlider s) {
        return s.handleSize();
    }

    public double favoriteSize(WHontunFavorite f) {
        return f.theme().textHeight();
    }

    public double scrollbarWidth(HontunGuiTheme t) {
        return t.scale(6);
    }

    public double popupOffset(HontunGuiTheme t) {
        return 6;
    }

    public float radius(HontunWidget w) {
        return (float) (w.theme().scale(w.theme().effCornerRadius()));
    }

    public float smallRadius(HontunWidget w) {
        return (float) (w.theme().scale(w.theme().effSmallCornerRadius()));
    }

    public float outlineWidth(HontunWidget w) {
        return 2f;
    }

    public Corners corners(HontunWidget w) {
        if (w instanceof WHontunModule m) {
            boolean prev = m.isPrevActive(), next = m.isNextActive();
            return prev && next ? Corners.NONE :
                    prev ? Corners.BOTTOM :
                    next ? Corners.TOP : Corners.ALL;
        }
        if (w instanceof WHontunTextBox b) return b.hasTitle() ? Corners.RIGHT : Corners.ALL;
        return Corners.ALL;
    }

    protected HontunGuiTheme theme() {
        HontunGuiTheme t = HontunRenderer.get().theme();
        return t != null ? t : Hontun.THEME;
    }

    protected static RoundedRect roundedRect() {
        return RoundedRect.get();
    }

    protected static HontunRenderer text() {
        return HontunRenderer.get();
    }

    public void paintWindowBack(WHontunWindow w, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme theme = w.theme();
        Color backgroundColor = ColorUtils.withAlpha(theme.mantleColor(), theme.windowOpacity());

        int shadowOffset = (int) chromeMargin(theme);
        double headerHeight = w.headerWidget().height;
        double windowHeight = Math.max((w.height - headerHeight) * w.expandProgress(), 0);

        if (theme.effWindowShadow()) {
            Color shadowColor = theme.shadowColor();

            roundedRect().pos(w.x - shadowOffset, w.y - shadowOffset)
                         .size(w.width + shadowOffset * 2,
                                 headerHeight + windowHeight + shadowOffset * 2)
                         .radius(w.radius() + shadowOffset / 2f)
                         .color(shadowColor)
                         .render();
        }

        if (w.isExpanded() || w.expandAnimating())
            roundedRect().pos(w.x, w.y + headerHeight)
                         .size(w.width, windowHeight)
                         .radius(w.radius() - shadowOffset, Corners.BOTTOM)
                         .color(backgroundColor)
                         .render();
    }

    public void paintWindowHeader(WHontunWindow w, WHontunWindow.WHontunHeader h, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme theme = w.theme();
        double cornerProgress = w.cornerProgress();
        float radius = w.radius();

        roundedRect().bounds(h)
                     .radii(radius,
                            radius,
                            (float) (radius * (1 - cornerProgress)),
                            (float) (radius * (1 - cornerProgress)))
                     .color(theme.crustColor())
                     .render();

        if (w.isExpanded() || (w.expandAnimating() && !w.cornerAnimating())) {
            Color transparentColor = ColorUtils.withAlpha(theme.baseColor(), 0);

            Color semiTransparentColor = ColorUtils.withAlpha(
                    theme.baseColor(),
                    0.5 * theme.windowOpacity()
            );

            renderer.quad(
                    h.x,
                    h.y + h.height,
                    h.width,
                    12,
                    semiTransparentColor,
                    semiTransparentColor,
                    transparentColor,
                    transparentColor
            );
        }
    }

    public void paintWindowFront(WHontunWindow w, GuiRenderer renderer, double mouseX, double mouseY) {
    }

    public void paintTopBar(WHontunTopBar b, GuiRenderer renderer, double mouseX, double mouseY) {
        roundedRect().bounds(b)
                     .radius(b.radius())
                     .color(b.theme().baseColor())
                     .render();
    }

    public void paintTab(WHontunTopBar.WTopBarButton b, GuiRenderer renderer, double mouseX, double mouseY) {
        WHontunTopBar bar = b.bar();
        HontunGuiTheme theme = bar.theme();
        Metrics mt = metrics();

        boolean isSelected = b.isSelected();
        double selectedProgress = b.selectedProgress();

        if (b.mouseOver && selectedProgress == 0) {
            roundedRect().bounds(b)
                         .radius(bar.smallRadius())
                         .color(theme.surface0Color())
                         .render();
        }

        if (selectedProgress > 0) {
            double glowSize = theme.scale(2);
            roundedRect().pos(b.x - glowSize, b.y - glowSize)
                         .size(b.width + glowSize * 2, b.height + glowSize * 2)
                         .radius(bar.smallRadius() + glowSize)
                         .color(ColorUtils.withAlpha(theme.accentColor(), (int) (30 * selectedProgress)))
                         .render();

            roundedRect().bounds(b)
                         .radius(bar.smallRadius())
                         .color(ColorUtils.withAlpha(theme.accentColor(), 60))
                         .render();
        }

        Color color = isSelected ? theme.accentColor() : theme.textColor();

        double textWidth = theme.textWidth(b.text());
        double gap = theme.scale(mt.tabIconGap);
        double iconSize = b.iconSize();
        double contentWidth = textWidth + (b.hasIcon() ? iconSize + gap : 0);

        double offsetX = b.width / 2 - contentWidth / 2;
        double offsetY = b.height / 2 - theme.textHeight() / 2;

        if (b.hasIcon()) {
            double iconY = b.height / 2 - iconSize / 2;

            renderer.quad(b.x + offsetX, b.y + iconY, iconSize, iconSize, b.icon(), color);

            offsetX += iconSize + gap;
        }

        text().text(
                b.text(),
                b.x + offsetX,
                b.y + offsetY,
                color
        );
    }

    public void paintModule(WHontunModule m, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme theme = m.theme();
        Metrics mt = metrics();
        boolean moduleActive = m.module().isActive();
        double pad = theme.scale(mt.rowPadH);

        double hoverProgress = m.hoverProgress();
        double highlightProgress = m.highlightProgress();

        if (hoverProgress > 0 || highlightProgress > 0) {
            int baseAlpha = theme.light() ? 38 : 60;
            double hoverMultiplier = (m.mouseOver && moduleActive) ? 1.3f : 1.0f;
            double mix = Math.min(1.0, highlightProgress + hoverProgress);
            double alpha = baseAlpha * mix * hoverMultiplier;

            Color color = ColorUtils.withAlpha(theme.accentColor(), (int) alpha);
            Corners bgCorners = (m.mouseOver && !moduleActive) ? Corners.ALL : m.corners();

            double maxOffset = 1.5;
            double offset = maxOffset * (1.0 - highlightProgress);

            roundedRect().pos(m.x + offset, m.y + offset)
                         .size(m.width - (offset * 2), m.height - (offset * 2))
                         .color(color)
                         .radius(m.smallRadius(), bgCorners)
                         .render();
        }

        double lineWidth = theme.scale(mt.activeBarWidth);

        if (highlightProgress > 0) {
            Corners corners = m.corners();

            double offset = theme.scale(mt.activeBarInset);
            double offsetTop = m.isPrevActive() ? 0 : offset;
            double offsetBottom = m.isNextActive() ? 0 : offset;

            double lineHeight = m.height - offsetTop - offsetBottom;

            double finalTop = m.y + offsetTop;
            double centerY = finalTop + lineHeight / 2;

            double lineX = m.x + pad;
            double lineY = centerY - (lineHeight * highlightProgress) / 2;

            roundedRect().pos(lineX, lineY)
                         .size(lineWidth, lineHeight * highlightProgress)
                         .color(ColorUtils.withAlpha(theme.accentColor(), highlightProgress))
                         .radius(m.smallRadius(), corners)
                         .render();
        }

        double x = m.x;
        double w = m.width;
        double titleWidth = m.titleWidth();

        switch (theme.moduleAlignment.get()) {
            case Center -> x += w / 2 - titleWidth / 2;
            case Right -> x += w - titleWidth - pad * 2;
            default -> x += pad + lineWidth + pad;
        }

        Color color = ColorUtils.interpolateColor(
                theme.textColor(),
                theme.accentColor(),
                highlightProgress
        );

        text().text(m.title(), x, m.y + theme.scale(mt.rowPadV), color);
    }

    public void paintSectionBody(WHontunSection s, GuiRenderer renderer, double mouseX, double mouseY) {
        if (!s.isExpanded() && !s.expandAnimating()) return;

        HontunGuiTheme theme = s.theme();
        double headerHeight = s.headerWidget().height;

        Color backgroundColor = ColorUtils.withAlpha(
                theme.baseColor(),
                theme.backgroundOpacity()
        );

        Color outlineColor = ColorUtils.withAlpha(
                theme.surface0Color(),
                theme.backgroundOpacity()
        );

        roundedRect().pos(s.x, s.y + headerHeight)
                     .size(s.width, s.height - headerHeight)
                     .radius(s.radius(), Corners.BOTTOM)
                     .color(backgroundColor)
                     .outline(outlineColor, s.outlineWidth())
                     .render();
    }

    public void paintSectionHeader(WHontunSection s, WHontunSection.WHontunHeader h, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme theme = s.theme();
        double cornerProgress = s.cornerProgress();
        float radius = s.radius();

        Color bgColor = ColorUtils.withAlpha(
                h.mouseOver ? theme.surface1Color() : theme.surface0Color(),
                theme.backgroundOpacity()
        );

        roundedRect().bounds(h)
                     .radii(radius,
                            radius,
                            (float) (radius * (1 - cornerProgress)),
                            (float) (radius * (1 - cornerProgress)))
                     .color(bgColor)
                     .render();
    }

    public void paintSeparatorH(WHontunHorizontalSeparator s, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme theme = s.theme();

        if (!s.hasText()) {
            double t = theme.scale(metrics().separatorThickness);
            double w = s.width / 2;

            renderer.quad(s.x, s.y + t, w, t, s.lineColor());
            renderer.quad(s.x + w, s.y + t, w, t, s.lineColor());
            return;
        }

        double offsetX = s.width / 2 - s.titleWidth() / 2;
        double offsetY = s.height / 2 - theme.textHeight() / 2;

        text().text(
                s.richText(),
                s.x + offsetX,
                s.y + offsetY,
                theme.accentColor()
        );
    }

    public void paintSeparatorV(WHontunVerticalSeparator s, GuiRenderer renderer, double mouseX, double mouseY) {
        roundedRect().bounds(s)
                     .radius(s.smallRadius())
                     .color(s.theme().surface0Color())
                     .render();
    }

    public void paintViewUnderlay(WHontunView v, GuiRenderer renderer, double mouseX, double mouseY) {
    }

    public void paintScrollbar(WHontunView v, GuiRenderer renderer, double mouseX, double mouseY) {
        if (v.scrollable() && v.hasScrollBar) {
            roundedRect().pos(v.barX(), v.barY())
                         .size(v.barWidth(), v.barHeight())
                         .radius(v.smallRadius())
                         .color(v.theme().scrollbarColor.get(

                                 v.isFocused(),

                                 v.barHovered()
                         ))
                         .render();
        }
    }

    public void paintButton(WHontunButton b, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme theme = b.theme();
        double hoverProgress = b.hoverProgress();

        Color bg = theme.backgroundColor.get(b.isPressed(), b.mouseOver);
        Color accent = ColorUtils.withAlpha(theme.accentColor(), 0.8);
        Color outline = ColorUtils.interpolateColor(bg, accent, hoverProgress);

        double pad = b.pad();

        b.background(bg, outline).render();

        RichText text = b.displayText();

        if (text != null) {
            text().text(
                    text,
                    b.x + b.width / 2 - b.labelWidth() / 2,
                    b.y + pad, theme.textColor()
            );
        }
        else {
            double ts = theme.textHeight();
            renderer.quad(b.x + b.width / 2 - ts / 2, b.y + pad, ts, ts, b.icon(), theme.textColor());
        }
    }

    public void paintCheckbox(WHontunCheckbox c, GuiRenderer renderer, double mouseX, double mouseY) {
        if (!c.checked || c.animating()) c.background(false, c.mouseOver).render();

        if (!c.checked && c.animFinished()) return;

        HontunGuiTheme theme = c.theme();
        double progress = c.progress();
        double size = c.width * progress;
        double tickSize = size * 0.6;
        double minSize = theme.scale(6);

        if (size <= minSize) return;

        double centerOffset = (c.width - size) / 2;

        roundedRect().pos(c.x + centerOffset, c.y + centerOffset)
                     .size(size, size)
                     .radius(c.smallRadius())
                     .color(theme.accentColor())
                     .outline(theme.accentColor().copy().a(c.mouseOver ? 140 : 80), 3f)
                     .render();

        if (tickSize <= minSize) return;

        centerOffset = (c.width - tickSize) / 2;

        renderer.rotatedQuad(
                c.x + centerOffset,
                c.y + centerOffset,
                tickSize,
                tickSize,
                0,
                HontunBuiltinIcons.TICK.texture(),
                theme.backgroundColor.get(160)
        );
    }

    public void paintColorPicker(WHontunColorPicker p, GuiRenderer renderer, double mouseX, double mouseY) {
        Color color = p.color();
        p.background(p.mouseOver ? ColorUtils.darker(color) : color, p.theme().surface2Color()).render();

        if (p.mouseOver) {
            double s = p.theme().textHeight();

            renderer.quad(
                    p.x + p.width / 2 - s / 2,
                    p.y + p.height / 2 - s / 2,
                    s,
                    s,
                    p.overlay(),
                    pickerIconTint(color)
            );
        }
    }

    protected static Color pickerIconTint(Color color) {
        Color shown = ColorUtils.darker(color);
        int rgb = (shown.r << 16) | (shown.g << 8) | shown.b;
        return HontunLightPalette.luminance(rgb) > 0.4 ? new Color(16, 17, 21) : new Color(255, 255, 255);
    }

    public void paintConfirmedButton(WHontunConfirmedButton b, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme theme = b.theme();
        double pad = b.pad();
        boolean pressed = b.isPressed();

        Color outline = theme.outlineColor.get(pressed, b.mouseOver);
        Color fg = b.armed() ? theme.backgroundColor.get(pressed, b.mouseOver) : theme.textColor();
        Color bg = b.armed() ? theme.textColor() : theme.backgroundColor.get(pressed, b.mouseOver);

        b.background(bg, outline).render();

        String text = b.getText();

        if (text != null) {
            renderer.text(text, b.x + b.width / 2 - b.labelWidth() / 2, b.y + pad, fg, false);
        }
        else {
            double ts = theme.textHeight();
            renderer.quad(b.x + b.width / 2 - ts / 2, b.y + pad, ts, ts, b.icon(), fg);
        }
    }

    public void paintConfirmedMinus(WHontunConfirmedMinus m, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme theme = m.theme();
        double pad = m.pad();
        double s = theme.scale(3);
        boolean pressed = m.isPressed();

        Color outline = theme.outlineColor.get(pressed, m.mouseOver);
        Color fg = m.armed() ? theme.backgroundColor.get(pressed, m.mouseOver) : theme.redColor();
        Color bg = m.armed() ? theme.redColor() : theme.backgroundColor.get(pressed, m.mouseOver);

        m.background(bg, outline).render();
        renderer.quad(m.x + pad, m.y + m.height / 2 - s / 2, m.width - pad * 2, s, fg);
    }

    public void paintMinus(WHontunMinus m, GuiRenderer renderer, double mouseX, double mouseY) {
        double pad = m.pad();
        double s = m.theme().textHeight();

        m.background(m.isPressed(), m.mouseOver).render();

        renderer.quad(
                m.x + pad,
                m.y + pad,
                s,
                s,
                HontunBuiltinIcons.MINUS.texture(),
                m.theme().redColor()
        );
    }

    public void paintPlus(WHontunPlus p, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme theme = p.theme();
        double pad = p.pad();
        double s = theme.textHeight();

        p.background(p.isPressed(), p.mouseOver).render();

        renderer.quad(
                p.x + pad,
                p.y + pad,
                s,
                s,
                HontunBuiltinIcons.PLUS.texture(),
                theme.greenColor()
        );
    }

    public void paintFavorite(WHontunFavorite f, GuiRenderer renderer, double mouseX, double mouseY) {
        renderer.quad(
                f.x,
                f.y,
                f.size(),
                f.size(),
                f.checked ? HontunBuiltinIcons.BOOKMARK_YES.texture() : HontunBuiltinIcons.BOOKMARK_NO.texture(),
                f.tint()
        );
    }

    public Color favoriteColor(WHontunFavorite f) {
        HontunGuiTheme theme = f.theme();
        return f.checked
                ? theme.accentColor()
                : f.mouseOver
                    ? theme.textSecondaryColor()
                    : theme.textColor();
    }

    public void paintTriangle(WHontunTriangle t, GuiRenderer renderer, double mouseX, double mouseY) {
        double s = t.theme().textHeight();

        renderer.rotatedQuad(
                t.x,
                t.y,
                s,
                s,
                t.rotation,
                HontunBuiltinIcons.ARROW.texture(),
                t.theme().textColor()
        );
    }

    public void paintKeybindExtra(WHontunKeybind k, GuiRenderer renderer, double mouseX, double mouseY) {
    }

    public void paintSlider(WHontunSlider s, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme theme = s.theme();

        double halfHandleSize = s.handleSize() / 2;
        double backgroundY = s.y + s.height / 2 - halfHandleSize / 2;
        float smallRadius = s.smallRadius();
        float smallerRadius = smallRadius / 2;
        double valueWidth = s.valueOffset();

        roundedRect().pos(s.x, backgroundY)
                     .size(valueWidth + halfHandleSize / 2, halfHandleSize)
                     .radii(smallRadius,
                             smallerRadius,
                             smallRadius,
                             smallerRadius)
                     .color(theme.accentColor())
                     .render();

        roundedRect().pos(s.x + valueWidth + s.handleSize() - halfHandleSize / 2, backgroundY)
                     .size(s.width - valueWidth - s.handleSize() + halfHandleSize / 2, halfHandleSize)
                     .radii(smallerRadius,
                             smallRadius,
                             smallerRadius,
                             smallRadius)
                     .color(ColorUtils.withAlpha(theme.accentColor(), 0.5))
                     .render();

        double size = s.handleSize();
        double handleX = s.x + valueWidth + size / 2;
        double handleY = s.y + s.height / 2 - size / 2;
        double handleWidth = theme.scale(s.isDragging() ? 2 : 4);

        roundedRect().pos(handleX - handleWidth / 2, handleY)
                     .size(handleWidth, size)
                     .radius(smallRadius)
                     .color(theme.accentColor())
                     .render();
    }

    public void paintDropdown(WHontunDropdown<?> d, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme theme = d.theme();
        double pad = d.pad();
        double s = theme.textHeight();

        double hoverProgress = d.hoverProgress();

        Color bg = theme.backgroundColor.get(d.isPressed(), d.mouseOver);
        Color accent = ColorUtils.withAlpha(theme.accentColor(), 0.8);
        Color outline = ColorUtils.interpolateColor(bg, accent, hoverProgress);

        d.background(bg, outline).render();

        text().text(
                d.titleText(),
                d.x + pad,
                d.y + pad,
                theme.textColor()
        );

        double dotSize = theme.textHeight() / 3;
        double dotX = d.x + pad + theme.textWidth(d.titleText()) + pad;
        double dotY = d.y + pad + theme.textHeight() / 2 - dotSize / 2;

        renderer.quad(
                dotX,
                dotY,
                dotSize,
                dotSize,
                GuiRenderer.CIRCLE,
                theme.accentColor()
        );

        text().text(
                d.valueText(),
                dotX + dotSize + pad,
                d.y + pad,
                theme.accentColor()
        );

        renderer.rotatedQuad(
                d.x + d.width - pad - s,
                d.y + d.height / 2 - s / 2,
                s,
                s,
                180 * (1 - d.indicatorProgress()),
                HontunBuiltinIcons.ARROW.texture(),
                theme.textColor()
        );
    }

    public void paintDropdownPopup(WHontunDropdown<?> d, WHontunDropdown.WRoot root, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme theme = root.theme();

        Color outlineColor = ColorUtils.withAlpha(
                theme.accentColor(),
                0.8 + (0.2 * theme.backgroundOpacity())
        );

        Color backgroundColor = ColorUtils.withAlpha(
                theme.backgroundColor.get(false, false),
                0.8 + (0.2 * theme.backgroundOpacity())
        );

        root.background(backgroundColor, outlineColor).render();
    }

    public void paintDropdownValue(WHontunDropdown<?>.WValue v, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme theme = v.theme();

        if (v.mouseOver)
            roundedRect().bounds(v)
                         .radius(v.smallRadius())
                         .color(ColorUtils.withAlpha(theme.accentColor(), 0.4))
                         .render();

        boolean isSelected = v.isSelectedValue();
        RichText text = v.valueName().boldIf(isSelected);
        Color textColor = isSelected ? theme.accentColor() : theme.textColor();

        text().text(
                text,
                v.x + v.width / 2 - theme.textWidth(text) / 2,
                v.y + v.pad(),
                textColor
        );
    }

    public void paintTextBox(WHontunTextBox b, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme theme = b.theme();
        double padding = b.padding();
        int horizontalListSpacing = 3;
        double titleWidth = (b.hasTitle() ? b.pad() + theme.textWidth(b.title()) + horizontalListSpacing : 0);

        if (b.rendersBackground()) {
            if (b.hasTitle()) {
                roundedRect().pos(b.x - titleWidth, b.y)
                             .size(titleWidth, b.height)
                             .radius(b.smallRadius(), Corners.LEFT)
                             .color(theme.surface0Color())
                             .render();
            }

            b.background(theme.baseColor(), theme.surface0Color()).render();
        }

        double overflowWidth = b.overflow();

        renderer.scissorStart(b.x + padding, b.y, b.width - padding * 2, b.height);

        String text = b.textValue();
        if (!text.isEmpty()) {
            Color custom = b.customColor();
            Color textColor = custom != null
                    ? custom
                    : (b.isFocused() ? theme.textColor() : dimmedText(theme));

            b.drawText(renderer, b.x + padding - overflowWidth, b.y + padding, text, textColor);
        }
        else if (b.placeholderValue() != null) {
            b.drawText(renderer, b.x + padding - overflowWidth, b.y + padding, b.placeholderValue(), theme.textSecondaryColor());
        }

        if (b.isFocused() && b.hasSelection()) {
            double selStart = b.x + padding + b.textWidthAt(b.selectionStartIndex()) - overflowWidth;
            double selEnd = b.x + padding + b.textWidthAt(b.selectionEndIndex()) - overflowWidth;

            paintTextSelection(b, renderer, selStart, selEnd, b.y + padding, theme.textHeight());
        }

        double caretAlpha = b.caretAlpha();

        if ((b.isFocused() && b.caretVisible()) || caretAlpha > 0) {
            paintCaret(b, renderer, b.x + padding + b.textWidthAt(b.cursorIndex()) - overflowWidth, b.y + padding, theme.textHeight(), caretAlpha);
        }

        renderer.scissorEnd();
    }

    protected static Color dimmedText(HontunGuiTheme theme) {
        return theme.light()
                ? ColorUtils.interpolateColor(theme.textSecondaryColor(), theme.baseColor(), 0.3)
                : ColorUtils.darker(theme.textSecondaryColor());
    }

    public void paintTextSelection(WHontunTextBox b, GuiRenderer renderer, double x0, double x1, double y, double h) {
        renderer.quad(x0, y, x1 - x0, h, b.theme().textHighlightColor().copy().a(120));
    }

    public void paintCaret(WHontunTextBox b, GuiRenderer renderer, double x, double y, double h, double alpha) {
        HontunGuiTheme theme = b.theme();
        renderer.setAlpha(alpha);
        renderer.quad(x, y, theme.scale(1), h, theme.textColor());
        renderer.setAlpha(1);
    }

    public void paintCompletions(WHontunTextBox b, GuiRenderer renderer, double x, double y, double w, double h) {
        HontunGuiTheme theme = b.theme();
        double s = theme.scale(2);
        Color c = theme.outlineColor.get();

        Color col = theme.backgroundColor.get().copy();
        col.a += col.a / 2;
        col.validate();
        renderer.quad(x, y, w, h, col);

        renderer.quad(x, y + h - s, w, s, c);
        renderer.quad(x, y, s, h - s, c);
        renderer.quad(x + w - s, y, s, h - s, c);
    }

    public void paintCompletionItem(WHontunTextBox.CompletionItem item, GuiRenderer renderer, double mouseX, double mouseY) {
        item.drawLabel(renderer, mouseX, mouseY);

        if (item.isSelected()) renderer.quad(item, item.theme().light() ? ColorUtils.withAlpha(item.theme().textColor(), 15) : SELECTED_COMPLETION);
    }

    public void paintSearchPanel(WHontunSearch s, GuiRenderer renderer, double mouseX, double mouseY) {
        Color shadowColor = s.theme().shadowColor();

        int shadowOffset = 2;
        roundedRect().pos(s.x - shadowOffset, s.y - shadowOffset)
                     .size(s.width + shadowOffset * 2, s.height + shadowOffset * 2)
                     .radius(s.radius() + shadowOffset)
                     .color(shadowColor)
                     .render();
    }

    public void paintSearchHeader(WHontunSearch.WHontunHeader h, GuiRenderer renderer, double mouseX, double mouseY) {
        roundedRect().bounds(h)
                     .color(h.theme().crustColor())
                     .radius(h.radius(), Corners.TOP)
                     .render();
    }

    public void paintSearchBody(WHontunSearch.WHontunResultsContainer c, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme theme = c.theme();

        roundedRect().bounds(c)
                     .color(ColorUtils.withAlpha(theme.baseColor(), theme.windowOpacity()))
                     .radius(c.radius(), Corners.BOTTOM)
                     .render();
    }

    public void paintSearchRow(WHontunSearch.WHontunResult row, GuiRenderer renderer, double mouseX, double mouseY) {
        if (!row.mouseOver) return;

        HontunGuiTheme theme = row.theme();

        Color outlineColor = ColorUtils.withAlpha(
                theme.accentColor(),
                theme.backgroundOpacity() * 0.5
        );

        row.background(row.getBackgroundColor(row.isPressed(), false), outlineColor).render();
    }

    public void paintChip(GuiRenderer renderer, double x, double y, double w, double h, Color c, ChipKind kind) {
        HontunGuiTheme theme = theme();

        roundedRect().pos(x, y)
                     .size(w, h)
                     .color(ColorUtils.withAlpha(c, 60))
                     .radius((float) theme.scale(theme.effSmallCornerRadius()))
                     .render();
    }

    public void paintMultiSelectHeader(WHontunMultiSelect<?>.WHontunHeader h, GuiRenderer renderer, double mouseX, double mouseY) {
        WHontunMultiSelect<?> m = h.owner();
        HontunGuiTheme theme = m.theme();
        Color bgColor = ColorUtils.withAlpha(
                h.mouseOver ? theme.surface1Color() : theme.surface0Color(),
                theme.backgroundOpacity()
        );

        roundedRect().bounds(h)
                     .radius(m.radius(), m.isExpanded() || m.animating() ? Corners.TOP : Corners.ALL)
                     .color(bgColor)
                     .render();
    }

    public void paintMultiSelectBody(WHontunMultiSelect<?> m, GuiRenderer renderer, double mouseX, double mouseY) {
        if (m.isExpanded() || m.animating()) {
            double headerHeight = m.headerWidget().height;
            roundedRect().pos(m.x, m.y + headerHeight)
                         .size(m.width, m.height - headerHeight)
                         .radius(m.radius(), Corners.BOTTOM)
                         .color(ColorUtils.withAlpha(m.theme().baseColor(), m.theme().backgroundOpacity()))
                         .render();
        }
    }

    public void paintMultiSelectItem(WHontunMultiSelect<?>.WHontunItem i, GuiRenderer renderer, double mouseX, double mouseY) {
        if (!i.mouseOver || i.checkboxHovered()) return;

        WHontunMultiSelect<?> m = i.owner();

        roundedRect().bounds(i)
                     .radius(m.smallRadius())
                     .color(m.theme().surface0Color())
                     .render();
    }

    public void paintTooltip(WHontunTooltip t, GuiRenderer renderer, double mouseX, double mouseY) {
        t.background(t.theme().baseColor(), t.theme().surface0Color()).render();
    }

    public void paintSwatchChip(WHontunSwatchLabel l, GuiRenderer renderer, double mouseX, double mouseY) {
        Color color = l.color;
        if (color == null || l.get().isEmpty()) return;

        HontunGuiTheme theme = l.theme();
        int rgb = packed(color);
        if (HontunLightPalette.contrast(rgb, packed(theme.mantleColor())) < WHontunSwatchLabel.MIN_CONTRAST) {
            Color chip = HontunLightPalette.luminance(rgb) > 0.4 ? new Color(22, 24, 28) : new Color(244, 245, 247);
            double pad = theme.scale(2);
            roundedRect().pos(l.x - pad, l.y - pad / 2)
                         .size(l.width + pad * 2, l.height + pad)
                         .radius(l.smallRadius())
                         .color(chip)
                         .render();
        }
    }

    protected static int packed(Color c) {
        return (c.r << 16) | (c.g << 8) | c.b;
    }

    public void paintCountChip(HontunSettingsWidgetFactory.WSelectedCountLabel l, GuiRenderer renderer, double mouseX, double mouseY) {
        HontunGuiTheme theme = l.theme();

        roundedRect().bounds(l)
                     .radius(l.smallRadius())
                     .color(theme.surface0Color())
                     .render();

        text().text(
                l.label(),
                l.x + l.offsetX(),
                l.y + l.offsetY(),
                l.color != null ? l.color : theme.textColor()
        );
    }

    public void paintMeteorBackground(WWidget w, GuiRenderer renderer, boolean pressed, boolean hovered) {
        HontunGuiTheme theme = theme();

        roundedRect()
                .bounds(w)
                .radius(theme.scale(theme.effSmallCornerRadius()))
                .color(theme.backgroundColor.get(pressed, hovered))
                .outline(theme.outlineColor.get(pressed, hovered), 2f)
                .render();
    }

    public void paintMeteorBackground(WWidget w, GuiRenderer renderer, Color outlineColor, Color backgroundColor) {
        HontunGuiTheme theme = theme();

        roundedRect()
                .bounds(w)
                .radius(theme.scale(theme.effSmallCornerRadius()))
                .color(backgroundColor)
                .outline(outlineColor, 2f)
                .render();
    }

    public void paintSnapGrid(GuiGraphicsExtractor g, int gridSize) {
        if (gridSize <= 0) return;

        HontunGuiTheme theme = theme();
        int color = theme.overlay0Color().copy().a(60).getPacked();
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
