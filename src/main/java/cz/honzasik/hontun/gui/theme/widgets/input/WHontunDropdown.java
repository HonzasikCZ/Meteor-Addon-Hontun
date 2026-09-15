package cz.honzasik.hontun.gui.theme.widgets.input;

import cz.honzasik.hontun.gui.api.animation.Animation;
import cz.honzasik.hontun.gui.api.animation.Direction;
import cz.honzasik.hontun.gui.api.animation.Easing;
import cz.honzasik.hontun.gui.api.text.RichText;
import cz.honzasik.hontun.gui.theme.HontunGuiTheme;
import cz.honzasik.hontun.gui.theme.HontunWidget;
import cz.honzasik.hontun.gui.theme.icons.HontunBuiltinIcons;
import cz.honzasik.hontun.gui.util.ColorUtils;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.utils.Cell;
import meteordevelopment.meteorclient.gui.widgets.input.WDropdown;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.render.color.Color;

import java.util.Locale;

public class WHontunDropdown<T> extends WDropdown<T> implements HontunWidget {
    private final RichText titleText;
    private RichText valueText;

    private Animation hoverAnimation;
    private Animation indicatorAnimation;

    public WHontunDropdown(String title, T[] values, T value) {
        super(values, value);
        titleText = RichText.of(title);
        valueText = RichText.of(getNameFor(value));
    }

    @Override
    public void init() {
        double pad = theme.pad();

        root = createRootWidget();
        root.theme = theme;
        root.spacing = pad;
        maxValueWidth = 0;

        for (int i = 0; i < values.length; i++) {
            WValue widget = new WValue(values[i]);
            widget.theme = theme;

            double valueWidth = theme().textWidth(widget.valueName);
            maxValueWidth = Math.max(maxValueWidth, valueWidth);

            Cell<?> cell = root.add(widget).padHorizontal(pad).expandWidgetX();
            if (i == 0) cell.padTop(pad);
            if (i == values.length - 1) cell.padBottom(pad);
        }

        hoverAnimation = new Animation(Easing.QUAD_OUT, 250);

        indicatorAnimation = new Animation(
                theme().guiAnimationEasing(),
                theme().guiAnimationDuration(),
                Direction.BACKWARDS
        );
    }

    @Override
    protected WDropdownRoot createRootWidget() {
        return new WRoot();
    }

    @Override
    protected WDropdownValue createValueWidget() {
        return null;
    }

    @Override
    protected void onCalculateSize() {
        double pad = pad();

        root.calculateSize();

        double titleWidth = pad + theme().textWidth(titleText) + pad;
        double valueWidth = pad + maxValueWidth + pad;
        double arrowWidth = pad + theme.textHeight() + pad;

        width = titleWidth + valueWidth + arrowWidth;
        height = pad + theme.textHeight() + pad;

        root.width = width;
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        HontunGuiTheme theme = theme();
        double pad = pad();
        double s = theme.textHeight();

        double hoverProgress = hoverAnimation.getProgress();

        if (mouseOver && hoverProgress == 0)
            hoverAnimation.start();

        if (!mouseOver && hoverProgress > 0)
            hoverAnimation.reset();

        Color bg = theme.backgroundColor.get(pressed, mouseOver);
        Color accent = ColorUtils.withAlpha(theme.accentColor(), 0.8);
        Color outline = ColorUtils.interpolateColor(bg, accent, hoverProgress);

        background(bg, outline).render();

        renderer().text(
                titleText,
                x + pad,
                y + pad,
                theme.textColor()
        );

        double dotSize = theme.textHeight() / 3;
        double dotX = x + pad + theme.textWidth(titleText) + pad;
        double dotY = y + pad + theme.textHeight() / 2 - dotSize / 2;

        renderer.quad(
                dotX,
                dotY,
                dotSize,
                dotSize,
                GuiRenderer.CIRCLE,
                theme.accentColor()
        );

        renderer().text(
                valueText,
                dotX + dotSize + pad,
                y + pad,
                theme.accentColor()
        );

        renderer.rotatedQuad(
                x + width - pad - s,
                y + height / 2 - s / 2,
                s,
                s,
                180 * (1 - indicatorAnimation.getProgress()),
                HontunBuiltinIcons.ARROW.texture(),
                theme.textColor()
        );
    }

    @Override
    public boolean render(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        animProgress = -8008135;

        boolean render = super.render(renderer, mouseX, mouseY, delta);
        double progress = indicatorAnimation.getProgress();

        if (!render && progress > 0) {
            renderer.absolutePost(() -> {
                renderer.scissorStart(root.x, root.y, root.width, root.height * progress);
                root.render(renderer, mouseX, mouseY, delta);
                renderer.scissorEnd();
            });
        }

        return render;
    }

    @Override
    protected void onPressed(int button) {
        super.onPressed(button);
        handlePressed();
    }

    @Override
    public void set(T value) {
        super.set(value);

        if (indicatorAnimation == null) return;

        indicatorAnimation.reset();
        handlePressed();
    }

    private void handlePressed() {
        valueText = RichText.of(getNameFor(value));
        indicatorAnimation.reverse();
    }

    private String getNameFor(T value) {
        String name = value.toString();

        if (name.contains("_")) return Utils.nameToTitle(name.toLowerCase(Locale.ROOT).replace("_", "-"));

        return Utils.nameToTitle(name.replaceAll("(?<=[a-z])([A-Z])", "-$1").toLowerCase(Locale.ROOT));
    }

    private static class WRoot extends WDropdownRoot implements HontunWidget {
        private static final int DROPDOWN_Y_OFFSET = 6;

        @Override
        protected void onCalculateWidgetPositions() {
            this.y += DROPDOWN_Y_OFFSET;
            super.onCalculateWidgetPositions();
        }

        @Override
        protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
            HontunGuiTheme theme = theme();

            Color outlineColor = ColorUtils.withAlpha(
                    theme.accentColor(),
                    0.8 + (0.2 * theme.backgroundOpacity())
            );

            Color backgroundColor = ColorUtils.withAlpha(
                    theme.backgroundColor.get(false, false),
                    0.8 + (0.2 * theme.backgroundOpacity())
            );

            background(backgroundColor, outlineColor).render();
        }
    }

    private class WValue extends WDropdownValue implements HontunWidget {
        private final RichText valueName;

        public WValue(T value) {
            this.value = value;
            this.valueName = RichText.of(getNameFor(value));
        }

        @Override
        protected void onCalculateSize() {
            double pad = pad();

            width = pad + theme().textWidth(valueName) + pad;
            height = pad + theme.textHeight() + pad;
        }

        @Override
        protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
            HontunGuiTheme theme = theme();

            if (mouseOver)
                roundedRect().bounds(this)
                             .radius(smallRadius())
                             .color(ColorUtils.withAlpha(theme.accentColor(), 0.4))
                             .render();

            boolean isSelected = get() == this.value;
            RichText text = valueName.boldIf(isSelected);
            Color textColor = isSelected ? theme.accentColor() : theme.textColor();

            renderer().text(
                    text,
                    x + width / 2 - theme.textWidth(text) / 2,
                    y + pad(),
                    textColor
            );
        }

        @Override
        protected void onPressed(int button) {
            super.onPressed(button);
            handlePressed();
        }
    }
}
