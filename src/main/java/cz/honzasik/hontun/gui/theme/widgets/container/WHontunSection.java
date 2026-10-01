package cz.honzasik.hontun.gui.theme.widgets.container;

import cz.honzasik.hontun.gui.api.animation.Direction;
import cz.honzasik.hontun.gui.theme.HontunGuiTheme;
import cz.honzasik.hontun.gui.theme.HontunWidget;
import cz.honzasik.hontun.gui.theme.style.AnimRole;
import cz.honzasik.hontun.gui.theme.style.ClickStyle;
import cz.honzasik.hontun.gui.theme.style.Metrics;
import cz.honzasik.hontun.gui.theme.style.StyleAnimation;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.utils.Cell;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;
import meteordevelopment.meteorclient.gui.widgets.containers.WSection;
import meteordevelopment.meteorclient.gui.widgets.pressable.WTriangle;

public class WHontunSection extends WSection implements HontunWidget {
    private double actualHeight;
    private double forcedHeight = -1;
    private double contentOffsetY;

    private WHontunHeader header;

    private StyleAnimation animation;
    private StyleAnimation cornerAnimation;

    public WHontunSection(String title, boolean expanded, WWidget headerWidget) {
        super(title, expanded, headerWidget);
    }

    @Override
    public void init() {
        super.init();

        ClickStyle style = style();
        animation = StyleAnimation.of(style.anim(AnimRole.SECTION_EXPAND), theme(), expanded);
        cornerAnimation = StyleAnimation.of(style.anim(AnimRole.SECTION_CORNER), theme(), expanded);
    }

    @Override
    protected void onCalculateSize() {
        super.onCalculateSize();

        actualHeight = height;

        if (animation.isRunning() || animation.getProgress() < 1) {
            double contentHeight = actualHeight - header.height;
            double animatedHeight = Math.max(contentHeight * animation.getProgress(), 0);
            forcedHeight = animatedHeight + header.height;
            height = forcedHeight;
        }
    }

    @Override
    protected void onCalculateWidgetPositions() {
        super.onCalculateWidgetPositions();

        if (contentOffsetY != 0) {
            for (Cell<?> cell : cells) {
                if (cell.widget() != header) cell.move(0, contentOffsetY);
            }

            contentOffsetY = 0;
        }
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        style().paintSectionBody(this, renderer, mouseX, mouseY);
    }

    @Override
    public boolean render(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        if (!visible) return true;

        double progress = animation.getProgress();
        boolean isAnimating = animation.isRunning();
        double contentHeight = height - header.height;

        animProgress = expanded ? 1 : 0;

        if (isAnimating) {
            forcedHeight = (actualHeight - header.height) * progress + header.height;

            if (progress > 1.0) {
                double overshot = Math.max(progress - 1, 0) * contentHeight;
                double shift = overshot / 2;

                if (contentOffsetY != shift)
                    contentOffsetY = shift;
            }

            invalidate();

            renderer.scissorStart(x, y, width, contentHeight * progress + header.height);
        }

        boolean toReturn = super.render(renderer, mouseX, mouseY, delta);

        if (isAnimating) renderer.scissorEnd();

        return toReturn;
    }

    @Override
    protected void renderWidget(WWidget widget, GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        if (expanded || animation.getProgress() > 0 || widget instanceof WHeader) {
            widget.render(renderer, mouseX, mouseY, delta);
        }
    }

    @Override
    public void setExpanded(boolean expanded) {
        super.setExpanded(expanded);

        animation.reverse();
        if (expanded) cornerAnimation.finishedAt(Direction.FORWARDS);
    }

    @Override
    protected WHeader createHeader() {
        header = new WHontunHeader(title);
        return header;
    }

    public WHontunHeader headerWidget() {
        return header;
    }

    public double expandProgress() {
        return animation.getProgress();
    }

    public boolean expandAnimating() {
        return animation.isRunning();
    }

    public double cornerProgress() {
        return cornerAnimation.getProgress();
    }

    public boolean cornerAnimating() {
        return cornerAnimation.isRunning();
    }

    public String titleText() {
        return title;
    }

    public WWidget customHeaderWidget() {
        return headerWidget;
    }

    public class WHontunHeader extends WHeader {
        private WHorizontalList list;
        private WTriangle openIndicator;
        private WWidget titleWidget;

        public WHontunHeader(String title) {
            super(title);
        }

        @Override
        public void init() {
            HontunGuiTheme hontun = theme();
            ClickStyle style = hontun.style();
            Metrics m = style.metrics();

            list = add(theme.horizontalList())
                    .padHorizontal(m.sectionHeaderPadH)
                    .padVertical(m.sectionHeaderPadV)
                    .expandX()
                    .widget();

            WWidget widget = headerWidget;

            if (widget == null) {
                openIndicator = theme.triangle();
                widget = openIndicator;
            }

            widget.calculateSize();
            double pad = widget.width;

            titleWidget = add(style.sectionTitle(hontun, WHontunSection.this, title))
                    .expandX()
                    .padLeft(pad)
                    .widget();

            add(widget);
        }

        public WHontunSection section() {
            return WHontunSection.this;
        }

        public WWidget titleWidget() {
            return titleWidget;
        }

        public WTriangle openIndicator() {
            return openIndicator;
        }

        @Override
        public <T extends WWidget> Cell<T> add(T widget) {
            if (list != null) return list.add(widget);
            return super.add(widget);
        }

        @Override
        protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
            double progress = animation.getProgress();
            double cornerProgress = cornerAnimation.getProgress();

            if (!expanded
                && progress <= 0.1
                && !cornerAnimation.isRunning()
                && cornerProgress > 0) {
                cornerAnimation.start(Direction.BACKWARDS);
            }

            style().paintSectionHeader(WHontunSection.this, this, renderer, mouseX, mouseY);

            if (openIndicator != null)
                openIndicator.rotation = 90 + 90 * animation.getProgress();
        }
    }
}
