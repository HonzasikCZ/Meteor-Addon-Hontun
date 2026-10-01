package cz.honzasik.hontun.gui.theme.widgets.input;

import cz.honzasik.hontun.gui.api.text.RichText;
import cz.honzasik.hontun.gui.render.route.Routers;
import cz.honzasik.hontun.gui.theme.HontunWidget;
import cz.honzasik.hontun.gui.theme.style.AnimRole;
import cz.honzasik.hontun.gui.theme.style.ClickStyle;
import cz.honzasik.hontun.gui.theme.style.StyleAnimation;
import cz.honzasik.hontun.utils.HontunTheme;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.utils.Cell;
import meteordevelopment.meteorclient.gui.widgets.input.WDropdown;
import meteordevelopment.meteorclient.utils.Utils;

import java.util.Locale;

public class WHontunDropdown<T> extends WDropdown<T> implements HontunWidget {
    private final RichText titleText;
    private RichText valueText;

    private StyleAnimation hoverAnimation;
    private StyleAnimation indicatorAnimation;

    private final double[] size = new double[2];

    public WHontunDropdown(String title, T[] values, T value) {
        super(values, value);
        titleText = RichText.of(title);
        valueText = RichText.of(getNameFor(value));
    }

    @Override
    public void init() {
        double pad = metrics().dropdownPad;

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

        ClickStyle style = style();
        hoverAnimation = StyleAnimation.of(style.anim(AnimRole.DROPDOWN_HOVER), theme());
        indicatorAnimation = StyleAnimation.of(style.anim(AnimRole.DROPDOWN_OPEN), theme(), false);
    }

    @Override
    protected WDropdownRoot createRootWidget() {
        WRoot r = new WRoot();
        r.owner = this;
        return r;
    }

    @Override
    protected WDropdownValue createValueWidget() {
        return null;
    }

    @Override
    protected void onCalculateSize() {
        root.calculateSize();

        style().dropdownSize(this, size);
        width = size[0];
        height = size[1];

        root.width = width;
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        double hoverProgress = hoverAnimation.getProgress();

        if (mouseOver && hoverProgress == 0)
            hoverAnimation.start();

        if (!mouseOver && hoverProgress > 0)
            hoverAnimation.reset();

        style().paintDropdown(this, renderer, mouseX, mouseY);
    }

    @Override
    public boolean render(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        animProgress = -8008135;

        boolean render = super.render(renderer, mouseX, mouseY, delta);
        double progress = indicatorAnimation.getProgress();

        if (!render && progress > 0) {
            renderer.absolutePost(() -> {
                Routers.of(style().pipeline()).windowLayer(renderer);
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

    public RichText titleText() {
        return titleText;
    }

    public RichText valueText() {
        return valueText;
    }

    public double maxValueWidth() {
        return maxValueWidth;
    }

    public double hoverProgress() {
        return hoverAnimation.getProgress();
    }

    public double indicatorProgress() {
        return indicatorAnimation.getProgress();
    }

    public boolean isExpanded() {
        return expanded;
    }

    public boolean isPressed() {
        return pressed;
    }

    public WRoot popup() {
        return (WRoot) root;
    }

    private String getNameFor(T value) {
        String name = value.toString();

        if (value instanceof HontunTheme.UiMode) return name;

        if (name.contains("_")) return Utils.nameToTitle(name.toLowerCase(Locale.ROOT).replace("_", "-"));

        return Utils.nameToTitle(name.replaceAll("(?<=[a-z])([A-Z])", "-$1").toLowerCase(Locale.ROOT));
    }

    public static class WRoot extends WDropdownRoot implements HontunWidget {
        private WHontunDropdown<?> owner;

        public WHontunDropdown<?> owner() {
            return owner;
        }

        @Override
        protected void onCalculateWidgetPositions() {
            this.y += style().popupOffset(theme());
            super.onCalculateWidgetPositions();
        }

        @Override
        protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
            style().paintDropdownPopup(owner, this, renderer, mouseX, mouseY);
        }
    }

    public class WValue extends WDropdownValue implements HontunWidget {
        private final RichText valueName;

        public WValue(T value) {
            this.value = value;
            this.valueName = RichText.of(getNameFor(value));
        }

        public RichText valueName() {
            return valueName;
        }

        public boolean isSelectedValue() {
            return get() == this.value;
        }

        public WHontunDropdown<T> owner() {
            return WHontunDropdown.this;
        }

        public boolean isPressed() {
            return pressed;
        }

        @Override
        protected void onCalculateSize() {
            double pad = pad();

            width = pad + theme().textWidth(valueName) + pad;
            height = pad + theme.textHeight() + pad;
        }

        @Override
        protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
            style().paintDropdownValue(this, renderer, mouseX, mouseY);
        }

        @Override
        protected void onPressed(int button) {
            super.onPressed(button);
            handlePressed();
            style().onPressed(this);
        }
    }
}
