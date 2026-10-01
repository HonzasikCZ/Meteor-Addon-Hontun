package cz.honzasik.hontun.gui.theme.widgets.input;

import cz.honzasik.hontun.gui.api.text.RichText;
import cz.honzasik.hontun.gui.render.route.Routers;
import cz.honzasik.hontun.gui.theme.HontunWidget;
import cz.honzasik.hontun.gui.theme.widgets.WHontunLabel;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.utils.CharFilter;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;
import meteordevelopment.meteorclient.gui.widgets.containers.WVerticalList;
import meteordevelopment.meteorclient.gui.widgets.input.WTextBox;
import meteordevelopment.meteorclient.utils.render.color.Color;

public class WHontunTextBox extends WTextBox implements HontunWidget {
    private final String title;
    private final double padding;
    private java.util.function.Supplier<Color> customColor;

    private boolean cursorVisible;
    private double cursorTimer;

    private double animProgress;
    private boolean renderBackground;

    private final double[] size = new double[2];

    public WHontunTextBox(String text, String placeholder, String title, double padding, CharFilter filter, Class<? extends Renderer> renderer) {
        super(text, placeholder, filter, renderer);
        this.title = title;
        this.padding = padding;
        this.renderBackground = true;
    }

    @Override
    protected void onCalculateSize() {
        super.onCalculateSize();

        style().textBoxSize(this, size);
        width = size[0];
        height = size[1];
    }

    @Override
    protected WContainer createCompletionsRootWidget() {
        return new WCompletions();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <T extends WWidget & ICompletionItem> T createCompletionsValueWidth(String completion, boolean selected) {
        return (T) new CompletionItem(completion, false, selected);
    }

    public class WCompletions extends WVerticalList {
        public WHontunTextBox owner() {
            return WHontunTextBox.this;
        }

        @Override
        public boolean render(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
            Routers.of(style().pipeline()).windowLayer(renderer);
            return super.render(renderer, mouseX, mouseY, delta);
        }

        @Override
        protected void onRender(GuiRenderer renderer1, double mouseX, double mouseY, double delta) {
            style().paintCompletions(WHontunTextBox.this, renderer1, x, y, width, height);
        }
    }

    public static class CompletionItem extends WHontunLabel implements ICompletionItem {
        private boolean selected;

        public CompletionItem(String text, boolean title, boolean selected) {
            super(RichText.of(text).boldIf(title));
            this.selected = selected;
        }

        @Override
        protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
            style().paintCompletionItem(this, renderer, mouseX, mouseY);
        }

        @Override
        public boolean isSelected() {
            return selected;
        }

        @Override
        public void setSelected(boolean selected) {
            this.selected = selected;
        }

        @Override
        public String getCompletion() {
            return text;
        }
    }

    @Override
    protected void onCursorChanged() {
        cursorVisible = true;
        cursorTimer = 0;
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        if (cursorTimer >= 1) {
            cursorVisible = !cursorVisible;
            cursorTimer = 0;
        }
        else {
            cursorTimer += delta * 1.75;
        }

        animProgress += delta * 10 * (focused && cursorVisible ? 1 : -1);
        animProgress = Math.clamp(animProgress, 0, 1);

        style().paintTextBox(this, renderer, mouseX, mouseY);
    }

    public boolean hasTitle() {
        return title != null && !title.isEmpty();
    }

    public String title() {
        return title;
    }

    public double padding() {
        return padding;
    }

    public boolean rendersBackground() {
        return renderBackground;
    }

    public double overflow() {
        return getOverflowWidthForRender();
    }

    public String textValue() {
        return text;
    }

    public String placeholderValue() {
        return placeholder;
    }

    public Color customColor() {
        return customColor != null ? customColor.get() : null;
    }

    public boolean caretVisible() {
        return cursorVisible;
    }

    public double caretAlpha() {
        return animProgress;
    }

    public boolean hasSelection() {
        return cursor != selectionStart || cursor != selectionEnd;
    }

    public int cursorIndex() {
        return cursor;
    }

    public int selectionStartIndex() {
        return selectionStart;
    }

    public int selectionEndIndex() {
        return selectionEnd;
    }

    public double textWidthAt(int position) {
        return getTextWidth(position);
    }

    public void drawText(GuiRenderer renderer, double x, double y, String text, Color color) {
        this.renderer.render(renderer, x, y, text, color);
    }

    public int getCursor() {
        return cursor;
    }

    public void shouldRenderBackground(boolean renderBackground) {
        this.renderBackground = renderBackground;
    }

    public void color(java.util.function.Supplier<Color> color) {
        this.customColor = color;
    }

    public void color(Color color) {
        this.customColor = color == null ? null : () -> color;
    }
}
