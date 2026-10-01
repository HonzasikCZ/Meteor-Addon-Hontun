package cz.honzasik.hontun.gui.theme.widgets.container;

import cz.honzasik.hontun.gui.api.animation.Direction;
import cz.honzasik.hontun.gui.render.route.Routers;
import cz.honzasik.hontun.gui.screen.HontunModulesScreen;
import cz.honzasik.hontun.gui.theme.HontunGuiTheme;
import cz.honzasik.hontun.gui.theme.HontunWidget;
import cz.honzasik.hontun.gui.theme.style.AnimRole;
import cz.honzasik.hontun.gui.theme.style.ClickStyle;
import cz.honzasik.hontun.gui.theme.style.Metrics;
import cz.honzasik.hontun.gui.theme.style.Pipeline;
import cz.honzasik.hontun.gui.theme.style.StyleAnimation;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.utils.Cell;
import meteordevelopment.meteorclient.gui.utils.WindowConfig;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;
import meteordevelopment.meteorclient.gui.widgets.containers.WWindow;
import meteordevelopment.meteorclient.gui.widgets.pressable.WTriangle;

import net.minecraft.client.input.MouseButtonEvent;

public class WHontunWindow extends WWindow implements HontunWidget {
    private HontunModulesScreen modulesScreen;
    private boolean shouldSnap = false;
    private int gridSize;

    private double mouseOffsetX;
    private double mouseOffsetY;

    private double contentOffsetY;

    private StyleAnimation animation;
    private StyleAnimation cornerAnimation;

    public WHontunWindow(WWidget icon, String title) {
        super(icon, title);
    }

    @Override
    public void init() {
        super.init();

        ClickStyle style = style();
        animation = StyleAnimation.of(style.anim(AnimRole.WINDOW_EXPAND), theme(), expanded);
        cornerAnimation = StyleAnimation.of(style.anim(AnimRole.WINDOW_CORNER), theme(), expanded);

        applyViewPadding();
    }

    public void rebuildChrome() {
        cells.clear();
        view = null;
        header = null;
        init();
    }

    public void setPadding(double padding) {
        this.padding = padding;
        applyViewPadding();
    }

    private void applyViewPadding() {
        if (view == null) return;

        Metrics m = metrics();
        for (Cell<?> cell : cells) {
            if (cell.widget() != view) continue;

            cell.padLeft(m.windowPadL >= 0 ? m.windowPadL : padding);
            cell.padRight(m.windowPadR >= 0 ? m.windowPadR : padding);
            cell.padTop(m.windowPadT >= 0 ? m.windowPadT : padding);
            cell.padBottom(m.windowPadB >= 0 ? m.windowPadB : padding);
        }
    }

    @Override
    protected void onCalculateSize() {
        super.onCalculateSize();
        minWidth = theme().scale(metrics().windowMinWidth);
    }

    public void initSnapping(HontunModulesScreen modulesScreen, int gridSize) {
        this.modulesScreen = modulesScreen;
        this.gridSize = gridSize;
        shouldSnap = true;
    }

    public <T extends WWidget> Cell<T> addDirect(T widget) {
        widget.parent = this;
        widget.theme = theme;

        Cell<T> cell = new Cell<>(widget).centerY();
        cells.add(cell);

        widget.init();
        invalidate();

        return cell;
    }

    @Override
    public void clear() {
        view.clear();

        cells.removeIf(cell -> cell.widget() != header && cell.widget() != view);
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
        style().paintWindowBack(this, renderer, mouseX, mouseY);
    }

    @Override
    public boolean render(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        if (!visible) return true;

        ClickStyle style = style();
        Routers.of(style.pipeline()).windowLayer(renderer);

        double progress = animation.getProgress();
        boolean isAnimating = animation.isRunning();
        double contentHeight = height - header.height;

        if (isAnimating && progress > 1.0) {
            double overshot = Math.max(progress - 1, 0) * contentHeight;
            double shift = overshot / 2;

            if (contentOffsetY != shift) {
                contentOffsetY = shift;
                invalidate();
            }
        }

        boolean scissor = style.pipeline() != Pipeline.PIXEL;

        if (scissor) {
            double margin = style.chromeMargin(theme());
            double windowHeight = Math.max(contentHeight * progress, 0);

            double left = Math.floor(x - margin);
            double top = Math.floor(y - margin);
            double right = Math.ceil(x + width + margin);
            double bottom = Math.ceil(y + header.height + windowHeight + margin);

            renderer.scissorStart(left, top, right - left, bottom - top);
        }

        boolean toReturn = super.render(renderer, mouseX, mouseY, delta);

        style.paintWindowFront(this, renderer, mouseX, mouseY);

        if (scissor) renderer.scissorEnd();

        return toReturn;
    }

    @Override
    protected void renderWidget(WWidget widget, GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        if (expanded || animation.isRunning() || widget instanceof WHeader)
            widget.render(renderer, mouseX, mouseY, delta);
    }

    @Override
    protected boolean propagateEvents(WWidget widget) {
        return widget instanceof WHeader || expanded;
    }

    @Override
    public void setExpanded(boolean expanded) {
        super.setExpanded(expanded);

        if (animation != null) {
            animation.reverse();
            style().onPressed(this);
        }

        if (expanded && cornerAnimation != null)
            cornerAnimation.finishedAt(Direction.FORWARDS);
    }

    @Override
    protected WHeader header(WWidget icon) {
        return new WHontunHeader(icon);
    }

    public WHontunHeader headerWidget() {
        return (WHontunHeader) header;
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

    public boolean isExpanded() {
        return expanded;
    }

    public boolean isDragging() {
        return dragging;
    }

    public String titleText() {
        return title;
    }

    public boolean isDialog() {
        return id == null;
    }

    public class WHontunHeader extends WHeader {
        private WHorizontalList list;
        private WTriangle openIndicator;
        private WWidget titleWidget;

        public WHontunHeader(WWidget icon) {
            super(icon);
        }

        @Override
        public void init() {
            HontunGuiTheme hontun = theme();
            ClickStyle style = hontun.style();
            Metrics m = style.metrics();

            list = add(theme.horizontalList())
                    .padHorizontal(m.headerPadH)
                    .padVertical(m.headerPadV)
                    .expandX()
                    .widget();

            list.spacing = m.headerSpacing;

            if (icon != null)
                add(icon).centerY();

            if (beforeHeaderInit != null)
                beforeHeaderInit.accept(this);

            titleWidget = add(style.headerLabel(hontun, WHontunWindow.this, title)).expandX().centerY().widget();

            WWidget trailing = style.headerTrailing(hontun, WHontunWindow.this);
            if (trailing != null) add(trailing).centerY();

            openIndicator = add(hontun.triangle())
                    .right()
                    .centerY()
                    .widget();
        }

        public WHontunWindow window() {
            return WHontunWindow.this;
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
            double cornerProgress = cornerAnimation.getProgress();

            if (!expanded
                    && animation.getProgress() <= 0.0
                    && !cornerAnimation.isRunning()
                    && cornerProgress > 0) {
                cornerAnimation.start(Direction.BACKWARDS);
            }

            style().paintWindowHeader(WHontunWindow.this, this, renderer, mouseX, mouseY);

            openIndicator.rotation = 90 + 90 * animation.getProgress();
        }

        @Override
        public boolean render(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
            boolean render = super.render(renderer, mouseX, mouseY, delta);
            animProgress = 1;
            return render;
        }

        @Override
        public boolean onMouseClicked(MouseButtonEvent click, boolean used) {
            boolean clicked = super.onMouseClicked(

                    click,

                    used
            );

            if (clicked) {
                mouseOffsetX = click.x() - x;
                mouseOffsetY = click.y() - y;
            }

            return clicked;
        }

        @Override
        public boolean mouseReleased(MouseButtonEvent click) {
            if (shouldSnap) modulesScreen.showGrid(false);
            return super.mouseReleased(

                    click

            );
        }

        @Override
        public void onMouseMoved(double mouseX, double mouseY, double lastMouseX, double lastMouseY) {
            if (!dragging) return;

            ClickStyle style = style();
            HontunGuiTheme hontun = theme();
            int grid = shouldSnap ? gridSize : 0;

            double deltaX = style.snapWindow(hontun, mouseX - mouseOffsetX, grid) - x;
            double deltaY = style.snapWindow(hontun, mouseY - mouseOffsetY, grid) - y;

            WHontunWindow.this.move(deltaX, deltaY);

            moved = true;
            movedX = x;
            movedY = y;

            if (id != null) {
                WindowConfig config = theme.getWindowConfig(id);

                config.x = x;
                config.y = y;
            }

            if (shouldSnap && !modulesScreen.showGrid()) modulesScreen.showGrid(true);
            dragged = true;
        }
    }
}
